package tiiehenry.android.mdviewer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Message
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import java.util.HashMap

open class MDWebView : WebView, IMDViewer, View.OnLongClickListener {
    override var previewText: String = ""

    var onTitleReceived:(String)->Unit={

    }
    private val webClient = object : MDWebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            onPageFinished()
        }
    }

    override val codeScrollDisable: Boolean = false

    override fun getWebView(): WebView {
        return this
    }

    private var downloadListener: MDDownLoadListener? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
    ) {
        initWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun initWebView() {
        downloadListener = MDDownLoadListener(context)

        overScrollMode = View.OVER_SCROLL_NEVER
        isScrollbarFadingEnabled = true
        scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        requestFocusFromTouch()
        webViewClient = webClient
        webChromeClient = chromeClient
        setDownloadListener(downloadListener)
        setOnLongClickListener(this)
        setWillNotDraw(false)
        settings.apply {
            allowUniversalAccessFromFileURLs = true
            allowFileAccessFromFileURLs = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            allowContentAccess = true
            //是否允许在WebView中访问内容URL（Content Url）
            allowFileAccess = true
            //是否允许访问文件，默认允许。注意，这里只是允许或禁止对文件系统的访问，Assets 和 resources 文件使用 file:///android_asset和 file:///android_res仍是可访问的。
            setAppCacheEnabled(true)
            setAppCachePath(context.cacheDir.absolutePath)
            //设置应用缓存文件的路径。为了让应用缓存API可用，此方法必须传入一个应用可写的路径。该方法只会执行一次，重复调用会被忽略。
            //已废弃setAppCacheMaxSize(MAX_VALUE)
            builtInZoomControls = true
            blockNetworkImage = true
            //阻塞图片
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            //使用缓存
            cacheMode = WebSettings.LOAD_NO_CACHE
            //默认不使用缓存！
            displayZoomControls = false
            databaseEnabled = true
            domStorageEnabled = true
            defaultTextEncodingName = "utf-8"
            //设置编码格式
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            loadWithOverviewMode = true
            //概览方式载入
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW//HTTPS
            mediaPlaybackRequiresUserGesture = false
            setSupportMultipleWindows(true)
            setSupportZoom(true)
            useWideViewPort = true
//            userAgentString = "$userAgentString HaustER/1.0"
        }

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(this@MDWebView, true)
        }

        setOnKeyListener(object : OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (event?.action == KeyEvent.ACTION_DOWN) {
                    //按返回键操作并且能回退网页
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        if (canGoBack()) {
                            webClient.goBack()
                            return true
                        }
                        return false
                    }
                }
                return false
            }
        })
    }


    val chromeClient = object : WebChromeClient() {
        //配置权限（同样在WebChromeClient中实现）
        override fun onGeolocationPermissionsShowPrompt(
                origin: String,
                callback: GeolocationPermissions.Callback
        ) {
            callback.invoke(origin, true, false)
            super.onGeolocationPermissionsShowPrompt(origin, callback)
        }

        override fun onProgressChanged(view: WebView, newProgress: Int) {
        }

        override fun onReceivedTitle(view: WebView, title: String) {
            //获取WebView的标题
            onTitleReceived.invoke(title)
        }

        override fun onJsConfirm(
                view: WebView,
                url: String,
                message: String,
                result: JsResult
        ): Boolean {
            AlertDialog.Builder(context).apply {
                //b.setTitle("删除");
                setMessage(message)
                setPositiveButton(android.R.string.ok) { _, _ -> result.confirm() }
                setNegativeButton(android.R.string.cancel) { _, _ -> result.cancel() }
                create().show()
            }
            return true
        }

        override fun onCreateWindow(
                view: WebView,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message
        ): Boolean {
            val newWebView = WebView(view.context)
            newWebView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    when {
                        url.startsWith("file") -> loadUrl(url)
                        webClient.shouldOverrideUrlLoadingByApp(context, url) -> return true
                        webClient.shouldOverrideUrlLoadingByUrl(url) -> {
                            // 在此处进行跳转URL的处理, 一般情况下_black需要重新打开一个页面, 这里我直接让当前的webview重新load了url
                            val extraHeaders = HashMap<String, String>()
                            extraHeaders["Referer"] = webClient.latestUrl
                            //告诉网站上一个网站地址，防止盗链检测
                            loadUrl(url, extraHeaders)
                        }
                    }
                    return true
                }

            }
            val transport = resultMsg.obj as WebView.WebViewTransport
            transport.webView = newWebView
            resultMsg.sendToTarget()
            return true
        }

    }

    override fun loadUrl(url: String?) {
        if (url?.let { loadMDUrl(url) } != true)
            super.loadUrl(url)
    }

    override fun onLongClick(v: View): Boolean {
        val hitTestResult = hitTestResult
        // 如果是图片类型或者是带有图片链接的类型
        if (hitTestResult.type == HitTestResult.IMAGE_TYPE || hitTestResult.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
            // 弹出保存图片的对话框
            hitTestResult.extra.let {
                downloadListener?.onDownloadStart(
                        it!!,
                        settings.userAgentString,
                        "",
                        "image/png",
                        0
                )
            }
        }
        return true
    }

}
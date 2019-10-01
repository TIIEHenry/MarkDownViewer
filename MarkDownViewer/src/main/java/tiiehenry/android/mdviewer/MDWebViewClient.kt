package tiiehenry.android.mdviewer

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.text.TextUtils
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import java.net.URISyntaxException

open class MDWebViewClient : WebViewClient() {
    val urlCollection = ArrayList<String>()
    var latestUrl: String = ""
    var prevUrl: String = ""

    fun goBack() {
        if (urlCollection.size > 0) {
            prevUrl = if (urlCollection.size - 2 >= 0) {
                urlCollection[urlCollection.size - 2]
            } else {
                ""
            }
            urlCollection.removeAt(urlCollection.size - 1)
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        //toast(url);
        if (shouldOverrideUrlLoadingByApp(view.context, url)) {
            return true
        } else if (shouldOverrideUrlLoadingByUrl(url)) {
            view.loadUrl(url)
        }

        //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
        return true
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            request.url.toString() else request.toString()
        if (shouldOverrideUrlLoadingByApp(view.context, url)) {
            return true
        } else if (shouldOverrideUrlLoadingByUrl(url)) {
            view.loadUrl(url)
        }
        //toast(request);
        return true
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        view.settings.blockNetworkImage = true
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        view.settings.blockNetworkImage = false

    }

    //判断是否LoadUrl，并记录网址
    fun shouldOverrideUrlLoadingByUrl(url: String): Boolean {
        if (TextUtils.isEmpty(url)) {
            return false
        }
        latestUrl = url
        if (prevUrl != latestUrl) {
            prevUrl = url
            urlCollection.add(prevUrl)
            return true
        }
        return false
    }

    /**
     * 根据url的scheme处理跳转第三方app的业务
     */
    fun shouldOverrideUrlLoadingByApp(context: Context, url: String): Boolean {
        if (url.startsWith("http") || url.startsWith("https") || url.startsWith("ftp")) {
            //不处理http, https, ftp的请求
            return false
        }
        val intent: Intent
        try {
            intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
        } catch (e: URISyntaxException) {
            return false
        }
        intent.component = null
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            return false
        }
        return true
    }
}
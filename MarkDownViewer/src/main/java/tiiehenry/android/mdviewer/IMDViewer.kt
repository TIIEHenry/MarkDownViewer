package tiiehenry.android.mdviewer

import android.os.Build
import android.util.Base64
import android.util.Log
import android.webkit.WebView
import java.io.*
import java.util.regex.Pattern

interface IMDViewer {
    val codeScrollDisable: Boolean
    var previewText: String

    fun getWebView(): WebView

    fun init(url: String = "file:///android_asset/html/md_preview.html"): IMDViewer {
        getWebView().loadUrl(url)
        return this
    }

    fun onPageFinished() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            getWebView().loadUrl(previewText)
        } else {
            getWebView().evaluateJavascript(previewText, null)
        }
    }

    fun loadAssets(name:String){
        getWebView().context.assets.open(name).use {
            loadMDStream(it)
        }
    }

    fun loadMDStream(inputStream: InputStream) {
        loadMDText(inputStream.bufferedReader().readText())
    }

    fun loadMDFile(mdFile: File) {
        loadMDText(mdFile.readText())
    }

    fun loadMDFile(filePath: String) {
        loadMDText(File(filePath).readText())
    }

    fun loadMDText(mdText: String) {
        previewText = text2Mark(mdText)
        onPageFinished()
    }

    fun text2Mark(mdText: String): String {
        val bs64MdText = imgToBase64(mdText)
        val escMdText = escapeForText(bs64MdText)
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            String.format("javascript:preview('%s', %b)", escMdText, codeScrollDisable)
        } else {
            String.format("preview('%s', %b)", escMdText, codeScrollDisable)
        }
    }

    private fun escapeForText(mdText: String): String {
        var escText = mdText.replace("\n", "\\\\n")
        escText = escText.replace("'", "\\\'")
        //in some cases the string may have "\r" and our view will show nothing,so replace it
        escText = escText.replace("\r", "")
        return escText
    }

    private fun imgToBase64(mdText: String): String {
        val TAG = "MDViewer"
        val IMAGE_PATTERN = "!\\[(.*)\\]\\((.*)\\)"
        val ptn = Pattern.compile(IMAGE_PATTERN)
        val matcher = ptn.matcher(mdText)
        if (!matcher.find()) {
            return mdText
        }

        val imgPath = matcher.group(2)
        if (isUrlPrefix(imgPath!!) || !isPathExChack(imgPath)) {
            return mdText
        }
        val baseType = imgEx2BaseType(imgPath)
        if (baseType == "") {
            // image load error.
            return mdText
        }

        val file = File(imgPath)
        val bytes = ByteArray(file.length().toInt())
        try {
            val buf = BufferedInputStream(FileInputStream(file))
            buf.read(bytes, 0, bytes.size)
            buf.close()
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "FileNotFoundException:$e")
        } catch (e: IOException) {
            Log.e(TAG, "IOException:$e")
        }

        val base64Img = baseType + Base64.encodeToString(bytes, Base64.NO_WRAP)

        return mdText.replace(imgPath, base64Img)
    }

    private fun isUrlPrefix(text: String): Boolean {
        return text.startsWith("http://") || text.startsWith("https://")
    }

    private fun isPathExChack(text: String): Boolean {
        return (text.endsWith(".png")
                || text.endsWith(".jpg")
                || text.endsWith(".jpeg")
                || text.endsWith(".gif"))
    }

    private fun imgEx2BaseType(text: String): String {
        return if (text.endsWith(".png")) {
            "data:image/png;base64,"
        } else if (text.endsWith(".jpg") || text.endsWith(".jpeg")) {
            "data:image/jpg;base64,"
        } else if (text.endsWith(".gif")) {
            "data:image/gif;base64,"
        } else {
            ""
        }
    }
}
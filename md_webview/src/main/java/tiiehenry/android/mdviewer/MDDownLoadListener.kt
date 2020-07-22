package tiiehenry.android.mdviewer

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.DownloadListener
import android.webkit.URLUtil
import android.widget.EditText

class MDDownLoadListener(val context: Context) : DownloadListener {

    override fun onDownloadStart(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimetype: String,
        contentLength: Long
    ) {
        val et = EditText(context).apply {
            setText(URLUtil.guessFileName(url, contentDisposition, mimetype))
        }
        AlertDialog.Builder(context).apply {
            setTitle("下载文件")
            setMessage(url)
            setView(et)
            setPositiveButton(android.R.string.ok) { _, _ ->
                downloadBySystem(
                    url,
                    userAgent,
                    contentDisposition,
                    mimetype,
                    contentLength,
                    et.text.toString()
                )
            }
            setNegativeButton(android.R.string.cancel, null)
            setNeutralButton("浏览器下载") { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW)
                intent.addCategory(Intent.CATEGORY_BROWSABLE)
                intent.data = Uri.parse(url)
                context.startActivity(intent)
            }
            create().show()
        }

    }

    private fun downloadBySystem(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimetype: String,
        contentLength: Long,
        fileName: String?
    ) {
        // 指定下载地址
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            // 允许媒体扫描，根据下载的文件类型被加入相册、音乐等媒体库
            allowScanningByMediaScanner()
            // 设置通知的显示类型，下载进行时和完成后显示通知
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            // 设置通知栏的标题，如果不设置，默认使用文件名
            setTitle(fileName)
            // 设置通知栏的描述
            setDescription(url)
            // 允许在计费流量下下载
            setAllowedOverMetered(true)
            // 允许该记录在下载管理界面可见
            setVisibleInDownloadsUi(true)
            // 允许漫游时下载
            setAllowedOverRoaming(true)
            // 允许下载的网路类型
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            // 设置下载文件保存的路径和文件名

            setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                fileName ?: URLUtil.guessFileName(url, contentDisposition, mimetype)
            )
            //        另外可选一下方法，自定义下载路径
            //        setDestinationUri()
            //        setDestinationInExternalFilesDir()
        }

        val downloadManager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        // 添加一个下载任务
        downloadManager.enqueue(request)
    }
}
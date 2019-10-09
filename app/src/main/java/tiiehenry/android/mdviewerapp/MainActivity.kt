package tiiehenry.android.mdviewerapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        md_viewer.init().apply {
            loadAssets("mxflutter.md")
//            loadMDFile("/storage/emulated/0/我的笔记/update.md")
//            loadMDText("# AAS\n## 奥斯丁")
        }

//        md_viewer.loadUrl("http://www.baidu.com")
//        md_viewer.loadUrl("file:///storage/emulated/0/我的笔记/update.html")
//        md_view.init()
//            md_view.setMDText("# 超好\n```\nsss\n```")
    }
}

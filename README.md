# MarkDownViewer
## 使用方法:

Add it in your root build.gradle at the end of repositories:
```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://www.jitpack.io' }
    }
}
```
Add the dependency:
```groovy
dependencies {
        implementation 'com.github.TIIEHenry:MarkDownViewer:{ReleaseTag}'
}
```
Use in your code:
```
val a=[WebVIew] extends IMDView
or 
val a=MDWebVIew(this)

a.init()
a.loadMDText("# AAA")

...
loadMDStream(in)
loadAssets("a.md")

```

## 基于开源项目：
https://github.com/mittsuu/MarkedView-for-Android
https://highlightjs.org/
https://github.com/markedjs/marked



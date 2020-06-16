package cn.starrah.thu_course_helper.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri

fun startDownloadIntent(activity: Activity, uri: Uri) {
    var it = Intent(Intent.ACTION_VIEW, uri)
    it = Intent.createChooser(it, "请选择一个浏览器进行下载")
    if (it.resolveActivity(activity.packageManager) != null) {
        activity.startActivity(it)
    }
}
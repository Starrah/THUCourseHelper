package cn.starrah.thu_course_helper.utils

import android.content.Context
import cn.starrah.thu_course_helper.data.database.CREP

suspend fun AppLoad(context: Context) {
    configNotificationChannel(context)
    CREP.initializeDefault(context)
    allAppTask(context)
}
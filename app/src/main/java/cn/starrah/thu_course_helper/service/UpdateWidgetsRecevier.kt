package cn.starrah.thu_course_helper.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cn.starrah.thu_course_helper.utils.updateAppWidgets
import cn.starrah.thu_course_helper.utils.updateWidgetsAndNotification
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

val updateWidgetsIntentAction = "cn.starrah.thu_course_helper.updateWidgets"
val updateWidgetsIntentInterval = 10 * 1000 // 5min一次

class UpdateWidgetsRecevier() : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult: PendingResult = goAsync()
        println("qwqwqwqwqwqw")
        GlobalScope.launch {
            context.updateWidgetsAndNotification()
            pendingResult.finish()
        }
    }
}
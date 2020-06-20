package cn.starrah.thu_course_helper.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

val updateWidgetsIntentAction = "cn.starrah.thu_course_helper.updateWidgets"
val updateWidgetsIntentInterval = 300 * 1000 // 5min一次

class UpdateWidgetsRecevier() : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult: PendingResult = goAsync()
        GlobalScope.launch {
            context.updateWidgetsAndNotification()
            pendingResult.finish()
        }
    }
}
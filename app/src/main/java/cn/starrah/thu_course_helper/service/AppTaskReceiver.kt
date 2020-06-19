package cn.starrah.thu_course_helper.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

val executeAppTaskIntentAction = "cn.starrah.thu_course_helper.executeAppTask"
val executeAppTaskIntentInterval = 3600 * 1000 // 1h一次

class AppTaskReceiver() : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult: PendingResult = goAsync()
        GlobalScope.launch {
            allAppTask(context)
            pendingResult.finish()
        }
    }
}
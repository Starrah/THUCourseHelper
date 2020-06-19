package cn.starrah.thu_course_helper.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PhoneRestartReceiver() : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "android.intent.action.BOOT_COMPLETED") return
        val pendingResult: PendingResult = goAsync()
        GlobalScope.launch {
            setAllTimelyAlarms(context)
            pendingResult.finish()
        }
    }
}
package cn.starrah.thu_course_helper.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import cn.starrah.thu_course_helper.utils.updateWidgetsAndNotification

suspend fun setAllTimelyAlarms(context: Context, immediatelyRun: Boolean = true) {
    run {
        // 设置定时更新allAppTask
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(
            context,
            AppTaskReceiver::class.java
        ).apply {
            action =
                executeAppTaskIntentAction
        }.let { PendingIntent.getBroadcast(context, 0, it, 0) }
        val timeStamp = System.currentTimeMillis() + executeAppTaskIntentInterval
        alarmMgr.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            timeStamp,
            executeAppTaskIntentInterval.toLong(),
            alarmIntent
        )
        if (immediatelyRun) {
            allAppTask(context)
        }
    }

    run {
        // 设置定时更新小部件
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(
            context,
            UpdateWidgetsRecevier::class.java
        ).apply {
            action =
                updateWidgetsIntentAction
        }.let { PendingIntent.getBroadcast(context.applicationContext, 0, it, 0) }
        val timeStamp = System.currentTimeMillis() + updateWidgetsIntentInterval
//        alarmMgr.setRepeating(
//            AlarmManager.ELAPSED_REALTIME_WAKEUP,
//            timeStamp,
//            updateWidgetsIntentInterval.toLong(),
//            alarmIntent
//        )
        alarmMgr.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            System.currentTimeMillis() + 30000,
            alarmIntent
        )
        if (immediatelyRun) {
            context.updateWidgetsAndNotification()
        }
    }
}
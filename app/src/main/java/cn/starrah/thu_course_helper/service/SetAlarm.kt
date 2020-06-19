package cn.starrah.thu_course_helper.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import cn.starrah.thu_course_helper.data.constants.THE_ZONE
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.*

fun setAlarm(context: Context, time: CalendarTimeData, baseTime: LocalDateTime = LocalDateTime.now(), shouldCancel: Boolean = true): Boolean {
    val nextRemindTime = time.getNextRemindTime(baseTime)
    if (nextRemindTime != null) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, RemindReceiver::class.java).apply {
            action = remindIntentAction
            addCategory("timeId:${time.id}")
        }.let { PendingIntent.getBroadcast(context, 0, it, 0) }
        val timeStamp = nextRemindTime.toInstant(THE_ZONE).toEpochMilli()
        alarmMgr.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeStamp,
            alarmIntent
        )
        return true
    }
    else if (shouldCancel) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, RemindReceiver::class.java).apply {
            action = remindIntentAction
            addCategory("timeId:${time.id}")
        }.let { PendingIntent.getBroadcast(context, 0, it, 0) }
        alarmMgr.cancel(alarmIntent)
        return false
    }
    return false
}

suspend fun setAlarmForAll(context: Context, shouldCancel: Boolean = true) {
    val allTimes = withContext(Dispatchers.IO) {
        CREP.DAO.findAllTimes()
    }
    val haveAlarm = allTimes.any { setAlarm(context, it, shouldCancel = shouldCancel) }
    println("haveAlarm: $haveAlarm")
}

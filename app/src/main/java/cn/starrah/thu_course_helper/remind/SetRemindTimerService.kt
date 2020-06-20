package cn.starrah.thu_course_helper.remind

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.data.constants.THE_ZONE
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeData
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarRemindMethodType
import cn.starrah.thu_course_helper.data.utils.DataInvalidException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.time.Duration
import java.time.LocalDateTime

/**
 * @param [checkAlarmLegal] 如果闹钟设置的没有提前两分钟，就拒绝修改
 */
fun setRemindTimerService(
    context: Context,
    time: CalendarTimeData,
    baseTime: LocalDateTime = LocalDateTime.now(),
    shouldCancel: Boolean = true,
    checkAlarmLegal: Boolean = false
): Boolean {
    val nextRemindTime = time.getNextRemindTime(baseTime)
    var remindBeforeTime: Duration? = null
    if (time.remindData.method == CalendarRemindMethodType.ALARM) {
        if (!isAlarmSupported(context)) throw Exception(context.getString(R.string.error_alarm_not_supported))
        remindBeforeTime = Duration.ofMinutes(MAKE_ALARM_BEFORE_MINUTE.toLong())
    }
    if (nextRemindTime != null) {
        val alarmSetTime =
            if (remindBeforeTime != null) nextRemindTime - remindBeforeTime else nextRemindTime
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, RemindReceiver::class.java).apply {
            action = remindIntentAction
            addCategory("timeId:${time.id}")
            remindBeforeTime?.let { putExtra(EXTRA_broadCastBeforeSecond, it.seconds.toInt()) }
        }.let { PendingIntent.getBroadcast(context, 0, it, 0) }
        val timeStamp = alarmSetTime.toInstant(THE_ZONE).toEpochMilli()
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

suspend fun setRemindTimerServiceForAll(context: Context, shouldCancel: Boolean = true) {
    val allTimes = withContext(Dispatchers.IO) {
        CREP.DAO.findAllTimes()
    }
    val haveAlarm = allTimes.any {
        setRemindTimerService(context, it, shouldCancel = shouldCancel)
    }
    println("haveAlarm: $haveAlarm")
}

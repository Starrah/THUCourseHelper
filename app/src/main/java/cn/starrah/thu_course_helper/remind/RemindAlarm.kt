package cn.starrah.thu_course_helper.remind

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.widget.Toast
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.data.constants.THE_ZONE
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeDataWithItem
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

val MAKE_ALARM_BEFORE_MINUTE = 2

fun makeRemindSystemAlarm(context: Context, time: CalendarTimeDataWithItem) {
    // 设置闹钟在2分钟之后响铃
    // 这部分代码在Android10以上很可能是无效的
    val item = time.calendarItem
    val alarmTime = LocalTime.now() + Duration.ofMinutes(MAKE_ALARM_BEFORE_MINUTE.toLong())
    val line1 = "${time.place.run { if (isNotEmpty()) plus("/") else "" }}${time.timeStr}"
    val message = "${item.name}${time.name}, ${line1}"
    val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
        putExtra(AlarmClock.EXTRA_HOUR, alarmTime.hour)
        putExtra(AlarmClock.EXTRA_MINUTES, alarmTime.minute)
        putExtra(AlarmClock.EXTRA_MESSAGE, message)
        putExtra(AlarmClock.EXTRA_SKIP_UI, true)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    if (intent.resolveActivity(context.packageManager) == null) {
        Toast.makeText(context, R.string.error_alarm_not_supported, Toast.LENGTH_LONG).show()
        throw Exception(context.getString(R.string.error_alarm_not_supported))
    }
    context.startActivity(intent)

    // 设置备用纯通知，与alarm一同出现。
    val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val alarmNoticeIntent = Intent(context, RemindAlarmBackupNoticeReceiver::class.java).apply {
        action = ACTION_RemindAlarmBackupNotice
        putExtra(
            "title",
            "${context.getString(R.string.notification_remind_prefix)}${item.name}${time.name}"
        )
        putExtra("line1", line1)
    }.let { PendingIntent.getBroadcast(context, 0, it, 0) }
    alarmMgr.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        alarmTime.atDate(LocalDate.now()).toInstant(THE_ZONE).toEpochMilli(),
        alarmNoticeIntent
    )

    // 核心：开启前台服务
    ReminderHelperService.alarmTime = alarmTime
    ReminderHelperService.message = message
    context.startService(Intent(context, ReminderHelperService::class.java))

    println("alarmSent")
}

private var _isAlarmSupported: Boolean? = null

fun isAlarmSupported(context: Context): Boolean {
    if (_isAlarmSupported != null) return _isAlarmSupported!!
    val alarmTime = LocalTime.now() + Duration.ofMinutes(2)
    val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
        putExtra(AlarmClock.EXTRA_HOUR, alarmTime.hour)
        putExtra(AlarmClock.EXTRA_MINUTES, alarmTime.minute)
        putExtra(AlarmClock.EXTRA_SKIP_UI, true)
    }
    val result = intent.resolveActivity(context.packageManager) != null
    _isAlarmSupported = result
    return result
}
package cn.starrah.thu_course_helper.service

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeDataWithItem
import cn.starrah.thu_course_helper.utils.configNotificationChannel

fun showRemindNotification(context: Context, time: CalendarTimeDataWithItem) {
    println(NotificationManagerCompat.from(context).getNotificationChannel("remind")!!.importance)
    val item = time.calendarItem
    val line1 = "${time.place.run { if (isNotEmpty()) plus("/") else "" }}${time.timeStr}"
    var builder = NotificationCompat.Builder(context, "remind")
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("${context.getString(R.string.notification_remind_prefix)}${item.name}${time.name}")
        .setContentText(line1)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
    NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), builder.build())
}



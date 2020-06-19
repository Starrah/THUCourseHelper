package cn.starrah.thu_course_helper.remind

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeDataWithItem

fun showRemindNotification(context: Context, time: CalendarTimeDataWithItem) {
    println(NotificationManagerCompat.from(context).getNotificationChannel("remind")!!.importance)
    val item = time.calendarItem
    val line1 = "${time.place.run { if (isNotEmpty()) plus("/") else "" }}${time.timeStr}"
    // TODO 跳转到对应日程的详情界面和管理导航顺序（看文档）
    var builder = NotificationCompat.Builder(context, "remind")
        .setWhen(System.currentTimeMillis())
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("${context.getString(R.string.notification_remind_prefix)}${item.name}${time.name}")
        .setContentText(line1)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
    NotificationManagerCompat.from(context).notify(((System.currentTimeMillis().toInt() % 1000000) + 1000000), builder.build())
}



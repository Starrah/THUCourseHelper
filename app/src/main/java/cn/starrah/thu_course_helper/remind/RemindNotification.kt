package cn.starrah.thu_course_helper.remind

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cn.starrah.thu_course_helper.MainActivity
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.TableFragment
import cn.starrah.thu_course_helper.activity.ItemShowActivity
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeDataWithItem

fun showRemindNotification(context: Context, time: CalendarTimeDataWithItem) {
    println(NotificationManagerCompat.from(context).getNotificationChannel("remind")!!.importance)
    val item = time.calendarItem
    val line1 = "${time.place.run { if (isNotEmpty()) plus("/") else "" }}${time.timeStr}"
    val intent = Intent(context, ItemShowActivity::class.java)
    intent.putExtra(TableFragment.EXTRA_MESSAGE, time.calendarItem.id)
    val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
    val builder = NotificationCompat.Builder(context, "remind")
        .setContentIntent(pendingIntent)
        .setWhen(System.currentTimeMillis())
        .setSmallIcon(R.drawable.logo)
        .setContentTitle("${context.getString(R.string.notification_remind_prefix)}${item.name}${time.name}")
        .setContentText(line1)
        .setVibrate(longArrayOf(0, 1000))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
    NotificationManagerCompat.from(context).notify(((System.currentTimeMillis().toInt() % 1000000) + 1000000), builder.build())
}



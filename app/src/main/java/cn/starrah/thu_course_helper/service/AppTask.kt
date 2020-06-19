package cn.starrah.thu_course_helper.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.widget.Toast
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.remind.setRemindTimerServiceForAll
import cn.starrah.thu_course_helper.utils.trySyncExam
import cn.starrah.thu_course_helper.utils.trySyncHomework

suspend fun allAppTask(context: Context) {
    configNotificationChannel(context)
    // APP任务，APP每次唤醒（主动唤醒、被动唤醒）都应该执行。
    // 暂时包括：

    // 设置闹钟计时器
    setRemindTimerServiceForAll(context, shouldCancel = false)

    // 刷新作业
    if (!trySyncHomework(context)) {
        Toast.makeText(context, R.string.warning_sync_hw_fail, Toast.LENGTH_SHORT).show()
    }

    // 刷新考试
    if (!trySyncExam(context)) {
        Toast.makeText(context, R.string.warning_sync_exam_fail, Toast.LENGTH_SHORT).show()
    }
}

fun configNotificationChannel(context: Context) {
    // Create the NotificationChannel
    val name = context.resources.getString(R.string.notification_channel_remind_name)

    val importance = NotificationManager.IMPORTANCE_HIGH
    val mChannel = NotificationChannel("remind", name, importance)
    // Register the channel with the system; you can't change the importance
    // or other notification behaviors after this
    val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(mChannel)
}


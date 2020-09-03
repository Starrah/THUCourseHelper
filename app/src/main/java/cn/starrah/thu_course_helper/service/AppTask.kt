package cn.starrah.thu_course_helper.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.graphics.Color
import android.os.Build
import android.widget.Toast
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.remind.setRemindTimerServiceForAll
import cn.starrah.thu_course_helper.utils.trySyncExam
import cn.starrah.thu_course_helper.utils.trySyncHomework
import java.lang.Exception

suspend fun allAppTask(context: Context, fromMainActivity: Boolean = false) {
    configNotificationChannel(context)
    // APP任务，APP每次唤醒（主动唤醒、被动唤醒）都应该执行。
    // 被动唤醒暂时指1h唤醒一次。
    // 暂时包括：

    // 设置闹钟计时器
    setRemindTimerServiceForAll(context, shouldCancel = false)

    // 尝试拉取推送信息
    try {
        pullMessage(context, fromMainActivity)
    }
    catch (e: Exception) {
        e.printStackTrace()
    }

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
    val notificationManager =
        context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    run {
        val name = context.resources.getString(R.string.notification_channel_remind_name)
        val mChannel = NotificationChannel("remind", name, NotificationManager.IMPORTANCE_HIGH)
        mChannel.enableVibration(true)
        mChannel.description = "您在APP中对日程设置的定时提醒"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mChannel.setAllowBubbles(true)
        }
        notificationManager.createNotificationChannel(mChannel)
    }
    run {
        val mChannel = NotificationChannel(
            "stay_notify",
            "常驻通知栏面板",
            NotificationManager.IMPORTANCE_MIN
        )
        mChannel.description = "显示今日的课程和日程，可在APP设置中打开"
        mChannel.enableLights(false)
        mChannel.lightColor = Color.BLUE
        mChannel.setSound(null, null)
        mChannel.enableVibration(false)
        notificationManager.createNotificationChannel(mChannel)
    }
    run {
        val mChannel =
            NotificationChannel("message", "消息通知", NotificationManager.IMPORTANCE_DEFAULT)
        mChannel.description = "如选课时间节点提醒、APP版本更新提示等。"
        notificationManager.createNotificationChannel(mChannel)
    }
}


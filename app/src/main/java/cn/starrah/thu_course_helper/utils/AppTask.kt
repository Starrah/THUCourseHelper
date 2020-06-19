package cn.starrah.thu_course_helper.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.data.SPRING2019TERMJSON
import cn.starrah.thu_course_helper.data.declares.school.SchoolTerm
import cn.starrah.thu_course_helper.onlinedata.backend.BACKEND_SITE
import cn.starrah.thu_course_helper.onlinedata.backend.BackendAPICheckVersion
import cn.starrah.thu_course_helper.onlinedata.backend.BackendAPITermData
import cn.starrah.thu_course_helper.onlinedata.backend.TermDescription
import cn.starrah.thu_course_helper.service.setAlarmForAll
import com.alibaba.fastjson.JSON
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

suspend fun allAppTask(context: Context) {
    setAlarmForAll(context)
    // APP任务，APP每次唤醒（主动唤醒、被动唤醒）都应该执行。
    if (!trySyncHomework(context)) {
        Toast.makeText(context, R.string.warning_sync_hw_fail, Toast.LENGTH_SHORT).show()
    }
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

fun updateWidgetView(){}
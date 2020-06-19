package cn.starrah.thu_course_helper.utils

import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.widget.AppWidgetCourse
import cn.starrah.thu_course_helper.widget.AppWidgetTime
import cn.starrah.thu_course_helper.widget.NotificationCourse
import cn.starrah.thu_course_helper.widget.NotificationTime

//小部件-通知栏更新函数

fun Context.updateWidgetsAndNotification() {
    //更新小部件
    updateAppWidgets()

    //按照设置更新通知栏
    val sp = PreferenceManager.getDefaultSharedPreferences(this)
    val notifications_settings = sp.getString("stay_notice", null)
    if(notifications_settings == resources.getString(R.string.settings_stay_notice_no)) {
        cancelNotificationCourse()
        cancelNotificationTime()
    }
    else if(notifications_settings == resources.getString(R.string.settings_stay_notice_course)) {
        updateNotificationCourse()
        cancelNotificationTime()
    }
    else {
        updateNotificationCourse()
        updateNotificationTime()
    }
}

/**
 * 描述：更新小部件
 * 参数：无
 * 返回：无
 */
fun Context.updateAppWidgets() {
    val intent_widget_time = Intent(this, AppWidgetTime::class.java)
    intent_widget_time.setAction("update_action")
    val intent_widget_course = Intent(this, AppWidgetCourse::class.java)
    intent_widget_course.setAction("update_action")
    sendBroadcast(intent_widget_time)
    sendBroadcast(intent_widget_course)
}

/**
 * 描述：更新通知栏(日程）
 * 参数：无
 * 返回：无
 */
fun Context.updateNotificationTime() {
    val intent_notify_time = Intent(this, NotificationTime::class.java)
    intent_notify_time.setAction("update_action")
    sendBroadcast(intent_notify_time)
}

/**
 * 描述：更新通知栏(课程）
 * 参数：无
 * 返回：无
 */
fun Context.updateNotificationCourse() {
    val intent_notify_course = Intent(this, NotificationCourse::class.java)
    intent_notify_course.setAction("update_action")
    sendBroadcast(intent_notify_course)
}

/**
 * 描述：取消通知栏（日程）
 * 参数：无
 * 返回：无
 */
fun Context.cancelNotificationTime() {
    val intent_notify_time = Intent(this, NotificationTime::class.java)
    intent_notify_time.setAction("delete_action")
    sendBroadcast(intent_notify_time)
}

/**
 * 描述：取消通知栏（课程）
 * 参数：无
 * 返回：无
 */
fun Context.cancelNotificationCourse() {
    val intent_notify_course = Intent(this, NotificationCourse::class.java)
    intent_notify_course.setAction("delete_action")
    sendBroadcast(intent_notify_course)
}
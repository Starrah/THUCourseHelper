package cn.starrah.thu_course_helper.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.onlinedata.backend.BackendAPIPullMessage
import cn.starrah.thu_course_helper.onlinedata.backend.PullMessageAPIRespItem
import com.alibaba.fastjson.JSON
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * @return null表示此刻不应发起网络请求拉取消息；非null的字符串表示应当在拉取消息的接口中传入的type参数。
 */
fun shouldPullMessageType(context: Context, force: Boolean = false): String? {
    val PULL_MESSAGE_INTERVAL_NOHMEX = 60 * 24 // 1天刷新一次
    val sp = PreferenceManager.getDefaultSharedPreferences(context)
    val sync_hmex_open = // 是否该用户可以自动刷新作业和考试。此种情况下刷新频率为每次apptask必刷新(2h)，否则刷新频率暂定为1天。
        sp.getStringSet("sync_hmex", setOf())!!.size != 0 && sp.getInt("login_status", 0) == 2
    val lastPullMessageTime = sp.getString("lastPullMessageTime", null)
        ?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME) }
    val lastPullMessagePassed = lastPullMessageTime?.let {
        Duration.between(it, LocalDateTime.now()).toMinutes().toInt()
    } ?: 100000
    if (force || sync_hmex_open || (lastPullMessagePassed >= PULL_MESSAGE_INTERVAL_NOHMEX)) {
        return sp.getString("pull_message_type", "")
    }
    else return null
}

val timedPullMessageAction = "timedPullMessageAction"
val EXTRA_timedPullMessageAction_objStr = "EXTRA_timedPullMessageAction_objStr"

suspend fun pullMessage(context: Context, force: Boolean = false) {
    val sp = PreferenceManager.getDefaultSharedPreferences(context)
    val pullTypeStr = shouldPullMessageType(context, force)
    if (pullTypeStr != null) {
        val remindedList = sp.getStringSet("remindedList", setOf())!!
        val rawRes = withContext(Dispatchers.IO) {
            BackendAPIPullMessage(
                pullTypeStr,
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            )
        }
        val res = rawRes.filter { it.id !in remindedList }
        // 有time字段、且time字段指定的时间晚于当前时间的，就将被设置alarm。
        val laterRemind = res.filter {
            it.time != null && ZonedDateTime.parse(it.time, DateTimeFormatter.ISO_DATE_TIME)
                .toLocalDateTime() - Duration.ofMinutes(1) > LocalDateTime.now()
        }
        val nowRemind = res - laterRemind
        // 立即提醒
        for (r in nowRemind) {
            showPulledMessageNotification(context, r)
        }
        // 延期提醒，设置alarm
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (r in laterRemind) {
            val toRemindTime = ZonedDateTime.parse(r.time, DateTimeFormatter.ISO_DATE_TIME)
            val seriStr = JSON.toJSONString(r)
            val alarmIntent = Intent(context, TimedPullMessageReceiver::class.java).apply {
                action = timedPullMessageAction
                addCategory("messageId:${r.id}")
                putExtra(EXTRA_timedPullMessageAction_objStr, seriStr)
            }.let { PendingIntent.getBroadcast(context, 0, it, 0) }
            alarmMgr.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                toRemindTime.toInstant().toEpochMilli(),
                alarmIntent
            )
        }
        sp.edit {
            putString(
                "lastPullMessageTime",
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
            )
        }
    }
}


fun showPulledMessageNotification(context: Context, message: PullMessageAPIRespItem) {
    println(NotificationManagerCompat.from(context).getNotificationChannel("message")!!.importance)
    val builder = NotificationCompat.Builder(context, "remind")
        .setWhen(System.currentTimeMillis())
        .setSmallIcon(R.drawable.logo)
        .setContentTitle(message.title)
    if (message.body != null) {
        builder.setContentText(message.body)
        builder.setStyle(NotificationCompat.BigTextStyle().bigText(message.body))
    }
    if (message.intentUri != null) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(message.intentUri))
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        builder.setContentIntent(pendingIntent)
    }
    NotificationManagerCompat.from(context)
        .notify(System.currentTimeMillis().toInt(), builder.build())
    val sp = PreferenceManager.getDefaultSharedPreferences(context)
    sp.edit(commit = true) {
        putStringSet(
            "remindedList",
            sp.getStringSet("remindedList", setOf())!!.toMutableSet().apply { add(message.id) })
    }
}

class TimedPullMessageReceiver() : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val obj = JSON.parseObject(
            intent.getStringExtra(EXTRA_timedPullMessageAction_objStr),
            PullMessageAPIRespItem::class.java
        )
        if (obj.id in sp.getStringSet("remindedList", setOf())!!) return // 若已提醒过就不提醒了
        showPulledMessageNotification(context, obj)
    }
}
package cn.starrah.thu_course_helper.remind

import android.app.Activity
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.provider.AlarmClock
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cn.starrah.thu_course_helper.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime


class RemindHelperActivity() : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.remind_helper)
        val fromIntent = this.intent

        // 这个全屏Intent对应的Activity，只做一件事：设置闹钟
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, fromIntent.getIntExtra(AlarmClock.EXTRA_HOUR, 0))
            putExtra(AlarmClock.EXTRA_MINUTES, fromIntent.getIntExtra(AlarmClock.EXTRA_MINUTES, 0))
            putExtra(AlarmClock.EXTRA_MESSAGE, fromIntent.getStringExtra(AlarmClock.EXTRA_MESSAGE))
            putExtra(
                AlarmClock.EXTRA_SKIP_UI,
                fromIntent.getBooleanExtra(AlarmClock.EXTRA_SKIP_UI, true)
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(packageManager) == null) {
            Toast.makeText(this, R.string.error_alarm_not_supported, Toast.LENGTH_LONG).show()
            throw Exception(getString(R.string.error_alarm_not_supported))
        }
        startActivity(intent)

        // 100ms后自动销毁
        GlobalScope.launch {
            delay(100)
            finish()
        }
    }
}

class ReminderHelperService() : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        var alarmTime: LocalTime? = null
        var message: String? = null
    }

    var iid: Int? = null

    override fun onCreate() {
        super.onCreate()
        val context = this

        // 通过发起前台服务（产生运行中的通知），实现进程唤醒
        // 构造全屏Intent，其作用是渲染RemindHelperActivity出来
        val pendingIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, RemindHelperActivity::class.java).apply {
                putExtra(AlarmClock.EXTRA_HOUR, alarmTime!!.hour)
                putExtra(AlarmClock.EXTRA_MINUTES, alarmTime!!.minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        // 构造全屏通知，指定其全屏Intent为上面的PendingIntent
        // 这样，当这个通知被发出时上面的PendingIntent会自动触发，引起RemindHelperActivity的调用
        val builder = NotificationCompat.Builder(context, "remind")
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("用于唤醒应用")
            .setContentText("请您忽略")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setNotificationSilent()
            .setFullScreenIntent(pendingIntent, true)
        iid = System.currentTimeMillis().toInt()
        startForeground(iid!!, builder.build())

        // 这个service里面也设置闹钟一下。虽然不一定有用。
        val anotherintent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, alarmTime!!.hour)
            putExtra(AlarmClock.EXTRA_MINUTES, alarmTime!!.minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (anotherintent.resolveActivity(packageManager) == null) {
            Toast.makeText(this, R.string.error_alarm_not_supported, Toast.LENGTH_LONG).show()
            throw Exception(getString(R.string.error_alarm_not_supported))
        }
        startActivity(anotherintent)

        // 1000ms后前台服务自动结束
        GlobalScope.launch {
            delay(1000)
            stopSelf()
        }

    }

    override fun onDestroy() {
        // 结束时关闭通知
        val mManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mManager.cancel(iid!!)
        super.onDestroy()
    }


}


val ACTION_RemindAlarmBackupNotice = "cn.starrah.thu_course_helper.RemindAlarmBackupNotice"

class RemindAlarmBackupNoticeReceiver() : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title")
        val line1 = intent.getStringExtra("line1")
        // 备用纯通知，在闹钟响铃的时间显示，
        val builder = NotificationCompat.Builder(context, "remind")
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(line1)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
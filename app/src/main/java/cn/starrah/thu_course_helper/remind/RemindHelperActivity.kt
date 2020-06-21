package cn.starrah.thu_course_helper.remind

import android.app.Activity
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.provider.AlarmClock
import android.widget.Toast
import androidx.core.app.NotificationCompat
import cn.starrah.thu_course_helper.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RemindHelperActivity(): Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.remind_helper)
        val fromIntent = this.intent

        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, fromIntent.getIntExtra(AlarmClock.EXTRA_HOUR, 0))
            putExtra(AlarmClock.EXTRA_MINUTES, fromIntent.getIntExtra(AlarmClock.EXTRA_MINUTES, 0))
            putExtra(AlarmClock.EXTRA_MESSAGE, fromIntent.getStringExtra(AlarmClock.EXTRA_MESSAGE))
            putExtra(AlarmClock.EXTRA_SKIP_UI, fromIntent.getBooleanExtra(AlarmClock.EXTRA_SKIP_UI, true))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(packageManager) == null) {
            Toast.makeText(this, R.string.error_alarm_not_supported, Toast.LENGTH_LONG).show()
            throw Exception(getString(R.string.error_alarm_not_supported))
        }
        startActivity(intent)
        GlobalScope.launch {
            delay(100)
            finish()
        }
    }
}

class ReminderHelperService(): Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onCreate() {
        super.onCreate();

        val builder = NotificationCompat.Builder(this, "remind")
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("DaemonService")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setNotificationSilent()

        startForeground(4353, builder.build());

        GlobalScope.launch {
            delay(1000)
            stopSelf()
        }

    }


}
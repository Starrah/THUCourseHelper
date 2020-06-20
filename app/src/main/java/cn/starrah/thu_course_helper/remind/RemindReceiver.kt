package cn.starrah.thu_course_helper.remind

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarRemindMethodType
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarRemindType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDateTime

val remindIntentAction = "cn.starrah.thu_course_helper.remind"

/**
 * Int格式的EXTRA。
 */
val EXTRA_broadCastBeforeSecond = "broadCastBeforeSecond"
val EXTRA_onlyClear = "onlyClear"

class RemindReceiver() : BroadcastReceiver() {
    val MOST_DELAY = Duration.ofMinutes(15)

    override fun onReceive(context: Context, intent: Intent) {
        val broadCastBeforeSecond = intent.getIntExtra(EXTRA_broadCastBeforeSecond, 0)
        val now = LocalDateTime.now() + Duration.ofSeconds(broadCastBeforeSecond.toLong())
        val timeId = intent.categories.find { "timeId" in it }!!.substring(7).toInt()
        val onlyClear = intent.getBooleanExtra(EXTRA_onlyClear, false)
        val pendingResult: PendingResult = goAsync()

        GlobalScope.launch {
            CREP.initializeDefaultTermIfUninitialized(context, true)
            val time = withContext(Dispatchers.IO) {
                CREP.DAO.findTimesByIdsNoLive(listOf(timeId)).single()
            }
            val pushBeforeTime =
                now - ((MOST_DELAY - time.remindData.aheadTime).coerceAtLeast(Duration.ZERO))
            val correctRemindTime = time.getNextRemindTime(pushBeforeTime)
            val correctStartTime = time.getNextHappenTime(pushBeforeTime)

            if (correctRemindTime != null &&
                now in (correctRemindTime - Duration.ofMinutes(1))..(correctRemindTime + MOST_DELAY)) {
                if (!onlyClear) {
                    when (time.remindData.method) {
                        CalendarRemindMethodType.NOTICE -> showRemindNotification(context, time)
                        CalendarRemindMethodType.ALARM  -> makeRemindSystemAlarm(context, time)
                    }
                }
                if (time.remindData.type != CalendarRemindType.REPEAT) {
                    time.remindData.type = CalendarRemindType.NONE
                    CREP.updateTimes(listOf(time))
                }
            }

            if (time.remindData.type == CalendarRemindType.REPEAT && correctStartTime != null) {
                setRemindTimerService(
                    context,
                    time,
                    correctStartTime.first + Duration.ofMinutes(1),
                    shouldCancel = true
                )
            }

            pendingResult.finish()
        }
    }
}
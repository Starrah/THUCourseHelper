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
val EXTRA_forceNotice = "forceNotice"

class RemindReceiver() : BroadcastReceiver() {
    val MOST_DELAY = Duration.ofMinutes(15) // 最多允许提醒比活动开始时间延迟15分钟，否则直接放弃提醒

    override fun onReceive(context: Context, intent: Intent) {
        val broadCastBeforeSecond = intent.getIntExtra(EXTRA_broadCastBeforeSecond, 0)
        val now = LocalDateTime.now() + Duration.ofSeconds(broadCastBeforeSecond.toLong())
        val timeId = intent.categories.find { "timeId" in it }!!.substring(7).toInt()
//        val onlyClear = intent.getBooleanExtra(EXTRA_onlyClear, false)
//        val forceNotice = intent.getBooleanExtra(EXTRA_forceNotice, false)
        val pendingResult: PendingResult = goAsync()

        GlobalScope.launch {
            CREP.initializeDefaultTermIfUninitialized(context, true)
            val time = withContext(Dispatchers.IO) {
                CREP.DAO.findTimesByIdsNoLive(listOf(timeId)).single()
            }
            // 方法：通过当前时间前推15分钟后调用函数求下次开始时间，来确认当前时间是否是保证在比活动开始时间延迟15分钟以内。
            val pushBeforeTime =
                now - ((MOST_DELAY - time.remindData.aheadTime).coerceAtLeast(Duration.ZERO))
            val correctStartTime = time.getNextHappenTime(pushBeforeTime)?.first
            val correctRemindTime = correctStartTime?.let { it - time.remindData.aheadTime }

            // 提醒确实发生，当且仅当当前时间落在（应该提醒时间~活动开始时间延迟15分钟）的范围内。
            if (correctRemindTime != null &&
                now in (correctRemindTime - Duration.ofMinutes(1))..(correctStartTime + MOST_DELAY)) {
//                if (!onlyClear) {
                when (time.remindData.method) {
                    CalendarRemindMethodType.NOTICE -> showRemindNotification(context, time)
                    CalendarRemindMethodType.ALARM -> {
//                            if (forceNotice) showRemindNotification(context, time)
//                            else
                        makeRemindSystemAlarm(context, time)
                    }
                }
            }
            // 非重复类型，在提醒完成一次后立即清空提醒设置
            if (time.remindData.type != CalendarRemindType.REPEAT) {
                time.remindData.type = CalendarRemindType.NONE
                CREP.updateTimes(listOf(time))
            }

            // 如果是重复的时间类型则设置下一次闹钟
            if (time.remindData.type == CalendarRemindType.REPEAT) {
                // now+提前时间+2分钟，可以保证在正常情况下是超过了本次的开始时间的。
                // 以此为baseTime设置下一次RemindTimer
                val baseTimeNext = now + time.remindData.aheadTime + Duration.ofMinutes(2)
                setRemindTimerService(
                    context,
                    time,
                    baseTimeNext,
                    shouldCancel = true
                )
            }

            pendingResult.finish()
        }
    }
}
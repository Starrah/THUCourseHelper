package cn.starrah.thu_course_helper.utils

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.data.database.CREP
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun shouldSyncHomework(context: Context): Boolean {
    val HW_SYNC_INTERVAL_MINUTES = 120

    val sp = PreferenceManager.getDefaultSharedPreferences(context)
    if (!(context.resources.getString(R.string.settings_open_sync_homework) in
                sp.getStringSet("sync_hmex", setOf())!! && sp.getInt("login_status", 0) == 2))
        return false

    val lastSyncHWTime = sp.getString("lastSyncHWTime", null)
        ?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME) }
    val lastSyncHWTimePassed = lastSyncHWTime?.let {
        Duration.between(it, LocalDateTime.now()).toMinutes().toInt()
    } ?: 100000

    return lastSyncHWTimePassed >= HW_SYNC_INTERVAL_MINUTES
}

fun setLastSyncHomeworkDatetime(context: Context) {
    val sp = PreferenceManager.getDefaultSharedPreferences(context)
    sp.edit {
        putString("lastSyncHWTime", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
    }
}

fun shouldSyncExam(context: Context): Boolean {
    val EXAM_SYNC_INTERVAL_DAYS = 7

    val sp = PreferenceManager.getDefaultSharedPreferences(context)
    if (!(context.resources.getString(R.string.settings_open_sync_exam) in
                sp.getStringSet("sync_hmex", setOf())!! && sp.getInt("login_status", 0) == 2))
        return false

    val lastSyncExamTime = sp.getString("lastSyncExamTime", null)
        ?.let { LocalDate.parse(it, DateTimeFormatter.ISO_DATE) }
    val lastSyncExamTimePassed = lastSyncExamTime?.let {
        Period.between(it, LocalDate.now()).get(ChronoUnit.DAYS).toInt()
    } ?: 100000

    return lastSyncExamTimePassed >= EXAM_SYNC_INTERVAL_DAYS
}

fun setLastSyncExamDate(context: Context) {
    val sp = PreferenceManager.getDefaultSharedPreferences(context)
    sp.edit {
        putString("lastSyncExamTime", LocalDate.now().format(DateTimeFormatter.ISO_DATE))
    }
}

/**
 * @return false表示发生了异常
 */
suspend fun trySyncHomework(context: Context): Boolean {
    try {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        if (shouldSyncHomework(context)) {
            CREP.onlineCourseDataSource!!.loadData(
                CREP.term, mapOf(
                    "homework" to true,
                    "activity" to context,
                    "username" to sp.getString("login_name", null)!!,
                    "password" to CREP.getUserPassword(context),
                    "onlyUnsubmitted" to true,
                    "apply" to true
                )
            )
            setLastSyncHomeworkDatetime(context)
        }
        return true
    }
    catch (e: Exception) {
        return false
    }

}

/**
 * @return false表示发生了异常
 */
suspend fun trySyncExam(context: Context): Boolean {
    try {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        if (shouldSyncExam(context)) {
            CREP.onlineCourseDataSource!!.loadData(
                CREP.term, mapOf(
                    "exam" to true,
                    "username" to sp.getString("login_name", null)!!,
                    "password" to CREP.getUserPassword(context),
                    "apply" to true
                )
            )
            setLastSyncExamDate(context)
        }
        return true
    }
    catch (e: Exception) {
        return false
    }
}
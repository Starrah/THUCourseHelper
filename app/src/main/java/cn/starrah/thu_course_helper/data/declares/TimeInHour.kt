package cn.starrah.thu_course_helper.data.declares

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

data class TimeInHour(
    /** 开始时间 */
    var startTime: LocalTime,

    /** 结束时间 */
    var endTime: LocalTime,

    /** 周几。此字段不一定含有（只在对应的类型为重复的情况下才含有） */
    var dayOfWeek: DayOfWeek? = null,

    /** 日期。此字段不一定含有（只在对应的类型为单次的情况下才含有） */
    var date: LocalDate? = null
) {
    val length: Duration
        get() = Duration.between(startTime, endTime)

    fun toTimeInCourseSchedule(): TimeInCourseSchedule {
        TODO()
    }
}
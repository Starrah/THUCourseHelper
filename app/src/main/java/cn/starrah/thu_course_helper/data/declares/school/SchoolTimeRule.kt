package cn.starrah.thu_course_helper.data.declares.school

import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.time.Duration

/**
 * 描述每天的课程大小节时间安排表。
 */
data class SchoolTimeRule(
    /** 一天的所有大节 */
    val bigs: List<BigClass>
) {
    data class BigClass(
        /** 一个大节中的所有小节 */
        val smalls: List<SmallClass>
    ) {
        val start: LocalTime
            get() = smalls.first().start
        val end: LocalTime
            get() = smalls.last().end
        val totalLengthInMinutes: Int
            get() = ChronoUnit.MINUTES.between(start, end).toInt()
        val totalLength: Duration
            get() = Duration.ofMinutes(totalLengthInMinutes.toLong())
        val realClassLengthInMinutes: Int
            get() = smalls.fold(0) {v, obj -> v + obj.lengthInMinutes}
        val realClassLength: Duration
            get() = Duration.ofMinutes(realClassLengthInMinutes.toLong())
    }
    data class SmallClass(
        /**
         * 开始时间
         */
        val start: LocalTime,

        /**
         * 结束时间
         */
        val end: LocalTime
    ) {
        val lengthInMinutes = ChronoUnit.MINUTES.between(start, end).toInt()
        val length: Duration
            get() = Duration.ofMinutes(lengthInMinutes.toLong())
    }
}
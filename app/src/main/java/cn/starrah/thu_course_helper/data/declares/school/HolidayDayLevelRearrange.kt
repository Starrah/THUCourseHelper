package cn.starrah.thu_course_helper.data.declares.school

import java.time.LocalDate

/**
 * 描述一个受到节假日调课安排影响的日子的调课安排内容
 */
data class HolidayDayLevelRearrange(
    /**
     * 一个受到节假日安排影响的日期
     */
    val date: LocalDate,

    /**
     * [date]日子的原排课程被改到了哪一日进行。如果为null，表示该日原排课程停上。
     */
    val to: LocalDate? = null
)
package cn.starrah.thu_course_helper.data.declares

import java.time.LocalDate

/**
 * 描述一个受到节假日调课安排影响的日子的调课安排内容
 */
data class HolidayDayLevelRearrange(
    /**
     * 一个具体的日期
     */
    val date: LocalDate,

    /**
     * [date]日子，按照学校安排，实际上应该上哪一个日子的课程。如果为null，表示该日原排课程停上。
     */
    val actualCourseDate: LocalDate?
)
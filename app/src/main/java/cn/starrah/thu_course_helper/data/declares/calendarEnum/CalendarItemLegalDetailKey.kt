package cn.starrah.thu_course_helper.data.declares.calendarEnum

import androidx.room.TypeConverter

/**
 * 日程的详情列表允许的Key。
 * @param [chineseName] 中文名，可直接用于前端显示
 * @param [allowedTypes] 该Key允许在哪些种类的日程中出现。
 */
enum class CalendarItemLegalDetailKey(
    val chineseName: String,
    val allowedTypes: List<CalendarItemType>
) {
    COMMENT("说明", CalendarItemType.values().asList()),
    COURSEID("课程号", listOf(CalendarItemType.COURSE)),
    TEACHER("教师", listOf(CalendarItemType.COURSE)),
    ORGANIZATION("组织", listOf(
        CalendarItemType.SOCIALWORK,
        CalendarItemType.ASSOCIATION
    )), ;

    class TC {
        @TypeConverter
        fun toDBDataType(value: CalendarItemLegalDetailKey): String {
            return value.name
        }

        @TypeConverter
        fun fromDBDataType(value: String): CalendarItemLegalDetailKey {
            return valueOf(value)
        }
    }

}
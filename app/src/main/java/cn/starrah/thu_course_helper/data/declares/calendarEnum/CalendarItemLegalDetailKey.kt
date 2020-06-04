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
    ORGANIZATION(
        "组织", listOf(
            CalendarItemType.SOCIALWORK,
            CalendarItemType.ASSOCIATION
        )
    ),

    /** 表示这个数据是不是从网络上获取的。对于网络获取数据与本地的合并策略有帮助。取值只有"y"一种，否则直接为null。
     *
     * **前端请不要显示这个字段的值！！** */
    FROM_WEB("", CalendarItemType.values().asList()),
    ;

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
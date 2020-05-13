package cn.starrah.thu_course_helper.data.declares.calendarEnum

import androidx.room.TypeConverter

/**
 * 日程的类型
 * @param [chineseName] 中文名，可直接用于前端显示
 */
enum class CalendarItemType(val chineseName: String) {
    COURSE("课程"),
    RESEARCH("科研"),
    SOCIALWORK("社工"),
    ASSOCIATION("社团"),
    OTHER("其他"),
    ;

    /** 获取该种活动类型允许的所有Key的列表。可能在编辑日程的时候会用到。 */
    val AllowedDetailKeys: List<CalendarItemLegalDetailKey>
        get() = CalendarItemLegalDetailKey.values().filter { it.allowedTypes.contains(this) }

    class TC {
        @TypeConverter
        fun toDBDataType(value: CalendarItemType): String {
            return value.name
        }

        @TypeConverter
        fun fromDBDataType(value: String): CalendarItemType {
            return valueOf(value)
        }
    }
}
package cn.starrah.thu_course_helper.data.declares.calendarEnum

import androidx.room.TypeConverter

/**
 * 时间段的类型。
 * @param [chineseName] 中文名，可直接用于前端显示
 */
enum class CalendarTimeType(val chineseName: String) {
    SINGLE_COURSE("单次（按大节）"),
    REPEAT_COURSE("重复（按大节）"),
    SINGLE_HOUR("单次（按时间）"),
    REPEAT_HOUR("重复（按时间）"),
    POINT("时间节点"),
    ;

    class TC {
        @TypeConverter
        fun toDBDataType(value: CalendarTimeType): String {
            return value.name
        }

        @TypeConverter
        fun fromDBDataType(value: String): CalendarTimeType {
            return valueOf(value)
        }
    }

    /**
     * 该时间段在现在的类型。
     */
    enum class TodayType {
        /** 今天不会发生 */ NONE,
        /** 今天已经发生完了 */ PAST,
        /** 今天正在发生 */ NOW,
        /** 今天将来会发生 */ FUTURE,
        ;
    }
}
package cn.starrah.thu_course_helper.data.declares

import androidx.room.TypeConverter

enum class CalendarRemindType(val chineseName: String) {
    NONE("无"),
    SINGAL("仅这一次"),
    REPEAT("每次"),
    ;

    class TC {
        @TypeConverter
        fun toDBDataType(value: CalendarRemindType): String {
            return value.name
        }

        @TypeConverter
        fun fromDBDataType(value: String): CalendarRemindType {
            return valueOf(value)
        }
    }
}
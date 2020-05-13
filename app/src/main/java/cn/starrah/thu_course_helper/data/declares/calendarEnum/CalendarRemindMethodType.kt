package cn.starrah.thu_course_helper.data.declares.calendarEnum

import androidx.room.TypeConverter

enum class CalendarRemindMethodType(val chineseName: String) {
    NOTICE("通知栏"),
    ALARM("闹钟"),
    ;

    class TC {
        @TypeConverter
        fun toDBDataType(value: CalendarRemindMethodType): String {
            return value.name
        }

        @TypeConverter
        fun fromDBDataType(value: String): CalendarRemindMethodType {
            return valueOf(value)
        }
    }
}
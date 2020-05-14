package cn.starrah.thu_course_helper.data.declares.calendarEntity

import androidx.room.TypeConverter
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarRemindMethodType
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarRemindType
import java.time.Duration

/**
 * 描述一个提醒安排的数据类。
 */
data class CalendarRemindData(
    /** 提醒类型 */
    var type: CalendarRemindType = CalendarRemindType.NONE,

    /** 提前时间 */
    var aheadTime: Duration = Duration.ZERO,

    /** 提醒方式 */
    var method: CalendarRemindMethodType = CalendarRemindMethodType.NOTICE,

    /** 闹钟铃声 */
    var alarmSound: String = ""
) {
    class TC {
        @TypeConverter
        fun toDBDataType(value: Duration): Long {
            return value.seconds
        }

        @TypeConverter
        fun fromDBDataType(value: Long): Duration {
            return Duration.ofSeconds(value)
        }
    }
}
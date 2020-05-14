package cn.starrah.thu_course_helper.data.declares.time

import androidx.room.TypeConverter
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import java.time.*
import java.time.format.DateTimeFormatter


data class TimeInHour(
    /** 开始时间 */
    var startTime: LocalTime,

    /** 结束时间 */
    var endTime: LocalTime,

    /** 周几。此字段不一定含有（只在对应的类型为重复的情况下才含有） */
    var dayOfWeek: DayOfWeek? = null,

    /** 日期。此字段不一定含有（只在对应的类型为单次的情况下才含有） */
    var date: LocalDate? = null
) {
    val length: Duration
        get() = Duration.between(startTime, endTime)

    fun toTimeInCourseSchedule(): TimeInCourseSchedule {
        TODO()
    }

    class TC {
        @TypeConverter
        fun toDBDataType(value: TimeInHour): String {
            val obj = JSONObject()
            obj["ST"] = value.startTime.format(DateTimeFormatter.ISO_TIME)
            obj["ET"] = value.endTime.format(DateTimeFormatter.ISO_TIME)
            obj["DW"] = value.dayOfWeek
            obj["DT"] = value.date?.format(DateTimeFormatter.ISO_DATE)
            return JSON.toJSONString(obj)
        }

        @TypeConverter
        fun fromDBDataType(value: String): TimeInHour {
            val obj = JSON.parseObject(value)
            return TimeInHour(
                LocalTime.parse(obj.getString("ST"), DateTimeFormatter.ISO_TIME),
                LocalTime.parse(obj.getString("ET"), DateTimeFormatter.ISO_TIME),
                obj.getString("DW")?.let { DayOfWeek.valueOf(it) },
                obj.getString("DT")?.let { LocalDate.parse(it, DateTimeFormatter.ISO_DATE) }
            )
        }
    }
}
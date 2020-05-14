package cn.starrah.thu_course_helper.data.declares.time

import androidx.room.TypeConverter
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.annotation.JSONField
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


data class TimeInHour(
    /** 开始时间 */
    @JSONField(format = "HH:mm")
    var startTime: LocalTime,

    /** 结束时间 */
    @JSONField(format = "HH:mm")
    var endTime: LocalTime,

    /** 周几。此字段不一定含有（只在对应的类型为重复的情况下才含有） */
    var dayOfWeek: DayOfWeek? = null,

    /** 日期。此字段不一定含有（只在对应的类型为单次的情况下才含有） */
    var date: LocalDate? = null
) {
    val length: Duration
        @JSONField(serialize = false)
        get() = Duration.between(startTime, endTime)

    fun toTimeInCourseSchedule(): TimeInCourseSchedule {
        TODO()
    }

    class TC {
        @TypeConverter
        fun toDBDataType(value: TimeInHour): String {
            return JSON.toJSONString(value)
        }

        @TypeConverter
        fun fromDBDataType(value: String): TimeInHour {
            return JSON.parseObject(value, TimeInHour::class.java)
        }
    }
}
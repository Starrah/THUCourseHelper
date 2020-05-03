package cn.starrah.thu_course_helper.data.declares

import androidx.room.TypeConverter
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 描述一个按大节定义的时间格式。
 */
data class TimeInCourseSchedule(
    /** 周几。
     *
     * 当定期的情况下此字段必然可以直接获取；当类型为单次的情况下，则可通过[LocalDate.getDayOfWeek]
     * 获取某个日子对应的星期后填入即可。 */
    var dayOfWeek: DayOfWeek = DayOfWeek.SUNDAY,

    /** 第几大节开始。注意第一大节为1。
     *
     * 例如一门课从第二大节一开始（第一小节）就开始上课，持续三小节，则*startBig*=2 */
    var startBig: Int = 0,

    /** 实际开始时间相对于上述大节的开始时间的偏移量。
     *
     * 例如一门课从第二大节一开始（第一小节）就开始上课，持续三小节，则*startOffsetSmall*=0.0f */
    var startOffsetSmall: Float = 0.0f,

    /** 持续的时间长度，以小节为单位
     *
     * 例如一门课从第二大节一开始（第一小节）就开始上课，持续三小节，则*lengthSmall*=3.0f */
    var lengthSmall: Float = 0.0f,

    /** 日期。此字段不一定含有（只在对应的类型为单次的情况下才含有） */
    var date: LocalDate? = null

) {
    /** 返回该以大节形式描述的时间定义的汉字格式，前端可使用显示。*/
    val chineseName: String
        get() {
            TODO()
        }

    /** 结束的时刻位于第几大节内。
     *
     * 例如一门课从第二大节一开始（第一小节）就开始上课，持续三小节，则*endBig*=2 */
    val endBig: Int
        get() {
            TODO()
        }

    /** 结束时刻相比于结束大节，发生了多少个小节的偏移。
     *
     * 例如一门课从第二大节一开始（第一小节）就开始上课，持续三小节，则*endOffsetSmall*=3.0f */
    val endOffsetSmall: Float
        get() {
            TODO()
        }

    fun toTimeInHour(): TimeInHour {
        TODO()
    }

    class TC {
        @TypeConverter
        fun toDBDataType(value: TimeInCourseSchedule): String {
            val obj = JSONObject()
            obj["DW"] = value.dayOfWeek
            obj["SB"] = value.startBig
            obj["SS"] = value.startOffsetSmall
            obj["L"] = value.lengthSmall
            obj["DT"] = value.date?.format(DateTimeFormatter.ISO_DATE)
            return JSON.toJSONString(obj)
        }

        @TypeConverter
        fun fromDBDataType(value: String): TimeInCourseSchedule {
            val obj = JSON.parseObject(value)
            return TimeInCourseSchedule(
                DayOfWeek.valueOf(obj.getString("DW")),
                obj.getIntValue("SB"),
                obj.getFloatValue("SS"),
                obj.getFloatValue("L"),
                obj.getString("DT")?.let { LocalDate.parse(it, DateTimeFormatter.ISO_DATE) }
            )
        }
    }
}
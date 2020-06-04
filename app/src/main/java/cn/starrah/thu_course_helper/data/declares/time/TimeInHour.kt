package cn.starrah.thu_course_helper.data.declares.time

import androidx.room.TypeConverter
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.utils.Verifiable
import cn.starrah.thu_course_helper.data.utils.assertData
import cn.starrah.thu_course_helper.data.utils.invLerp
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.annotation.JSONField
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime


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
): Verifiable {
    override fun assertValid() {
        assertData(endTime >= startTime, "结束时间不能早于开始时间！")
        date?.let { assertData(CREP.term.isDateInTerm(date!!), "设置的日期不在本学期内！") }
    }

    val length: Duration
        @JSONField(serialize = false)
        get() = Duration.between(startTime, endTime)

    private fun _hourToCourseNum(time: LocalTime, isForEndTime: Boolean): Pair<Int, Float> {
        val bigs = CREP.timeRule.bigs
        if (time < bigs[0].startTime) {
            return Pair(0, invLerp(time, null, bigs[0].startTime))
        } else if (time > CREP.timeRule.bigs.last().endTime) {
            return Pair(
                bigs.size + 1,
                invLerp(time, bigs.last().endTime, null)
            )
        } else if (isForEndTime) {
            var bigIndex = 0
            for (i in 1..bigs.size - 1) {
                if (bigs[i].startTime >= time) break
                bigIndex = i
            }
            val bigClass = bigs[bigIndex]
            var smallIndex = 0
            for (i in 1..bigClass.smalls.size - 1) {
                if (bigClass.smalls[i].startTime >= time) break
                smallIndex = i
            }
            val smallClass = bigClass.smalls[smallIndex]
            val smallOffset = smallIndex + invLerp(time, smallClass.startTime, smallClass.endTime)
            return Pair(bigIndex + 1, smallOffset)
        } else {
            var bigIndex = 0
            for (i in 0..bigs.size - 2) {
                if (bigs[i].endTime > time) break
                bigIndex = i + 1
            }
            val bigClass = bigs[bigIndex]
            var smallIndex = 0
            for (i in 0..bigClass.smalls.size - 2) {
                if (bigClass.smalls[i].endTime > time) break
                smallIndex = i + 1
            }
            val smallClass = bigClass.smalls[smallIndex]
            val smallOffset = smallIndex + invLerp(time, smallClass.startTime, smallClass.endTime)
            return Pair(bigIndex + 1, smallOffset)
        }
    }

    fun toTimeInCourseSchedule(): TimeInCourseSchedule {
        val (startBig, startOffsetSmall) = _hourToCourseNum(startTime, false)
        val (endBig, endOffsetSmall) = _hourToCourseNum(endTime, true)
        var length = -startOffsetSmall
        for (i in startBig until endBig) {
            length += CREP.timeRule.getBigByNumber(i).smallsCount
        }
        length += endOffsetSmall
        return TimeInCourseSchedule(
            dayOfWeek ?: date!!.dayOfWeek,
            startBig,
            startOffsetSmall,
            length,
            date
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TimeInHour) return false

        if (startTime != other.startTime) return false
        if (endTime != other.endTime) return false
        if (dayOfWeek != other.dayOfWeek) return false
        if (date != other.date) return false

        return true
    }

    override fun hashCode(): Int {
        var result = startTime.hashCode()
        result = 31 * result + endTime.hashCode()
        result = 31 * result + (dayOfWeek?.hashCode() ?: 0)
        result = 31 * result + (date?.hashCode() ?: 0)
        return result
    }

    class TC {
        @TypeConverter
        fun toDBDataType(value: TimeInHour?): String? {
            return value?.let { JSON.toJSONString(it) }
        }

        @TypeConverter
        fun fromDBDataType(value: String?): TimeInHour? {
            return value?.let { JSON.parseObject(it, TimeInHour::class.java) }
        }
    }
}
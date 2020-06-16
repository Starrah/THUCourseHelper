package cn.starrah.thu_course_helper.data.declares.calendarEntity

import androidx.lifecycle.LiveData
import androidx.room.*
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.database.CalendarRepository
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarRemindType
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarTimeType
import cn.starrah.thu_course_helper.data.declares.time.TimeInCourseSchedule
import cn.starrah.thu_course_helper.data.declares.time.TimeInHour
import cn.starrah.thu_course_helper.data.utils.Verifiable
import cn.starrah.thu_course_helper.data.utils.assertData
import cn.starrah.thu_course_helper.data.utils.assertDataSystem
import cn.starrah.thu_course_helper.data.utils.toTermDayId
import com.alibaba.fastjson.JSON
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 描述一个时间段的数据类。
 */
@Entity(
    foreignKeys = [ForeignKey(
        entity = CalendarItemData::class, parentColumns = arrayOf("id"),
        childColumns = arrayOf("item_id"), onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("item_id")]
)
open class CalendarTimeData(
    /** 时间段的数据库id，各个时间段唯一。当试图插入新时间段到数据库中时，请保证id为默认值0。 */
    @PrimaryKey(autoGenerate = true) var id: Int = 0,

    /** 时间段的名称 */
    var name: String = "",

    /** 时间段的类型 */
    var type: CalendarTimeType = CalendarTimeType.SINGLE_COURSE,

    /** 以大节格式描述的时间段信息。如果类型不是按大节，则为null。 */
    var timeInCourseSchedule: TimeInCourseSchedule? = null,

    /** 以时间格式描述的时间段信息。如果类型不是按时间或时间节点，则为null。 */
    var timeInHour: TimeInHour? = null,

    /** 需要让事件重复发生的所有周次的周次序号。注意从1开始。
     * 当事件类型为单次的时候，直接使用空列表（也就是默认的参数）即可。 */
    var repeatWeeks: MutableList<Int> = mutableListOf(),

    /** 地点 */
    var place: String = "",

    /** 说明 */
    var comment: String = "",

    /** 提醒设置 */
    @Embedded(prefix = "RMD") var remindData: CalendarRemindData = CalendarRemindData(),

    /** 该时间段所对应关联的日程项的数据表外键。默认等于calendarItem的id（如果传了calendarItem就不必传这个了）*/
    var item_id: Int = 0
) : Verifiable {
    /**
     * 可以在主线程调用。
     *
     * 获得该时间段所对应的日程项的、[CalendarItemDataWithTimes]格式数据的[LiveData]；
     */
    open suspend fun queryItem(): LiveData<CalendarItemDataWithTimes> {
        return CalendarRepository.findItemByTime(this)
    }

    /**
     * 计算该日程的日期设定在本学期的所有的日期的日期Id，以供数据库快速查询表使用。
     */
    fun calculateDayIdsInTerm(): List<Int> {
        return when (type) {
            CalendarTimeType.SINGLE_COURSE                       -> CREP.term.applyHolidayRearrange(
                listOf(timeInCourseSchedule!!.date!!.toTermDayId())
            )
            CalendarTimeType.REPEAT_COURSE                       -> {
                val zeroWeekId = timeInCourseSchedule!!.dayOfWeek.value - 7
                CREP.term.applyHolidayRearrange(repeatWeeks.map { zeroWeekId + (it * 7) })
            }
            CalendarTimeType.SINGLE_HOUR, CalendarTimeType.POINT -> listOf(timeInHour!!.date!!.toTermDayId())
            CalendarTimeType.REPEAT_HOUR                         -> {
                val zeroWeekId = timeInHour!!.dayOfWeek!!.value - 7
                repeatWeeks.map { zeroWeekId + (it * 7) }
            }
        }
    }

    /**
     * 该时间段数据，在现实中下次发生的时间。如果没有下一次发生的时间，那么为null。
     *
     * **数据类型说明**：由两个[LocalDateTime]组成的[Pair]，其中前一个表示开始时间、后一个表示结束时间。
     */
    val nextHappenTime: Pair<LocalDateTime, LocalDateTime>?
        get() {
            val timeData = when (type) {
                CalendarTimeType.SINGLE_COURSE, CalendarTimeType.REPEAT_COURSE                     -> timeInCourseSchedule!!.toTimeInHour()
                CalendarTimeType.SINGLE_HOUR, CalendarTimeType.REPEAT_HOUR, CalendarTimeType.POINT -> timeInHour!!
            }
            return when (type) {
                CalendarTimeType.SINGLE_COURSE, CalendarTimeType.SINGLE_HOUR, CalendarTimeType.POINT -> {
                    val realStartTime = LocalDateTime.of(timeData.date, timeData.startTime)
                    val realEndTime = LocalDateTime.of(timeData.date, timeData.endTime)
                    if (LocalDateTime.now() < realStartTime) Pair(
                        realStartTime,
                        realEndTime
                    )
                    else null
                }
                CalendarTimeType.REPEAT_COURSE, CalendarTimeType.REPEAT_HOUR                         -> {
                    val currentWeekNumber = CREP.term.dateToWeekNumber(LocalDate.now())
                    val futureTimes = repeatWeeks.filter { it >= currentWeekNumber }.sorted().run {
                        if (size > 2) subList(0, 2) else this
                    }.map {
                        Pair(
                            LocalDateTime.of(
                                CREP.term.dateOfWeekAndDay(it, timeData.dayOfWeek!!),
                                timeData.startTime
                            ),
                            LocalDateTime.of(
                                CREP.term.dateOfWeekAndDay(it, timeData.dayOfWeek!!),
                                timeData.endTime
                            )
                        )
                    }
                    var result: Pair<LocalDateTime, LocalDateTime>? = null
                    for (futureTime in futureTimes) {
                        if (LocalDateTime.now() < futureTime.first) {
                            result = futureTime
                            break
                        }
                    }
                    result
                }
            }
        }

    /**
     * 该时间段数据下次应提醒的时间。事实上等于[nextHappenTime].first - [remindData].aheadTime
     */
    val nextRemindTime: LocalDateTime?
        get() = when (remindData.type) {
            CalendarRemindType.NONE                              -> null
            CalendarRemindType.SINGAL, CalendarRemindType.REPEAT -> nextHappenTime?.first?.let {
                it - remindData.aheadTime
            }
        }


    class TC {
        @TypeConverter
        fun toDBDataType(value: MutableList<Int>): String {
            return JSON.toJSONString(value)
        }

        @TypeConverter
        fun fromDBDataType(value: String): MutableList<Int> {
            return JSON.parseArray(value, Int::class.java)
        }
    }

    override fun assertValid() {
        when (type) {
            CalendarTimeType.SINGLE_COURSE -> {
                assertData(timeInCourseSchedule != null, "时间定义不能为空！")
                timeInCourseSchedule!!.assertValid()
                assertData(timeInCourseSchedule!!.date != null, "请选择日期！")
                timeInCourseSchedule!!.dayOfWeek = timeInCourseSchedule!!.date!!.dayOfWeek
            }
            CalendarTimeType.REPEAT_COURSE -> {
                assertData(timeInCourseSchedule != null, "时间定义不能为空！")
                timeInCourseSchedule!!.assertValid()
                assertData(repeatWeeks.isNotEmpty(), "请选择重复周！")
                assertData(repeatWeeks.all { CREP.term.isWeekInTerm(it) }, "重复周选择不合法！")
            }
            CalendarTimeType.SINGLE_HOUR   -> {
                assertData(timeInHour != null, "时间定义不能为空！")
                timeInHour!!.assertValid()
                assertData(timeInHour!!.date != null, "请选择日期！")
            }
            CalendarTimeType.REPEAT_HOUR   -> {
                assertData(timeInHour != null, "时间定义不能为空！")
                timeInHour!!.assertValid()
                assertData(repeatWeeks.isNotEmpty(), "请选择重复周！")
                assertData(repeatWeeks.all { CREP.term.isWeekInTerm(it) }, "重复周选择不合法！")
            }
            CalendarTimeType.POINT         -> {
                assertData(timeInHour != null, "时间定义不能为空！")
                timeInHour!!.assertValid()
                assertData(timeInHour!!.date != null, "请选择日期！")
                assertDataSystem(
                    timeInHour!!.startTime == timeInHour!!.endTime,
                    "时间节点类型的时间段数据必须被设置为startTime==endTime！"
                )
            }
        }
        remindData.assertValid()
    }

    fun assertValidWithItem(item: CalendarItemData) {
        assertValid()
        item.assertValid()
        assertDataSystem(
            item_id == item.id,
            "TimeData的时间段的item_id与关联的ItemData不一致！"
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CalendarTimeData) return false

        if (id != other.id) return false
        if (name != other.name) return false
        if (type != other.type) return false
        if (timeInCourseSchedule != other.timeInCourseSchedule) return false
        if (timeInHour != other.timeInHour) return false
        if (repeatWeeks != other.repeatWeeks) return false
        if (place != other.place) return false
        if (comment != other.comment) return false
        if (remindData != other.remindData) return false
        if (item_id != other.item_id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + (timeInCourseSchedule?.hashCode() ?: 0)
        result = 31 * result + (timeInHour?.hashCode() ?: 0)
        result = 31 * result + repeatWeeks.hashCode()
        result = 31 * result + place.hashCode()
        result = 31 * result + comment.hashCode()
        result = 31 * result + remindData.hashCode()
        result = 31 * result + item_id
        return result
    }

}


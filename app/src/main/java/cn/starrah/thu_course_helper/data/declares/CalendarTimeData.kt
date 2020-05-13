package cn.starrah.thu_course_helper.data.declares

import androidx.lifecycle.LiveData
import androidx.room.*
import cn.starrah.thu_course_helper.data.database.CalendarRepository
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.TypeReference
import java.time.DayOfWeek
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

    /** 提醒设置 */
    @Embedded(prefix = "RMD") var remindData: CalendarRemindData = CalendarRemindData(),

    /** 该时间段所对应关联的日程项的数据表外键。默认等于calendarItem的id（如果传了calendarItem就不必传这个了）*/
    var item_id: Int = 0
) {
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
            CalendarTimeType.SINGLE_COURSE -> listOf(timeInCourseSchedule!!.date!!.toTermDayId())
            CalendarTimeType.REPEAT_COURSE -> {
                val zeroWeekId = timeInCourseSchedule!!.dayOfWeek.value - 7
                repeatWeeks.map { zeroWeekId + (it * 7) }
            }
            CalendarTimeType.SINGLE_HOUR, CalendarTimeType.POINT -> listOf(timeInHour!!.date!!.toTermDayId())
            CalendarTimeType.REPEAT_HOUR -> {
                val zeroWeekId = timeInHour!!.dayOfWeek!!.value - 7
                repeatWeeks.map { zeroWeekId + (it * 7) }
            }
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
}

class CalendarTimeDataWithItem : CalendarTimeData() {
    @Relation(parentColumn = "item_id", entityColumn = "id")
    var _im: List<CalendarItemData> = listOf()

    /** 该时间段所对应关联的日程项数据对象的引用。*/
    var calendarItem: CalendarItemData
        get() = _im[0]
        set(value) {
            _im = listOf(value)
        }

    /**
     * 可以在主线程调用。
     *
     * 获得该时间段所对应的日程项的、[CalendarItemDataWithTimes]格式数据的[LiveData]；
     * 同时[calendarItem]属性的值也会被设置为本函数返回的[LiveData]的*value*。
     */
    override suspend fun queryItem(): LiveData<CalendarItemDataWithTimes> {
        val superRes = super.queryItem()
        calendarItem = superRes.value ?: calendarItem
        return superRes
    }
}
package cn.starrah.thu_course_helper.data.declares

import androidx.room.*
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.TypeReference
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
data class CalendarTimeData(
    /** 时间段的数据库id，各个时间段唯一。当试图插入新时间段到数据库中时，请保证id为默认值0。 */
    @PrimaryKey(autoGenerate = true)var id: Int = 0,

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

    /** 该时间段所对应关联的日程项数据对象的引用。 */
    @Ignore var calendarItem: CalendarItemData? = null,

    /** 该时间段所对应关联的日程项的数据表外键。默认等于calendarItem的id（如果传了calendarItem就不必传这个了）*/
    var item_id: Int = calendarItem?.id?:0
) {


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
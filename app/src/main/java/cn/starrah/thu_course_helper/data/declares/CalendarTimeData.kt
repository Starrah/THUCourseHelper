package cn.starrah.thu_course_helper.data.declares

import java.time.DayOfWeek
import java.time.LocalDateTime

/**
 * 时间段的类型。
 * @param [chineseName] 中文名，可直接用于前端显示
 */
enum class CalendarTimeType(val chineseName: String) {
    SINGLE_COURSE("单次（按大节）"),
    REPEAT_COURSE("重复（按大节）"),
    SINGLE_HOUR("单次（按时间）"),
    REPEAT_HOUR("重复（按时间）"),
    POINT("时间节点")
}

data class CalendarTimeData(
    /** 时间段的数据库id，各个时间段唯一 */
    val id: Int = 0,

    /** 时间段的名称 */
    var name: String = "",

    /** 时间段的类型 */
    var type: CalendarTimeType = CalendarTimeType.SINGLE_COURSE,

    /** 以大节格式描述的时间段信息。如果类型不是按大节，则为null。 */
    var timeInCourseSchedule: TimeInCourseSchedule? = null,

    /** 以时间格式描述的时间段信息。如果类型不是按时间，则为null。 */
    var timeInHour: TimeInHour? = null,

    /** 以时间节点描述的时间段信息。如果类型不是时间节点，则为null。 */
    var timeInPoint: LocalDateTime? = null,

    /** 需要让事件重复发生的所有周次的周次序号。注意从1开始。 */
    var repeatWeeks: MutableList<Int> = mutableListOf(),

    /** 地点 */
    var place: String = "",

    /** 提醒设置 */
    var remindData: CalendarRemindData = CalendarRemindData()
)
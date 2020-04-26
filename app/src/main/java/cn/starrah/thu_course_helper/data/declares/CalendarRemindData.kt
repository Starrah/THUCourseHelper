package cn.starrah.thu_course_helper.data.declares

import java.time.Duration

enum class CalendarRemindType (val chineseName: String) {
    NONE("无"),
    SINGAL("仅这一次"),
    REPEAT("每次")
}

enum class CalendarRemindMethodType (val chineseName: String) {
    NOTICE("通知栏"),
    ALARM("闹钟")
}

data class CalendarRemindData (
    /** 提醒类型 */
    var type: CalendarRemindType = CalendarRemindType.NONE,

    /** 提前时间 */
    var aheadTime: Duration = Duration.ZERO,

    /** 提醒方式 */
    var method: CalendarRemindMethodType = CalendarRemindMethodType.NOTICE,

    /** 闹钟铃声 */
    var alarmSound: String = ""
)
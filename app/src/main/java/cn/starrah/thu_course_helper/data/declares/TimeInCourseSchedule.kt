package cn.starrah.thu_course_helper.data.declares

import java.time.DayOfWeek

data class TimeInCourseSchedule(
    /** 周几 */
    var dayOfWeek: DayOfWeek,

    /** 第几大节开始。注意第一大节为1。
     *
     * 例如一门课从第二大节一开始（第一小节）就开始上课，持续三小节，则*startBig*=2 */
    var startBig: Int,

    /** 实际开始时间相对于上述大节的开始时间的偏移量。
     *
     * 例如一门课从第二大节一开始（第一小节）就开始上课，持续三小节，则*startOffsetSmall*=0.0f */
    var startOffsetSmall: Float,

    /** 持续的时间长度，以小节为单位
     *
     * 例如一门课从第二大节一开始（第一小节）就开始上课，持续三小节，则*lengthSmall*=3.0f */
    var lengthSmall: Float

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
}
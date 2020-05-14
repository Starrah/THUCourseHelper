package cn.starrah.thu_course_helper.data.declares.school

import com.alibaba.fastjson.annotation.JSONField
import java.time.Duration
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/**
 * 描述每天的课程大小节时间安排表。
 */
data class SchoolTimeRule(
    /**
     * 一天的所有大节
     *
     * 注意：**序号是从零开始，即`bigs[0]`代表的是第一大节。**
     *
     * 另有函数[getBigByNumber]也可获得大节、但是是通过从1开始的自然顺序。
     * 即`getBigByNumber(1)`和`bigs[0]`是等价的、返回的都是第一大节。
     * */
    val bigs: List<BigClass>
) {

    /**
     * 获得一个大节
     *
     * @param [numberBeginFrom1] 大节序号 从1开始
     */
    fun getBigByNumber(numberBeginFrom1: Int): BigClass = bigs[numberBeginFrom1 - 1]

    /**
     * 获得某个大节的第一个小节，在一天中的下标（这里指它相对当天第一个小节的偏移量，**从0开始**）
     *
     * @param [numberBeginFrom1] 大节序号 **从1开始**
     * @return 小节在一天中的下标 **从0开始**
     */
    fun getStartSmallIndex(numberBeginFrom1: Int): Int = bigs.subList(0, numberBeginFrom1 - 1)
        .fold(0) { value, obj -> value + obj.smallsCount }

    val bigsCount: Int
        @JSONField(serialize = false)
        get() = bigs.size

    data class BigClass(
        /** 一个大节中的所有小节 */
        val smalls: List<SmallClass>
    ) {
        val smallsCount: Int
            @JSONField(serialize = false)
            get() = smalls.size

        val startTime: LocalTime
            @JSONField(serialize = false)
            get() = smalls.first().startTime

        val endTime: LocalTime
            @JSONField(serialize = false)
            get() = smalls.last().endTime

        val totalLengthInMinutes: Int
            @JSONField(serialize = false)
            get() = ChronoUnit.MINUTES.between(startTime, endTime).toInt()

        val totalLength: Duration
            @JSONField(serialize = false)
            get() = Duration.ofMinutes(totalLengthInMinutes.toLong())

        val excludeRestLengthInMinutes: Int
            @JSONField(serialize = false)
            get() = smalls.fold(0) {v, obj -> v + obj.lengthInMinutes}

        val excludeRestClassLength: Duration
            @JSONField(serialize = false)
            get() = Duration.ofMinutes(excludeRestLengthInMinutes.toLong())

        val oneRestLengthInMinutes: Int
            @JSONField(serialize = false)
            get() = ChronoUnit.MINUTES.between(smalls[0].endTime, smalls[1].startTime).toInt()

        val oneRestLength: Duration
            @JSONField(serialize = false)
            get() = Duration.ofMinutes(oneRestLengthInMinutes.toLong())

        val oneSmallLengthInMinutes: Int
            @JSONField(serialize = false)
            get() = smalls[0].lengthInMinutes

        val oneSmallLength: Duration
            @JSONField(serialize = false)
            get() = smalls[0].length
    }
    data class SmallClass(
        /** 开始时间 */
        @JSONField(format = "HH:mm")
        val startTime: LocalTime,

        /** 结束时间 */
        @JSONField(format = "HH:mm")
        val endTime: LocalTime
    ) {
        @JSONField(serialize = false)
        val lengthInMinutes = ChronoUnit.MINUTES.between(startTime, endTime).toInt()

        val length: Duration
            @JSONField(serialize = false)
            get() = Duration.ofMinutes(lengthInMinutes.toLong())
    }
}
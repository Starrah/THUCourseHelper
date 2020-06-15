package cn.starrah.thu_course_helper.data.declares.school

import cn.starrah.thu_course_helper.data.utils.Verifiable
import cn.starrah.thu_course_helper.data.utils.assertDataSystem
import cn.starrah.thu_course_helper.data.utils.toTermDayId
import com.alibaba.fastjson.annotation.JSONField
import java.time.LocalDate
import java.time.LocalTime
import java.time.Period
import java.time.temporal.ChronoUnit

data class SchoolTerm(
    /**
     * 学校名称
     */
    val schoolName: String = "",

    /** 学年度的开始年份。
     *
     * 例如2019-2020学年度秋季学期，则此字段为2019。 */
    val beginYear: Int = 0,

    /** 学期的类型。
     *
     * 例如2019-2020学年度秋季学期，则此字段为[SchoolTermType.AUTUMN]。 */
    val type: SchoolTermType = SchoolTermType.AUTUMN,

    /**
     * 学期开始日期。
     */
    val startDate: LocalDate = LocalDate.now(),

    /**
     * 学期正常周数（不含考试周）
     */
    val normalWeekCount: Int = 0,

    /**
     * 学期考试周数
     */
    val examWeekCount: Int = 0,

    /**
     * 该学期调课的安排。每个受影响的日子是List中的一个元素。
     */
    val holidays: List<HolidayDayLevelRearrange> = mutableListOf(),

    /**
     * 每天的课程大小节时间安排表
     */
    val timeRule: SchoolTimeRule = SchoolTimeRule(listOf())
) : Verifiable {
    /**
     * 形如"2019-2020学年度秋季学期"格式的中文名称
     */
    val chineseName: String
        @JSONField(serialize = false)
        get() = "${beginYear}-${beginYear + 1}学年度${type.chineseName}"

    /**
     * 形如"19-20秋"格式的中文名称
     */
    val chineseShortName: String
        @JSONField(serialize = false)
        get() = "${beginYear % 100}-${(beginYear % 100) + 1}${type.oneCharChineseName}"

    val termId: String
        @JSONField(serialize = false)
        get() = "${schoolName},${beginYear},${type}"

    /**
     * 数据库中使用的名称，形如"CAL19AUT"
     */
    val dbName: String
        @JSONField(serialize = false)
        get() = "CAL${beginYear % 100}${type.name.substring(0 until 3)}"

    /**
     * 学期总周数（正常周+考试周）
     */
    val totalWeekCount: Int
        @JSONField(serialize = false)
        get() = normalWeekCount + examWeekCount

    /**
     * 结束日期的[LocalDate]类型对象。指学期的最后一天（即最后一周的周日）
     */
    val endInclusiveDate: LocalDate
        @JSONField(serialize = false)
        get() = startDate + Period.ofDays((totalWeekCount * 7 - 1))

    /**
     * 考试周开始日期的[LocalDate]类型对象。
     */
    val examWeekStartDate: LocalDate
        @JSONField(serialize = false)
        get() = startDate + Period.ofDays((normalWeekCount * 7))

    /**
     * 判断所给日期是否在学期内。
     * @param [date] 日期对象
     */
    fun isDateInTerm(date: LocalDate): Boolean {
        val weekNumber = (ChronoUnit.DAYS.between(startDate, date).toInt() / 7) + 1
        return weekNumber in 1..totalWeekCount
    }

    /**
     * 判断所给周是否在学期内。
     * @param [weekNumber] 周数，从1开始
     */
    fun isWeekInTerm(weekNumber: Int): Boolean {
        return weekNumber in 1..totalWeekCount
    }

    /**
     * 把日期转换为学期的周数。
     * @return 周数 从1开始
     */
    fun dateToWeekNumber(date: LocalDate): Int {
        val weekNumber = (ChronoUnit.DAYS.between(startDate, date).toInt() / 7) + 1
        return if (weekNumber in 1..totalWeekCount) weekNumber else throw RuntimeException("日期不在本学期内！")
    }

    /**
     * 获取某一个周数对应的所有日期构成的列表。
     * @param [weekNumber] 周数 从1开始
     * @param [onlyMONToFRI] 如果为true，则只返回周一到周五五天。
     */
    fun datesInAWeek(weekNumber: Int, onlyMONToFRI: Boolean = false): List<LocalDate> {
        if (weekNumber !in 1..totalWeekCount) throw RuntimeException("周数不合法！")
        val range = if (onlyMONToFRI) 0 until 5 else 0 until 7
        return range.map { startDate.plusDays((((weekNumber - 1) * 7) + it).toLong()) }
    }

    /**
     * 判断某一周是否是考试周。
     * @param [weekNumber] 周数 从1开始
     */
    fun isInExamWeek(weekNumber: Int): Boolean {
        return when (weekNumber) {
            in 1..normalWeekCount -> false
            in (normalWeekCount + 1)..totalWeekCount -> true
            else -> throw RuntimeException("周数不合法！")
        }
    }

    /**
     * 判断某一天是否在考试周内。
     */
    fun isInExamWeek(date: LocalDate): Boolean {
        return isInExamWeek(dateToWeekNumber(date))
    }

    /** 帮助节假日计算使用 */
    private val _holidayCalHelpMap: MutableMap<Int, Int> = mutableMapOf()

    init {
        holidays.forEach { _holidayCalHelpMap[it.date.toTermDayId(this)] = it.to?.toTermDayId(this)?: 0 }
        for (i in 0 until (totalWeekCount * 7)) {
            val v = _holidayCalHelpMap[i]
            if (v == null) _holidayCalHelpMap[i] = i
            else if (v == 0) _holidayCalHelpMap.remove(i)
        }
    }

    fun applyHolidayRearrange(dayIds: List<Int>): List<Int> =
        dayIds.mapNotNull { _holidayCalHelpMap[it] }

    override fun assertValid() {
        assertDataSystem(_holidayCalHelpMap.values.toSet().size == _holidayCalHelpMap.size,
            "节假日设置错误！")

        assertDataSystem(startDate.year in beginYear..beginYear+1 &&
                endInclusiveDate.year in beginYear..beginYear+1,
            "学期的的日期与学年度的设置不匹配！")

        assertDataSystem(normalWeekCount + examWeekCount > 0,
            "学期的周数设置不合法！")

        var lastEnd = LocalTime.of(0, 0)
        assertDataSystem(timeRule.bigs.isNotEmpty(), "上课时间表设置不合法！")
        for (big in timeRule.bigs){
            assertDataSystem(big.smalls.isNotEmpty(), "上课时间表设置不合法！")
            for (small in big.smalls) {
                assertDataSystem(small.startTime >= lastEnd && small.endTime > small.startTime,
                    "上课时间表设置不合法！")
                lastEnd = small.endTime
            }
        }
    }
}
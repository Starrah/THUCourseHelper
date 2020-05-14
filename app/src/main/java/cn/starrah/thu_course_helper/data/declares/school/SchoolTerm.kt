package cn.starrah.thu_course_helper.data.declares.school

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

data class SchoolTerm(
    /** 学年度的开始年份。
     *
     * 例如2019-2020学年度秋季学期，则此字段为2019。 */
    val beginYear: Int = 0,

    /** 学期的类型。
     *
     * 例如2019-2020学年度秋季学期，则此字段为[SchoolTermType.AUTUMN]。 */
    val type: SchoolTermType = SchoolTermType.AUTUMN,

    /**
     * 学期开始日期、格式为yyyy-MM-dd。
     */
    val startDateStr: String = "1970-01-01",

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
    val holidayDayLevelRearrange: List<HolidayDayLevelRearrange> = mutableListOf(),

    /**
     * 每天的课程大小节时间安排表
     */
    val timeRule: SchoolTimeRule = SchoolTimeRule(listOf())
) {
    /**
     * 形如"2019-2020学年度秋季学期"格式的中文名称
     */
    val chineseName: String
        get() = "${beginYear}-${beginYear + 1}学年度${type.chineseName}"

    /**
     * 形如"19-20秋"格式的中文名称
     */
    val chineseShortName: String
        get() = "${beginYear % 100}-${(beginYear % 100) + 1}${type.oneCharChineseName}"

    /**
     * 数据库中使用的名称，形如"CAL19AUT"
     */
    val dbName: String
        get() = "CAL${beginYear % 100}${type.name.substring(0 until 3)}"

    /**
     * 学期总周数（正常周+考试周）
     */
    val totalWeekCount: Int
        get() = normalWeekCount + examWeekCount

    /**
     * 开始日期的[LocalDate]类型对象。
     */
    val startDate: LocalDate
        get() = LocalDate.parse(startDateStr, DateTimeFormatter.ISO_DATE)

    /**
     * 结束日期的[LocalDate]类型对象。指学期的最后一天（即最后一周的周日）
     */
    val endInclusiveDate: LocalDate
        get() = startDate + Period.ofDays((totalWeekCount * 7 - 1))

    /**
     * 考试周开始日期的[LocalDate]类型对象。
     */
    val examWeekStartDate: LocalDate
        get() = startDate + Period.ofDays((normalWeekCount * 7))
}
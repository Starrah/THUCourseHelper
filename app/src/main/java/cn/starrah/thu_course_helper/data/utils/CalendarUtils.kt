package cn.starrah.thu_course_helper.data.utils

import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.declares.school.SchoolTerm
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit

/**
 * 转换[LocalDate]为日期ID格式（即本学期的开始当天为1、之后顺次排）
 */
fun LocalDate.toTermDayId(term: SchoolTerm = CREP.term): Int{
    return ChronoUnit.DAYS.between(term.startDate, this).toInt() + 1
}

fun DayIdToLocalDate(value: Int, term: SchoolTerm = CREP.term): LocalDate {
    return term.startDate + Period.ofDays(value - 1)
}

private val _dayOfWeekChineseNameTable = mapOf<DayOfWeek, String>(
    DayOfWeek.MONDAY to "周一",
    DayOfWeek.TUESDAY to "周二",
    DayOfWeek.WEDNESDAY to "周三",
    DayOfWeek.THURSDAY to "周四",
    DayOfWeek.FRIDAY to "周五",
    DayOfWeek.SATURDAY to "周六",
    DayOfWeek.SUNDAY to "周日"
)

val DayOfWeek.chineseName: String
    get() = _dayOfWeekChineseNameTable[this]!!

val OneBitNumToChineseTable: List<String> = listOf("零", "一", "二", "三", "四", "五", "六", "七", "八", "九")

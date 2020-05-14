package cn.starrah.thu_course_helper.data.utils

import cn.starrah.thu_course_helper.data.declares.calendarEntity.FastSearchTable
import java.time.DayOfWeek
import java.time.LocalDate

private val LocalDateTCInstance =
    FastSearchTable.TC()

fun LocalDate.toTermDayId(): Int = LocalDateTCInstance.toDBDataType(this)

fun DayIdToLocalDate(value: Int): LocalDate = LocalDateTCInstance.fromDBDataType(value)

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

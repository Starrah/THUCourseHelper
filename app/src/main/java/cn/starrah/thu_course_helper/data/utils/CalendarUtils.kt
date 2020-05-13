package cn.starrah.thu_course_helper.data.utils

import cn.starrah.thu_course_helper.data.declares.calendarEntity.FastSearchTable
import java.time.LocalDate

private val LocalDateTCInstance =
    FastSearchTable.TC()

fun LocalDate.toTermDayId(): Int = LocalDateTCInstance.toDBDataType(this)

fun DayIdToLocalDate(value: Int): LocalDate = LocalDateTCInstance.fromDBDataType(value)
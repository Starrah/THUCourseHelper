package cn.starrah.thu_course_helper.data.declares

import java.time.LocalDate

private val LocalDateTCInstance = CalendarFastSearchHelpTable.TC()

fun LocalDate.toTermDayId(): Int = LocalDateTCInstance.toDBDataType(this)

fun DayIdToLocalDate(value: Int): LocalDate = LocalDateTCInstance.fromDBDataType(value)
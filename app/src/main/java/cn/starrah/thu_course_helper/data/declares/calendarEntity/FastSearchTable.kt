package cn.starrah.thu_course_helper.data.declares.calendarEntity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.TypeConverter
import cn.starrah.thu_course_helper.data.utils.DayIdToLocalDate
import cn.starrah.thu_course_helper.data.utils.toTermDayId
import java.time.LocalDate

/**
 * 用于快速查找一个日期对应的所有时间段和所有日程的副主索引数据库表的实体定义
 */
@Entity(
    primaryKeys = ["dayId", "timeId"],
    indices = [Index("timeId"), Index("dayId")],
    foreignKeys = [ForeignKey(
        entity = CalendarTimeData::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("timeId"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class FastSearchTable(
    val dayId: Int = 0,
    val timeId: Int = 0
) {
    class TC {
        /**
         * 转换[LocalDate]为日期ID格式（即本学期的开始当天为1、之后顺次排）
         */
        @TypeConverter
        fun toDBDataType(value: LocalDate): Int {
            return value.toTermDayId()
        }

        @TypeConverter
        fun fromDBDataType(value: Int): LocalDate {
            return DayIdToLocalDate(value)
        }
    }
}


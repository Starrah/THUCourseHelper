package cn.starrah.thu_course_helper.data.declares

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.alibaba.fastjson.JSON
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * 时间段索引数据表中使用的、描述一个日子的数据结构。
 */
@Entity
data class DayEntityInFastSearchHelpTable(
    /** 数据库id */
    @PrimaryKey(autoGenerate = true) var id: Int = 0,

    /** 对应的日期 */
    var date: LocalDate = LocalDate.now()
) {
    /**
     * 获取这个日期对应于学期的第几周
     */
    fun getWeekInTerm(term: SchoolTerm): Int {
        return term.startDate.until(date, ChronoUnit.DAYS).toInt() / 7 + 1
    }

    class TC {
        @TypeConverter
        fun toDBDataType(value: LocalDate): String {
            return value.format(DateTimeFormatter.ISO_DATE)
        }

        @TypeConverter
        fun fromDBDataType(value: String): LocalDate {
            return LocalDate.parse(value, DateTimeFormatter.ISO_DATE)
        }
    }
}

/**
 * 用于快速查找一个日期对应的所有时间段和所有日程的副主索引数据库表的实体定义
 */
@Entity(
    primaryKeys = ["dayId", "timeId"],
    foreignKeys = [ForeignKey(
        entity = DayEntityInFastSearchHelpTable::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("dayId"),
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = CalendarTimeData::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("timeId"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class CalendarFastSearchHelpTable(
    val dayId: Int = 0,
    val timeId: Int = 0
)

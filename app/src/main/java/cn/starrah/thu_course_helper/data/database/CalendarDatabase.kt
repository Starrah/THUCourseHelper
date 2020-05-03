package cn.starrah.thu_course_helper.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cn.starrah.thu_course_helper.data.declares.*

@TypeConverters(
    CalendarItemType.TC::class,
    CalendarItemLegalDetailKey.TC::class,
    CalendarTimeType.TC::class,
    CalendarRemindType.TC::class,
    CalendarRemindMethodType.TC::class,
    CalendarItemData.TC::class,
    CalendarTimeData.TC::class,
    CalendarRemindData.TC::class,
    TimeInCourseSchedule.TC::class,
    TimeInHour.TC::class
)
@Database(
    entities = arrayOf(
        CalendarItemData::class,
        CalendarTimeData::class
    ),
    version = 1
)
abstract class CalendarDatabase : RoomDatabase() {
    abstract fun Dao(): CalendarDao

    companion object {
        fun getDatabaseInstance(context: Context, term: SchoolTerm): CalendarDatabase {
            return Room.databaseBuilder(
                context,
                CalendarDatabase::class.java,
                term.dbName
            ).build()
        }
    }
}
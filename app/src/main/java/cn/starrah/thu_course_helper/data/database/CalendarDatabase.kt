package cn.starrah.thu_course_helper.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarFastSearchHelpTable
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarItemData
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarRemindData
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeData
import cn.starrah.thu_course_helper.data.declares.calendarEnum.*
import cn.starrah.thu_course_helper.data.declares.time.TimeInCourseSchedule
import cn.starrah.thu_course_helper.data.declares.time.TimeInHour

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
    TimeInHour.TC::class,
    CalendarFastSearchHelpTable.TC::class
)
@Database(
    entities = [CalendarItemData::class, CalendarTimeData::class, CalendarFastSearchHelpTable::class],
    version = 1
)
abstract class CalendarDatabase : RoomDatabase() {
    abstract fun Dao(): CalendarDao

    companion object {
        fun getDatabaseInstance(context: Context, name: String): CalendarDatabase {
            return Room.databaseBuilder(
                context,
                CalendarDatabase::class.java,
                name
            ).build()
        }
    }
}
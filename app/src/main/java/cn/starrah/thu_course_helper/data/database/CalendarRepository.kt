package cn.starrah.thu_course_helper.data.database

import android.content.Context
import androidx.lifecycle.LiveData
import cn.starrah.thu_course_helper.data.declares.CalendarTimeDataWithItem
import cn.starrah.thu_course_helper.data.declares.SchoolTerm
import cn.starrah.thu_course_helper.data.declares.toTermDayId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object CalendarRepository {
    lateinit var database: CalendarDatabase
        get
        private set

    lateinit var DAO: CalendarDao
        get
        private set

    lateinit var term: SchoolTerm
        get
        private set

    /**
     * 可以在主线程中运行。
     *
     * 设置（或变更）当前的所选学期。
     * 在调用Repository进行任何操作之前，必须保证[initializeTerm]被调用过。
     */
    suspend fun initializeTerm(context: Context, term: SchoolTerm) {
        withContext(Dispatchers.IO) {
            this@CalendarRepository.term = term
            database = CalendarDatabase.getDatabaseInstance(context, term.dbName)
            DAO = database.Dao()
//            val daysCount = DAO.checkTermInitialized()
//            if (daysCount != term.totalWeekCount * 7){
//                DAO.deletAllDaysInTerm()
//                val list = mutableListOf<DayEntityInFastSearchHelpTable>()
//                for (i in 0 until term.totalWeekCount * 7) {
//                    list.add(DayEntityInFastSearchHelpTable(date = term.startDate.plusDays(i.toLong())))
//                }
//                DAO.insertDaysInTerm(*list.toTypedArray())
//            }
        }
    }




    fun findTimesByDays(days: List<LocalDate>): LiveData<List<CalendarTimeDataWithItem>> {
        return DAO.findTimesByDays(days.map { it.toTermDayId() })
    }

//    fun updateTimeByDays
}
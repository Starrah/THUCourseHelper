package cn.starrah.thu_course_helper.data.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import cn.starrah.thu_course_helper.data.declares.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * 本类是获取各类数据的接口。通过设置学期[initializeTerm]，自动打开对应的数据库；
 * 内部通过数据库的DAO对象访问数据，并进行适当处理后返回。
 *
 * 本类中的所有函数均为挂起函数、并均可在主线程中直接调用。
 *
 * 建议总是使用本类，不要使用[CalendarDao]，因为本类封装为挂起函数，避免手动切换到副线程的麻烦。
 */
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
     * 可以在主线程中调用。
     *
     * 设置（或变更）当前的所选学期。
     * 在调用Repository进行任何操作之前，必须保证[initializeTerm]被调用过。
     */
    suspend fun initializeTerm(context: Context, term: SchoolTerm) {
        withContext(Dispatchers.IO) {
            this@CalendarRepository.term = term
            database = CalendarDatabase.getDatabaseInstance(context, term.dbName)
            DAO = database.Dao()
        }
    }

    /**
     * 可以在主线程中调用。
     *
     * 根据日期查找该日的所有日程
     * @param [days] 所有要查找的日期的[LocalDate]对象构成的列表
     * @return [days]中所有日期对应的所有日程构成的列表；
     * 返回[CalendarTimeDataWithItem]的列表，可以同时获得时间段数据和对应的日程数据。
     */
    suspend fun findTimesByDays(days: List<LocalDate>): LiveData<List<CalendarTimeDataWithItem>> {
        return withContext(Dispatchers.IO) {
            DAO.findTimesByDays(days.map { it.toTermDayId() })
        }
    }

    /**
     * 可以在主线程中调用。
     *
     * 插入或更新时间段的数据，并维护快速查找表[CalendarFastSearchHelpTable]。
     *
     * 具体行为是：对每个时间段，首先判断时间段数据改变是否包括日期的改变、需要调整[CalendarFastSearchHelpTable]快速查找表中的内容：
     * 如果不需要，那么直接使用UPDATE语句更新[CalendarTimeData]中的数据；
     * 如果需要，则使用INSERT OR REPLACE语句。对于此前不存在的时间段，该语句会插入数据；
     * 对于已存在的时间段，该语句等价于首先DELETE掉原来的记录（同时[CalendarFastSearchHelpTable]快速查找表中的内容由于CASCADE而自然删除），
     * 然后INSERT进新的记录。最后，计算每个被INSERT的项的日期数据，插入快速查找表中。
     *
     * 注意：对于调整某个[CalendarItemData]的时间段的情况，不应使用本函数，而是应使用[updateItemAndTimes]函数，
     * 因为本函数不会删除[CalendarItemData]中原来有、但是[times]中没有传入的时间段。
     * @param [times] 要更新或插入的所有时间段的列表
     */
    suspend fun updateTimes(times: List<CalendarTimeData>) {
        withContext(Dispatchers.IO) {
            DAO.updateTimes(times)
        }
    }

    /**
     * 可以在主线程中调用。
     *
     * 插入或更新日程项的数据。只处理[CalendarItemData]的基本数据，不会影响其中时间段的数据。
     *
     * 首先判断日程项是否存在（通过id是否为0），若存在则执行UPDATE，若不存在则执行INSERT。
     *
     * 注意：对于日程项数据和时间段数据同时改变的情况，不应使用本函数，而是应使用[updateItemAndTimes]函数，
     * 因为本函数不会改变时间段数据。
     * @param [item] 要更新或插入的日程项。
     * @return 插入或更新的[CalendarItemData]的id
     */
    suspend fun updateItem(item: CalendarItemData): Int {
        return withContext(Dispatchers.IO) {
            DAO.updateItem(item)
        }
    }

    /**
     * 可以在主线程中调用。
     *
     * 对于指定的[CalendarItemData]，更新其的所有子时间段。
     *
     * 具体的，对于传入的[times]的每一项，如果它在原来的[CalendarItemData]中不存在，则插入，否则则更新；
     * 对于原来的[CalendarItemData]中的每一个时间段，如果不存在于[times]中，则删除该记录。
     * 无论何种情况，都会自动维护[CalendarFastSearchHelpTable]快速查找表中的记录。
     *
     * 注意：对于日程项数据和时间段数据同时改变的情况，不应使用本函数，而是应使用[updateItemAndTimes]函数，
     * 因为本函数不会改变时间段数据。
     * @param [times] 修改后，[item]的所有时间段构成的列表。
     * @param [item] 日程项
     */
    suspend fun updateTimesInItem(times: List<CalendarTimeData>, item: CalendarItemData) {
        withContext(Dispatchers.IO) {
            DAO.updateTimesInItem(times, item.id)
        }
    }

    /**
     * 可以在主线程中调用。
     *
     * 更新（或插入）指定的日程项和其名下的所有时间段。
     *
     * 具体的，首先尝试更新或插入[item]；
     * 然后，对于这个[item]，根据传入的[times]，尝试更新、插入、删除[item]下面的所有时间段信息，
     * 同时自动维护[CalendarFastSearchHelpTable]快速查找表中的记录。
     *
     * @param [item] 日程项
     * @param [times] 该日程下属的所有时间段
     */
    suspend fun updateItemAndTimes(item: CalendarItemData, times: List<CalendarTimeData>) {
        withContext(Dispatchers.IO) {
            DAO.updateItemAndTimes(item, times)
        }
    }

    /**
     * 可以在主线程中调用。
     *
     * 更新（或插入）指定的日程项和其名下的所有时间段。
     *
     * 等价于*updateItemAndTimes(itemDataWithTimes, itemDataWithTimes.times)*
     *
     * @see updateItemAndTimes(CalendarItemData, List<CalendarTimeData>)
     *
     * @param [itemDataWithTimes] 带时间段的日程项
     */
    suspend fun updateItemAndTimes(itemDataWithTimes: CalendarItemDataWithTimes) {
        updateItemAndTimes(itemDataWithTimes, itemDataWithTimes.times)
    }

    /**
     * 可以在主线程中调用。
     *
     * 根据id查询时间段。
     * @param [timeIds] id的列表
     * @return 对应的含Item时间段（[CalendarTimeDataWithItem]）的列表
     */
    suspend fun findTimesByIds(timeIds: List<Int>): LiveData<List<CalendarTimeDataWithItem>> {
        return withContext(Dispatchers.IO){
            DAO.findTimesByIds(timeIds)
        }
    }

    /**
     * 可以在主线程中调用。
     *
     * 根据id查询日程。
     * @param [itemIds] id的列表
     * @return 对应的含Times日程项（[CalendarItemDataWithTimes]）的列表
     */
    suspend fun findItemsByIds(itemIds: List<Int>): LiveData<List<CalendarItemDataWithTimes>> {
        return withContext(Dispatchers.IO){
            DAO.findItemsByIds(itemIds)
        }
    }

    /**
     * 可以在主线程中调用。
     *
     * 根据日程项查询时间段。
     *
     * Notes: 建议直接通过调用[CalendarItemData.queryTimes]得到某个日程下的所有时间段；这与调用本函数是等价的。
     * @param [item] 日程项
     * @return 该日程下所有含时间段（[CalendarTimeData]）的列表
     */
    suspend fun findTimesByItem(item: CalendarItemData): LiveData<List<CalendarTimeData>> {
        return withContext(Dispatchers.IO){
            DAO.findTimesByItem(item.id)
        }
    }

    /**
     * 可以在主线程中调用。
     *
     * 根据时间段查询日程项。
     *
     * Notes: 建议直接通过调用[CalendarTimeData.queryItem]得到时间段对应的日程；这与调用本函数是等价的。
     * @param [time] 时间段
     * @return 该时间段对应的日程项[CalendarItemDataWithTimes]
     */
    suspend fun findItemByTime(time: CalendarTimeData): LiveData<CalendarItemDataWithTimes> {
        return withContext(Dispatchers.IO){
            DAO.findItemByTime(time.id)
        }
    }
}
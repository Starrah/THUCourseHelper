package cn.starrah.thu_course_helper.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import cn.starrah.thu_course_helper.data.declares.calendarEntity.*

@Dao
abstract class CalendarDao {

    /**
     * 根据一组日期，查找这些日期对应的所有日程，返回它们的并集。
     * @param [dayIds] 所有要查找的日期的学期Id构成的列表
     * @return [dayIds]中所有日期对应的所有日程构成的列表；返回的是[CalendarTimeDataWithItem]的列表，
     * 可以同时获得时间段数据和对应的日程数据。
     */
    @Transaction
    @Query(
        """
        SELECT * FROM CalendarTimeData
        INNER JOIN FastSearchTable
        ON CalendarTimeData.id=FastSearchTable.timeId
        WHERE FastSearchTable.dayId=:dayIds
    """
    )
    abstract fun findTimesByDays(dayIds: List<Int>): LiveData<List<CalendarTimeDataWithItem>>

    @Query(
        """
        SELECT * FROM CalendarTimeData
        WHERE CalendarTimeData.id=:timeIds
    """
    )
    protected abstract fun _findTimesByIds(timeIds: List<Int>): List<CalendarTimeData>

    @Query(
        """
        SELECT * FROM CalendarTimeData
        WHERE CalendarTimeData.item_id=:itemId
    """
    )
    protected abstract fun _findTimesByItem(itemId: Int): List<CalendarTimeData>

    @Query(
        """
        SELECT * FROM CalendarItemData
        WHERE CalendarItemData.id=:itemIds
    """
    )
    protected abstract fun _findItemsByIds(itemIds: List<Int>): List<CalendarItemData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun _insertTimes(times: List<CalendarTimeData>): List<Long>

    @Update
    protected abstract fun _updateTimes(times: List<CalendarTimeData>): Int

    @Insert
    protected abstract fun _insertFastSearch(l: List<FastSearchTable>)

    @Delete
    protected abstract fun _deleteTimes(times: List<CalendarTimeData>): Int

    @Insert
    protected abstract fun _insertItems(items: List<CalendarItemData>): List<Long>

    @Update
    protected abstract fun _updateItems(items: List<CalendarItemData>): Int

    /**
     * 插入或更新时间段的数据，并维护快速查找表[FastSearchTable]。
     *
     * 具体行为是：对每个时间段，首先判断时间段数据改变是否包括日期的改变、需要调整[FastSearchTable]快速查找表中的内容：
     * 如果不需要，那么直接使用UPDATE语句更新[CalendarTimeData]中的数据；
     * 如果需要，则使用INSERT OR REPLACE语句。对于此前不存在的时间段，该语句会插入数据；
     * 对于已存在的时间段，该语句等价于首先DELETE掉原来的记录（同时[FastSearchTable]快速查找表中的内容由于CASCADE而自然删除），
     * 然后INSERT进新的记录。最后，计算每个被INSERT的项的日期数据，插入快速查找表中。
     *
     * 注意：对于调整某个[CalendarItemData]的时间段的情况，不应使用本函数，而是应使用[updateItemAndTimes]函数，
     * 因为本函数不会删除[CalendarItemData]中原来有、但是[times]中没有传入的时间段。
     * @param [times] 要更新或插入的所有时间段的列表
     */
    @Transaction
    open fun updateTimes(times: List<CalendarTimeData>) {
        val existList = times.filter { it.id != 0 }.map { it.id }.run {
            if (!isEmpty()) _findTimesByIds(this) else listOf()
        }
        _updateOrInsertTimes(times, existList)
    }

    /**
     * 插入或更新日程项的数据。只处理[CalendarItemData]的基本数据，不会影响其中时间段的数据。
     *
     * 首先判断日程项是否存在（通过id是否为0），若存在则执行UPDATE，若不存在则执行INSERT。
     *
     * 注意：对于日程项数据和时间段数据同时改变的情况，不应使用本函数，而是应使用[updateItemAndTimes]函数，
     * 因为本函数不会改变时间段数据。
     * @param [item] 要更新或插入的日程项。
     * @return 插入或更新的[CalendarItemData]的id
     */
    @Transaction
    open fun updateItem(item: CalendarItemData): Int {
        if (item.id == 0 || _findItemsByIds(listOf(item.id)).size <= 0) {
            return _insertItems(listOf(item)).single().toInt()
        }
        else {
            val r = _updateItems(listOf(item))
            assert(r == 1)
            return item.id
        }
    }

    /**
     * 对于指定的[CalendarItemData]，更新其的所有子时间段。
     *
     * 具体的，对于传入的[times]的每一项，如果它在原来的[CalendarItemData]中不存在，则插入，否则则更新；
     * 对于原来的[CalendarItemData]中的每一个时间段，如果不存在于[times]中，则删除该记录。
     * 无论何种情况，都会自动维护[FastSearchTable]快速查找表中的记录。
     *
     * 注意：对于日程项数据和时间段数据同时改变的情况，不应使用本函数，而是应使用[updateItemAndTimes]函数，
     * 因为本函数不会改变时间段数据。
     * @param [times] 修改后，[itemId]对应日程项的所有时间段构成的列表。
     * @param [itemId] 日程项的Id
     */
    @Transaction
    open fun updateTimesInItem(times: List<CalendarTimeData>, itemId: Int) {
        for (time in times) {
            if (time.item_id == 0) time.item_id = itemId
            assert(time.item_id == itemId)
        }

        val oldTimes = _findTimesByItem(itemId)
        val newTimesMap = times.associateBy { it.id }
        val oldToUpdateOnes = oldTimes.filter { newTimesMap.containsKey(it.id) }
        val oldToDeleteOnes = oldTimes - oldToUpdateOnes

        oldToDeleteOnes.run { if (!isEmpty()) _deleteTimes(oldToDeleteOnes) }
        _updateOrInsertTimes(times, oldToUpdateOnes)
    }

    /**
     * 更新（或插入）指定的日程项和其名下的所有时间段。
     *
     * 具体的，首先尝试更新或插入[item]；
     * 然后，对于这个[item]，根据传入的[times]，尝试更新、插入、删除[item]下面的所有时间段信息，
     * 同时自动维护[FastSearchTable]快速查找表中的记录。
     *
     * @param [item] 日程项
     * @param [times] 该日程下属的所有时间段
     */
    @Transaction
    open fun updateItemAndTimes(item: CalendarItemData, times: List<CalendarTimeData>) {
        val itemId: Int
        val oldTimes: List<CalendarTimeData>
        if (item.id == 0 || _findItemsByIds(listOf(item.id)).isEmpty()) {
            itemId = _insertItems(listOf(item)).single().toInt()
            oldTimes = listOf()
        }
        else {
            val r = _updateItems(listOf(item))
            assert(r == 1)
            itemId = item.id
            oldTimes = _findTimesByItem(itemId)
        }

        for (time in times) {
            if (time.item_id == 0) time.item_id = itemId
            assert(time.item_id == itemId)
        }

        val newTimesMap = times.associateBy { it.id }
        val oldToUpdateOnes = oldTimes.filter { newTimesMap.containsKey(it.id) }
        val oldToDeleteOnes = oldTimes - oldToUpdateOnes

        oldToDeleteOnes.run { if (!isEmpty()) _deleteTimes(oldToDeleteOnes) }
        _updateOrInsertTimes(times, oldToUpdateOnes)
    }

    protected fun _updateOrInsertTimes(
        times: List<CalendarTimeData>,
        oldToUpdateOnes: List<CalendarTimeData>
    ) {
        var toUpdateOnes = times.filter { it.id != 0 }
        assert(oldToUpdateOnes.size == toUpdateOnes.size)
        val oldToUpdateOnesMap = oldToUpdateOnes.associateBy { it.id }
        toUpdateOnes = toUpdateOnes.filter {
            oldToUpdateOnesMap[it.id]?.calculateDayIdsInTerm() == it.calculateDayIdsInTerm()
        }
        val toInsertOnes = times - toUpdateOnes

        val updatedCount = toUpdateOnes.run { if (!isEmpty()) _updateTimes(toUpdateOnes) else 0 }
        assert(updatedCount == toUpdateOnes.size)

        toInsertOnes.run {
            if (!isEmpty()) {
                val res = _insertTimes(this)
                for ((one, id) in this.zip(res)) {
                    one.id = id.toInt()
                }
            }
        }

        val fastDatas = mutableListOf<FastSearchTable>()
        toInsertOnes.forEach { t ->
            fastDatas += t.calculateDayIdsInTerm().map {
                FastSearchTable(
                    it,
                    t.id
                )
            }
        }
        fastDatas.run { if (!isEmpty()) _insertFastSearch(this) }
    }


    /**
     * 根据id查询时间段。
     * @param [timeIds] id的列表
     * @return 对应的含Item时间段（[CalendarTimeDataWithItem]）的列表
     */
    @Transaction
    @Query(
        """
        SELECT * FROM CalendarTimeData
        WHERE CalendarTimeData.id=:timeIds
    """
    )
    abstract fun findTimesByIds(timeIds: List<Int>): LiveData<List<CalendarTimeDataWithItem>>

    /**
     * 根据id查询日程。
     * @param [itemIds] id的列表
     * @return 对应的含Times日程项（[CalendarItemDataWithTimes]）的列表
     */
    @Transaction
    @Query(
        """
        SELECT * FROM CalendarItemData
        WHERE CalendarItemData.id=:itemIds
    """
    )
    abstract fun findItemsByIds(itemIds: List<Int>): LiveData<List<CalendarItemDataWithTimes>>

    /**
     * 根据日程项查询时间段。
     *
     * Notes: 建议直接通过调用[CalendarItemData.queryTimes]得到某个日程下的所有时间段；这与调用本函数是等价的。
     * @param [itemId] 日程项的id
     * @return 该日程下所有含时间段（[CalendarTimeData]）的列表
     */
    @Transaction
    @Query(
        """
        SELECT * FROM CalendarTimeData
        WHERE CalendarTimeData.id=:itemId
    """
    )
    abstract fun findTimesByItem(itemId: Int): LiveData<List<CalendarTimeData>>

    /**
     * 根据时间段查询日程项。
     *
     * Notes: 建议直接通过调用[CalendarTimeData.queryItem]得到时间段对应的日程；这与调用本函数是等价的。
     * @param [timeId] 时间段的id
     * @return 该时间段对应的日程项[CalendarItemDataWithTimes]
     */
    @Transaction
    @Query(
        """
        SELECT * FROM CalendarItemData
        INNER JOIN CalendarTimeData
        ON CalendarItemData.id=CalendarTimeData.item_id
        WHERE CalendarTimeData.id=:timeId
    """
    )
    abstract fun findItemByTime(timeId: Int): LiveData<CalendarItemDataWithTimes>

    @Delete
    abstract fun deleteItems(items: List<CalendarItemData>)

    @Delete
    abstract fun deleteTimes(times: List<CalendarTimeData>)

    @Transaction
    @Query(
        """
        SELECT * FROM CalendarItemData
    """
    )
    abstract fun findAllItems(): List<CalendarItemDataWithTimes>
}
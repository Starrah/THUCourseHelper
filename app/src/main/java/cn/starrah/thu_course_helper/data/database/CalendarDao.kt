package cn.starrah.thu_course_helper.data.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Dao
import androidx.room.Query
import cn.starrah.thu_course_helper.data.declares.CalendarItemData
import cn.starrah.thu_course_helper.data.declares.CalendarTimeData
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate

@Dao
abstract class CalendarDao {
//    class SingleItemTimeListLiveDataWrapper(
//        val originLiveData: LiveData<List<CalendarTimeData>>,
//        val item: CalendarItemData
//    ) : LiveData<List<CalendarTimeData>>(originLiveData.value)
//    {
//        val observer = Observer<List<CalendarTimeData>> { t ->
//            postValue(t?.onEach { it.calendarItem = item })
//        }
//        override fun onActive() {
//            super.onActive()
//            originLiveData.observeForever(observer)
//        }
//        override fun onInactive() {
//            super.onInactive()
//            originLiveData.removeObserver(observer)
//        }
//    }
//
//    /**
//     * 根据日程项找到它下面所有的时间段
//     * @param [item] 日程项数据
//     * @return 所有时间段构成的列表的LiveData
//     */
//    suspend fun findTimesByItem(item: CalendarItemData): LiveData<List<CalendarTimeData>> {
//        val dbLiveData = _findTimesByItemId(item.id)
//        return SingleItemTimeListLiveDataWrapper(dbLiveData, item)
//    }
//
//    @Query("")
//    protected abstract suspend fun _findTimesByItemId(itemId: Int): LiveData<List<CalendarTimeData>>
//
//    /**
//     * 根据时间段找到它对应的日程。
//     *
//     * 这个接口通常可能不会用到，（因为返回给前端的[CalendarTimeData]数据通常是带有对应的[CalendarItemData]的）
//     */
//    suspend fun findItemByTime(time: CalendarTimeData): LiveData<CalendarItemData> {
//        return _findItemByItemId(time.item_id)
//    }
//
//    @Query("")
//    protected abstract suspend fun _findItemByItemId(itemId: Int): LiveData<CalendarItemData>
//
//    /**
//     * 根据日期，找到它下面所有的时间段。
//     *
//     * 本函数返回的[LiveData]已经经过特殊处理，保证其中的List<CalendarTimeData>的每个元素，
//     * 除了第一次[Observer.onChanged]它们的[CalendarTimeData.calendarItem]项均不为空，可以直接访问到这个时间段对应的日程项；
//     * 并且如果List<CalendarTimeData>中有某个[CalendarTimeData]，它里面的[CalendarTimeData.calendarItem]
//     * 发生改变，则本函数返回的这个[LiveData]照样会向观察者推送事件。
//     * @param [date] 日期数据
//     * @return 所有时间段构成的列表的LiveData
//     */
//    suspend fun findTimesByDate(date: LocalDate): LiveData<List<CalendarTimeData>> {
//        class MultipleItemTimeListLiveDataWrapper(
//            val originLiveData: LiveData<List<CalendarTimeData>>
//        ) : LiveData<List<CalendarTimeData>>(originLiveData.value)
//        {
//
//            inner class ItemLiveDataObserver: Observer<CalendarItemData> {
//                var inited = false
//                override fun onChanged(t: CalendarItemData?) {
//                    if (inited) _updateAllValueAsync(value!!)
//                    else inited = true
//                }
//            }
//
//            private val itemLiveDataMap: MutableMap<Int, LiveData<CalendarItemData>> =
//                mutableMapOf()
//
//            private val itemObserverMap: MutableMap<Int, ItemLiveDataObserver> =
//                mutableMapOf()
//
//            private val mutex = Mutex()
//
//            private fun _updateAllValueAsync(t: List<CalendarTimeData>){
//                GlobalScope.launch {
//                    val newList = t.map { async { it.apply {
//                        calendarItem = findItemFromMap(it.item_id)
//                    } } }.awaitAll()
//                    postValue(newList)
//                }
//            }
//
//            suspend fun findItemFromMap(itemId: Int): CalendarItemData {
//                var item = itemLiveDataMap[itemId]
//                if (item != null) return item.value!!
//                withContext(Dispatchers.IO) {
//                    item = _findItemByItemId(itemId)
//                }
//                mutex.withLock {
//                    itemLiveDataMap[itemId] = item!!
//                    if (hasActiveObservers()) {
//                        val observer = ItemLiveDataObserver()
//                        itemObserverMap[itemId] = observer
//                        item!!.observeForever(observer)
//                    }
//                }
//                return item!!.value!!
//            }
//
//
//
//            val observer = Observer<List<CalendarTimeData>> { _updateAllValueAsync(it) }
//
//
//            override fun onActive() {
//                super.onActive()
//                originLiveData.observeForever(observer)
//                itemLiveDataMap.forEach { (_, value) ->
//                    if (!value.hasActiveObservers()) {
//                        val observer = ItemLiveDataObserver()
//                        itemObserverMap[value.value!!.id] = observer
//                        value.observeForever(observer)
//                    }
//                }
//            }
//
//            override fun onInactive() {
//                super.onInactive()
//                originLiveData.removeObserver(observer)
//                itemLiveDataMap.forEach { _, value ->
//                    itemObserverMap[value.value!!.id]?.let { value.removeObserver(it) }
//                }
//            }
//        }
//        val dbLiveData = _findTimesByDate(date)
//        return MultipleItemTimeListLiveDataWrapper(dbLiveData)
//    }
//
//    @Query("")
//    protected abstract suspend fun _findTimesByDate(date: LocalDate): LiveData<List<CalendarTimeData>>
//
////    fun insertOrUpdateItem()
//
//    @Query("")
//    protected abstract suspend fun _insert(item: CalendarItemData): LiveData<List<CalendarTimeData>>
//
//    @Query("")
//    protected abstract suspend fun _insert(time: CalendarTimeData): LiveData<List<CalendarTimeData>>
//
//    @Query("")
//    protected abstract suspend fun _update(item: CalendarItemData): LiveData<List<CalendarTimeData>>
//
//    @Query("")
//    protected abstract suspend fun _update(time: CalendarTimeData): LiveData<List<CalendarTimeData>>
}
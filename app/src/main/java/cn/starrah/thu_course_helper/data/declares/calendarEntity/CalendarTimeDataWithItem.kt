package cn.starrah.thu_course_helper.data.declares.calendarEntity

import androidx.lifecycle.LiveData
import androidx.room.Relation
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarItemData
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarItemDataWithTimes
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeData

class CalendarTimeDataWithItem : CalendarTimeData() {
    @Relation(parentColumn = "item_id", entityColumn = "id")
    var _im: List<CalendarItemData> = listOf()

    /** 该时间段所对应关联的日程项数据对象的引用。*/
    var calendarItem: CalendarItemData
        get() = _im[0]
        set(value) {
            _im = listOf(value)
        }

    /**
     * 可以在主线程调用。
     *
     * 获得该时间段所对应的日程项的、[CalendarItemDataWithTimes]格式数据的[LiveData]；
     * 同时[calendarItem]属性的值也会被设置为本函数返回的[LiveData]的*value*。
     */
    override suspend fun queryItem(): LiveData<CalendarItemDataWithTimes> {
        val superRes = super.queryItem()
        calendarItem = superRes.value ?: calendarItem
        return superRes
    }
}
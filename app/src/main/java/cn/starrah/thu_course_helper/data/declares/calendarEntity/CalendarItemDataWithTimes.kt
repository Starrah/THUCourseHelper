package cn.starrah.thu_course_helper.data.declares.calendarEntity

import androidx.lifecycle.LiveData
import androidx.room.Relation

class CalendarItemDataWithTimes(
    /** 该日程的所有时间段的信息。*/
    @Relation(parentColumn = "rowid", entityColumn = "item_id")
    var times: MutableList<CalendarTimeData> = mutableListOf()
) : CalendarItemData() {
    constructor(
        superInstance: CalendarItemData,
        times: MutableList<CalendarTimeData> = mutableListOf()
    ) : this(times) {
        id = superInstance.id
        name = superInstance.name
        type = superInstance.type
        detail = superInstance.detail
    }

    /**
     * 可以在主线程调用。
     *
     * 获得该Item的时间段列表的[LiveData]；
     * 同时[times]属性的值也会被设置为本函数返回的[LiveData]的*value*。
     */
    override suspend fun queryTimes(): LiveData<List<CalendarTimeData>> {
        val superRes = super.queryTimes()
        times = superRes.value?.toMutableList() ?: times
        return superRes
    }

    fun assertValidWithTimes() {
        super.assertValidWithTimes(times)
    }
}
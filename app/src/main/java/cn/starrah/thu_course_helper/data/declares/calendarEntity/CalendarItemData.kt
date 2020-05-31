package cn.starrah.thu_course_helper.data.declares.calendarEntity

import androidx.lifecycle.LiveData
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import cn.starrah.thu_course_helper.data.database.CalendarRepository
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarItemLegalDetailKey
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarItemType
import cn.starrah.thu_course_helper.data.utils.Verifiable
import cn.starrah.thu_course_helper.data.utils.assertData
import cn.starrah.thu_course_helper.data.utils.assertDataSystem
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.TypeReference

/**
 * 描述一个日程的数据类。
 */
@Entity
open class CalendarItemData(
    /** 日程的数据库id，各个日程唯一。当试图插入新日程到数据库中时，请保证id为默认值0。*/
    @PrimaryKey(autoGenerate = true) var id: Int = 0,

    /** 日程的名称 */
    var name: String = "",

    /** 日程类型 */
    var type: CalendarItemType = CalendarItemType.COURSE,

    /**
     * 日程的详细信息。以Map的形式给出。
     * Key是详细信息的枚举类型（其*chineseName*属性可直接获得汉字名称用于前端显示），Value是对应的内容。
     *
     * 一个日程所包含的Key的种类由日程类型决定，但本字段返回的结果不会含有对应Value为空的Key。
     * 例如一个课程类型的日程正常应该包括教师，但如果用户在创建这个日程时没有填写教师字段，那么本字段中不会
     * 含有教师这个Key。
     *
     * 如果想获得该字段允许的所有Key的列表，请使用[CalendarItemType.AllowedDetailKeys]。
     */
    var detail: MutableMap<CalendarItemLegalDetailKey, String> = mutableMapOf()
): Verifiable {
    /**
     * 可以在主线程调用。
     *
     * 获得该Item的时间段列表的[LiveData]；
     */
    open suspend fun queryTimes(): LiveData<List<CalendarTimeData>> {
        return CalendarRepository.findTimesByItem(this)
    }

    class TC {
        @TypeConverter
        fun toDBDataType(value: MutableMap<CalendarItemLegalDetailKey, String>): String {
            return JSON.toJSONString(value)
        }

        @TypeConverter
        fun fromDBDataType(value: String): MutableMap<CalendarItemLegalDetailKey, String> {
            return JSON.parseObject(
                value,
                object : TypeReference<MutableMap<CalendarItemLegalDetailKey, String>>() {})
        }
    }

    override fun assertValid() {}

    fun assertValidWithTimes(times: List<CalendarTimeData>) {
        assertValid()
        for (time in times) {
            time.assertValid()
            assertDataSystem(time.item_id == id, "TimeData的时间段的item_id与关联的ItemData不一致！")
        }
    }
}


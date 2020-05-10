package cn.starrah.thu_course_helper.data.declares

import androidx.lifecycle.LiveData
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.TypeReference

/**
 * 描述一个日程的数据类。
 */
@Entity
data class CalendarItemData(
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

//    /** 该日程的所有时间段的信息 */
//    @Ignore var times: List<CalendarTimeData> = mutableListOf()
) {
    /**
     * 可以在主线程调用。
     */
    suspend fun getTimes(): LiveData<List<CalendarTimeData>> {
        TODO()
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

}
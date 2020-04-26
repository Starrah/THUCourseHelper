package cn.starrah.thu_course_helper.data.declares

/**
 * 日程的类型
 * @param [chineseName] 中文名，可直接用于前端显示
 */
enum class CalendarItemType(val chineseName: String) {
    COURSE("课程"),
    RESEARCH("科研"),
    SOCIALWORK("社工"),
    ASSOCIATION("社团"),
    OTHER("其他"),
    ;

    /** 获取该种活动类型允许的所有Key的列表。可能在编辑日程的时候会用到。 */
    val AllowedDetailKeys: List<CalendarItemLegalDetailKey>
        get() = CalendarItemLegalDetailKey.values().filter { it.allowedTypes.contains(this) }
}

/**
 * 日程的详情列表允许的Key。
 * @param [chineseName] 中文名，可直接用于前端显示
 * @param [allowedTypes] 该Key允许在哪些种类的日程中出现。
 */
enum class CalendarItemLegalDetailKey(
    val chineseName: String,
    val allowedTypes: List<CalendarItemType>
) {
    COMMENT("说明", CalendarItemType.values().asList()),
    COURSEID("课程号", listOf(CalendarItemType.COURSE)),
    TEACHER("教师", listOf(CalendarItemType.COURSE)),
    ORGANIZATION("组织", listOf(CalendarItemType.SOCIALWORK, CalendarItemType.ASSOCIATION)),
}

/**
 * 描述一个日程的数据类。
 */
data class CalendarItemData(
    /** 日程的数据库id，各个日程唯一 */
    val id: Int = 0,

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
    var detail: MutableMap<CalendarItemLegalDetailKey, String> = mutableMapOf(),

    /** 该日程的所有时间段的信息 */
    var times: MutableList<CalendarTimeData> = mutableListOf()
)
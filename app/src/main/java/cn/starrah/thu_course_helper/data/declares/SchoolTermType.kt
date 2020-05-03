package cn.starrah.thu_course_helper.data.declares

/**
 * 学期的类型
 * @param [chineseName] 中文名，可直接用于前端显示
 * @param [oneCharChineseName] 单汉字的中文名，可直接用于前端显示
 */
enum class SchoolTermType(val chineseName: String, val oneCharChineseName: String) {
    AUTUMN("秋季学期", "秋"),
    WINTER("寒假", "冬"),
    SPRING("春季学期", "春"),
    SUMMER("夏季学期", "夏"),
    ;
}
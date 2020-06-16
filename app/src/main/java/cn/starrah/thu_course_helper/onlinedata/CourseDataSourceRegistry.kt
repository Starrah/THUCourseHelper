package cn.starrah.thu_course_helper.onlinedata

import cn.starrah.thu_course_helper.onlinedata.thu.THUCourseDataSouce

/**
 * 在这里注册一个在线课程数据源，以便在CREP等地方读取
 */
val CourseDataSourceRegistry: Map<String, AbstractCourseDataSource> = mapOf(
    "清华大学" to THUCourseDataSouce
)
package cn.starrah.thu_course_helper.data.utils

import java.lang.AssertionError
import java.lang.RuntimeException

open class DataInvalidException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * 断言，如果失败抛出[DataInvalidException]并附带信息[message]
 */
fun assertData(condition: Boolean, message: String = "") {
    if (!condition) throw DataInvalidException(message)
}

/**
 * 断言，如果失败抛出[DataInvalidException]并附带信息："[message]+这是一个系统错误，请与开发者联系。"
 */
fun assertDataSystem(condition: Boolean, message: String = "") {
    return assertData(condition, "$message\r\n这是一个系统错误，请与开发者联系。")
}
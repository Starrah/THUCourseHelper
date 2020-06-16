package cn.starrah.thu_course_helper.onlinedata

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarItemDataWithTimes
import cn.starrah.thu_course_helper.data.declares.school.SchoolTerm
import cn.starrah.thu_course_helper.data.utils.DataInvalidException

abstract class AbstractCourseDataSource {
    abstract val schoolName: String

    /**
     * 登录
     * @param [activity] 当前的[Activity]，因为登陆过程可能需要验证码等事项，有可能需要显示一个对话框，因此需要提供一下。
     * @param [username] 用户名
     * @param [password] 密码
     * @param [extra] 其他信息
     */
    abstract suspend fun login(
        username: String,
        password: String,
        extra: Map<String, Any> = mapOf()
    ): Any?

    /**
     * 读取所有的课程
     */
    abstract suspend fun loadAllCourses(
        term: SchoolTerm,
        extra: Map<String, Any> = mapOf()
    ): List<CalendarItemDataWithTimes>


    private val errorMsgAbstractNotImplemented =
        "抽象的AbstractCourseDataSource类没有提供该方法的实现。该方法的实现应当由子类负责。"

    /**
     * 读取所有的课程，并将其直接合并进课表当中。
     *
     * 子类建议实现这个方法，但并不强制。
     */
    open suspend fun applyLoadedCourses(
        courses: List<CalendarItemDataWithTimes>,
        extra: Map<String, Any> = mapOf()
    ): Unit = throw NotImplementedError(errorMsgAbstractNotImplemented)

    /**
     * 读取数据。这种数据有可能有很多种来源，也可以有各种类型。通过extra字段指定要获取的数据的具体内容。
     */
    open suspend fun loadData(term: SchoolTerm, extra: Map<String, Any> = mapOf()): Any? =
        throw NotImplementedError(errorMsgAbstractNotImplemented)

    /**
     * 子类可以实现这个方法来做任何的事情。
     */
    open suspend fun doSomething(vararg params: Any?): Any? =
        throw NotImplementedError(errorMsgAbstractNotImplemented)

    /**
     * 登录态是否还有效
     */
    var isSessionValid = false
        protected set

    class LoginStatusTimeoutException : DataInvalidException("登录状态已过期，请您重新输入密码登录！")
}
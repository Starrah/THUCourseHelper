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
    abstract suspend fun login(activity: FragmentActivity, username: String, password: String, extra: Map<String, Any>? = null)

    /**
     * 读取所有的课程
     */
    abstract suspend fun loadAllCourse(term: SchoolTerm): List<CalendarItemDataWithTimes>

    /**
     * 登录态是否还有效
     */
    var isSessionValid = false
        protected set

    /**
     * 获取cookie
     * @param [username] 用户名
     * @param [password] 密码
     * @return cookie字符串，可发给后端用于鉴权
     */
    abstract suspend fun getCookie(username: String, password: String): String

    class LoginStatusTimeoutException: DataInvalidException("登录状态已过期，请您重新输入密码登录！")
}
package cn.starrah.thu_course_helper.onlinedata

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarItemDataWithTimes
import cn.starrah.thu_course_helper.data.declares.school.SchoolTerm

abstract class AbstractCourseDataSource {
    abstract val schoolName: String

    /**
     * 登录
     * @param [activity] 当前的[Activity]，因为登陆过程可能需要验证码等事项，有可能需要显示一个对话框，因此需要提供一下。
     * @param [username] 用户名
     * @param [password] 密码
     * @param [extra] 其他信息
     * @return cookie，可能可以用于发回后端做进一步的身份鉴权。
     */
    abstract fun login(activity: FragmentActivity, username: String, password: String, extra: Map<String, Any>? = null): String

    /**
     * 读取所有的课程
     */
    abstract fun loadAllCourse(term: SchoolTerm): List<CalendarItemDataWithTimes>
}
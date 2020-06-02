package cn.starrah.thu_course_helper.onlinedata.thu

import androidx.fragment.app.FragmentActivity
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarItemDataWithTimes
import cn.starrah.thu_course_helper.data.declares.school.SchoolTerm
import cn.starrah.thu_course_helper.onlinedata.AbstractCourseDataSource

class THUCourseDataSouce: AbstractCourseDataSource() {
    override val schoolName: String = "清华大学"
    override fun login(activity: FragmentActivity, username: String, password: String, extra: Map<String, Any>?): String {
        TODO("Not yet implemented")
    }

    override fun loadAllCourse(term: SchoolTerm): List<CalendarItemDataWithTimes> {
        TODO("Not yet implemented")
    }
}
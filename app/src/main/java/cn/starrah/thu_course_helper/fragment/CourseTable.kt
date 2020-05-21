package cn.starrah.thu_course_helper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeData
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeDataWithItem
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarTimeType
import cn.starrah.thu_course_helper.data.declares.time.TimeInCourseSchedule
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.time.DayOfWeek
import java.time.LocalDate


class CourseTable : TableFragment() {



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        theActivity = requireActivity()
        showDays = 7
        showType = "course"
        var view:View? = null
        if(showType == "course") {
            view = inflater.inflate(R.layout.table_class, container, false)
        }
        else{
            view = inflater.inflate(R.layout.table_hour, container, false)
        }
        return view
    }


    /*
    描述：按照设置初始化视图
    参数：无
    返回：无
    */
    override fun initializeLayout() {
        if(theActivity == null)
        {
            return
        }
        initializeBaseLayout()
        if(showType == "course") {
            initializeLeftCourse()
        }
        else
        {
            initializeLeftHour()
        }
        initializeListWidth()
    }

    /**
    * 描述：获取本周的所有课程时间段（这里应该是个虚函数，课程，日程表实现不同）
    * 参数：日期
    * 返回：无
     * TODO
    */
    override protected suspend fun getValidTimes() {
        for (week_num in DayOfWeek.values()) {
            var the_day: LocalDate = allDates[week_num]!!
            var the_list = listOf<LocalDate>(the_day)

            timeList[week_num] = CREP.findTimesByDays(the_list)
        }
    }


    /*
    描述：显示某个日程时间段
    参数：这个时间段在周几，这个时间段的信息
    返回：无
    */
    override fun showOneItem(theWeekDay: DayOfWeek, theItem: CalendarTimeDataWithItem) {
        var v:View? = null;
        if (showType == "course") {
            if (theItem.timeInCourseSchedule == null) {
                return
            }
            else
            {
                v = showOneCourse(theWeekDay, theItem)
            }
        }
        else
        {
            v = showOneHour(theWeekDay, theItem)
        }

        //TODO:绑定事件
    }



}
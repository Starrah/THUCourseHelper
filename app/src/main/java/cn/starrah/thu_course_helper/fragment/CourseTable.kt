package cn.starrah.thu_course_helper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeData
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeDataWithItem
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarTimeType
import cn.starrah.thu_course_helper.data.declares.school.SchoolTimeRule
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

    /**
     * 描述：初始化左侧大节文字
     * 参数：无
     * 返回：无
     * @sample "第一\n大节\n08:00-\n09:35"
     */
    fun initalizeClassText() {
        val show_class_text_id = arrayOf<Int>(0, R.id.class_1_text, R.id.class_2_text,
            R.id.class_3_text, R.id.class_4_text, R.id.class_5_text, R.id.class_6_text)
        val show_string_list = arrayOf<String>("", "第一\n大节\n", "第二\n大节\n", "第三\n大节\n", "第四\n大节\n", "第五\n大节\n", "第六\n大节\n")
        var i = 1;
        while(i <= 6) {
            var show_place = theActivity!!.findViewById<TextView?>(show_class_text_id[i])
            var text_start_hour = CREP.timeRule.getBigByNumber(i).startTime.hour.toString()
            if (text_start_hour.length < 2) {
                text_start_hour = "0" + text_start_hour
            }
            var text_start_minute = CREP.timeRule.getBigByNumber(i).startTime.minute.toString()
            if (text_start_minute.length < 2) {
                text_start_minute = "0" + text_start_minute
            }
            var text_end_hour = CREP.timeRule.getBigByNumber(i).endTime.hour.toString()
            if (text_end_hour.length < 2) {
                text_end_hour = "0" + text_end_hour
            }
            var text_end_minute = CREP.timeRule.getBigByNumber(i).endTime.minute.toString()
            if (text_end_minute.length < 2) {
                text_end_minute = "0" + text_end_minute
            }
            var text_final:String = show_string_list[i] + text_start_hour + ":" + text_start_minute + "-\n" + text_end_hour + ":" + text_end_minute
            show_place!!.setText(text_final)
            i ++
        }
    }

    /**
    *描述：按照设置初始化视图
    *参数：无
    *返回：无
    */
    override fun initializeLayout() {
        if(theActivity == null)
        {
            return
        }
        initializeBaseLayout()
        if(showType == "course") {
            initializeLeftCourse()
            initalizeClassText()
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
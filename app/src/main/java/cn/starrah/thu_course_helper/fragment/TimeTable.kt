package cn.starrah.thu_course_helper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeData
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarTimeType
import cn.starrah.thu_course_helper.data.declares.time.TimeInCourseSchedule
import java.time.DayOfWeek
import java.time.LocalDate


class TimeTable : TableFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        showDays = 5

        theActivity = requireActivity()
        var view:View = inflater.inflate(R.layout.table_hour, container, false)

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
        initializeLeftHour()
        initializeListWidth()
    }

    /*
    描述：获取本周的所有课程时间段（这里应该是个虚函数，课程，日程表实现不同）
    参数：日期
    返回：无
    TODO
    */
    override protected suspend fun getValidTimes() {
        for (week_num in DayOfWeek.values()) {
            var the_day: LocalDate = allDates[week_num]!!
            var the_list = listOf<LocalDate>(the_day)

            timeList[week_num] = CREP.findTimesByDays(the_list)
        }
    }

    /*
    val time1: TimeInCourseSchedule = TimeInCourseSchedule(
        startBig = 6, startOffsetSmall = 0.0f,
        lengthSmall = 2.0f, date = LocalDate.parse("2020-05-05")
    )
    val data1: CalendarTimeData = CalendarTimeData(
        name = "摸鱼", type = CalendarTimeType.SINGLE_COURSE
        , timeInCourseSchedule = time1
    )

    val time2: TimeInCourseSchedule = TimeInCourseSchedule(
        dayOfWeek = DayOfWeek.TUESDAY, startBig = 2, startOffsetSmall = 0.0f,
        lengthSmall = 3.0f
    )
    val data2: CalendarTimeData = CalendarTimeData(
        name = "数据库", type = CalendarTimeType.REPEAT_COURSE
        , timeInCourseSchedule = time2, repeatWeeks = mutableListOf(12, 14, 16)
    )
    var theList1: MutableList<CalendarTimeData> = timeList[DayOfWeek.TUESDAY]!!;
    theList1.add(data1)
    theList1.add(data2)
    timeList[DayOfWeek.TUESDAY] = theList1


    val time3: TimeInCourseSchedule = TimeInCourseSchedule(
        startBig = 5, startOffsetSmall = 0.0f,
        lengthSmall = 2.0f, date = LocalDate.parse("2020-05-05")
    )
    val data3: CalendarTimeData = CalendarTimeData(
        name = "摸鱼", type = CalendarTimeType.SINGLE_COURSE
        , timeInCourseSchedule = time3
    )

    val time4: TimeInCourseSchedule = TimeInCourseSchedule(
        dayOfWeek = DayOfWeek.TUESDAY, startBig = 2, startOffsetSmall = 0.0f,
        lengthSmall = 2.0f
    )
    val data4: CalendarTimeData = CalendarTimeData(
        name = "移动软件开发", type = CalendarTimeType.REPEAT_COURSE
        , timeInCourseSchedule = time4, repeatWeeks = mutableListOf(12, 14, 16)
    )
    var theList2: MutableList<CalendarTimeData> = timeList[DayOfWeek.WEDNESDAY]!!;
    theList2.add(data3)
    theList2.add(data4)
    timeList[DayOfWeek.WEDNESDAY] = theList2

    val time5: TimeInCourseSchedule = TimeInCourseSchedule(
        startBig = 6, startOffsetSmall = 0.0f,
        lengthSmall = 3.0f, date = LocalDate.parse("2020-05-07")
    )
    val data5: CalendarTimeData = CalendarTimeData(
        name = "摸鱼", type = CalendarTimeType.SINGLE_COURSE
        , timeInCourseSchedule = time5
    )

    val time6: TimeInCourseSchedule = TimeInCourseSchedule(
        dayOfWeek = DayOfWeek.THURSDAY, startBig = 3, startOffsetSmall = 0.0f,
        lengthSmall = 3.0f
    )
    val data6: CalendarTimeData = CalendarTimeData(
        name = "数据结构", type = CalendarTimeType.REPEAT_COURSE
        , timeInCourseSchedule = time6, repeatWeeks = mutableListOf(12, 14, 16)
    )
    var theList3: MutableList<CalendarTimeData> = timeList[DayOfWeek.THURSDAY]!!;
    theList3.add(data5)
    theList3.add(data6)
    timeList[DayOfWeek.THURSDAY] = theList3*/


    /*
    描述：显示某个日程时间段
    参数：这个时间段在周几，这个时间段的信息
    返回：无
    */
    override fun showOneItem(theWeekDay: DayOfWeek, theItem: CalendarTimeData) {
        //System.out.println(theItem)
        if(theItem == null) {
            return
        }
        var v:View? = null;
        v = showOneHour(theWeekDay, theItem)

        //TODO:绑定事件
    }



}
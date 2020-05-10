package cn.starrah.thu_course_helper


import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import cn.starrah.thu_course_helper.data.declares.*
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime


/*
课程表/日程表的父类，封装了所有主要函数
 */
abstract class TableFragment : Fragment(){

    //父类activity
    protected var theActivity: FragmentActivity? = null

    /*当前显示设置*/
    //显示几天
    protected var showDays: Int = 5

    //大节还是小时
    protected var showType: String = "hour"

    /*当前周*/
    protected val currentWeek: Int = 12;

    /*当前周所有日期，以string形式yyyy-MM-dd表示*/
    protected val allDates = mutableMapOf<DayOfWeek, LocalDate>(
        DayOfWeek.MONDAY to LocalDate.parse("2020-05-04"),
        DayOfWeek.TUESDAY to LocalDate.parse("2020-05-05"),
        DayOfWeek.WEDNESDAY to LocalDate.parse("2020-05-06"),
        DayOfWeek.THURSDAY to LocalDate.parse("2020-05-07"),
        DayOfWeek.FRIDAY to LocalDate.parse("2020-05-08"),
        DayOfWeek.SATURDAY to LocalDate.parse("2020-05-09"),
        DayOfWeek.SUNDAY to LocalDate.parse("2020-05-10")
    )

    /*所有在当前周有效的时间*/
    protected val timeList = mutableMapOf<DayOfWeek, MutableList<CalendarTimeData>>(
        DayOfWeek.MONDAY to mutableListOf<CalendarTimeData>(),
        DayOfWeek.TUESDAY to mutableListOf<CalendarTimeData>(),
        DayOfWeek.WEDNESDAY to mutableListOf<CalendarTimeData>(),
        DayOfWeek.THURSDAY to mutableListOf<CalendarTimeData>(),
        DayOfWeek.FRIDAY to mutableListOf<CalendarTimeData>(),
        DayOfWeek.SATURDAY to mutableListOf<CalendarTimeData>(),
        DayOfWeek.SUNDAY to mutableListOf<CalendarTimeData>()
    )

    /*周一到周日显示的视图*/
    protected val showPlaceID = mapOf<DayOfWeek, Int>(
        DayOfWeek.MONDAY to R.id.monday_place,
        DayOfWeek.TUESDAY to R.id.tuesday_place,
        DayOfWeek.WEDNESDAY to R.id.wednesday_place,
        DayOfWeek.THURSDAY to R.id.thursday_place,
        DayOfWeek.FRIDAY to R.id.friday_place,
        DayOfWeek.SATURDAY to R.id.saturday_place,
        DayOfWeek.SUNDAY to R.id.sunday_place
    )

    //控件初始化相关函数
    /*
    描述：初始化控件的宽度，高度（都是linearlayout）
    参数：id，宽度，高度
    返回：无
     */
    fun setWidthHeight(ID:Int, Width:Int, Height:Int) {
        val view: LinearLayout = theActivity!!.findViewById(ID)
        var params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(Width, Height);
        view.setLayoutParams(params)
    }

    /*
    描述：按照设置初始化视图
    参数：无
    返回：无
    */
    abstract protected fun initializeLayout();

    /*
    描述：初始化基本都layout
    参数：无
    返回：无
     */
    protected fun initializeBaseLayout() {
        //绑定上下scrollview

        val ScrollTopTime: BindableScrollView = theActivity!!.findViewById(R.id.scroll_top)
        val ScrollBottomTime: BindableScrollView = theActivity!!.findViewById(R.id.scroll_bottom)
        ScrollBottomTime.bindView(ScrollTopTime)
        ScrollTopTime.bindView(ScrollBottomTime)

        //获取手机高度宽度
        val dm = DisplayMetrics()
        theActivity!!.windowManager.defaultDisplay.getMetrics(dm)
        val TotalWidth: Int = dm.widthPixels
        val TotalHeight: Int = dm.heightPixels

        //初始化最上方高度
        setWidthHeight(R.id.top, ViewGroup.LayoutParams.MATCH_PARENT, LayoutConstants.TopTabHeight)

        //初始化中间高度
        //val middleHeight = TotalHeight - LayoutConstants.BottomTabHeight - 100
        //setWidthHeight(R.id.main_place, ViewGroup.LayoutParams.MATCH_PARENT, middleHeight)

        //初始化最下方高度
        //setWidthHeight(R.id.bottom, ViewGroup.LayoutParams.MATCH_PARENT, LayoutConstants.BottomTabHeight)

        //初始化最左一栏宽度
        setWidthHeight(R.id.left_view_layout, LayoutConstants.LeftWidth, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    /*
    描述：初始化左侧一栏的高度和宽度（按照大节）
    参数：无
    返回：无
    */
    protected fun initializeLeftCourse() {
        val showCourseIDCourseClass = arrayOf<Int>(
            0, R.id.class_1, R.id.class_2, R.id.class_3, R.id.class_4,
            R.id.class_5, R.id.class_6
        )
        for (i in showCourseIDCourseClass.indices) {
            if (i == 0) {
                continue
            }
            val SmallNumber = CourseConstants.SmallNumber[i]
            val Height: Int = SmallNumber * LayoutConstants.HeightPerSmall
            setWidthHeight(showCourseIDCourseClass[i], ViewGroup.LayoutParams.MATCH_PARENT, Height)
        }
    }

    /*
    描述：初始化左侧一栏的高度和宽度（按照小时）
    参数：无
    返回：无
    */
    protected fun initializeLeftHour() {
        val showCourseID = arrayOf<Int>(
            R.id.hour_0, R.id.hour_1, R.id.hour_2, R.id.hour_3, R.id.hour_4, R.id.hour_5,
            R.id.hour_6, R.id.hour_7, R.id.hour_8, R.id.hour_9, R.id.hour_10, R.id.hour_11,
            R.id.hour_12, R.id.hour_13, R.id.hour_14, R.id.hour_15, R.id.hour_16, R.id.hour_17,
            R.id.hour_18, R.id.hour_19, R.id.hour_20, R.id.hour_21, R.id.hour_22, R.id.hour_23
        )
        for (i in showCourseID.indices) {
            val Height: Int = LayoutConstants.HeightPerHour
            setWidthHeight(showCourseID[i], ViewGroup.LayoutParams.MATCH_PARENT, Height)
        }
    }

    /*
    描述：初始化周一到周日的宽度，在设置改变的时候需要调用
    参数：无
    返回：无
    */
    protected fun initializeListWidth() {
        //获取手机高度宽度
        val dm = DisplayMetrics()
        theActivity!!.windowManager.defaultDisplay.getMetrics(dm)
        val TotalWidth: Int = dm.widthPixels
        val TotalHeight: Int = dm.heightPixels
        var averageWidth: Int = (TotalWidth - LayoutConstants.LeftWidth) / showDays

        //初始化日期/周几宽度
        //显示的视图所在列，要求宽度
        val showLineIDCourseClass = mapOf<DayOfWeek, Int>(
            DayOfWeek.MONDAY to R.id.monday_list,
            DayOfWeek.TUESDAY to R.id.tuesday_list,
            DayOfWeek.WEDNESDAY to R.id.wednesday_list,
            DayOfWeek.THURSDAY to R.id.thursday_list,
            DayOfWeek.FRIDAY to R.id.friday_list,
            DayOfWeek.SATURDAY to R.id.saturday_list,
            DayOfWeek.SUNDAY to R.id.sunday_list
        )

        //显示的视图所在列的头，要求宽度
        val showLineTopIDCourseClass = mapOf<DayOfWeek, Int>(
            DayOfWeek.MONDAY to R.id.monday_top,
            DayOfWeek.TUESDAY to R.id.tuesday_top,
            DayOfWeek.WEDNESDAY to R.id.wednesday_top,
            DayOfWeek.THURSDAY to R.id.thursday_top,
            DayOfWeek.FRIDAY to R.id.friday_top,
            DayOfWeek.SATURDAY to R.id.saturday_top,
            DayOfWeek.SUNDAY to R.id.sunday_top
        )

        for (day in DayOfWeek.values()) {
            setWidthHeight(showLineIDCourseClass[day]!!, averageWidth, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        for (day in DayOfWeek.values()) {
            setWidthHeight(showLineTopIDCourseClass[day]!!, averageWidth, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }


    override fun onStart() {
        super.onStart()
        //TODO 根据当前日期获取周
        //TODO 根据当前周获取各个日期

        getValidTimes()
        if(theActivity == null)
        {
            return
        }
        initializeLayout()
        showAllCourses()

    }


    /*
    描述：获取本周的所有月，日信息
    参数：无
    返回：无
    TODO
    */
    fun getAllDates() {

    }

    /*
    描述：获取本周的所有课程时间段（这里应该是个虚函数，课程，日程表实现不同）
    参数：日期
    返回：无
    TODO
     */
    abstract protected fun getValidTimes();


    /*
    描述：显示所有课程
    参数：无
    返回：无
    */
    public fun showAllCourses() {
        if(theActivity == null)
        {
            return
        }
        for (day in DayOfWeek.values()) {
            for (course in timeList[day]!!) {
                showOneItem(day, course)
            }
        }
    }

    abstract protected fun showOneItem(theWeekDay: DayOfWeek, theItem: CalendarTimeData);


    /*
    描述：显示某个日程时间段（大节）
    参数：这个时间段在周几，这个时间段的信息
    返回：绑定的view
    */
    protected fun showOneCourse(theWeekDay: DayOfWeek, theCourse: CalendarTimeData): View {


        var viewID: Int = showPlaceID[theWeekDay]!!
        var dayView = theActivity!!.findViewById<RelativeLayout>(viewID)
        var startBig: Int = theCourse.timeInCourseSchedule!!.startBig
        var startSmall: Float =
            CourseConstants.StartSmallNumber[startBig] + theCourse.timeInCourseSchedule!!.startOffsetSmall
        var intevalSmall: Float = theCourse.timeInCourseSchedule!!.lengthSmall
        var startHeight: Float = LayoutConstants.HeightPerSmall * startSmall
        var theHeight: Int = (LayoutConstants.HeightPerSmall * intevalSmall).toInt()

        var v: View = LayoutInflater.from(theActivity!!).inflate(R.layout.course_item, null); //加载单个课程布局

        v.setY(startHeight); //设置开始高度,即第几节课开始

        var params: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, theHeight)
        v.setLayoutParams(params);

        var theTextView = v.findViewById<TextView>(R.id.text_view);
        theTextView.setText(theCourse.name); //显示课程名
        dayView.addView(v);
        return v
    }


    /*
    描述：显示某个日程时间段（小时）
    参数：这个时间段在周几，这个时间段的信息
    返回：绑定的view
    */
    protected fun showOneHour(theWeekDay: DayOfWeek, theItem: CalendarTimeData): View {

        var viewID: Int = showPlaceID[theWeekDay]!!
        var dayView = requireActivity().findViewById<RelativeLayout>(viewID)

        var startTime: LocalTime? = null
        var endTime: LocalTime? = null
        //大节转小时
        if (theItem.timeInCourseSchedule != null) {
            var hourTime = GetHour(theItem.timeInCourseSchedule!!)
            startTime = hourTime.startTime
            endTime = hourTime.endTime
        }
        //小时
        else
        {
            startTime = theItem.timeInHour!!.startTime
            endTime = theItem.timeInHour!!.endTime
        }
        var startInteval: Duration = Duration.between(LocalTime.parse("00:00"), startTime)
        var intevalTime: Duration = Duration.between(startTime, endTime)
        var startY:Float = GetPlaceByDuration(startInteval)
        var lengthY:Int = GetPlaceByDuration(intevalTime).toInt()

        var v: View = LayoutInflater.from(theActivity!!).inflate(R.layout.course_item, null); //加载单个课程布局
        v.setY(startY); //设置开始高度,即第几节课开始

        var params: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, lengthY)
        v.setLayoutParams(params);

        var theTextView = v.findViewById<TextView>(R.id.text_view);
        theTextView.setText(theItem.name); //显示课程名
        dayView.addView(v);
        return v
    }

    /*
    描述：根据课程的大节信息求得小时信息
    参数：大节信息
    返回：小时信息
    */
    protected fun GetHour(courseSchedule: TimeInCourseSchedule):TimeInHour {

        var startBig: Int = courseSchedule.startBig
        var startSmall: Float = courseSchedule.startOffsetSmall
        var intevalSmall: Float = courseSchedule.lengthSmall

        //计算开始时间
        var startBigTime:LocalTime = CourseConstants.StartTime[startBig]
        var startSmallTime:Long = (startSmall * (CourseConstants.ClassTime + CourseConstants.RestTime)).toLong()
        var startTime:LocalTime = startBigTime.plusMinutes(startSmallTime)

        //计算结束时间
        var totalSmall = startSmall + intevalSmall
        var endBig:Int = startBig
        while(totalSmall > CourseConstants.SmallNumber[endBig])
        {
            totalSmall -= CourseConstants.SmallNumber[endBig]
            endBig += 1
        }
        var endBigTime = CourseConstants.StartTime[endBig]
        var endSmallTime:Long = (totalSmall * (CourseConstants.ClassTime + CourseConstants.RestTime)).toLong()
        var endTime:LocalTime = endBigTime.plusMinutes(endSmallTime)

        var hourTime:TimeInHour = TimeInHour(startTime, endTime)
        return hourTime
    }

    /*
    描述：根据时间求得在小时表的y坐标
    参数：时间
    返回：y坐标
    */
    protected fun GetPlaceByDuration(time:Duration):Float {
        var place:Float = 0.0F
        place = (time.seconds * LayoutConstants.HeightPerHour).toFloat() / 3600
        return place
    }
}

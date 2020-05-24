package cn.starrah.thu_course_helper


import android.content.Intent
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import cn.starrah.thu_course_helper.activity.ItemShowActivity
import cn.starrah.thu_course_helper.data.constants.LayoutConstants
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeDataWithItem
import cn.starrah.thu_course_helper.data.utils.getNotNullValue
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime


/*
课程表/日程表的父类，封装了所有主要函数
 */
abstract class TableFragment : Fragment(){

    //父类activity
    public var theActivity: FragmentActivity? = null

    /*当前显示设置*/
    //显示几天
    protected var showDays: Int = 5

    //大节还是小时
    protected var showType: String = "hour"

    /*当前周*/
    protected var currentWeek: Int = 12;

    /*当前年*/
    protected var currentYear: Int = 2020;

    /*当前月*/
    protected var currentMonth: Int = 5;

    /*学期信息显示位置id*/
    protected var termInfoShowPlace: Int = R.id.term_info

    /*周信息显示位置id*/
    protected var dateInfoShowPlace: Int = R.id.date_info

    companion object {
        public val EXTRA_MESSAGE = "cn.starrah.thu_course_helper.extra.MESSAGE"
    }


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
    protected val timeList = mutableMapOf<DayOfWeek, LiveData<List<CalendarTimeDataWithItem>>>()

    /*周一到周日显示r日期的视图*/
    protected val showDateID = mapOf<DayOfWeek, Int>(
        DayOfWeek.MONDAY to R.id.monday_date,
        DayOfWeek.TUESDAY to R.id.tuesday_date,
        DayOfWeek.WEDNESDAY to R.id.wednesday_date,
        DayOfWeek.THURSDAY to R.id.thursday_date,
        DayOfWeek.FRIDAY to R.id.friday_date,
        DayOfWeek.SATURDAY to R.id.saturday_date,
        DayOfWeek.SUNDAY to R.id.sunday_date
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
    /**
    *描述：按照设置初始化视图
    *参数：无
    *返回：无
    */
    abstract protected fun initializeLayout();

    /**
     *描述：初始化控件的宽度，高度（都是linearlayout）
     *参数：id，宽度，高度
     *返回：无
     */
    fun setWidthHeight(ID:Int, Width:Int, Height:Int) {
        val view: LinearLayout = theActivity!!.findViewById(ID)
        var params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(Width, Height);
        view.setLayoutParams(params)
    }


    /**
    *描述：初始化基本layout
    *参数：无
    *返回：无
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

        //初始化最上方高度
        setWidthHeight(R.id.top, ViewGroup.LayoutParams.MATCH_PARENT, LayoutConstants.TopTabHeight)


        //初始化最左一栏宽度
        setWidthHeight(R.id.left_view_layout, LayoutConstants.LeftWidth, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    /**
    *描述：初始化左侧一栏的高度和宽度（按照大节）
    *参数：无
    *返回：无
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
            val SmallNumber = CREP.timeRule.getBigByNumber(i).smallsCount
            val Height: Int = SmallNumber * LayoutConstants.HeightPerSmall
            setWidthHeight(showCourseIDCourseClass[i], ViewGroup.LayoutParams.MATCH_PARENT, Height)
        }
    }

    /**
    *描述：初始化左侧一栏的高度和宽度（按照小时）
    *参数：无
    *返回：无
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

    /**
    *描述：初始化周一到周日的宽度，在设置改变的时候需要调用
    *参数：无
    *返回：无
    */
    protected fun initializeListWidth() {
        //获取手机高度宽度
        val dm = DisplayMetrics()
        theActivity!!.windowManager.defaultDisplay.getMetrics(dm)
        val TotalWidth: Int = dm.widthPixels
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
        setWeekToday()
        //currentWeek = 15

        updateAllDates()
        showAllDates()


        if(theActivity == null)
        {
            return
        }
        initializeLayout()
        lifecycleScope.launch {
            getValidTimes()
            showAllCourses()
        }


    }



    /**
     * 描述：设置当前周为本周，更新currentWeek
     * 参数：无
     * 返回：无
     */
    fun setWeekToday() {
        var today:LocalDate = LocalDate.now()
        var current_week:Int = CREP.term.dateToWeekNumber(today)
        currentWeek = current_week
    }

    /**
    * 描述：更新本周的所有年，月，日信息,更新allDates,currentYear,currentMonth
    * 参数：无
    * 返回：无
     * @sample：如果要设置为今天对应的周，必须先调用setWeekToday。否则要设置当前周。
    */
    fun updateAllDates() {
        var day_list = CREP.term.datesInAWeek(currentWeek, false)
        var year_list:ArrayList<Int> = ArrayList()
        var month_list:ArrayList<Int> = ArrayList()
        for(i in day_list.indices) {
            year_list.add(day_list.get(i).year)
            month_list.add(day_list.get(i).monthValue)
            if(i == 0) {
                allDates.replace(DayOfWeek.MONDAY, day_list.get(i))
            }
            else if(i == 1) {
                allDates.replace(DayOfWeek.TUESDAY, day_list.get(i))
            }
            else if(i == 2) {
                allDates.replace(DayOfWeek.WEDNESDAY, day_list.get(i))
            }
            else if(i == 3) {
                allDates.replace(DayOfWeek.THURSDAY, day_list.get(i))
            }
            else if(i == 4) {
                allDates.replace(DayOfWeek.FRIDAY, day_list.get(i))
            }
            else if(i == 5) {
                allDates.replace(DayOfWeek.SATURDAY, day_list.get(i))
            }
            else if(i == 6) {
                allDates.replace(DayOfWeek.SUNDAY, day_list.get(i))
            }
        }

        //更新年份信息
        var start_year:Int = year_list.get(0)
        var end_year:Int = year_list.get(6)
        if(start_year == end_year) {
            currentYear = start_year
        }
        else{
            var start_num:Int = 0
            for(item in year_list) {
                if(item == start_year) {
                    start_num ++
                }
            }
            if(start_num >= 4){
                currentYear = start_year
            }
            else {
                currentYear = end_year
            }
        }

        //更新月份信息
        var start_month:Int = month_list.get(0)
        var end_month:Int = month_list.get(6)
        if(start_month == end_month) {
            currentMonth = start_month
        }
        else{
            var start_num:Int = 0
            for(item in month_list) {
                if(item == start_month) {
                    start_num ++
                }
            }
            if(start_num >= 4){
                currentMonth = start_month
            }
            else {
                currentMonth = end_month
            }
        }
    }




    /**
     * 描述：更新本周的所有年,月，日信息显示
     * 参数：无
     * 返回：无
     * @sample：必须在updateAllDates后调用
     */
    protected fun showAllDates() {
        //更新学期显示
        //学期信息的显示位置
        var term_item: TextView = theActivity!!.findViewById<TextView>(termInfoShowPlace!!)
        var term_text:String = CREP.term.chineseShortName+ " 第" + currentWeek + "周"
        term_item.setText(term_text)

        //年月期显示位置
        var date_item: TextView = theActivity!!.findViewById<TextView>(dateInfoShowPlace!!)
        var date_text:String = "" + currentYear + "年" + currentMonth + "月"
        date_item.setText(date_text)

        //更新日显示
        for(week_day in DayOfWeek.values()) {
            var show_place = theActivity!!.findViewById<TextView>(showDateID[week_day]!!)
            var the_date:LocalDate = allDates[week_day]!!
            var show_num:Int = the_date.dayOfMonth
            show_place.setText("" + show_num)
        }
    }



    /**
     * 描述：获取本周的所有日程时间段
     * 参数：日期
     * 返回：无
     */
    protected suspend fun getValidTimes() {
        for (week_num in DayOfWeek.values()) {
            var the_day: LocalDate = allDates[week_num]!!
            var the_list = listOf<LocalDate>(the_day)

            timeList[week_num] = CREP.findTimesByDays(the_list)
        }
    }


    /**
    * 描述：显示所有课程
    * 参数：无
    * 返回：无
    */
    public suspend fun showAllCourses() {
        if(theActivity == null)
        {
            return
        }
        clearOriginalCourses()
        for (day in DayOfWeek.values()) {
            for (course in timeList[day]!!.getNotNullValue()) {
                showOneItem(day, course)
            }
        }
    }

    abstract protected fun showOneItem(theWeekDay: DayOfWeek, theItem: CalendarTimeDataWithItem);

    /**
     * 描述：清除之前显示的课程view
     * 参数：无
     * 返回：无
     */
    protected fun clearOriginalCourses() {
        for (day in DayOfWeek.values()) {
            var viewID: Int = showPlaceID[day]!!
            var dayView = theActivity!!.findViewById<RelativeLayout>(viewID)
            dayView.removeAllViews()
        }
    }

    /**
    * 描述：显示某个日程时间段（大节）
    * 参数：这个时间段在周几，这个时间段的信息
    * 返回：绑定的view
    */
    protected fun showOneCourse(theWeekDay: DayOfWeek, theCourse: CalendarTimeDataWithItem): View {


        var viewID: Int = showPlaceID[theWeekDay]!!
        var dayView = theActivity!!.findViewById<RelativeLayout>(viewID)
        var startBig: Int = theCourse.timeInCourseSchedule!!.startBig
        var startSmall: Float =
            CREP.timeRule.getStartSmallIndex(startBig) + theCourse.timeInCourseSchedule!!.startOffsetSmall
        var intevalSmall: Float = theCourse.timeInCourseSchedule!!.lengthSmall
        var startHeight: Float = LayoutConstants.HeightPerSmall * startSmall
        var theHeight: Int = (LayoutConstants.HeightPerSmall * intevalSmall).toInt()

        var v: View = LayoutInflater.from(theActivity!!).inflate(R.layout.course_item, null); //加载单个课程布局


        v.setY(startHeight); //设置开始高度,即第几节课开始

        var params: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, theHeight)
        v.setLayoutParams(params);

        var theTextView = v.findViewById<TextView>(R.id.text_view);
        var sub_name:String = theCourse.name
        var main_name:String = theCourse.calendarItem.name
        theTextView.setText(main_name + sub_name); //显示课程名        dayView.addView(v);

        //设置v的id和绑定时间处理函数
        v.id = theCourse.calendarItem.id
        v.setOnClickListener(View.OnClickListener() {
            var id: Int = v.id
            var intent = Intent(theActivity!!, ItemShowActivity::class.java)
            intent.putExtra(EXTRA_MESSAGE, id)
            theActivity!!.startActivity(intent)
        })


        dayView.addView(v);
        return v
    }


    /**
    *描述：显示某个日程时间段（小时）
    *参数：这个时间段在周几，这个时间段的信息
    *返回：绑定的view
    */
    protected fun showOneHour(theWeekDay: DayOfWeek, theItem: CalendarTimeDataWithItem): View {

        var viewID: Int = showPlaceID[theWeekDay]!!
        var dayView = requireActivity().findViewById<RelativeLayout>(viewID)

        var startTime: LocalTime? = null
        var endTime: LocalTime? = null
        //大节转小时
        if (theItem.timeInCourseSchedule != null) {
            var hourTime = theItem.timeInCourseSchedule!!.toTimeInHour()
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
        var sub_name:String = theItem.name
        var main_name:String = theItem.calendarItem.name
        theTextView.setText(main_name + sub_name); //显示课程名

        //设置v的id和绑定时间处理函数
        v.id = theItem.calendarItem.id
        v.setOnClickListener(View.OnClickListener() {
                var id: Int = v.id
                var intent = Intent(theActivity!!, ItemShowActivity::class.java)
                intent.putExtra(EXTRA_MESSAGE, id)
                theActivity!!.startActivity(intent)
        })

        dayView.addView(v)
        return v
    }

    /**
    *描述：根据时间求得在小时表的y坐标
    *参数：时间
    *返回：y坐标
    */
    protected fun GetPlaceByDuration(time:Duration):Float {
        var place:Float = 0.0F
        place = (time.seconds * LayoutConstants.HeightPerHour).toFloat() / 3600
        return place
    }



}

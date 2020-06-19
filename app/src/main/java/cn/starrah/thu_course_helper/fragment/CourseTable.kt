package cn.starrah.thu_course_helper.fragment

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.TableFragment
import cn.starrah.thu_course_helper.activity.ItemEditActivity
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeDataWithItem
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarItemType
import cn.starrah.thu_course_helper.data.declares.time.TimeInCourseSchedule
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.DayOfWeek


class CourseTable : TableFragment() {



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        theActivity = requireActivity()
        initSettings()
        var view:View? = null
        if(showType == showTypeCourse) {
            view = inflater.inflate(R.layout.table_class, container, false)
        }
        else{
            view = inflater.inflate(R.layout.table_hour, container, false)
        }
        return view
    }

    /**
     * 描述：加载设置--显示方式和显示天数，在oncreateview调用
     * 参数：无
     * 返回：无
     */
    @SuppressLint("ResourceType")
    override fun initSettings() {

        //加载常量字符串
        showDayFive = resources.getString(R.string.table_show_day_number_5d).toString();
        showDaySeven = resources.getString(R.string.table_show_day_number_7d).toString();
        showTypeCourse = resources.getString(R.string.settings_course_show_type_course).toString();
        showTypeHour = resources.getString(R.string.settings_course_show_type_hour).toString();

        var prefs:SharedPreferences = PreferenceManager.getDefaultSharedPreferences(theActivity)

        showType = prefs.getString("course_show_type", showTypeCourse).toString()
        var showDaysString = prefs.getString("course_show_days", showDayFive).toString()
        if(showDaysString.equals(showDayFive)) {
            showDays = 5
        }
        else {
            showDays = 7
        }
    }

    /**
     * 描述：初始化左侧大节文字
     * 参数：无
     * 返回：无
     * @sample "第一\n大节\n08:00-\n09:35"
     */
    private fun initalizeClassText() {
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
        if(showType == showTypeCourse) {
            initializeLeftCourse()
            initalizeClassText()
        }
        else
        {
            initializeLeftHour()
        }
        initializeListWidth()

        var add_button: FloatingActionButton = theActivity!!.findViewById<FloatingActionButton>(R.id.add_item)
        add_button.setVisibility(View.VISIBLE)
        add_button.setOnClickListener(View.OnClickListener {
            var intent = Intent(theActivity!!, ItemEditActivity::class.java)
            intent.putExtra(EXTRA_MESSAGE, -1)
            theActivity!!.startActivity(intent)
        })

    }

    /**
     * 描述：画所有横线
     * 参数：无
     * 返回：无
     */
    override fun drawStrokes() {
        if(showType == showTypeCourse) {
            drawStrokesCourse()
        }
        else
        {
            drawStrokesHour()
        }
    }

    /**
    描述：显示某个日程时间段
    参数：这个时间段在周几，这个时间段的信息
    返回：无
    */
    override fun showOneItem(theWeekDay: DayOfWeek, theItem: CalendarTimeDataWithItem) {
        var v:View? = null;
        //只显示能大节显示的课程
        if(theItem.calendarItem.type != CalendarItemType.COURSE) {
            return
        }
        if (showType == showTypeCourse) {
            if (theItem.timeInCourseSchedule == null) {
                var time: TimeInCourseSchedule = theItem.timeInHour!!.toTimeInCourseSchedule()
                theItem.timeInCourseSchedule = time
                v = showOneCourse(theWeekDay, theItem)
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

    }

    /**
     * 描述：修改当前周并且存到shared preference里
     * 参数：当前周
     * 返回：无
     */
    protected override fun changeCurrentWeek(week: Int) {
        val sp = PreferenceManager.getDefaultSharedPreferences(theActivity!!)
        if(week <= 0 || week > CREP.term.normalWeekCount + CREP.term.examWeekCount) {
            setWeekToday()
        }
        else {
            currentWeek = week
        }
        sp.edit {
            putInt("currentWeekCourseTable", currentWeek)
        }
    }

    override fun onStart() {
        val sp = PreferenceManager.getDefaultSharedPreferences(theActivity!!)
        var current_week = sp.getInt("currentWeekCourseTable", 0)
        if(current_week <= 0 || current_week > CREP.term.normalWeekCount + CREP.term.examWeekCount) {
            setWeekToday()
        }
        else {
            currentWeek = current_week
        }

        super.onStart()
    }


    /**
     * 描述：设置初始位置
     * 参数：无
     * 返回：无
     */
    override fun setOriginalPlace() {
        if(showType == showTypeCourse) {
            setOriginalPlaceCourse()
        }
        else
        {
            setOriginalPlaceHour()
        }
    }

}
package cn.starrah.thu_course_helper.fragment

import android.annotation.SuppressLint
import cn.starrah.thu_course_helper.R
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import cn.starrah.thu_course_helper.TableFragment
import cn.starrah.thu_course_helper.activity.ItemEditActivity
import cn.starrah.thu_course_helper.activity.ItemShowActivity
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeDataWithItem
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.DayOfWeek


class TimeTable : TableFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        theActivity = requireActivity()
        initSettings()
        var view:View = inflater.inflate(R.layout.table_hour, container, false)

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

        var prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(theActivity)

        showType = showTypeHour
        var showDaysString = prefs.getString("time_show_days", showDayFive).toString()
        if(showDaysString.equals(showDayFive)) {
            showDays = 5
        }
        else {
            showDays = 7
        }
    }

    /**
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
        var add_button: FloatingActionButton = theActivity!!.findViewById<FloatingActionButton>(R.id.add_item)
        add_button.setVisibility(View.VISIBLE)
        add_button.setOnClickListener(View.OnClickListener {
            var intent = Intent(theActivity!!, ItemEditActivity::class.java)
            intent.putExtra(EXTRA_MESSAGE, -1)
            theActivity!!.startActivity(intent)
        })
    }


    /**
    描述：显示某个日程时间段
    参数：这个时间段在周几，这个时间段的信息
    返回：无
    */
    override fun showOneItem(theWeekDay: DayOfWeek, theItem: CalendarTimeDataWithItem) {
        var v:View? = null;
        v = showOneHour(theWeekDay, theItem)
    }

    /**
     * 描述：画所有横线
     * 参数：无
     * 返回：无
     */
    override fun drawStrokes() {
        drawStrokesHour()
    }

}
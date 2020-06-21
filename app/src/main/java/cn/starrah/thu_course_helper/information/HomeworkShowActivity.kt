package cn.starrah.thu_course_helper.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.activity.ItemEditActivity
import cn.starrah.thu_course_helper.activity.ItemShowActivity
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarItemDataWithTimes
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeData
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarRemindType
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarTimeType
import cn.starrah.thu_course_helper.data.utils.chineseName
import kotlinx.android.synthetic.main.calendar_time_edit.*
import kotlinx.coroutines.launch
import java.lang.Exception
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class HomeworkShowActivity: AppCompatActivity() {
    private var showItem: ArrayList<CalendarItemDataWithTimes> = arrayListOf()

    /**
     * 描述：初始化
     * @param savedInstanceState 存储的data
     */
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homework_show)
        var loading_bar = findViewById<LinearLayout>(R.id.login_bar_place)
        lifecycleScope.launch {
            try {
                getData()
            }
            catch (e: Exception) {
                Toast.makeText(this@HomeworkShowActivity, e.message, Toast.LENGTH_LONG)
                    .show()
            }
            finally {
                ItemEditActivity.HideItem(loading_bar)
            }
            sortData()
            showData()
        }
    }


    /**
     * 描述：从数据库获取数据，读取的数据存在showItem里
     * 参数：无
     * 返回：无
     */
    suspend fun getData() {
        //数据获取
        var the_item = CREP.helper_findHomeworkItems(this)
        showItem.addAll(the_item)
    }

    /**
     * 描述：将待显示数据排序,按照时间升序，而且未完成数据更靠前
     * 参数：无
     * 返回：无
     */
    fun sortData() {
        var item_unfinished: ArrayList<CalendarItemDataWithTimes> = arrayListOf()
        var item_finished: ArrayList<CalendarItemDataWithTimes> = arrayListOf()
        for(item in showItem) {
            if(item.times.get(0).comment.equals("未提交")) {
                item_unfinished.add(item)
            }
            else {
                item_finished.add(item)
            }
        }
        item_unfinished.sortBy { LocalDateTime.of(it.times.get(0).timeInHour!!.date, it.times.get(0).timeInHour!!.startTime) }
        item_finished.sortBy { LocalDateTime.of(it.times.get(0).timeInHour!!.date, it.times.get(0).timeInHour!!.startTime) }
        showItem.clear()
        showItem.addAll(item_unfinished)
        showItem.addAll(item_finished)
    }

    /**
     * 描述:显示数据，包括上方，名称和每一个单一日程
     * 参数：无
     * 返回：无
     */
    fun showData() {
        var parent_place = findViewById<LinearLayout>(R.id.new_homework_place_show)
        parent_place.removeAllViews()
        for(item in showItem) {
            var the_date = item.times.get(0).timeInHour!!.date
            var the_time = item.times.get(0).timeInHour!!.startTime
            var the_date_time = LocalDateTime.of(the_date, the_time)
            if (the_date_time.isAfter(LocalDateTime.now()) ) {
                showOneItem(item, parent_place)
            }
        }
    }

    /**
     * 描述：根据showItem的具体时间段信息修改显示界面
     * 参数：具体时间段信息time, 父亲节点位置
     * 返回：无
     */
    fun showOneItem(item:CalendarItemDataWithTimes, parent_place: LinearLayout) {
        val layout = LayoutInflater.from(this).inflate(R.layout.homework_show_item, null);
        var time:CalendarTimeData = item.times.get(0)
        var show_name: TextView = layout.findViewById(R.id.time_show_name)
        var show_course_name:TextView = layout.findViewById(R.id.time_show_course_name)
        var show_date: TextView = layout.findViewById(R.id.time_show_date)
        var show_time: TextView = layout.findViewById(R.id.time_show_time)

        //名称
        var name_string:String = item.name
        name_string = name_string + "(" + time.comment + ")"
        show_name.setText(name_string)


        //课程名
        show_course_name.setText(time.name)

        //日期和时间
        assert(time.type == CalendarTimeType.POINT)
        //时间---周五9:00
        var schedule = time.timeInHour
        var time_string:String = ItemEditActivity.getTimeString(schedule!!.startTime)
        show_time.setText(time_string)

        //日期--5月22日
        var the_date_year = schedule.date!!.year
        var the_date_month = schedule.date!!.month.value
        var the_date_day = schedule.date!!.dayOfMonth
        var date_string:String = "" + the_date_year + "年" + the_date_month + "月" + the_date_day + "日"
        show_date.setText(date_string)


        //添加
        parent_place.addView(layout)
    }

    /**
     * 描述：处理返回按钮的事件--返回
     * 参数：无
     * 返回：无
     */
    fun handleReturn(view: View) {
        finish()
    }
}
package cn.starrah.thu_course_helper.information

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.TableFragment
import cn.starrah.thu_course_helper.activity.ItemEditActivity
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarItemData
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarItemDataWithTimes
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeData
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeDataWithItem
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarItemLegalDetailKey
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarItemType
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarRemindType
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarTimeType
import cn.starrah.thu_course_helper.data.utils.chineseName
import cn.starrah.thu_course_helper.data.utils.getNotNullValue
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ExamShowActivity : AppCompatActivity(){

    private var showItem: ArrayList<CalendarTimeDataWithItem> = arrayListOf()

    /**
     * 描述：初始化
     * @param savedInstanceState 存储的data
     */
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.exam_show)

        lifecycleScope.launch {
            getData()
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
        var the_item = CREP.helper_findFinalExamTimes(this)
        showItem.addAll(the_item)
    }

    /**
     * 描述：将待显示数据排序,按照时间升序
     * 参数：无
     * 返回：无
     */
    private fun sortData() {
        showItem.sortBy { LocalDateTime.of(it.timeInHour!!.date, it.timeInHour!!.startTime)}
    }

    /**
     * 描述:显示数据，包括上方，名称和每一个单一日程
     * 参数：无
     * 返回：无
     */
    fun showData() {
        var parent_place = findViewById<LinearLayout>(R.id.new_exam_place_show)
        parent_place.removeAllViews()
        for(item in showItem) {
            showOneItem(item, parent_place)
        }
    }

    /**
     * 描述：根据showItem的具体时间段信息修改显示界面
     * 参数：具体时间段信息time, 父亲节点位置
     * 返回：无
     */
    fun showOneItem(time:CalendarTimeDataWithItem, parent_place: LinearLayout) {
        val layout = LayoutInflater.from(this).inflate(R.layout.exam_show_item, null);
        var item:CalendarItemData = time.calendarItem
        var show_name: TextView = layout.findViewById(R.id.time_show_name)
        var show_date: TextView = layout.findViewById(R.id.time_show_date)
        var show_time: TextView = layout.findViewById(R.id.time_show_time)
        var show_place:TextView = layout.findViewById(R.id.time_show_place)

        //名称
        var name_string:String = item.name + time.name
        show_name.setText(name_string)


        //日期和时间
        assert(time.type == CalendarTimeType.SINGLE_HOUR)
        //时间---周五9:00
        var schedule = time.timeInHour
        var start_string:String = ItemEditActivity.getTimeString(schedule!!.startTime)
        var end_string:String = ItemEditActivity.getTimeString(schedule!!.endTime)
        var time_string:String = start_string + "-" + end_string
        show_time.setText(time_string)

        //日期--5月22日
        var the_date_year = schedule.date!!.year
        var the_date_month = schedule.date!!.month.value
        var the_date_day = schedule.date!!.dayOfMonth
        var date_string:String = "" + the_date_year + "年" + the_date_month + "月" + the_date_day + "日"
        show_date.setText(date_string)

        //地点
        show_place.setText(time.place)

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
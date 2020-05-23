package cn.starrah.thu_course_helper.activity

import cn.starrah.thu_course_helper.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
//import butterknife.Bind
//import butterknife.ButterKnife
import cn.starrah.thu_course_helper.TableFragment
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarItemDataWithTimes
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeData
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarItemLegalDetailKey
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarItemType
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarTimeType
import cn.starrah.thu_course_helper.data.utils.chineseName
import cn.starrah.thu_course_helper.data.utils.getNotNullValue
import kotlinx.coroutines.launch
import org.w3c.dom.Text


/**
 * 描述：显示某个日程和全部的activity类
 */
class ItemShowActivity : AppCompatActivity(){

    private var showItem: CalendarItemDataWithTimes? = null;
    private var showID:Int = -1;
    private val showDefault:String = "暂无"

    /**
     * 描述：初始化
     * @param savedInstanceState 存储的data，其实只有待显示活动的ID
     */
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_item_show_main)
        //ButterKnife.bind(this)

        val intent = intent
        val message = intent.getIntExtra(TableFragment.EXTRA_MESSAGE, -1)
        if(message < 0) {
            //TODO:异常处理
            finish()
        }
        showID = message
        lifecycleScope.launch {
            getData()
            showData()
        }
    }

    /**
     * 描述：根据id从数据库获取数据，id是类变量showID，读取的数据存在showItem里
     * 参数：无
     * 返回：无
     */
    suspend fun getData() {
        //数据获取
        var list: List<Int> = listOf(showID)
        var the_item = CREP.findItemsByIds(list)
        var size = the_item.getNotNullValue().size
        if(size <= 0 || size > 1) {
            //TODO:异常处理--未找到数据
            finish()
        }
        showItem = the_item.getNotNullValue()[0]
        if(showItem == null) {
            //TODO:异常处理--未找到数据
            finish()
        }
    }

    /**
     * 描述：根据读取的数据showItem修改显示界面
     * 参数：无
     * 返回：无
     */
    suspend fun showData() {

        //名称
        var item_name:String = showItem!!.name
        var item_show_name:TextView = findViewById(R.id.item_show_name)
        item_show_name.setText(item_name)
        var item_show_top:TextView = findViewById(R.id.item_show_top)
        item_show_top.setText(item_name)


        //类别
        var item_type:CalendarItemType = showItem!!.type
        var item_type_show:String = item_type.chineseName
        var show_type:TextView = findViewById(R.id.item_show_type)
        show_type.setText(item_type_show)

        //教师，课程号，detail
        var detail_place = findViewById<FrameLayout>(R.id.item_show_info)
        if(item_type == CalendarItemType.COURSE) {
            val layout = LayoutInflater.from(this).inflate(R.layout.calendar_item_show_course, null);

            var item_teacher:String? = showItem!!.detail[CalendarItemLegalDetailKey.TEACHER]
            if(item_teacher == null) {
                item_teacher = showDefault
            }
            var show_teacher:TextView = layout.findViewById(R.id.item_show_teacher)
            show_teacher.setText(item_teacher)

            var item_course_id:String? = showItem!!.detail[CalendarItemLegalDetailKey.COURSEID]
            if(item_course_id == null) {
                item_course_id = showDefault
            }
            var show_course_id:TextView = layout.findViewById(R.id.item_show_course_id)
            show_course_id.setText(item_course_id)

            detail_place.addView(layout)

        }
        else if(item_type == CalendarItemType.SOCIALWORK || item_type == CalendarItemType.ASSOCIATION) {
            val layout =
                LayoutInflater.from(this).inflate(R.layout.calendar_item_show_social, null);

            var item_association: String? =
                showItem!!.detail[CalendarItemLegalDetailKey.ORGANIZATION]
            if (item_association == null) {
                item_association = showDefault
            }
            var show_teacher: TextView = layout.findViewById(R.id.item_show_teacher)
            show_teacher.setText(item_association)

            detail_place.addView(layout)

        }

        //详情
        var item_comment:String? = showItem!!.detail[CalendarItemLegalDetailKey.COMMENT]
        var show_comment:TextView = findViewById(R.id.item_show_comment)
        if(item_comment == null) {
            item_comment = ""
        }
        show_comment.setText(item_comment)

        //具体下面
        var parent_place = findViewById<LinearLayout>(R.id.new_time_place_show)
        parent_place.removeAllViews()
        for(time in showItem!!.times) {
            showOneTime(time, parent_place)
        }
    }



    /**
     * 描述：根据showItem的具体时间段信息修改显示界面
     * 参数：具体时间段信息time, 父亲节点位置
     * 返回：无
     */
    suspend fun showOneTime(time:CalendarTimeData, parent_place:LinearLayout) {
        val layout = LayoutInflater.from(this).inflate(R.layout.calendar_time_show, null);


        //名称
        var time_name:String = time.name
        var show_name:TextView = layout.findViewById(R.id.time_show_name)
        show_name.setText(time_name)

        var show_date:TextView = layout.findViewById(R.id.time_show_date)
        var show_time:TextView = layout.findViewById(R.id.time_show_time)

        //日期和时间
        if(time.type == CalendarTimeType.REPEAT_COURSE) {
            //时间---周三第三大节
            var schedule = time.timeInCourseSchedule
            var day_time:String = schedule!!.chineseName
            show_time.setText(day_time)

            //日期---后八周
            var week_list = time.repeatWeeks
            var week_show = getWeeksString(week_list)
            show_date.setText(week_show)

        }
        else if(time.type == CalendarTimeType.REPEAT_HOUR) {
            //时间---周三9:00-10:00
            var schedule = time.timeInHour
            var start_time:String = "" + schedule!!.startTime.hour + ":" + schedule!!.startTime.minute
            var end_time:String = "" + schedule!!.endTime.hour + ":" + schedule!!.endTime.minute
            var week_day = schedule!!.dayOfWeek
            var week_day_string:String = week_day!!.chineseName
            var day_time:String = week_day_string + start_time  + "-" + end_time
            show_time.setText(day_time)

            //日期--后八周
            var week_list = time.repeatWeeks
            var week_show = getWeeksString(week_list)
            show_date.setText(week_show)
        }
        else if(time.type == CalendarTimeType.SINGLE_COURSE) {
            //时间---周五第三大节
            var schedule = time.timeInCourseSchedule
            var day_time:String = schedule!!.chineseName
            show_time.setText(day_time)

            //日期--第14周周五（5月22日）
            var the_date = schedule.date
            var time_month = the_date!!.month.value
            var time_day = the_date!!.dayOfMonth
            var the_week_num = CREP.term.dateToWeekNumber(the_date)
            var the_week_day = the_date.dayOfWeek.chineseName
            var date_string = "第" + the_week_num + "周" + the_week_day + "(" + time_month + "月" + time_day + "日)"
            show_date.setText(date_string)
        }
        else if(time.type == CalendarTimeType.SINGLE_HOUR) {
            //时间---周五9:00-10:00
            var schedule = time.timeInHour
            var start_time:String = "" + schedule!!.startTime.hour + ":" + schedule!!.startTime.minute
            var end_time:String = "" + schedule!!.endTime.hour + ":" + schedule!!.endTime.minute
            var week_day = schedule!!.dayOfWeek
            var week_day_string:String = week_day!!.chineseName
            var day_time:String = week_day_string + start_time  + "-" + end_time
            show_time.setText(day_time)

            //日期--5月22日
            var the_date_year = schedule.date!!.year
            var the_date_month = schedule.date!!.month.value
            var the_date_day = schedule.date!!.dayOfMonth
            var date_string:String = "" + the_date_year + "年" + the_date_month + "月" + the_date_day + "日"
            show_date.setText(date_string)
        }
        else if(time.type == CalendarTimeType.POINT) {
            //时间---周五9:00
            var schedule = time.timeInHour
            var time:String = "" + schedule!!.startTime.hour + ":" + schedule!!.startTime.minute
            var week_day = schedule!!.dayOfWeek
            var week_day_string:String = week_day!!.chineseName
            var day_time:String = week_day_string + time
            show_time.setText(day_time)

            //日期--5月22日
            var the_date_year = schedule.date!!.year
            var the_date_month = schedule.date!!.month.value
            var the_date_day = schedule.date!!.dayOfMonth
            var date_string:String = "" + the_date_year + "年" + the_date_month + "月" + the_date_day + "日"
            show_date.setText(date_string)
        }

        //地点
        var time_place:String = time.place
        var show_place:TextView = layout.findViewById(R.id.time_show_place)
        show_place.setText(time_place)

        //说明
        var time_comment:String = time.comment
        var show_comment:TextView = layout.findViewById(R.id.time_show_comment)
        show_comment.setText(time_comment)

        //添加
        parent_place.addView(layout)
    }

    /**
     * 描述：将周列表转换成字符串
     * @param ：int类型列表, 代表周列表
     * @return ：显示的字符串
     * @see ：全周，前半学期，后半学期，单周，双周，考试周，第1,2,3,4,6周，etc
     */
    fun getWeeksString(week_list:MutableList<Int>):String {
        //先map映射，去重+排序
        var week_map:MutableMap<Int, Boolean> = mutableMapOf()
        for(item in week_list) {
            week_map[item] = true
        }
        var total_weeks = CREP.term.totalWeekCount
        var normal_weeks = CREP.term.normalWeekCount

        //一个filter，用于筛选全周，前八周，后八周，单，双周，考试周
        //有考试周--非考试周的全false，有正常周的，考试周false
        var i = 1
        var week_list:MutableList<Int> = mutableListOf()
        var whether_full = true
        var whether_first_eight = true
        var whether_last_eight = true
        var whether_single = true
        var whether_double = true
        var whether_exam = true
        while(i <= total_weeks) {
            var result:Boolean? = week_map[i]

            if(result == true) {
                if(i <= normal_weeks) {
                    whether_exam = false
                }
                else {
                    whether_full = false
                    whether_first_eight = false
                    whether_last_eight = false
                    whether_single = false
                    whether_double = false
                }
                week_list.add(i)
            }
            else {
                if(i <= normal_weeks) {
                    whether_full = false
                }
                if(i <= normal_weeks && i <= normal_weeks / 2) {
                    whether_first_eight = false
                }
                if(i <= normal_weeks && i > normal_weeks / 2) {
                    whether_last_eight = false
                }
                if(i <= normal_weeks && i % 2 == 1) {
                    whether_single = false
                }
                if(i <= normal_weeks && i % 2 == 0) {
                    whether_double = false
                }
                if(i > normal_weeks) {
                    whether_exam = false
                }
            }
            i ++
        }

        //判断是否是几种特殊情况
        var result:String = ""
        if(whether_full) {
            result = "全周"
        }
        else if(whether_first_eight) {
            result = "前半学期"
        }
        else if(whether_last_eight) {
            result = "后半学期"
        }
        else if(whether_single) {
            result = "单周"
        }
        else if(whether_double) {
            result = "双周"
        }
        else if(whether_exam) {
            result = "考试周"
        }
        else if(week_list.size <= 0) {
            result = "空"
        }
        else {
            result = "第"
            for(i in week_list.indices) {
                result = result + week_list[i]
                if(i != week_list.size - 1) {
                    result = result + ","
                }
            }
            result = result + "周"
        }
        return result
    }


    /**
     * 描述：处理返回按钮的事件--返回
     * 参数：无
     * 返回：无
     */
    fun handleReturn(view: View) {
        finish()
    }

    /**
     * 描述：处理编辑按钮的事件--跳转到编辑页面
     * 参数：无
     * 返回：无
     * TODO
     */
    fun handleEdit(view: View) {

    }

    /**
     * 描述：处理提醒按钮的事件--跳转到提醒界面
     * 参数：无
     * 返回：无
     * TODO
     */
    fun handleRemind(view: View) {

    }

    /**
     * 描述：处理删除按钮的事件--删除当前内容，跳转回去
     * 参数：无
     * 返回：无
     */
    fun handleDelete(view: View) {
        lifecycleScope.launch{
            CREP.deleteItem(showItem!!)
            finish()
        }
    }
}
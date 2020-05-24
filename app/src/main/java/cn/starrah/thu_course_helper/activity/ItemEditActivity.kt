package cn.starrah.thu_course_helper.activity

//import butterknife.Bind
//import butterknife.ButterKnife

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarItemDataWithTimes
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeData
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarItemLegalDetailKey
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarItemType
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarTimeType
import cn.starrah.thu_course_helper.data.utils.chineseName
import cn.starrah.thu_course_helper.data.utils.getNotNullValue
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectChangeListener
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.TimePickerView
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import java.time.LocalDate
import java.time.LocalTime
import java.util.*


/**
 * 描述：编辑/新建日程活动
 */
class ItemEditActivity : AppCompatActivity(){

    //当前的元素
    private var currentItem: CalendarItemDataWithTimes? = null;

    //当前元素的id，如果没有就-1
    private var currentID:Int = -1;

    //时间选择器（滚轮），用来选择时间
    private lateinit var pvTime: TimePickerView

    companion object {

        /**
         * 描述：将时间转换成 08:00 这种形式
         */
        public fun getTimeString(time:LocalTime):String {
            var hour = "" + time.hour
            var minute = "" + time.minute
            if(hour.length != 2) {
                hour = "0" + hour
            }
            if(minute.length != 2) {
                minute = "0" + minute
            }
            return hour + ":" + minute
        }
    }


    /**
     *描述：隐藏控件
     *参数：id， 父亲(null代表自己）
     *返回：无
     */
    private fun HideItem(ID:Int, parent:View?) {
        val view: LinearLayout
        if(parent == null) {
            view = findViewById(ID)
        }
        else {
            view = parent.findViewById(ID)
        }
        var params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , 0);
        view.layoutParams = params
    }

    /**
     *描述：显示控件
     *参数：id， 父亲
     *返回：无
     */
    private fun ShowItem(ID:Int, parent: View?) {
        val view: LinearLayout
        if(parent == null) {
            view = findViewById(ID)
        }
        else {
            view = parent.findViewById(ID)
        }
        //和style一致
        var params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , 40);
        params.height = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            60f,
            resources.displayMetrics
        ).toInt()
        view.layoutParams = params
    }

    /**
     * 描述：初始化
     * @param savedInstanceState 存储的data，其实只有待编辑活动的ID，如果找不到intent就是新建，弄一个新的
     */
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_item_edit)

        val intent = intent
        val message = intent.getIntExtra(ItemShowActivity.EXTRA_MESSAGE, -1)
        currentID = message


        initTimePicker();


        lifecycleScope.launch {
            //数据获取
            if(currentID < 0) {
                getInitData()
            }
            else {
                getData()
            }
            //初始化界面
            showData()

        }

    }

    /**
     * 描述：新建日程的时候，建立一个新id
     * 参数：无
     * 返回：无
     */
    suspend fun getInitData() {
        currentItem = CalendarItemDataWithTimes()
    }

    /**
     * 描述：根据id从数据库获取数据，id是类变量showID，读取的数据存在showItem里
     * 参数：无
     * 返回：无
     */
    suspend fun getData() {
        var list: List<Int> = listOf(currentID)
        var the_item = CREP.findItemsByIds(list)
        var size = the_item.getNotNullValue().size
        if(size <= 0 || size > 1) {
            //TODO:异常处理--未找到数据
            finish()
        }
        currentItem = the_item.getNotNullValue()[0]
        if(currentItem == null) {
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
        var item_name:String = currentItem!!.name
        var item_edit_name: EditText = findViewById(R.id.item_edit_name)
        item_edit_name.setText(item_name)


        //类别
        var item_type:CalendarItemType = currentItem!!.type
        var item_type_string: String = currentItem!!.type.chineseName
        var edit_type: TextView = findViewById(R.id.item_edit_type)
        edit_type.text = item_type_string

        //教师，课程号，detail
        if(item_type == CalendarItemType.COURSE) {
            //教师，课程号显示，其余隐藏
            ShowItem(R.id.item_edit_teacher_place, null)
            ShowItem(R.id.item_edit_course_id_place, null)
            HideItem(R.id.item_edit_association_place, null)

            //设置教师，课程号初值
            var item_teacher:String? = currentItem!!.detail[CalendarItemLegalDetailKey.TEACHER]
            if(item_teacher == null) {
                item_teacher = ""
            }
            var edit_teacher: EditText = findViewById(R.id.item_edit_teacher)
            edit_teacher.setText(item_teacher)

            var item_course_id:String? = currentItem!!.detail[CalendarItemLegalDetailKey.COURSEID]
            if(item_course_id == null) {
                item_course_id = ""
            }

            var edit_course_id: EditText = findViewById(R.id.item_edit_course_id)
            edit_course_id.setText(item_course_id)
        }
        else if(item_type == CalendarItemType.SOCIALWORK || item_type == CalendarItemType.ASSOCIATION) {
            //组织显示，其余隐藏
            HideItem(R.id.item_edit_teacher_place, null)
            HideItem(R.id.item_edit_course_id_place, null)
            ShowItem(R.id.item_edit_association_place, null)


            //设置组织初值
            var item_association: String? =
                currentItem!!.detail[CalendarItemLegalDetailKey.ORGANIZATION]
            if (item_association == null) {
                item_association = ""
            }
            var edit_association: TextView = findViewById(R.id.item_edit_association)
            edit_association.setText(item_association)

        }
        else {
            //全隐藏
            HideItem(R.id.item_edit_teacher_place, null)
            HideItem(R.id.item_edit_course_id_place, null)
            HideItem(R.id.item_edit_association_place, null)
        }

        //详情
        var item_comment:String? = currentItem!!.detail[CalendarItemLegalDetailKey.COMMENT]
        var edit_comment: EditText = findViewById(R.id.item_edit_comment)
        if(item_comment == null) {
            item_comment = ""
        }
        edit_comment.setText(item_comment)


        //具体下面
        var parent_place = findViewById<LinearLayout>(R.id.new_time_place)
        parent_place.removeAllViews()
        for(time in currentItem!!.times) {
            showOneTime(time, parent_place)
        }
    }



    /**
     * 描述：根据showItem的具体时间段信息修改显示界面
     * 参数：具体时间段信息time, 父亲节点位置
     * 返回：无
     */
    suspend fun showOneTime(time: CalendarTimeData, parent_place:LinearLayout) {
        val layout = LayoutInflater.from(this).inflate(R.layout.calendar_time_edit, null);


        //名称
        var time_name:String = time.name
        var edit_name: EditText = layout.findViewById(R.id.time_edit_name)
        edit_name.setText(time_name)

        //地点
        var time_place:String = time.place
        var edit_place: EditText = layout.findViewById(R.id.time_edit_place)
        edit_place.setText(time_place)


        //类别
        var time_type: CalendarTimeType = time.type
        var time_type_edit:String = time_type.chineseName
        var edit_type: TextView = layout.findViewById(R.id.time_edit_time_type)
        edit_type.setText(time_type_edit)

        //日期等
        if(time_type == CalendarTimeType.REPEAT_COURSE || time_type == CalendarTimeType.REPEAT_HOUR) {
            //周，星期显示，日期隐藏
            ShowItem(R.id.time_edit_day_place, layout)
            ShowItem(R.id.time_edit_week_place, layout)
            HideItem(R.id.time_edit_date_place, layout)

            //设置周，星期初值
            var week_list = time.repeatWeeks
            var week_show:String = getWeeksString(week_list)
            var edit_week: TextView = layout.findViewById(R.id.time_edit_week)
            edit_week.setText(week_show)

            var day_in_week:String = ""
            if(time.timeInCourseSchedule != null) {
                day_in_week = time.timeInCourseSchedule!!.dayOfWeek!!.chineseName
            }
            else if(time.timeInHour != null){
                day_in_week = time.timeInHour!!.dayOfWeek!!.chineseName
            }


            var edit_day: TextView = layout.findViewById(R.id.time_edit_day)
            edit_day.setText(day_in_week)

        }
        else {
            //日期显示，周，星期隐藏
            HideItem(R.id.time_edit_day_place, layout)
            HideItem(R.id.time_edit_week_place, layout)
            ShowItem(R.id.time_edit_date_place, layout)

            //设置日期初值
            var date:LocalDate = LocalDate.now()
            if(time.timeInCourseSchedule != null) {
                date = time.timeInCourseSchedule!!.date!!
            }
            else if(time.timeInHour != null){
                date = time.timeInHour!!.date!!
            }
            var date_string = date.toString()
            var edit_date: TextView = layout.findViewById(R.id.time_edit_date)
            edit_date.setText(date_string)
        }


        //时间等
        if(time_type == CalendarTimeType.REPEAT_COURSE || time_type == CalendarTimeType.SINGLE_COURSE) {
            //开始大节，时长显示，开始时间，结束时间，时间隐藏
            ShowItem(R.id.time_edit_start_course_place, layout)
            ShowItem(R.id.time_edit_length_course_place, layout)
            HideItem(R.id.time_edit_start_hour_place, layout)
            HideItem(R.id.time_edit_end_hour_place, layout)
            HideItem(R.id.time_edit_point_place, layout)

            //设置初值
            var start_big = time.timeInCourseSchedule!!.startBig.toInt()
            var start_small = time.timeInCourseSchedule!!.startOffsetSmall.toInt()
            var start_string = "第" + start_big + "大节"+"第" + (start_small + 1) + "小节"
            var edit_start: TextView = layout.findViewById(R.id.time_edit_start_hour)
            edit_start.setText(start_string)




            var length_string:String = "" + time.timeInCourseSchedule!!.lengthSmall.toInt()
            var edit_length: TextView = layout.findViewById(R.id.time_edit_length_course)
            edit_length.setText(length_string)
        }
        else if(time_type == CalendarTimeType.REPEAT_HOUR || time_type == CalendarTimeType.SINGLE_HOUR){
            //开始时间，结束时间显示，开始大节，结束大节，时间隐藏
            HideItem(R.id.time_edit_start_course_place, layout)
            HideItem(R.id.time_edit_length_course_place, layout)
            ShowItem(R.id.time_edit_start_hour_place, layout)
            ShowItem(R.id.time_edit_end_hour_place, layout)
            HideItem(R.id.time_edit_point_place, layout)

            var start_string:String = getTimeString(time.timeInHour!!.startTime)
            var end_string:String = getTimeString(time.timeInHour!!.endTime)

            var edit_start: TextView = layout.findViewById(R.id.time_edit_start_hour)
            edit_start.setText(start_string)
            //绑定选择器
            edit_start.setOnClickListener(View.OnClickListener() {
                if (pvTime != null) {
                    // pvTime.setDate(Calendar.getInstance());
                    /* pvTime.show(); //show timePicker*/
                    pvTime.show(edit_start);//弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
                }
            })

            var edit_end: TextView = layout.findViewById(R.id.time_edit_end_hour)
            edit_end.setText(end_string)
            //绑定选择器
            edit_end.setOnClickListener(View.OnClickListener() {
                if (pvTime != null) {
                    // pvTime.setDate(Calendar.getInstance());
                    /* pvTime.show(); //show timePicker*/
                    pvTime.show(edit_end);//弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
                }
            })
        }
        else {
            //时间显示，其余隐藏
            HideItem(R.id.time_edit_start_course_place, layout)
            HideItem(R.id.time_edit_length_course_place, layout)
            HideItem(R.id.time_edit_start_hour_place, layout)
            HideItem(R.id.time_edit_end_hour_place, layout)
            ShowItem(R.id.time_edit_point_place, layout)
            var time_string:String = getTimeString(time.timeInHour!!.startTime)

            var edit_point: TextView = layout.findViewById(R.id.time_edit_point)
            edit_point.setText(time_string)
            //绑定选择器
            edit_point.setOnClickListener(View.OnClickListener() {
                if (pvTime != null) {
                    // pvTime.setDate(Calendar.getInstance());
                    /* pvTime.show(); //show timePicker*/
                    pvTime.show(edit_point);//弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
                }
            })
        }

        //说明
        var time_comment:String = time.comment
        var edit_comment: EditText = layout.findViewById(R.id.time_edit_comment)
        edit_comment.setText(time_comment)

        //添加
        //TODO 存储在一个数组里
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


    //选择类控件初始化函数
    /**
     * 描述：初始化时间选择器
     * 参数：无
     * 返回：无
     */
    private fun initTimePicker() { //Dialog 模式下，在底部弹出
        pvTime = TimePickerBuilder(this, object : OnTimeSelectListener {
            override fun onTimeSelect(date: Date?, v: View?) {
                Toast.makeText(this@ItemEditActivity, date!!.time.toString(), Toast.LENGTH_SHORT)
                    .show()
                Log.i("pvTime", "onTimeSelect")
            }
        })
            .setTimeSelectChangeListener(object : OnTimeSelectChangeListener {
                override fun onTimeSelectChanged(date: Date?) {
                    Log.i("pvTime", "onTimeSelectChanged")
                }
            })
            .setType(booleanArrayOf(true, true, true, true, true, true))
            .isDialog(true) //默认设置false ，内部实现将DecorView 作为它的父控件。
            .addOnCancelClickListener(object : View.OnClickListener {
                override fun onClick(view: View?) {
                    Log.i("pvTime", "onCancelClickListener")
                }
            })
            .setItemVisibleCount(5) //若设置偶数，实际值会加1（比如设置6，则最大可见条目为7）
            .setLineSpacingMultiplier(2.0f)
            .isAlphaGradient(true)
            .build()
        val mDialog: Dialog = pvTime.getDialog()
        if (mDialog != null) {
            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            )
            params.leftMargin = 0
            params.rightMargin = 0
            pvTime.getDialogContainerLayout().setLayoutParams(params)
            val dialogWindow: Window? = mDialog.getWindow()
            if (dialogWindow != null) {
                dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim) //修改动画样式
                dialogWindow.setGravity(Gravity.BOTTOM) //改成Bottom,底部显示
                dialogWindow.setDimAmount(0.3f)
            }
        }
    }


    //按钮绑定函数
    /**
     * 描述：处理返回按钮的事件--返回
     * 参数：无
     * 返回：无
     */
    fun handleReturn(view: View) {
        //TODO Dialog提示
        finish()
    }

    /**
     * 描述：处理保存按钮的事件--保存并且返回
     * 参数：无
     * 返回：无
     */
    fun handleSave(view: View) {
        //TODO
    }


}
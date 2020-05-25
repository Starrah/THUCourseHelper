package cn.starrah.thu_course_helper.activity

//import butterknife.Bind
//import butterknife.ButterKnife

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
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
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.bigkoo.pickerview.listener.OnTimeSelectChangeListener
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.OptionsPickerView
import com.bigkoo.pickerview.view.TimePickerView
import kotlinx.coroutines.launch
import java.time.*
import java.util.*


/**
 * 描述：编辑/新建日程活动
 */
class ItemEditActivity : AppCompatActivity(){

    //当前的元素
    private var currentItem: CalendarItemDataWithTimes? = null;

    //当前元素的id，如果没有就-1
    private var currentID:Int = -1;




    /**
     * 描述：用于记录所有时间段的标签信息
     * 编码规则：最上方：名称，类别，教师，课程号，组织，说明：0-5
     * 第i个下方标签：13*i + 6
     * 第i个标签的删除按钮：13*i + 7
     * 第i个标签的11个元素:13 * i + 8 --13 * i + 18
     */
    private var tagList:MutableList<Int> = mutableListOf()
    //父亲view的存储列表，和上方一一对应
    private var viewList:MutableList<View> = mutableListOf()
    //新view赋值的数字
    private var currentNum = 0

    /**
     * 描述：根据tag获取所需的father的view
     * 参数：tag
     * 返回：view
     */
    private fun getFatherViewByTag(tag: Int): View {
        var current_num:Int = (tag - 6) / 13
        var current_view:View? = null
        for(i in tagList.indices) {
            if(tagList[i] == current_num) {
                current_view = viewList[i]
                break
            }
        }
        return current_view!!
    }

    /**
     * 描述：根据tag获取所需的father的view
     * 参数：tag
     * 返回：view
     */
    private fun getTimeByTag(tag: Int): CalendarTimeData {
        var current_num:Int = (tag - 6) / 13
        var current_time:CalendarTimeData? = null
        for(i in tagList.indices) {
            if(tagList[i] == current_num) {
                current_time = currentItem!!.times[i]
                break
            }
        }
        return current_time!!
    }


    /**
     * 描述：添加一个新的下方栏
     * 参数：view
     * 返回：无
     */
    private fun addOneView(view:View) {
        var int_tag = view.tag as Int
        tagList.add((int_tag - 6) / 13)
        viewList.add(view)
    }

    /**
     * 描述：设置最上方几个元素的tag
     * 参数：无
     * 返回：无
     */
    private fun setBaseTags() {
        var item_edit_name: EditText = findViewById(R.id.item_edit_name)
        item_edit_name.setTag(0)
        var item_edit_type: TextView = findViewById(R.id.item_edit_type)
        item_edit_type.setTag(1)
        var item_edit_teacher: EditText = findViewById(R.id.item_edit_teacher)
        item_edit_teacher.setTag(2)
        var item_edit_course_id: EditText = findViewById(R.id.item_edit_course_id)
        item_edit_course_id.setTag(3)
        var item_edit_association: EditText = findViewById(R.id.item_edit_association)
        item_edit_association.setTag(4)
        var item_edit_comment: EditText = findViewById(R.id.item_edit_comment)
        item_edit_comment.setTag(5)
    }

    /**
     * 描述：设置下方某个元素tag
     * 参数：这个tag的view
     * 返回：无
     */
    private fun setSonTags(layout:View) {
        layout.setTag(currentNum * 13 + 6)
        var time_edit_name: EditText = layout.findViewById(R.id.time_edit_name)
        time_edit_name.setTag(currentNum * 13 + 7)
        var time_edit_place: EditText = layout.findViewById(R.id.time_edit_place)
        time_edit_place.setTag(currentNum * 13 + 8)
        var time_edit_type: TextView = layout.findViewById(R.id.time_edit_time_type)
        time_edit_type.setTag(currentNum * 13 + 9)
        var time_edit_week: TextView = layout.findViewById(R.id.time_edit_week)
        time_edit_week.setTag(currentNum * 13 + 10)
        var time_edit_day: TextView = layout.findViewById(R.id.time_edit_day)
        time_edit_day.setTag(currentNum * 13 + 11)
        var time_edit_date: TextView = layout.findViewById(R.id.time_edit_date)
        time_edit_date.setTag(currentNum * 13 + 12)
        var time_edit_start_course: TextView = layout.findViewById(R.id.time_edit_start_course)
        time_edit_start_course.setTag(currentNum * 13 + 13)
        var time_edit_length_course: TextView = layout.findViewById(R.id.time_edit_length_course)
        time_edit_length_course.setTag(currentNum * 13 + 14)
        var time_edit_start_hour: TextView = layout.findViewById(R.id.time_edit_start_hour)
        time_edit_start_hour.setTag(currentNum * 13 + 15)
        var time_edit_end_hour: TextView = layout.findViewById(R.id.time_edit_end_hour)
        time_edit_end_hour.setTag(currentNum * 13 + 16)
        var time_edit_point: TextView = layout.findViewById(R.id.time_edit_point)
        time_edit_point.setTag(currentNum * 13 + 17)
        var time_edit_comment: EditText = layout.findViewById(R.id.time_edit_comment)
        time_edit_comment.setTag(currentNum * 13 + 18)
        currentNum ++
    }

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
        var params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , 50);
        params.height = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            50f,
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


        initTimePicker()
        initDatePicker()

        getCourseOptionData()
        initCourseOptionPicker()
        initWeekDayOptionPicker()
        initLengthOptionPicker()

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

        //设置tag
        setBaseTags()

        //具体下面
        var parent_place = findViewById<LinearLayout>(R.id.new_time_place)
        parent_place.removeAllViews()
        for(time in currentItem!!.times) {
            val layout = LayoutInflater.from(this).inflate(R.layout.calendar_time_edit, null);
            showOneTime(time, layout!!)
            //设置新tag
            setSonTags(layout)
            parent_place.addView(layout)
            addOneView(layout)
        }
    }



    /**
     * 描述：根据showItem的具体时间段信息修改显示界面
     * 参数：具体时间段信息time, 父亲节点位置
     * 返回：无
     */
    suspend fun showOneTime(time: CalendarTimeData, layout: View) {

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
            edit_day.setOnClickListener(View.OnClickListener() {
                if (pvWeekDayOptions != null) {
                    pvWeekDayOptions.show(edit_day);//弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
                }
            })

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
            //绑定选择器
            edit_date.setOnClickListener(View.OnClickListener() {
                if (pvDate != null) {
                    pvDate.setDate(Calendar.getInstance());
                    pvDate.show(edit_date);//弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
                }
            })
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
            var start_string = "第" + start_big + "大节"
            if(start_small != 0) {
                start_string = start_string +"第" + (start_small + 1) + "小节"
            }
            var edit_start: TextView = layout.findViewById(R.id.time_edit_start_course)
            edit_start.text = start_string
            edit_start.setOnClickListener(View.OnClickListener() {
                if (pvCourseOptions != null) {
                    pvCourseOptions.show(edit_start);//弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
                }
            })



            var length_string:String = "" + time.timeInCourseSchedule!!.lengthSmall.toInt()
            var edit_length: TextView = layout.findViewById(R.id.time_edit_length_course)
            edit_length.setText(length_string)
            edit_length.setOnClickListener(View.OnClickListener() {
                if (pvLengthOptions != null) {
                    pvLengthOptions.show(edit_length);//弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
                }
            })

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

            var edit_start:TextView = layout.findViewById(R.id.time_edit_start_hour)
            //绑定选择器
            edit_start.setOnClickListener(View.OnClickListener() {
                if (pvTime != null) {
                    pvTime.setDate(Calendar.getInstance());
                    pvTime.show(edit_start);//弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
                }
            })
            edit_start.text = start_string


            var edit_end: TextView = layout.findViewById(R.id.time_edit_end_hour)
            //绑定选择器
            edit_end.setOnClickListener(View.OnClickListener() {
                if (pvTime != null) {
                    pvTime.setDate(Calendar.getInstance());
                    pvTime.show(edit_end);//弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
                }
            })
            edit_end.text = end_string
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
            //绑定选择器
            edit_point.setOnClickListener(View.OnClickListener() {
                if (pvTime != null) {
                    pvTime.setDate(Calendar.getInstance());
                    pvTime.show(edit_point);//弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
                }
            })
            edit_point.text = time_string

        }

        //说明
        var time_comment:String = time.comment
        var edit_comment: EditText = layout.findViewById(R.id.time_edit_comment)
        edit_comment.setText(time_comment)

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

    //时间选择器（滚轮），用来选择时间
    private lateinit var pvTime: TimePickerView

    //日期选择器
    private lateinit var pvDate: TimePickerView

    /**
     * 描述：初始化时间选择器---用于时间和时间节点选择
     * 参数：无
     * 返回：无
     */
    private fun initTimePicker() { //Dialog 模式下，在底部弹出
        pvTime = TimePickerBuilder(this, object : OnTimeSelectListener {
            override fun onTimeSelect(date: Date?, v: View?) {
                val instant: Instant = date!!.toInstant()
                val zone: ZoneId = ZoneId.systemDefault()
                val localDateTime: LocalDateTime = LocalDateTime.ofInstant(instant, zone)
                val localTime: LocalTime = localDateTime.toLocalTime()
                val string = getTimeString(localTime)

                val tag:Int = v!!.tag!! as Int
                var layout:View = getFatherViewByTag(tag)
                val current_time:CalendarTimeData = getTimeByTag(tag)
                if (tag % 13 == 2 || tag % 13 == 4) {
                    current_time.timeInHour!!.startTime = localTime
                }
                else if(tag % 13 == 3 || tag % 13 == 4) {
                    current_time.timeInHour!!.endTime = localTime
                }
                (v as TextView).text = string
            }
        })
            .setTimeSelectChangeListener(object : OnTimeSelectChangeListener {
                override fun onTimeSelectChanged(date: Date?) {
                    Log.i("pvTime", "onTimeSelectChanged")
                }
            })
            .setTitleText("时间选择")
            .setType(booleanArrayOf(false, false, false, true, true, false))
            .setLabel("年", "月", "日", "时", "分", "秒")
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


    /**
     * 描述：初始化日期选择器--选择哪一天
     * 参数：无
     * 返回：无
     */
    private fun initDatePicker() { //Dialog 模式下，在底部弹出
        pvDate = TimePickerBuilder(this, object : OnTimeSelectListener {
            override fun onTimeSelect(date: Date?, v: View?) {
                val instant: Instant = date!!.toInstant()
                val zone: ZoneId = ZoneId.systemDefault()
                val localDateTime: LocalDateTime = LocalDateTime.ofInstant(instant, zone)
                val localDate: LocalDate = localDateTime.toLocalDate()
                val string = localDate.toString()

                val tag:Int = v!!.tag!! as Int
                var layout:View = getFatherViewByTag(tag)
                val current_time:CalendarTimeData = getTimeByTag(tag)
                if(current_time.timeInCourseSchedule != null) {
                    current_time.timeInCourseSchedule!!.date = localDate
                    current_time.timeInCourseSchedule!!.dayOfWeek = localDate.dayOfWeek
                }
                else if(current_time.timeInHour != null) {
                    current_time.timeInHour!!.date = localDate
                    current_time.timeInHour!!.dayOfWeek = localDate.dayOfWeek
                }

                (v as TextView).text = string
                //layout.findViewById<TextView>(R.id.time_edit_date).text = string

            }
        })
            .setTimeSelectChangeListener(object : OnTimeSelectChangeListener {
                override fun onTimeSelectChanged(date: Date?) {
                    Log.i("pvTime", "onTimeSelectChanged")
                }
            })
            .setTitleText("日期选择")
            .setType(booleanArrayOf(true, true, true, false, false, false))
            .setLabel("年", "月", "日", "时", "分", "秒")
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
        val mDialog: Dialog = pvDate.getDialog()
        if (mDialog != null) {
            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            )
            params.leftMargin = 0
            params.rightMargin = 0
            pvDate.getDialogContainerLayout().setLayoutParams(params)
            val dialogWindow: Window? = mDialog.getWindow()
            if (dialogWindow != null) {
                dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim) //修改动画样式
                dialogWindow.setGravity(Gravity.BOTTOM) //改成Bottom,底部显示
                dialogWindow.setDimAmount(0.3f)
            }
        }
    }


    //大节选择，用于选择开始时间
    private val bigCourseChoices = ArrayList<String>()
    //大节对应的小节选择，用于选择开始时间
    private val smallCourseChoices = ArrayList<ArrayList<String>>()

    //大节-小节选择器
    private lateinit var pvCourseOptions: OptionsPickerView<Any>

    /**
     * 描述：加载大节-小节选择器，之前必须调用getCourseOptionData
     * 参数：无
     * 返回：无
     */
    private fun initCourseOptionPicker() {
        pvCourseOptions = OptionsPickerBuilder(this,
            OnOptionsSelectListener { options1, options2, options3, v -> //返回的分别是三个级别的选中位置
                var string: String = bigCourseChoices.get(options1)
                if(options2 != 0) {
                    string = string + smallCourseChoices.get(options1).get(options2)
                }

                val tag:Int = v!!.tag!! as Int
                val current_time:CalendarTimeData = getTimeByTag(tag)
                current_time.timeInCourseSchedule!!.startBig = options1 + 1
                current_time.timeInCourseSchedule!!.startOffsetSmall = options2.toFloat()
                (v as TextView).text = string

            })
            .setTitleText("时间选择（大节）")
            .setContentTextSize(20) //设置滚轮文字大小
            .setDividerColor(Color.LTGRAY) //设置分割线的颜色
            .setSelectOptions(0, 1) //默认选中项
            .setBgColor(Color.BLACK)
            .setTitleBgColor(Color.DKGRAY)
            .setTitleColor(Color.LTGRAY)
            .setCancelColor(Color.YELLOW)
            .setSubmitColor(Color.YELLOW)
            .setTextColorCenter(Color.LTGRAY)
            .isRestoreItem(true) //切换时是否还原，设置默认选中第一项。
            .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
            .setOutSideColor(0x00000000) //设置外部遮罩颜色
            .setOptionsSelectChangeListener { options1, options2, options3 ->

            }
            .build<Any>()

        pvCourseOptions.setPicker(bigCourseChoices as List<Any>?, smallCourseChoices as List<MutableList<Any>>?) //二级选择器
    }

    /**
     * 描述：初始化大节-小节选项数据
     * 参数：无
     * 返回：无
     */
    private fun getCourseOptionData() {
        bigCourseChoices.add("第一大节")
        bigCourseChoices.add("第二大节")
        bigCourseChoices.add("第三大节")
        bigCourseChoices.add("第四大节")
        bigCourseChoices.add("第五大节")
        bigCourseChoices.add("第六大节")

        var smallCourseTwo:ArrayList<String> = arrayListOf("第一小节", "第二小节")
        var smallCourseThree:ArrayList<String> = arrayListOf("第一小节", "第二小节","第三小节")
        smallCourseChoices.add(smallCourseTwo)
        smallCourseChoices.add(smallCourseThree)
        smallCourseChoices.add(smallCourseTwo)
        smallCourseChoices.add(smallCourseTwo)
        smallCourseChoices.add(smallCourseTwo)
        smallCourseChoices.add(smallCourseThree)
    }


    //星期选择，用于选择对应的星期
    private val weekDayChoices:ArrayList<String>  = arrayListOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    //小节个数选择，用于选择对应的小节长度
    private val lengthChoices:ArrayList<String>  = arrayListOf("1", "2", "3", "4", "5", "6")
    //类别选择，用于选择对应的类别
    private val typeChoices:ArrayList<String>  = arrayListOf("课程", "科研", "社工", "社团", "其他")
    //时间类别选择，用于选择对应的时间类别
    private val timeTypeChoices:ArrayList<String>  = arrayListOf("单次（按大节）", "重复（按大节）", "单次（按时间）", "重复（按时间）", "时间节点")


    //星期选择器
    private lateinit var pvWeekDayOptions: OptionsPickerView<Any>
    //小节长度选择器
    private lateinit var pvLengthOptions: OptionsPickerView<Any>
    //活动类别选择器
    private lateinit var pvTypeOptions: OptionsPickerView<Any>
    //时间类别选择器
    private lateinit var pvTimeTypeOptions: OptionsPickerView<Any>

    /**
     * 描述：加载星期选择器
     * 参数：无
     * 返回：无
     */
    private fun initWeekDayOptionPicker() {
        pvWeekDayOptions = OptionsPickerBuilder(this,
            OnOptionsSelectListener { options1, options2, options3, v -> //返回的分别是三个级别的选中位置
                var string: String = weekDayChoices.get(options1)

                val tag:Int = v!!.tag!! as Int
                val current_time:CalendarTimeData = getTimeByTag(tag)
                if(current_time.timeInCourseSchedule != null) {
                    current_time.timeInCourseSchedule!!.dayOfWeek = DayOfWeek.of(options1 + 1)
                }
                else if(current_time.timeInHour != null) {
                    current_time.timeInHour!!.dayOfWeek = DayOfWeek.of(options1 + 1)
                }
                (v as TextView).text = string

            })
            .setTitleText("星期几选择")
            .setContentTextSize(20) //设置滚轮文字大小
            .setDividerColor(Color.LTGRAY) //设置分割线的颜色
            .setSelectOptions(0, 1) //默认选中项
            .setBgColor(Color.BLACK)
            .setTitleBgColor(Color.DKGRAY)
            .setTitleColor(Color.LTGRAY)
            .setCancelColor(Color.YELLOW)
            .setSubmitColor(Color.YELLOW)
            .setTextColorCenter(Color.LTGRAY)
            .isRestoreItem(true) //切换时是否还原，设置默认选中第一项。
            .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
            .setOutSideColor(0x00000000) //设置外部遮罩颜色
            .setOptionsSelectChangeListener { options1, options2, options3 ->
            }
            .build<Any>()
        pvWeekDayOptions.setPicker(weekDayChoices as List<Any>?) //一级选择器
    }
    /**
     * 描述：加载小节长度选择器
     * 参数：无
     * 返回：无
     */
    private fun initLengthOptionPicker() {
        pvLengthOptions = OptionsPickerBuilder(this,
            OnOptionsSelectListener { options1, options2, options3, v -> //返回的分别是三个级别的选中位置
                var string: String = lengthChoices.get(options1)

                val tag:Int = v!!.tag!! as Int
                val current_time:CalendarTimeData = getTimeByTag(tag)
                current_time.timeInCourseSchedule!!.lengthSmall = (options1 + 1).toFloat()
                (v as TextView).text = string

            })
            .setTitleText("星期几选择")
            .setContentTextSize(20) //设置滚轮文字大小
            .setDividerColor(Color.LTGRAY) //设置分割线的颜色
            .setSelectOptions(0, 1) //默认选中项
            .setBgColor(Color.BLACK)
            .setTitleBgColor(Color.DKGRAY)
            .setTitleColor(Color.LTGRAY)
            .setCancelColor(Color.YELLOW)
            .setSubmitColor(Color.YELLOW)
            .setTextColorCenter(Color.LTGRAY)
            .setLabels("小节", "", "")
            .isRestoreItem(true) //切换时是否还原，设置默认选中第一项。
            .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
            .setOutSideColor(0x00000000) //设置外部遮罩颜色
            .setOptionsSelectChangeListener { options1, options2, options3 ->
            }
            .build<Any>()
        pvLengthOptions.setPicker(lengthChoices as List<Any>?) //一级选择器
    }
    /**
     * 描述：加载类别选择器
     * 参数：无
     * 返回：无
     */
    private fun initTypeOptionPicker() {
        pvTypeOptions = OptionsPickerBuilder(this,
            OnOptionsSelectListener { options1, options2, options3, v -> //返回的分别是三个级别的选中位置
                var string: String = typeChoices.get(options1)

                currentItem!!.type = CalendarItemType.valueOf(string)
                //TODO:根据type刷新显示
                (v as TextView).text = string

            })
            .setTitleText("日程类别选择")
            .setContentTextSize(20) //设置滚轮文字大小
            .setDividerColor(Color.LTGRAY) //设置分割线的颜色
            .setSelectOptions(0, 1) //默认选中项
            .setBgColor(Color.BLACK)
            .setTitleBgColor(Color.DKGRAY)
            .setTitleColor(Color.LTGRAY)
            .setCancelColor(Color.YELLOW)
            .setSubmitColor(Color.YELLOW)
            .setTextColorCenter(Color.LTGRAY)
            .isRestoreItem(true) //切换时是否还原，设置默认选中第一项。
            .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
            .setOutSideColor(0x00000000) //设置外部遮罩颜色
            .setOptionsSelectChangeListener { options1, options2, options3 ->
            }
            .build<Any>()
        pvTypeOptions.setPicker(typeChoices as List<Any>?) //一级选择器
    }
    /**
     * 描述：加载时间类别选择器
     * 参数：无
     * 返回：无
     */
    private fun initTimeTypeOptionPicker() {
        pvTimeTypeOptions = OptionsPickerBuilder(this,
            OnOptionsSelectListener { options1, options2, options3, v -> //返回的分别是三个级别的选中位置
                var string: String = timeTypeChoices.get(options1)

                val tag:Int = v!!.tag!! as Int
                val current_time:CalendarTimeData = getTimeByTag(tag)
                current_time.type = CalendarTimeType.valueOf(string)
                //TODO:根据type更新数据和显示
                (v as TextView).text = string

            })
            .setTitleText("时间类别选择")
            .setContentTextSize(20) //设置滚轮文字大小
            .setDividerColor(Color.LTGRAY) //设置分割线的颜色
            .setSelectOptions(0, 1) //默认选中项
            .setBgColor(Color.BLACK)
            .setTitleBgColor(Color.DKGRAY)
            .setTitleColor(Color.LTGRAY)
            .setCancelColor(Color.YELLOW)
            .setSubmitColor(Color.YELLOW)
            .setTextColorCenter(Color.LTGRAY)
            .isRestoreItem(true) //切换时是否还原，设置默认选中第一项。
            .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
            .setOutSideColor(0x00000000) //设置外部遮罩颜色
            .setOptionsSelectChangeListener { options1, options2, options3 ->
            }
            .build<Any>()
        pvTimeTypeOptions.setPicker(timeTypeChoices as List<Any>?) //一级选择器
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
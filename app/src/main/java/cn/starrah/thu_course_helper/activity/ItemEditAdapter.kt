package cn.starrah.thu_course_helper.activity

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarItemDataWithTimes
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeData
import cn.starrah.thu_course_helper.data.declares.calendarEnum.*
import cn.starrah.thu_course_helper.data.declares.time.TimeInCourseSchedule
import cn.starrah.thu_course_helper.data.declares.time.TimeInHour
import cn.starrah.thu_course_helper.data.utils.chineseName
import cn.starrah.thu_course_helper.picker.PickerDialog
import cn.starrah.thu_course_helper.picker.PickerView
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.bigkoo.pickerview.listener.OnTimeSelectChangeListener
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.OptionsPickerView
import com.bigkoo.pickerview.view.TimePickerView
import java.time.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.minutes


class ItemEditAdapter(currentItem: CalendarItemDataWithTimes, activity: ItemEditActivity) :
    RecyclerView.Adapter<ItemEditAdapter.ItemEditHolder>() {
    private var mCurrentItem: CalendarItemDataWithTimes = currentItem
    private var theActivity = activity
    public var colorGrey: Int = activity.resources.getColor(R.color.colorGreyBG)
    public var colorWhite: Int = activity.resources.getColor(R.color.colorWhite)


    public class ItemEditHolder(
        view: View,
        adapter: cn.starrah.thu_course_helper.activity.ItemEditAdapter
    ) :
        RecyclerView.ViewHolder(view), View.OnClickListener {
        public var theView: View = view

        public var itemName: EditText = view.findViewById<EditText>(R.id.item_edit_name)
        public var itemType: TextView = view.findViewById<TextView>(R.id.item_edit_type)
        public var itemTeacher: EditText = view.findViewById<EditText>(R.id.item_edit_teacher)
        public var itemCourseID: EditText = view.findViewById<EditText>(R.id.item_edit_course_id)
        public var itemAssociation: EditText = view.findViewById<EditText>(R.id.item_edit_association)

        public var timeName: EditText = view.findViewById<EditText>(R.id.time_edit_name)
        public var timePlace: EditText = view.findViewById<EditText>(R.id.time_edit_place)
        public var timeType: TextView = view.findViewById<TextView>(R.id.time_edit_time_type)
        public var timeWeek: TextView = view.findViewById<TextView>(R.id.time_edit_week)
        public var timeDayWeek: TextView = view.findViewById<TextView>(R.id.time_edit_day)
        public var timeDate: TextView = view.findViewById<TextView>(R.id.time_edit_date)
        public var timeStartCourse: TextView = view.findViewById<TextView>(R.id.time_edit_start_course)
        public var timeLengthCourse: TextView = view.findViewById<TextView>(R.id.time_edit_length_course)
        public var timeStartHour: TextView = view.findViewById<TextView>(R.id.time_edit_start_hour)
        public var timeEndHour: TextView = view.findViewById<TextView>(R.id.time_edit_end_hour)
        public var timePoint: TextView = view.findViewById<TextView>(R.id.time_edit_point)
        public var timeComment: EditText = view.findViewById<EditText>(R.id.time_edit_comment)

        public var timeRemindRepeat: TextView = view.findViewById<TextView>(R.id.time_edit_remind_repeat)
        public var timeRemindTime: TextView = view.findViewById<TextView>(R.id.time_edit_remind_time)
        public var timeRemindType: TextView = view.findViewById<TextView>(R.id.time_edit_remind_type)

        public var itemNamePlace: LinearLayout = view.findViewById<LinearLayout>(R.id.item_edit_name_place)
        public var itemTypePlace: LinearLayout = view.findViewById<LinearLayout>(R.id.item_edit_type_place)
        public var itemTeacherPlace: LinearLayout = view.findViewById<LinearLayout>(R.id.item_edit_teacher_place)
        public var itemCourseIDPlace: LinearLayout = view.findViewById<LinearLayout>(R.id.item_edit_course_id_place)
        public var itemAssociationPlace: LinearLayout = view.findViewById<LinearLayout>(R.id.item_edit_association_place)


        public var timeNamePlace: LinearLayout = view.findViewById<LinearLayout>(R.id.time_edit_name_place)
        public var timePlacePlace: LinearLayout = view.findViewById<LinearLayout>(R.id.time_edit_place_place)
        public var timeTypePlace: LinearLayout = view.findViewById<LinearLayout>(R.id.time_edit_type_place)
        public var timeWeekPlace: LinearLayout = view.findViewById<LinearLayout>(R.id.time_edit_week_place)
        public var timeDayWeekPlace: LinearLayout = view.findViewById<LinearLayout>(R.id.time_edit_day_place)
        public var timeDatePlace: LinearLayout = view.findViewById<LinearLayout>(R.id.time_edit_date_place)
        public var timeStartCoursePlace: LinearLayout = view.findViewById<LinearLayout>(R.id.time_edit_start_course_place)
        public var timeLengthCoursePlace: LinearLayout = view.findViewById<LinearLayout>(R.id.time_edit_length_course_place)
        public var timeStartHourPlace: LinearLayout = view.findViewById<LinearLayout>(R.id.time_edit_start_hour_place)
        public var timeEndHourPlace: LinearLayout = view.findViewById<LinearLayout>(R.id.time_edit_end_hour_place)
        public var timePointPlace: LinearLayout = view.findViewById<LinearLayout>(R.id.time_edit_point_place)
        public var timeCommentPlace: LinearLayout = view.findViewById<LinearLayout>(R.id.time_edit_comment_place)
        public var timeDeleteButtonPlace: LinearLayout = view.findViewById<LinearLayout>(R.id.delete_time_place)

        public var timeRemindRepeatPlace: LinearLayout = view.findViewById<LinearLayout>(R.id.time_edit_remind_repeat_place)
        public var timeRemindTimePlace: LinearLayout = view.findViewById<LinearLayout>(R.id.time_edit_remind_time_place)
        public var timeRemindTypePlace: LinearLayout = view.findViewById<LinearLayout>(R.id.time_edit_remind_type_place)

        var mAdapter: cn.starrah.thu_course_helper.activity.ItemEditAdapter = adapter

        public var timeDeleteButton:ImageButton = view.findViewById(R.id.delete_time)



        //大节选择，用于选择开始时间
        public val bigCourseChoices = ArrayList<String>()
        //大节对应的小节选择，用于选择开始时间
        public val smallCourseChoices = ArrayList<ArrayList<String>>()
        //时间选择器（滚轮），用来选择时间
        public lateinit var pvTime: TimePickerView

        //日期选择器
        public lateinit var pvDate: TimePickerView

        //星期选择，用于选择对应的星期
        private val weekDayChoices:ArrayList<String>  = arrayListOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
        //小节个数选择，用于选择对应的小节长度
        private val lengthChoices:ArrayList<String>  = arrayListOf("1", "2", "3", "4", "5", "6")
        //时间类别选择，用于选择对应的时间类别
        private val timeTypeChoices:ArrayList<String>  = arrayListOf("单次（按大节）", "重复（按大节）", "单次（按时间）", "重复（按时间）", "时间节点")
        //日程类别选择，用于选择对应的日程类别
        private val itemTypeChoices: ArrayList<String> = arrayListOf("课程", "科研", "社工", "社团", "其他")
        //星期选择器
        lateinit var pvWeekDayOptions: OptionsPickerView<Any>
        //小节长度选择器
        lateinit var pvLengthOptions: OptionsPickerView<Any>
        //时间类别选择器
        public lateinit var pvTimeTypeOptions: OptionsPickerView<Any>
        //大节-小节选择器
        public lateinit var pvCourseOptions: OptionsPickerView<Any>
        //周选择器
        public lateinit var pvWeekOptions: PickerDialog.Builder
        //日程类别选择器
        public lateinit var pvItemTypeOptions: OptionsPickerView<Any>

        //提醒重复选择(用于重复活动）
        private val remindRepeatChoicesRegular: ArrayList<String> = arrayListOf("无", "仅这一次", "每次")
        //提醒重复选择(用于单次活动）
        private val remindRepeatChoicesSingle: ArrayList<String> = arrayListOf("无", "仅这一次")
        //提醒方式选择
        private val remindTypeChoices: ArrayList<String> = arrayListOf("通知栏", "闹钟")
        //提醒重复选择器
        public lateinit var pvRemindRepeatRegular: OptionsPickerView<Any>
        public lateinit var pvRemindRepeatSingle: OptionsPickerView<Any>

        //提醒类别选择器
        public lateinit var pvRemindType: OptionsPickerView<Any>
        //提醒提前时间选择
        public lateinit var pvRemindTime: TimePickerView

        //日程name监听器
        public lateinit var itemNameChanger:TextWatcher
        //日程teacher监听器
        public lateinit var itemTeacherChanger:TextWatcher
        //日程courseid监听器
        public lateinit var itemCourseIDChanger:TextWatcher
        //日程association监听器
        public lateinit var itemAssociationChanger:TextWatcher
        //时间段name监听器
        public lateinit var timeNameChanger:TextWatcher
        //时间段place监听器
        public lateinit var timePlaceChanger:TextWatcher
        //时间段comment监听器
        public lateinit var timeDetailChanger:TextWatcher

        init {
            getCourseOptionData()
            initTimePicker()
            initDatePicker()
            initCourseOptionPicker()
            initWeekDayOptionPicker()
            initLengthOptionPicker()
            initTimeTypeOptionPicker()
            initWeekPickerDialog()
            initItemTypeOptionPicker()
            initRemindRepeatRegularPicker()
            initRemindRepeatSinglePicker()
            initRemindTimePicker()
            initRemindTypePicker()


            timeDeleteButton.setOnClickListener(this)


            initItemNameChanger()
            initItemTeacherChanger()
            initItemCourseIDChanger()
            initItemAssociationChanger()
            initTimeNameChanger()
            initTimePlaceChanger()
            initTimeDetailChanger()
        }


        //EditText监听初始化函数
        /**
         * 描述：初始化日程名称监听器
         * 参数：无
         * 返回：无
         */
        private fun initItemNameChanger() {
            itemNameChanger = (object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                }

                override fun afterTextChanged(editable: Editable) {
                    val position: Int = getAdapterPosition()
                    if(position == 0) {
                        mAdapter.mCurrentItem.name = editable.toString()
                    }
                }
            })
        }
        /**
         * 描述：初始化日程教师监听器
         * 参数：无
         * 返回：无
         */
        private fun initItemTeacherChanger() {
            itemTeacherChanger = (object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                }

                override fun afterTextChanged(editable: Editable) {
                    val position: Int = getAdapterPosition()
                    if(position == 0) {
                        if (mAdapter.mCurrentItem.type.equals(CalendarItemType.COURSE)) {
                            mAdapter.mCurrentItem.detail[CalendarItemLegalDetailKey.TEACHER] =
                                editable.toString()
                        }
                    }
                }
            })
        }

        /**
         * 描述：初始化日程课程号监听器
         * 参数：无
         * 返回：无
         */
        private fun initItemCourseIDChanger() {
            itemCourseIDChanger = (object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                }

                override fun afterTextChanged(editable: Editable) {
                    val position: Int = getAdapterPosition()
                    if(position == 0) {
                        if (mAdapter.mCurrentItem.type.equals(CalendarItemType.COURSE)) {
                            mAdapter.mCurrentItem.detail[CalendarItemLegalDetailKey.COURSEID] =
                                editable.toString()
                        }
                    }
                }
            })
        }

        /**
         * 描述：初始化日程组织监听器
         * 参数：无
         * 返回：无
         */
        private fun initItemAssociationChanger() {
            itemAssociationChanger = (object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                }

                override fun afterTextChanged(editable: Editable) {
                    val position: Int = getAdapterPosition()
                    if(position == 0) {
                        if (mAdapter.mCurrentItem.type.equals(CalendarItemType.SOCIALWORK) || mAdapter.mCurrentItem.type.equals(
                                CalendarItemType.ASSOCIATION
                            )) {
                            mAdapter.mCurrentItem.detail[CalendarItemLegalDetailKey.ORGANIZATION] =
                                editable.toString()
                        }
                    }
                }
            })
        }


        /**
         * 描述：初始化时间段名称监听器
         * 参数：无
         * 返回：无
         */
        public fun initTimeNameChanger() {
            timeNameChanger = (object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                }

                override fun afterTextChanged(editable: Editable) {
                    val position: Int = getAdapterPosition()
                    if(position > 0) {
                        var time: CalendarTimeData = mAdapter.mCurrentItem.times.get(position - 1)
                        time.name = editable.toString()
                    }
                }
            })
        }
        /**
         * 描述：初始化时间段地点监听器
         * 参数：无
         * 返回：无
         */
        public fun initTimePlaceChanger() {
            timePlaceChanger = (object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                }

                override fun afterTextChanged(editable: Editable) {
                    val position: Int = getAdapterPosition()
                    if(position > 0) {
                        var time: CalendarTimeData = mAdapter.mCurrentItem.times.get(position - 1)
                        time.place = editable.toString()
                    }
                }
            })
        }
        /**
         * 描述：初始化时详情监听器
         * 参数：无
         * 返回：无
         */
        public fun initTimeDetailChanger() {
            timeDetailChanger = (object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                }

                override fun afterTextChanged(editable: Editable) {
                    val position: Int = getAdapterPosition()
                    if(position > 0) {
                        var time: CalendarTimeData = mAdapter.mCurrentItem.times.get(position - 1)
                        time.comment = editable.toString()
                    }
                    else if(position == 0) {
                        mAdapter.mCurrentItem.detail[CalendarItemLegalDetailKey.COMMENT] = editable.toString()
                    }
                }
            })
        }


        //选择类控件初始化函数
        /**
         * 描述：初始化时间选择器---用于时间和时间节点选择
         * 参数：无
         * 返回：无
         */
        public fun initTimePicker() { //Dialog 模式下，在底部弹出
            pvTime = TimePickerBuilder(mAdapter.theActivity, object : OnTimeSelectListener {
                override fun onTimeSelect(date: Date?, v: View?) {
                    val instant: Instant = date!!.toInstant()
                    val zone: ZoneId = ZoneId.systemDefault()
                    val localDateTime: LocalDateTime = LocalDateTime.ofInstant(instant, zone)
                    val localTime: LocalTime = localDateTime.toLocalTime()
                    val string = ItemEditActivity.getTimeString(localTime)

                    val position: Int = getAdapterPosition()

                    if(position > 0) {
                        var time: CalendarTimeData = mAdapter.mCurrentItem.times.get(position - 1)
                        var tag_int = v!!.tag as Int
                        if (tag_int == 1) {
                            time.timeInHour!!.startTime = localTime
                        }
                        else if (tag_int == 2) {
                            time.timeInHour!!.endTime = localTime
                        }
                        else if (tag_int == 3) {
                            time.timeInHour!!.startTime = localTime
                            time.timeInHour!!.endTime = localTime
                        }
                        mAdapter.notifyDataSetChanged()
                    }
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
        public fun initDatePicker() { //Dialog 模式下，在底部弹出
            pvDate = TimePickerBuilder(mAdapter.theActivity, object : OnTimeSelectListener {
                override fun onTimeSelect(date: Date?, v: View?) {

                    val instant: Instant = date!!.toInstant()
                    val zone: ZoneId = ZoneId.systemDefault()
                    val localDateTime: LocalDateTime = LocalDateTime.ofInstant(instant, zone)
                    val localDate: LocalDate = localDateTime.toLocalDate()

                    val position: Int = getAdapterPosition()
                    if(position > 0) {
                        var time: CalendarTimeData = mAdapter.mCurrentItem.times.get(position - 1)
                        if (time.timeInCourseSchedule != null) {
                            time.timeInCourseSchedule!!.date = localDate
                            time.timeInCourseSchedule!!.dayOfWeek = localDate.dayOfWeek
                        }
                        else if (time.timeInHour != null) {
                            time.timeInHour!!.date = localDate
                            time.timeInHour!!.dayOfWeek = localDate.dayOfWeek
                        }
                        mAdapter.notifyDataSetChanged()
                    }
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


        /**
         * 描述：加载大节-小节选择器，之前必须调用getCourseOptionData
         * 参数：无
         * 返回：无
         */
        private fun initCourseOptionPicker() {
            pvCourseOptions = OptionsPickerBuilder(mAdapter.theActivity,
                OnOptionsSelectListener { options1, options2, options3, v -> //返回的分别是三个级别的选中位置
                    val position: Int = getAdapterPosition()
                    if(position > 0) {
                        var time: CalendarTimeData = mAdapter.mCurrentItem.times.get(position - 1)
                        time.timeInCourseSchedule!!.startBig = options1 + 1
                        time.timeInCourseSchedule!!.startOffsetSmall = options2.toFloat()
                        mAdapter.notifyDataSetChanged()
                    }

                })
                .setTitleText("时间选择（大节）")
                .setContentTextSize(20) //设置滚轮文字大小
                .setDividerColor(Color.DKGRAY) //设置分割线的颜色
                .setSelectOptions(0, 1) //默认选中项
                .setBgColor(Color.WHITE)
                .setTitleBgColor(mAdapter.colorGrey)
                .setTitleColor(Color.BLACK)
                .setCancelColor(Color.BLUE)
                .setSubmitColor(Color.BLUE)
                .setTextColorCenter(Color.BLACK)
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

        /**
         * 描述：加载星期选择器
         * 参数：无
         * 返回：无
         */
        private fun initWeekDayOptionPicker() {
            pvWeekDayOptions = OptionsPickerBuilder(mAdapter.theActivity,
                OnOptionsSelectListener { options1, options2, options3, v -> //返回的分别是三个级别的选中位置

                    val position: Int = getAdapterPosition()
                    if(position > 0) {
                        var time: CalendarTimeData = mAdapter.mCurrentItem.times.get(position - 1)
                        if (time.timeInCourseSchedule != null) {
                            time.timeInCourseSchedule!!.dayOfWeek = DayOfWeek.of(options1 + 1)
                        }
                        else if (time.timeInHour != null) {
                            time.timeInHour!!.dayOfWeek = DayOfWeek.of(options1 + 1)
                        }
                        mAdapter.notifyDataSetChanged()
                    }

                })
                .setTitleText("星期几选择")
                .setContentTextSize(20) //设置滚轮文字大小
                .setDividerColor(Color.DKGRAY) //设置分割线的颜色
                .setSelectOptions(0, 1) //默认选中项
                .setBgColor(Color.WHITE)
                .setTitleBgColor(mAdapter.colorGrey)
                .setTitleColor(Color.BLACK)
                .setCancelColor(Color.BLUE)
                .setSubmitColor(Color.BLUE)
                .setTextColorCenter(Color.BLACK)
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
            pvLengthOptions = OptionsPickerBuilder(mAdapter.theActivity,
                OnOptionsSelectListener { options1, options2, options3, v -> //返回的分别是三个级别的选中位置
                    val position: Int = getAdapterPosition()
                    if(position > 0) {
                        var time: CalendarTimeData = mAdapter.mCurrentItem.times.get(position - 1)
                        time.timeInCourseSchedule!!.lengthSmall = (options1 + 1).toFloat()
                        mAdapter.notifyDataSetChanged()
                    }
                })
                .setTitleText("小节个数选择")
                .setContentTextSize(20) //设置滚轮文字大小
                .setDividerColor(Color.DKGRAY) //设置分割线的颜色
                .setSelectOptions(0, 1) //默认选中项
                .setBgColor(Color.WHITE)
                .setTitleBgColor(mAdapter.colorGrey)
                .setTitleColor(Color.BLACK)
                .setCancelColor(Color.BLUE)
                .setSubmitColor(Color.BLUE)
                .setTextColorCenter(Color.BLACK)
                .isRestoreItem(true) //切换时是否还原，设置默认选中第一项。
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setOutSideColor(0x00000000) //设置外部遮罩颜色
                .setOptionsSelectChangeListener { options1, options2, options3 ->
                }
                .build<Any>()
            pvLengthOptions.setPicker(lengthChoices as List<Any>?) //一级选择器
        }

        /**
         * 描述：加载时间类别选择器
         * 参数：无
         * 返回：无
         */
        private fun initTimeTypeOptionPicker() {
            pvTimeTypeOptions = OptionsPickerBuilder(mAdapter.theActivity,
                OnOptionsSelectListener { options1, options2, options3, v -> //返回的分别是三个级别的选中位置
                    val position: Int = getAdapterPosition()
                    if(position > 0) {
                        var time: CalendarTimeData = mAdapter.mCurrentItem.times.get(position - 1)
                        handleTimeTypeChange(time, options1)
                        mAdapter.notifyDataSetChanged()
                    }
                })
                .setTitleText("时间类别选择")
                .setContentTextSize(20) //设置滚轮文字大小
                .setDividerColor(Color.DKGRAY) //设置分割线的颜色
                .setSelectOptions(0, 1) //默认选中项
                .setBgColor(Color.WHITE)
                .setTitleBgColor(mAdapter.colorGrey)
                .setTitleColor(Color.BLACK)
                .setCancelColor(Color.BLUE)
                .setSubmitColor(Color.BLUE)
                .setTextColorCenter(Color.BLACK)
                .isRestoreItem(true) //切换时是否还原，设置默认选中第一项。
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setOutSideColor(0x00000000) //设置外部遮罩颜色
                .setOptionsSelectChangeListener { options1, options2, options3 ->
                }
                .build<Any>()
            pvTimeTypeOptions.setPicker(timeTypeChoices as List<Any>?) //一级选择器
        }

        /**
         * 描述：加载时间类别选择器
         * 参数：无
         * 返回：无
         */
        private fun initItemTypeOptionPicker() {
            pvItemTypeOptions = OptionsPickerBuilder(mAdapter.theActivity,
                OnOptionsSelectListener { options1, options2, options3, v -> //返回的分别是三个级别的选中位置
                    val position: Int = getAdapterPosition()
                    if(position == 0) {
                        handleItemTypeChange(options1)
                        mAdapter.notifyDataSetChanged()
                    }
                })
                .setTitleText("日程类别选择")
                .setContentTextSize(20) //设置滚轮文字大小
                .setDividerColor(Color.DKGRAY) //设置分割线的颜色
                .setSelectOptions(0, 1) //默认选中项
                .setBgColor(Color.WHITE)
                .setTitleBgColor(mAdapter.colorGrey)
                .setTitleColor(Color.BLACK)
                .setCancelColor(Color.BLUE)
                .setSubmitColor(Color.BLUE)
                .setTextColorCenter(Color.BLACK)
                .isRestoreItem(true) //切换时是否还原，设置默认选中第一项。
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setOutSideColor(0x00000000) //设置外部遮罩颜色
                .setOptionsSelectChangeListener { options1, options2, options3 ->
                }
                .build<Any>()
            pvItemTypeOptions.setPicker(itemTypeChoices as List<Any>?) //一级选择器
        }


        /**
         * 描述：给删除按钮绑定事件处理函数
         * 参数：被删除的view
         * 返回：无
         */
        override fun onClick(v: View?) {
            showDialogDelete()
        }

        /**
         * 描述：删除单一日程时的对话框，如果确定，就删除，否则继续
         * 参数：无
         * 返回：无
         */
        private fun showDialogDelete() {
            val dialog: AlertDialog.Builder =
                object : AlertDialog.Builder(mAdapter.theActivity) {
                    override fun create(): AlertDialog {
                        return super.create()
                    }

                    override fun show(): AlertDialog {
                        return super.show()
                    }
                }
            dialog.setOnCancelListener { }
            dialog.setOnDismissListener { }
            dialog.setIcon(R.mipmap.ic_launcher_round)
                .setTitle("删除单一时间段")
                .setMessage("确定要删除此时间段吗？")
                .setCancelable(true)
                .setPositiveButton("确定",
                    DialogInterface.OnClickListener { dialog, which ->
                        val position: Int = getAdapterPosition()
                        if(position > 0) {
                            mAdapter.mCurrentItem.times.removeAt(position - 1)
                            mAdapter.notifyDataSetChanged()
                        }})
                .setNegativeButton("取消",
                    DialogInterface.OnClickListener { dialog, which ->  })
            dialog.show()
        }

        /**
         * 描述：显示周选择器
         * 参数：无
         * 返回：无
         */
        private fun initWeekPickerDialog() {
            var selected:ArrayList<Int> = arrayListOf()
            pvWeekOptions = PickerDialog.Builder(mAdapter.theActivity)
                .setWeeks(16, 2)
                .setInitialSelectedWeeks(selected)
                .setOnWeeksSelectedListener(object : PickerDialog.OnWeeksSelectedListener {
                    override fun onWeeksSelected(
                        pickerView: PickerView?,
                        selectedWeeks: java.util.ArrayList<Int?>?
                    ) {
                        val position: Int = getAdapterPosition()
                        if(position > 0) {
                            var time: CalendarTimeData = mAdapter.mCurrentItem.times.get(position - 1)
                            time.repeatWeeks.clear()
                            if (selectedWeeks != null) {
                                for (item in selectedWeeks) {
                                    if (item != null) {
                                        time.repeatWeeks.add(item)
                                    }
                                }
                            }
                            mAdapter.notifyDataSetChanged()
                        }
                    }
                })
        }

        /**
         * 描述：初始化提醒时间选择器
         * 参数：无
         * 返回：无
         */
        public fun initRemindTimePicker() { //Dialog 模式下，在底部弹出
            pvRemindTime = TimePickerBuilder(mAdapter.theActivity, object : OnTimeSelectListener {
                override fun onTimeSelect(date: Date?, v: View?) {
                    val instant: Instant = date!!.toInstant()
                    val zone: ZoneId = ZoneId.systemDefault()
                    val localDateTime: LocalDateTime = LocalDateTime.ofInstant(instant, zone)
                    val localTime: LocalTime = localDateTime.toLocalTime()

                    val position: Int = getAdapterPosition()
                    if(position > 0) {
                        var time: CalendarTimeData = mAdapter.mCurrentItem.times.get(position - 1)
                        var minute: Int = localTime.minute
                        var duration:java.time.Duration = java.time.Duration.ofMinutes(minute.toLong())
                        time.remindData.aheadTime = duration
                        mAdapter.notifyDataSetChanged()
                    }
                }
            })
                .setTitleText("提醒时间选择")
                .setType(booleanArrayOf(false, false, false, false, true, false))
                .setLabel("年", "月", "日", "时", "分钟", "秒")
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
            val mDialog: Dialog = pvRemindTime.getDialog()
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
         * 描述：加载提醒重复类别选择器（重复活动）
         * 参数：无
         * 返回：无
         */
        private fun initRemindRepeatRegularPicker() {
            pvRemindRepeatRegular = OptionsPickerBuilder(mAdapter.theActivity,
                OnOptionsSelectListener { options1, options2, options3, v -> //返回的分别是三个级别的选中位置
                    val position: Int = getAdapterPosition()
                    if(position > 0) {
                        var time: CalendarTimeData = mAdapter.mCurrentItem.times.get(position - 1)
                        var the_type: CalendarRemindType = CalendarRemindType.NONE
                        for(type in CalendarRemindType.values()) {
                            if(type.ordinal == options1) {
                                the_type = type
                                break
                            }
                        }
                        time.remindData.type = the_type
                        mAdapter.notifyDataSetChanged()
                    }
                })
                .setTitleText("提醒重复类别选择")
                .setContentTextSize(20) //设置滚轮文字大小
                .setDividerColor(Color.DKGRAY) //设置分割线的颜色
                .setSelectOptions(0, 1) //默认选中项
                .setBgColor(Color.WHITE)
                .setTitleBgColor(mAdapter.colorGrey)
                .setTitleColor(Color.BLACK)
                .setCancelColor(Color.BLUE)
                .setSubmitColor(Color.BLUE)
                .setTextColorCenter(Color.BLACK)
                .isRestoreItem(true) //切换时是否还原，设置默认选中第一项。
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setOutSideColor(0x00000000) //设置外部遮罩颜色
                .setOptionsSelectChangeListener { options1, options2, options3 ->
                }
                .build<Any>()
            pvRemindRepeatRegular.setPicker(remindRepeatChoicesRegular as List<Any>?) //一级选择器
        }

        /**
         * 描述：加载提醒重复类别选择器（单次活动）
         * 参数：无
         * 返回：无
         */
        private fun initRemindRepeatSinglePicker() {
            pvRemindRepeatSingle = OptionsPickerBuilder(mAdapter.theActivity,
                OnOptionsSelectListener { options1, options2, options3, v -> //返回的分别是三个级别的选中位置
                    val position: Int = getAdapterPosition()
                    if(position > 0) {
                        var time: CalendarTimeData = mAdapter.mCurrentItem.times.get(position - 1)
                        var the_type: CalendarRemindType = CalendarRemindType.NONE
                        for(type in CalendarRemindType.values()) {
                            if(type.ordinal == options1) {
                                the_type = type
                                break
                            }
                        }
                        time.remindData.type = the_type
                        mAdapter.notifyDataSetChanged()
                    }
                })
                .setTitleText("提醒重复类别选择")
                .setContentTextSize(20) //设置滚轮文字大小
                .setDividerColor(Color.DKGRAY) //设置分割线的颜色
                .setSelectOptions(0, 1) //默认选中项
                .setBgColor(Color.WHITE)
                .setTitleBgColor(mAdapter.colorGrey)
                .setTitleColor(Color.BLACK)
                .setCancelColor(Color.BLUE)
                .setSubmitColor(Color.BLUE)
                .setTextColorCenter(Color.BLACK)
                .isRestoreItem(true) //切换时是否还原，设置默认选中第一项。
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setOutSideColor(0x00000000) //设置外部遮罩颜色
                .setOptionsSelectChangeListener { options1, options2, options3 ->
                }
                .build<Any>()
            pvRemindRepeatSingle.setPicker(remindRepeatChoicesSingle as List<Any>?) //一级选择器
        }

        /**
         * 描述：加载提醒类别选择器
         * 参数：无
         * 返回：无
         */
        private fun initRemindTypePicker() {
            pvRemindType = OptionsPickerBuilder(mAdapter.theActivity,
                OnOptionsSelectListener { options1, options2, options3, v -> //返回的分别是三个级别的选中位置
                    val position: Int = getAdapterPosition()
                    if(position > 0) {
                        var time: CalendarTimeData = mAdapter.mCurrentItem.times.get(position - 1)
                        var the_type: CalendarRemindMethodType = CalendarRemindMethodType.ALARM
                        for(type in CalendarRemindMethodType.values()) {
                            if(type.ordinal == options1) {
                                the_type = type
                                break
                            }
                        }
                        time.remindData.method = the_type
                        mAdapter.notifyDataSetChanged()
                    }
                })
                .setTitleText("提醒方法选择")
                .setContentTextSize(20) //设置滚轮文字大小
                .setDividerColor(Color.DKGRAY) //设置分割线的颜色
                .setSelectOptions(0, 1) //默认选中项
                .setBgColor(Color.WHITE)
                .setTitleBgColor(mAdapter.colorGrey)
                .setTitleColor(Color.BLACK)
                .setCancelColor(Color.BLUE)
                .setSubmitColor(Color.BLUE)
                .setTextColorCenter(Color.BLACK)
                .isRestoreItem(true) //切换时是否还原，设置默认选中第一项。
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setOutSideColor(0x00000000) //设置外部遮罩颜色
                .setOptionsSelectChangeListener { options1, options2, options3 ->
                }
                .build<Any>()
            pvRemindType.setPicker(remindTypeChoices as List<Any>?) //一级选择器
        }


        /**
         * 描述：根据修改的日程类别信息来修改数据
         * 参数：一个int，从0开始，对应修改后当前日程的类型在枚举里对应的数值
         * 返回：无
         */
        fun handleItemTypeChange(new_type_int:Int) {
            var the_type:CalendarItemType = CalendarItemType.COURSE
            for(type in CalendarItemType.values()) {
                if(type.ordinal == new_type_int) {
                    the_type = type
                    break
                }
            }
            mAdapter.mCurrentItem.type = the_type

            if(the_type == CalendarItemType.COURSE) {
                var the_teacher = mAdapter.mCurrentItem.detail.get(CalendarItemLegalDetailKey.TEACHER)
                if(the_teacher == null) {
                    mAdapter.mCurrentItem.detail.replace(CalendarItemLegalDetailKey.TEACHER, "")
                }
                var the_course_id = mAdapter.mCurrentItem.detail.get(CalendarItemLegalDetailKey.COURSEID)
                if(the_course_id == null) {
                    mAdapter.mCurrentItem.detail.replace(CalendarItemLegalDetailKey.COURSEID, "")
                }
                mAdapter.mCurrentItem.detail.remove(CalendarItemLegalDetailKey.ORGANIZATION)

            }
            else if(the_type == CalendarItemType.ASSOCIATION || the_type == CalendarItemType.SOCIALWORK) {
                //修改数据
                mAdapter.mCurrentItem.detail.remove(CalendarItemLegalDetailKey.TEACHER)
                mAdapter.mCurrentItem.detail.remove(CalendarItemLegalDetailKey.COURSEID)
                var the_org_id = mAdapter.mCurrentItem.detail.get(CalendarItemLegalDetailKey.ORGANIZATION)
                if(the_org_id == null) {
                    mAdapter.mCurrentItem.detail.replace(CalendarItemLegalDetailKey.ORGANIZATION, "")
                }

            }
            else {
                //修改数据
                mAdapter.mCurrentItem.detail.remove(CalendarItemLegalDetailKey.ORGANIZATION)
                mAdapter.mCurrentItem.detail.remove(CalendarItemLegalDetailKey.TEACHER)
                mAdapter.mCurrentItem.detail.remove(CalendarItemLegalDetailKey.COURSEID)

            }
        }

        /**
         * 描述：根据修改的时间类别信息来修改数据
         * 参数：第一个是待修改的时间数据，第二个是一个int，从0开始，对应修改后当前日程的类型在枚举里对应的数值
         * 返回：无
         */
        fun handleTimeTypeChange(the_time:CalendarTimeData, new_type_int:Int) {
            var the_type: CalendarTimeType = CalendarTimeType.REPEAT_COURSE
            for(type in CalendarTimeType.values()) {
                if(type.ordinal == new_type_int) {
                    the_type = type
                    break
                }
            }

            the_time.type = the_type
            if(the_type == CalendarTimeType.REPEAT_COURSE || the_type == CalendarTimeType.SINGLE_COURSE) {
                the_time.timeInHour = null
                if(the_time.timeInCourseSchedule == null) {
                    the_time.timeInCourseSchedule = TimeInCourseSchedule(dayOfWeek = LocalDate.now().dayOfWeek,
                        startBig = 1, lengthSmall = 1.0f, date = LocalDate.now())
                }
            }
            else{
                the_time.timeInCourseSchedule = null
                if(the_time.timeInHour == null) {
                    the_time.timeInHour = TimeInHour(startTime = LocalTime.now(), endTime = LocalTime.now(),
                        dayOfWeek = LocalDate.now().dayOfWeek, date = LocalDate.now())
                }
            }

            if(the_type == CalendarTimeType.REPEAT_COURSE) {
                the_time.timeInCourseSchedule!!.date = null
                the_time.repeatWeeks = mutableListOf(1)
            }
            else if(the_type == CalendarTimeType.SINGLE_COURSE) {
                the_time.timeInCourseSchedule!!.date = LocalDate.now()
                the_time.timeInCourseSchedule!!.dayOfWeek = the_time.timeInCourseSchedule!!.date!!.dayOfWeek
                the_time.repeatWeeks = mutableListOf()
            }
            else if(the_type == CalendarTimeType.REPEAT_HOUR) {
                the_time.timeInHour!!.dayOfWeek = LocalDate.now().dayOfWeek
                the_time.timeInHour!!.date = null
                the_time.repeatWeeks = mutableListOf(1)
            }
            else if(the_type == CalendarTimeType.SINGLE_HOUR) {
                the_time.timeInHour!!.dayOfWeek = LocalDate.now().dayOfWeek
                the_time.timeInHour!!.date = LocalDate.now()
                the_time.repeatWeeks = mutableListOf()
            }
            else if(the_type == CalendarTimeType.POINT) {
                the_time.timeInHour!!.endTime = the_time.timeInHour!!.startTime
                the_time.timeInHour!!.dayOfWeek = null
                the_time.timeInHour!!.date = LocalDate.now()
                the_time.repeatWeeks = mutableListOf()
            }
        }

    }

    /**
     * 描述：adapter初始化viewholder
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemEditHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.calendar_time_edit, parent, false)
        return ItemEditHolder(view, this)
    }


    /**
     * 描述：绑定viewholder函数，其实就是显示一个time对应的信息
     * 参数：对应的holder，和数据对应position（在recyclerview的数据）
     * 返回：无
     */
    override fun onBindViewHolder(holder: ItemEditHolder, position: Int) {
        if(position < 0) {
            return
        }
        else if(position == 0) {
            showDataItem(holder)
        }
        else {
            showDataTime(holder, position - 1)
        }
    }

    /**
     * 描述：onbindholder中使用，用于设置头部数据
     * 参数：holder
     * 返回：无
     */
    fun showDataItem(holder: ItemEditHolder) {
        //显示全部上方item信息，隐藏下方time信息
        ItemEditActivity.ShowItem(holder.itemNamePlace)
        ItemEditActivity.ShowItem(holder.itemTypePlace)
        ItemEditActivity.ShowItem(holder.itemTeacherPlace)
        ItemEditActivity.ShowItem(holder.itemCourseIDPlace)
        ItemEditActivity.ShowItem(holder.itemAssociationPlace)
        ItemEditActivity.ShowItem(holder.timeCommentPlace)
        ItemEditActivity.HideItem(holder.timeNamePlace)
        ItemEditActivity.HideItem(holder.timePlacePlace)
        ItemEditActivity.HideItem(holder.timeTypePlace)
        ItemEditActivity.HideItem(holder.timeWeekPlace)
        ItemEditActivity.HideItem(holder.timeDayWeekPlace)
        ItemEditActivity.HideItem(holder.timeDatePlace)
        ItemEditActivity.HideItem(holder.timeStartCoursePlace)
        ItemEditActivity.HideItem(holder.timeLengthCoursePlace)
        ItemEditActivity.HideItem(holder.timeStartHourPlace)
        ItemEditActivity.HideItem(holder.timeEndHourPlace)
        ItemEditActivity.HideItem(holder.timePointPlace)
        ItemEditActivity.HideItem(holder.timeDeleteButtonPlace)
        ItemEditActivity.HideItem(holder.timeRemindRepeatPlace)
        ItemEditActivity.HideItem(holder.timeRemindTimePlace)
        ItemEditActivity.HideItem(holder.timeRemindTypePlace)
        holder.theView.setBackgroundColor(colorWhite)

        //名称
        var item_name:String = mCurrentItem.name
        holder.itemName.setText(item_name)
        holder.itemName.addTextChangedListener(holder.itemNameChanger)

        //类别
        var item_type: CalendarItemType = mCurrentItem.type
        var item_type_string: String = item_type.chineseName
        holder.itemType.setText(item_type_string)
        holder.itemType.setOnClickListener(View.OnClickListener() {
            if (holder.pvItemTypeOptions != null) {
                holder.pvItemTypeOptions.show(holder.itemType);
            }
        })


        //教师，课程号，detail
        if(item_type == CalendarItemType.COURSE) {
            //教师，课程号显示，其余隐藏
            ItemEditActivity.ShowItem(holder.itemTeacherPlace)
            ItemEditActivity.ShowItem(holder.itemCourseIDPlace)
            ItemEditActivity.HideItem(holder.itemAssociationPlace)

            //设置教师，课程号初值
            var item_teacher:String? = mCurrentItem.detail[CalendarItemLegalDetailKey.TEACHER]
            if(item_teacher == null) {
                item_teacher = ""
            }

            holder.itemTeacher.setText(item_teacher)
            holder.itemTeacher.addTextChangedListener(holder.itemTeacherChanger)


            var item_course_id:String? = mCurrentItem.detail[CalendarItemLegalDetailKey.COURSEID]
            if(item_course_id == null) {
                item_course_id = ""
            }

            holder.itemCourseID.setText(item_course_id)
            holder.itemCourseID.addTextChangedListener(holder.itemCourseIDChanger)
        }
        else if(item_type == CalendarItemType.SOCIALWORK || item_type == CalendarItemType.ASSOCIATION) {
            //组织显示，其余隐藏
            ItemEditActivity.HideItem(holder.itemTeacherPlace)
            ItemEditActivity.HideItem(holder.itemCourseIDPlace)
            ItemEditActivity.ShowItem(holder.itemAssociationPlace)


            //设置组织初值
            var item_association: String? = mCurrentItem!!.detail[CalendarItemLegalDetailKey.ORGANIZATION]
            if (item_association == null) {
                item_association = ""
            }
            holder.itemAssociation.setText(item_association)
            holder.itemAssociation.addTextChangedListener(holder.itemAssociationChanger)
        }
        else {
            //全隐藏
            ItemEditActivity.HideItem(holder.itemTeacherPlace)
            ItemEditActivity.HideItem(holder.itemCourseIDPlace)
            ItemEditActivity.HideItem(holder.itemAssociationPlace)
        }

        //详情
        var item_comment:String? = mCurrentItem!!.detail[CalendarItemLegalDetailKey.COMMENT]
        if(item_comment == null) {
            item_comment = ""
        }
        holder.timeComment.setText(item_comment)
        holder.timeComment.addTextChangedListener(holder.timeDetailChanger)
    }

    /**
     * 描述：onbindholder中使用，用于设置一个time对应的信息
     * 参数：对应的holder，和数据对应position（在timelist的数据）
     * 返回：无
     */
    fun showDataTime(holder: ItemEditHolder, position: Int) {
        //隐藏全部上方item信息，显示下方time信息
        ItemEditActivity.HideItem(holder.itemNamePlace)
        ItemEditActivity.HideItem(holder.itemTypePlace)
        ItemEditActivity.HideItem(holder.itemTeacherPlace)
        ItemEditActivity.HideItem(holder.itemCourseIDPlace)
        ItemEditActivity.HideItem(holder.itemAssociationPlace)
        ItemEditActivity.ShowItem(holder.timeNamePlace)
        ItemEditActivity.ShowItem(holder.timePlacePlace)
        ItemEditActivity.ShowItem(holder.timeTypePlace)
        ItemEditActivity.ShowItem(holder.timeWeekPlace)
        ItemEditActivity.ShowItem(holder.timeDayWeekPlace)
        ItemEditActivity.ShowItem(holder.timeDatePlace)
        ItemEditActivity.ShowItem(holder.timeStartCoursePlace)
        ItemEditActivity.ShowItem(holder.timeLengthCoursePlace)
        ItemEditActivity.ShowItem(holder.timeStartHourPlace)
        ItemEditActivity.ShowItem(holder.timeEndHourPlace)
        ItemEditActivity.ShowItem(holder.timePointPlace)
        ItemEditActivity.ShowItem(holder.timeDeleteButtonPlace)
        ItemEditActivity.ShowItem(holder.timeRemindRepeatPlace)
        ItemEditActivity.ShowItem(holder.timeRemindTimePlace)
        ItemEditActivity.ShowItem(holder.timeRemindTypePlace)
        ItemEditActivity.ShowItem(holder.timeCommentPlace)
        holder.theView.setBackgroundColor(colorGrey)


        val time: CalendarTimeData = mCurrentItem.times[position]
        //名称
        holder.timeName.setText(time.name)
        holder.timeName.addTextChangedListener(holder.timeNameChanger)

        //地点
        holder.timePlace.setText(time.place)
        holder.timePlace.addTextChangedListener(holder.timePlaceChanger)

        //类别
        holder.timeType.setText(time.type.chineseName)
        holder.timeType.setOnClickListener(View.OnClickListener() {
            if (holder.pvTimeTypeOptions != null) {
                holder.pvTimeTypeOptions.show(holder.timeType);//弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
            }
        })

        //日期等
        if(time.type == CalendarTimeType.REPEAT_COURSE || time.type == CalendarTimeType.REPEAT_HOUR) {
            //周，星期显示，日期隐藏
            ItemEditActivity.ShowItem(holder.timeWeekPlace)
            ItemEditActivity.ShowItem(holder.timeDayWeekPlace)
            ItemEditActivity.HideItem(holder.timeDatePlace)

            //设置周，星期初值
            var week_show: String = ItemEditActivity.getWeeksString(time.repeatWeeks)
            holder.timeWeek.setText(week_show)
            holder.timeWeek.setOnClickListener(View.OnClickListener() {
                if (holder.pvWeekOptions != null) {
                    var selected:ArrayList<Int> = arrayListOf()
                    for(item in time.repeatWeeks) {
                        selected.add(item)
                    }
                    holder.pvWeekOptions.initialSelectedWeeks = selected
                    holder.pvWeekOptions.show();//弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
                }
            })

            var day_in_week:String = ""
            if(time.timeInCourseSchedule != null) {
                day_in_week = time.timeInCourseSchedule!!.dayOfWeek!!.chineseName
            }
            else if(time.timeInHour != null){
                day_in_week = time.timeInHour!!.dayOfWeek!!.chineseName
            }

            holder.timeDayWeek.setText(day_in_week)
            holder.timeDayWeek.setOnClickListener(View.OnClickListener() {
                if (holder.pvWeekDayOptions != null) {
                    holder.pvWeekDayOptions.show(holder.timeDayWeek);//弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
                }
            })

        }
        else {
            //日期显示，周，星期隐藏
            ItemEditActivity.HideItem(holder.timeWeekPlace)
            ItemEditActivity.HideItem(holder.timeDayWeekPlace)
            ItemEditActivity.ShowItem(holder.timeDatePlace)

            //设置日期初值
            var date: LocalDate = LocalDate.now()
            if(time.timeInCourseSchedule != null) {
                date = time.timeInCourseSchedule!!.date!!
            }
            else if(time.timeInHour != null){
                date = time.timeInHour!!.date!!
            }
            var date_string = date.toString()
            holder.timeDate.setText(date_string)
            //绑定选择器
            holder.timeDate.setOnClickListener(View.OnClickListener() {
                if (holder.pvDate != null) {
                    holder.pvDate.setDate(Calendar.getInstance());
                    holder.pvDate.show(holder.timeDate);//弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
                }
            })
        }


        //时间等
        if(time.type == CalendarTimeType.REPEAT_COURSE || time.type == CalendarTimeType.SINGLE_COURSE) {
            //开始大节，时长显示，开始时间，结束时间，时间隐藏
            ItemEditActivity.ShowItem(holder.timeStartCoursePlace)
            ItemEditActivity.ShowItem(holder.timeLengthCoursePlace)
            ItemEditActivity.HideItem(holder.timeStartHourPlace)
            ItemEditActivity.HideItem(holder.timeEndHourPlace)
            ItemEditActivity.HideItem(holder.timePointPlace)

            //设置初值
            var start_big = time.timeInCourseSchedule!!.startBig
            var start_small = time.timeInCourseSchedule!!.startOffsetSmall.toInt()
            var start_string = "第" + start_big + "大节"
            if(start_small != 0) {
                start_string = start_string +"第" + (start_small + 1) + "小节"
            }
            holder.timeStartCourse.setText(start_string)
            holder.timeStartCourse.setOnClickListener(View.OnClickListener() {
                if (holder.pvCourseOptions != null) {
                    holder.pvCourseOptions.show(holder.timeStartCourse);//弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
                }
            })


            var length_string:String = "" + time.timeInCourseSchedule!!.lengthSmall.toInt()
            holder.timeLengthCourse.setText(length_string)
            holder.timeLengthCourse.setOnClickListener(View.OnClickListener() {
                if (holder.pvLengthOptions != null) {
                    holder.pvLengthOptions.show(holder.timeLengthCourse);//弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
                }
            })

        }
        else if(time.type == CalendarTimeType.REPEAT_HOUR || time.type == CalendarTimeType.SINGLE_HOUR){
            //开始时间，结束时间显示，开始大节，结束大节，时间隐藏
            ItemEditActivity.HideItem(holder.timeStartCoursePlace)
            ItemEditActivity.HideItem(holder.timeLengthCoursePlace)
            ItemEditActivity.ShowItem(holder.timeStartHourPlace)
            ItemEditActivity.ShowItem(holder.timeEndHourPlace)
            ItemEditActivity.HideItem(holder.timePointPlace)

            var start_string:String = ItemEditActivity.getTimeString(time.timeInHour!!.startTime)

            holder.timeStartHour.setText(start_string)
            holder.timeStartHour.setTag(1)

            //绑定选择器
            holder.timeStartHour.setOnClickListener(View.OnClickListener() {
                if (holder.pvTime != null) {
                    holder.pvTime.setDate(Calendar.getInstance());
                    holder.pvTime.show(holder.timeStartHour);//弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
                }
            })

            var end_string:String = ItemEditActivity.getTimeString(time.timeInHour!!.endTime)
            holder.timeEndHour.setText(end_string)
            holder.timeEndHour.setTag(2)
            //绑定选择器
            holder.timeEndHour.setOnClickListener(View.OnClickListener() {
                if (holder.pvTime != null) {
                    holder.pvTime.setDate(Calendar.getInstance());
                    holder.pvTime.show(holder.timeEndHour);//弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
                }
            })
        }
        else {
            //时间显示，其余隐藏
            ItemEditActivity.HideItem(holder.timeStartCoursePlace)
            ItemEditActivity.HideItem(holder.timeLengthCoursePlace)
            ItemEditActivity.HideItem(holder.timeStartHourPlace)
            ItemEditActivity.HideItem(holder.timeEndHourPlace)
            ItemEditActivity.ShowItem(holder.timePointPlace)

            var time_string:String = ItemEditActivity.getTimeString(time.timeInHour!!.startTime)
            holder.timePoint.setText(time_string)
            holder.timePoint.setTag(3)
            //绑定选择器
            holder.timePoint.setOnClickListener(View.OnClickListener() {
                if (holder.pvTime != null) {
                    holder.pvTime.setDate(Calendar.getInstance());
                    holder.pvTime.show(holder.timePoint);//弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
                }
            })
        }

        //说明
        holder.timeComment.setText(time.comment)
        holder.timeComment.addTextChangedListener(holder.timeDetailChanger)

        //重复活动才能设置每次，其余没有
        if(time.type == CalendarTimeType.REPEAT_COURSE || time.type == CalendarTimeType.REPEAT_HOUR) {
            holder.timeRemindRepeat.setText(time.remindData.type.chineseName)
            holder.timeRemindRepeat.setOnClickListener(View.OnClickListener() {
                if (holder.pvRemindRepeatRegular != null) {
                    holder.pvRemindRepeatRegular.show(holder.timeRemindRepeat);//弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
                }
            })
        }
        else {
            if(time.remindData.type == CalendarRemindType.REPEAT) {
                time.remindData.type = CalendarRemindType.SINGAL
            }
            holder.timeRemindRepeat.setText(time.remindData.type.chineseName)
            holder.timeRemindRepeat.setOnClickListener(View.OnClickListener() {
                if (holder.pvRemindRepeatSingle != null) {
                    holder.pvRemindRepeatSingle.show(holder.timeRemindRepeat);//弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
                }
            })
        }

        //显示提前时间和提醒类型
        if(time.remindData.type != CalendarRemindType.NONE) {
            ItemEditActivity.ShowItem(holder.timeRemindTimePlace)
            ItemEditActivity.ShowItem(holder.timeRemindTypePlace)


            holder.timeRemindTime.setText("提前" + time.remindData.aheadTime.toMinutes() + "分钟提醒")
            holder.timeRemindTime.setOnClickListener() {
                if (holder.pvRemindTime != null) {
                    var calendar:Calendar = Calendar.getInstance()
                    calendar.set(Calendar.MINUTE, 5)
                    holder.pvRemindTime.setDate(calendar);
                    holder.pvRemindTime.show(holder.timeRemindTime);//弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
                }
            }

            holder.timeRemindType.setText(time.remindData.method.chineseName)
            holder.timeRemindType.setOnClickListener(View.OnClickListener() {
                    if (holder.pvRemindType != null) {
                        holder.pvRemindType.show(holder.timeRemindType);//弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
                    }
                })
        }
        else {
            ItemEditActivity.HideItem(holder.timeRemindTimePlace)
            ItemEditActivity.HideItem(holder.timeRemindTypePlace)
        }
    }

    /**
     * 描述：获取数组长度
     * 参数：无
     * 返回：长度
     */
    override fun getItemCount(): Int {
        return mCurrentItem.times.size + 1
    }




}
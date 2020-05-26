package cn.starrah.thu_course_helper.activity

//import butterknife.Bind
//import butterknife.ButterKnife
import cn.starrah.thu_course_helper.R
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarItemDataWithTimes
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeData
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarItemLegalDetailKey
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarItemType
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarTimeType
import cn.starrah.thu_course_helper.data.declares.time.TimeInCourseSchedule
import cn.starrah.thu_course_helper.data.utils.getNotNullValue
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.bigkoo.pickerview.view.OptionsPickerView
import kotlinx.coroutines.launch
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

    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: ItemEditAdapter? = null

    //各种edittext的监听器
    //name监听器
    public lateinit var nameChanger:TextWatcher
    //teacher监听器
    public lateinit var teacherChanger:TextWatcher
    //courseid监听器
    public lateinit var courseIDChanger:TextWatcher
    //association监听器
    public lateinit var associationChanger:TextWatcher
    //comment监听器
    public lateinit var detailChanger:TextWatcher

    //时间类别选择，用于选择对应的时间类别
    private val typeChoices: ArrayList<String> = arrayListOf("课程", "科研", "社工", "社团", "其他")
    private lateinit var pvTypeOptions: OptionsPickerView<Any>

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
         *描述：隐藏控件
         *参数：自己
         *返回：无
         */
        fun HideItem(item:LinearLayout) {
            var params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , 0);
            item.layoutParams = params
        }

        /**
         *描述：显示控件
         *参数：自己
         *返回：无
         */
        fun ShowItem(item:LinearLayout) {
            //和style一致
            var params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , 140);
            item.layoutParams = params
        }
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

        initNameChanger()
        initTeacherChanger()
        initCourseIDChanger()
        initAssociationChanger()
        initDetailChanger()
        initTypeOptionPicker()

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

            mRecyclerView = findViewById(R.id.recycler_view)
            mAdapter = ItemEditAdapter(currentItem!!.times!!, this@ItemEditActivity)
            mRecyclerView!!.setAdapter(mAdapter)
            mRecyclerView!!.setLayoutManager(LinearLayoutManager(this@ItemEditActivity))
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
            Toast.makeText(this, "未找到数据!", Toast.LENGTH_LONG).show()
            finish()
        }
        currentItem = the_item.getNotNullValue()[0]
        if(currentItem == null) {
            Toast.makeText(this, "未找到数据!", Toast.LENGTH_LONG).show()
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
        item_edit_name.addTextChangedListener(nameChanger)

        //类别
        var item_type:CalendarItemType = currentItem!!.type
        var item_type_string: String = currentItem!!.type.chineseName
        var edit_type: TextView = findViewById(R.id.item_edit_type)
        edit_type.text = item_type_string
        edit_type.setOnClickListener(View.OnClickListener() {
            if (pvTypeOptions != null) {
                pvTypeOptions.show(edit_type);
            }
        })

        var edit_teacher: EditText = findViewById(R.id.item_edit_teacher)
        var edit_course_id: EditText = findViewById(R.id.item_edit_course_id)
        var edit_association: TextView = findViewById(R.id.item_edit_association)
        var edit_teacher_place: LinearLayout = findViewById(R.id.item_edit_teacher_place)
        var edit_course_id_place: LinearLayout = findViewById(R.id.item_edit_course_id_place)
        var edit_association_place: LinearLayout = findViewById(R.id.item_edit_association_place)

        //教师，课程号，detail
        if(item_type == CalendarItemType.COURSE) {
            //教师，课程号显示，其余隐藏
            ShowItem(edit_teacher_place)
            ShowItem(edit_course_id_place)
            HideItem(edit_association_place)

            //设置教师，课程号初值
            var item_teacher:String? = currentItem!!.detail[CalendarItemLegalDetailKey.TEACHER]
            if(item_teacher == null) {
                item_teacher = ""
            }

            edit_teacher.setText(item_teacher)
            edit_teacher.addTextChangedListener(teacherChanger)


            var item_course_id:String? = currentItem!!.detail[CalendarItemLegalDetailKey.COURSEID]
            if(item_course_id == null) {
                item_course_id = ""
            }

            edit_course_id.setText(item_course_id)
            edit_course_id.addTextChangedListener(courseIDChanger)
        }
        else if(item_type == CalendarItemType.SOCIALWORK || item_type == CalendarItemType.ASSOCIATION) {
            //组织显示，其余隐藏
            HideItem(edit_teacher_place)
            HideItem(edit_course_id_place)
            ShowItem(edit_association_place)


            //设置组织初值
            var item_association: String? =
                currentItem!!.detail[CalendarItemLegalDetailKey.ORGANIZATION]
            if (item_association == null) {
                item_association = ""
            }
            edit_association.setText(item_association)
            edit_association.addTextChangedListener(associationChanger)

        }
        else {
            //全隐藏
            HideItem(edit_teacher_place)
            HideItem(edit_course_id_place)
            HideItem(edit_association_place)
        }

        //详情
        var item_comment:String? = currentItem!!.detail[CalendarItemLegalDetailKey.COMMENT]
        var edit_comment: EditText = findViewById(R.id.item_edit_comment)
        if(item_comment == null) {
            item_comment = ""
        }
        edit_comment.setText(item_comment)
        edit_comment.addTextChangedListener(detailChanger)
    }


    /**
     * 描述：根据修改的类别信息来修改数据和显示
     * 参数：一个int，从0开始，对应修改后当前日程的类型在枚举里对应的数值
     * 返回：无
     */
    fun handleTypeChange(new_type_int:Int) {
        var the_type:CalendarItemType = CalendarItemType.COURSE
        for(type in CalendarItemType.values()) {
            if(type.ordinal == new_type_int) {
                the_type = type
                break
            }
        }
        currentItem!!.type = the_type
        var type_edit:TextView = findViewById(R.id.item_edit_type)
        type_edit.setText(the_type.chineseName)

        var teacher_place:LinearLayout = findViewById(R.id.item_edit_teacher_place)
        var course_id_place:LinearLayout = findViewById(R.id.item_edit_course_id_place)
        var association_place:LinearLayout = findViewById(R.id.item_edit_association_place)
        var teacher_edit:EditText = findViewById(R.id.item_edit_teacher)
        var course_id_edit:EditText = findViewById(R.id.item_edit_course_id)
        var association_edit:EditText = findViewById(R.id.item_edit_association)
        var teacher_string:String = ""
        var course_id_string:String = ""
        var association_string:String = ""
        if(the_type == CalendarItemType.COURSE) {
            //修改数据
            var the_teacher = currentItem!!.detail.get(CalendarItemLegalDetailKey.TEACHER)
            if(the_teacher != null) {
                teacher_string = the_teacher
            }
            var the_course_id = currentItem!!.detail.get(CalendarItemLegalDetailKey.COURSEID)
            if(the_course_id != null) {
                course_id_string = the_course_id
            }
            currentItem!!.detail.remove(CalendarItemLegalDetailKey.ORGANIZATION)

            //修改显示
            ShowItem(teacher_place)
            ShowItem(course_id_place)
            HideItem(association_place)
            teacher_edit.setText(teacher_string)
            course_id_edit.setText(course_id_string)
        }
        else if(the_type == CalendarItemType.ASSOCIATION || the_type == CalendarItemType.SOCIALWORK) {
            //修改数据
            currentItem!!.detail.remove(CalendarItemLegalDetailKey.TEACHER)
            currentItem!!.detail.remove(CalendarItemLegalDetailKey.COURSEID)
            var the_org_id = currentItem!!.detail.get(CalendarItemLegalDetailKey.ORGANIZATION)
            if(the_org_id != null) {
                association_string = the_org_id
            }

            //修改显示
            HideItem(teacher_place)
            HideItem(course_id_place)
            ShowItem(association_place)
            association_edit.setText(association_string)
        }
        else {
            //修改数据
            currentItem!!.detail.remove(CalendarItemLegalDetailKey.ORGANIZATION)
            currentItem!!.detail.remove(CalendarItemLegalDetailKey.TEACHER)
            currentItem!!.detail.remove(CalendarItemLegalDetailKey.COURSEID)

            //修改显示
            HideItem(teacher_place)
            HideItem(course_id_place)
            HideItem(association_place)
        }

    }

    /**
     * 描述：用于保存时，用于判断当前currentItem是否符合要求
     * 参数：无
     * 返回：符合true，不符合false,并且报错
     */
    fun integrityCheck() :Boolean{
        if(currentItem == null) {
            Toast.makeText(this, "未找到数据!", Toast.LENGTH_LONG).show()
            return false
        }
        if(currentItem!!.times == null || currentItem!!.times.size < 1) {
            Toast.makeText(this, "当前日程没有任何时间段!", Toast.LENGTH_LONG).show()
            return false
        }
        //TODO
        return true
    }

    /**
     * 描述：保存修改
     * 参数：无
     * 返回：无
     */
    suspend fun saveItem() {
        if(currentID < 0) {
            currentItem!!.id = 0
        }
        CREP.updateItemAndTimes(currentItem!!)
    }

    //字符编辑绑定加载函数
    /**
     * 描述：初始化名称监听器
     * 参数：无
     * 返回：无
     */
    private fun initNameChanger() {
        nameChanger = (object : TextWatcher {
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
                currentItem!!.name = editable.toString()
            }
        })
    }
    /**
     * 描述：初始化教师监听器
     * 参数：无
     * 返回：无
     */
    private fun initTeacherChanger() {
        teacherChanger = (object : TextWatcher {
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
                if (currentItem!!.type.equals(CalendarItemType.COURSE)) {
                    currentItem!!.detail[CalendarItemLegalDetailKey.TEACHER] = editable.toString()
                }
            }
        })
    }

    /**
     * 描述：初始化课程号监听器
     * 参数：无
     * 返回：无
     */
    private fun initCourseIDChanger() {
        courseIDChanger = (object : TextWatcher {
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
                if(currentItem!!.type.equals(CalendarItemType.COURSE)) {
                    currentItem!!.detail[CalendarItemLegalDetailKey.COURSEID] = editable.toString()
                }
            }
        })
    }

    /**
     * 描述：初始化组织监听器
     * 参数：无
     * 返回：无
     */
    private fun initAssociationChanger() {
        associationChanger = (object : TextWatcher {
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
                if(currentItem!!.type.equals(CalendarItemType.SOCIALWORK) || currentItem!!.type.equals(CalendarItemType.ASSOCIATION)) {
                    currentItem!!.detail[CalendarItemLegalDetailKey.ORGANIZATION] = editable.toString()
                }
            }
        })
    }



    /**
     * 描述：初始化详情监听器
     * 参数：无
     * 返回：无
     */
    private fun initDetailChanger() {
        detailChanger = (object : TextWatcher {
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
                currentItem!!.detail[CalendarItemLegalDetailKey.COMMENT] = editable.toString()
            }
        })
    }

    //选择器加载函数
    /**
     * 描述：加载类别选择器
     * 参数：无
     * 返回：无
     */
    private fun initTypeOptionPicker() {
        pvTypeOptions = OptionsPickerBuilder(this,
            OnOptionsSelectListener { options1, options2, options3, v -> //返回的分别是三个级别的选中位置
                handleTypeChange(options1)
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
        pvTypeOptions.setPicker(typeChoices as List<Any>?) //一级选择器
    }

    //对话框显示函数
    /**
     * 描述：返回时的对话框，如果确定，就不保存就返回，否则继续
     * 参数：无
     * 返回：无
     */
    private fun showDialogReturn() {
        val dialog: AlertDialog.Builder =
            object : AlertDialog.Builder(this@ItemEditActivity) {
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
            .setTitle("返回详情")
            .setMessage("您的编辑未保存，确定要不保存直接退出吗？")
            .setCancelable(true)
            .setPositiveButton("确定",
                DialogInterface.OnClickListener { dialog, which -> finish() })
            .setNegativeButton("取消",
                DialogInterface.OnClickListener { dialog, which ->  })
        dialog.show()
    }

    /**
     * 描述：保存时的对话框，如果确定，就保存然后返回，否则取消
     * 参数：无
     * 返回：无
     */
    private fun showDialogSave() {
        val dialog: AlertDialog.Builder =
            object : AlertDialog.Builder(this@ItemEditActivity) {
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
            .setTitle("保存日程")
            .setMessage("确定要保存日程吗")
            .setCancelable(true)
            .setPositiveButton("确定",
                DialogInterface.OnClickListener { dialog, which ->
                    lifecycleScope.launch{
                        saveItem()
                        finish()
                    }
                })
            .setNegativeButton("取消",
                DialogInterface.OnClickListener { dialog, which ->  })
        dialog.show()
    }

    //按钮绑定函数
    /**
     * 描述：处理返回按钮的事件--返回
     * 参数：无
     * 返回：无
     */
    fun handleReturn(view: View) {
        showDialogReturn()
    }

    /**
     * 描述：处理添加按钮的事件--添加
     * 参数：无
     * 返回：无
     */
    fun handleAdd(view: View) {
        val new_time_data:TimeInCourseSchedule = TimeInCourseSchedule(dayOfWeek = LocalDate.now().dayOfWeek, date = LocalDate.now(), startBig = 1)
        val newTime:CalendarTimeData = CalendarTimeData(type = CalendarTimeType.SINGLE_COURSE, timeInCourseSchedule = new_time_data, timeInHour = null)
        currentItem!!.times.add(newTime)
        mAdapter!!.notifyDataSetChanged()
        mRecyclerView!!.scrollToPosition(mAdapter!!.itemCount - 1)
    }

    /**
     * 描述：处理保存按钮的事件--保存并且返回
     * 参数：无
     * 返回：无
     */
    fun handleSave(view: View) {
        //判断是否合法
        if(integrityCheck()) {
            //对话框，保存
            showDialogSave()
        }
    }



}
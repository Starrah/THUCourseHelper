@file:Suppress("UNUSED_ANONYMOUS_PARAMETER", "UNUSED_PARAMETER")

package cn.starrah.thu_course_helper.activity


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
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarItemDataWithTimes
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeData
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarItemLegalDetailKey
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarItemType
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarRemindType
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarTimeType
import cn.starrah.thu_course_helper.data.utils.chineseName
import cn.starrah.thu_course_helper.data.utils.getNotNullValue
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter


/**
 * 描述：显示某个日程和全部的activity类
 */
class ItemShowActivity : AppCompatActivity(){

    private var showItem: CalendarItemDataWithTimes? = null;
    private var showID:Int = -1;

    protected var showTeacher: String = ""
    protected var showCourseID: String = ""
    protected var showAssociation: String = ""
    protected var showComment: String = ""

    companion object {
        public val EXTRA_MESSAGE = "cn.starrah.thu_course_helper.extra.MESSAGE"
        public val EDIT_CODE = 1024

        /**
         *描述：隐藏控件
         *参数：自己
         *返回：无
         */
        fun HideItem(item:LinearLayout) {
            val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , 0);
            item.layoutParams = params
        }

        /**
         *描述：显示控件
         *参数：自己
         *返回：无
         */
        fun ShowItem(item:LinearLayout) {
            //和style一致
            val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , 65);
            item.layoutParams = params
        }
    }


    /**
     * 描述：根据id从数据库获取数据，id是类变量showID，读取的数据存在showItem里
     * 参数：无
     * 返回：无
     */
    suspend fun getData() {
        //数据获取
        val list: List<Int> = listOf(showID)
        val the_item = CREP.findItemsByIds(list)
        val size = the_item.getNotNullValue().size
        if(size <= 0 || size > 1) {
            Toast.makeText(this, "未找到数据!", Toast.LENGTH_LONG).show()
            finish()
        }
        showItem = the_item.getNotNullValue()[0]
        if(showItem == null) {
            Toast.makeText(this, "未找到数据!", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    /**
     * 描述：初始化
     * @param savedInstanceState 存储的data，其实只有待显示活动的ID
     */
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_item_show)
        //ButterKnife.bind(this)

        showTeacher = resources.getString(R.string.item_teacher).toString();
        showCourseID = resources.getString(R.string.item_course_id).toString();
        showAssociation = resources.getString(R.string.item_association).toString();
        showComment = resources.getString(R.string.item_comment).toString();


        val intent = intent
        val message = intent.getIntExtra(TableFragment.EXTRA_MESSAGE, -1)
        if(message < 0) {
            Toast.makeText(this, "未找到数据!", Toast.LENGTH_LONG).show()
            finish()
        }
        showID = message
        lifecycleScope.launch {
            getData()
            showData()
        }
    }

    /**
     * 描述：根据读取的数据showItem修改显示界面
     * 参数：无
     * 返回：无
     */
    suspend fun showData() {

        //名称
        val item_name:String = showItem!!.name
        val item_show_name:TextView = findViewById(R.id.item_show_name)
        item_show_name.setText(item_name)
        val item_show_top:TextView = findViewById(R.id.item_show_top)
        item_show_top.setText(item_name)

        //教师，课程号，detail
        val item_teacher:String? = showItem!!.detail[CalendarItemLegalDetailKey.TEACHER]
        val show_teacher_place:LinearLayout = findViewById(R.id.item_show_teacher_place)
        val show_teacher: TextView = findViewById(R.id.item_show_teacher)
        if(item_teacher == null) {
            HideItem(show_teacher_place)
        }
        else {
            val show_item_teacher = showTeacher + ": " + item_teacher;
            ShowItem(show_teacher_place)
            show_teacher.setText(show_item_teacher)
        }


        val item_course_id:String? = showItem!!.detail[CalendarItemLegalDetailKey.COURSEID]
        val show_course_id: TextView = findViewById(R.id.item_show_course_id)
        val show_course_id_place: LinearLayout = findViewById(R.id.item_show_course_id_place)

        if(item_course_id == null) {
            HideItem(show_course_id_place)
        }
        else {
            val show_item_course_id = showCourseID + ": " + item_course_id;
            ShowItem(show_course_id_place)
            show_course_id.setText(show_item_course_id)
        }

        val item_association: String? =
            showItem!!.detail[CalendarItemLegalDetailKey.ORGANIZATION]
        val show_association: TextView = findViewById(R.id.item_show_association)
        val show_association_place: LinearLayout = findViewById(R.id.item_show_association_place)

        if (item_association == null) {
            HideItem(show_association_place)
        }
        else {
            val show_item_association = showAssociation + ": " + item_association;
            ShowItem(show_association_place)
            show_association.setText(show_item_association)
        }

        //详情
        var item_comment:String? = showItem!!.detail[CalendarItemLegalDetailKey.COMMENT]
        val show_comment:TextView = findViewById(R.id.item_show_comment)
        if(item_comment == null) {
            item_comment = ""
        }
        val show_item_comment = showComment + ": " + item_comment;
        show_comment.setText(show_item_comment)

        //具体下面
        val parent_place = findViewById<LinearLayout>(R.id.new_time_place_show)
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
        val time_name:String = time.name
        val show_name:TextView = layout.findViewById(R.id.time_show_name)
        show_name.setText(time_name)

        val show_date:TextView = layout.findViewById(R.id.time_show_date)
        val show_time:TextView = layout.findViewById(R.id.time_show_time)

        //日期和时间
        if(time.type == CalendarTimeType.REPEAT_COURSE) {
            //时间---周三第三大节
            val schedule = time.timeInCourseSchedule
            val day_time:String = schedule!!.chineseName
            show_time.setText(day_time)

            //日期---后八周
            val week_list = time.repeatWeeks
            val week_show = ItemEditActivity.getWeeksString(week_list)
            show_date.setText(week_show)

        }
        else if(time.type == CalendarTimeType.REPEAT_HOUR) {
            //时间---周三9:00-10:00
            val schedule = time.timeInHour
            val start_time:String = ItemEditActivity.getTimeString(schedule!!.startTime)
            val end_time:String = ItemEditActivity.getTimeString(schedule.endTime)
            val week_day = schedule.dayOfWeek
            val week_day_string:String = week_day!!.chineseName
            val day_time:String = week_day_string + start_time  + "-" + end_time
            show_time.setText(day_time)

            //日期--后八周
            val week_list = time.repeatWeeks
            val week_show = ItemEditActivity.getWeeksString(week_list)
            show_date.setText(week_show)
        }
        else if(time.type == CalendarTimeType.SINGLE_COURSE) {
            //时间---周五第三大节
            val schedule = time.timeInCourseSchedule
            val day_time:String = schedule!!.chineseName
            show_time.setText(day_time)

            //日期--第14周周五（5月22日）
            val the_date = schedule.date
            val time_month = the_date!!.month.value
            val time_day = the_date.dayOfMonth
            val the_week_num = CREP.term.dateToWeekNumber(the_date)
            val the_week_day = the_date.dayOfWeek.chineseName
            val date_string = "第" + the_week_num + "周" + the_week_day + "(" + time_month + "月" + time_day + "日)"
            show_date.setText(date_string)
        }
        else if(time.type == CalendarTimeType.SINGLE_HOUR) {
            //时间---周五9:00-10:00
            val schedule = time.timeInHour
            val start_time:String = ItemEditActivity.getTimeString(schedule!!.startTime)
            val end_time:String = ItemEditActivity.getTimeString(schedule.endTime)
            var week_day = schedule.dayOfWeek
            if(week_day == null) {
                week_day = schedule.date!!.dayOfWeek
            }
            val week_day_string:String = week_day!!.chineseName
            val day_time:String = week_day_string + start_time  + "-" + end_time
            show_time.setText(day_time)

            //日期--5月22日
            val the_date_year = schedule.date!!.year
            val the_date_month = schedule.date!!.month.value
            val the_date_day = schedule.date!!.dayOfMonth
            val date_string:String = "" + the_date_year + "年" + the_date_month + "月" + the_date_day + "日"
            show_date.setText(date_string)
        }
        else if(time.type == CalendarTimeType.POINT) {
            //时间---周五9:00
            val schedule = time.timeInHour
            val time:String = ItemEditActivity.getTimeString(schedule!!.startTime)
            var week_day = schedule.dayOfWeek
            if(week_day == null) {
                week_day = schedule.date!!.dayOfWeek
            }
            val week_day_string:String = week_day!!.chineseName
            val day_time:String = week_day_string + time
            show_time.setText(day_time)

            //日期--5月22日
            val the_date_year = schedule.date!!.year
            val the_date_month = schedule.date!!.month.value
            val the_date_day = schedule.date!!.dayOfMonth
            val date_string:String = "" + the_date_year + "年" + the_date_month + "月" + the_date_day + "日"
            show_date.setText(date_string)
        }

        //地点
        val time_place:String = time.place
        val show_place:TextView = layout.findViewById(R.id.time_show_place)
        show_place.setText(time_place)

        //说明
        val time_comment:String = time.comment
        val show_comment:TextView = layout.findViewById(R.id.time_show_comment)
        show_comment.setText(time_comment)

        //提醒
        var time_remind: String
        val time_remind_type_string:String = "" + time.remindData.method.chineseName
        val time_remind_time_string:String = ItemEditActivity.getAheadTimeString(time.remindData.aheadTime)
        if(time.remindData.type == CalendarRemindType.NONE) {
            time_remind = "未设置提醒"
        }
        else{
            val time_remind_repeat_string:String = time.remindData.type.chineseName
            time_remind = "提前" + time_remind_time_string + "，"+ time_remind_type_string + "，" +  time_remind_repeat_string
        }
        val show_remind:TextView = layout.findViewById(R.id.time_show_remind)
        show_remind.setText(time_remind)

        //下次提醒时间
        val show_next_remind_place = layout.findViewById<LinearLayout>(R.id.time_show_remind_next_place)
        if(time.remindData.type == CalendarRemindType.NONE) {
            HideItem(show_next_remind_place)
        }
        else {
            val show_next_remind:TextView = layout.findViewById(R.id.time_show_remind_next)
            val new_remind_time = time.nextRemindTime
            var new_remind_time_string: String
            if(new_remind_time == null) {
                new_remind_time_string = "提醒已关闭"
            }
            else {
                val df: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");
                val time_string:String = df.format(new_remind_time);
                new_remind_time_string = "下次提醒时间： " + time_string
            }
            show_next_remind.setText(new_remind_time_string)
        }

        //添加
        parent_place.addView(layout)
    }


    /**
     * 描述：删除时的对话框，如果确定，就删除然后返回，否则取消
     * 参数：无
     * 返回：无
     */
    private fun showDialogDelete() {
        val dialog: AlertDialog.Builder =
            object : AlertDialog.Builder(this@ItemShowActivity) {
                override fun create(): AlertDialog {
                    return super.create()
                }

                override fun show(): AlertDialog {
                    return super.show()
                }
            }
        dialog.setOnCancelListener { }
        dialog.setOnDismissListener { }
        dialog.setIcon(R.drawable.logo)
            .setTitle("删除日程")
            .setMessage("确定要删除日程吗")
            .setCancelable(true)
            .setPositiveButton("确定",
                { _, which ->
                    lifecycleScope.launch{
                        try {
                            CREP.deleteItem(showItem!!)
                            setResult(Activity.RESULT_OK);
                            finish()
                        }
                        catch (e: Exception) {
                            Toast.makeText(this@ItemShowActivity, e.message, Toast.LENGTH_LONG).show()
                        }
                    }
                })
            .setNegativeButton("取消",
                { _, which ->  })
        dialog.show()
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
     */
    fun handleEdit(view: View) {
        val id: Int = showID
        val intent = Intent(this, ItemEditActivity::class.java)
        intent.putExtra(EXTRA_MESSAGE, id)
        startActivityForResult(intent, EDIT_CODE)
    }

    /**
     * 描述：处理从编辑活动退出的情况，如果编辑保存了，就刷新显示
     * 参数：requestcode，resultcode，data
     * 返回：无
     */
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_CODE) {
            //保存了,就刷新显示
            if (resultCode == Activity.RESULT_OK) {
                lifecycleScope.launch() {
                    getData()
                    showData()
                }
            }
        }
    }


    /**
     * 描述：处理删除按钮的事件--删除当前内容，跳转回去
     * 参数：无
     * 返回：无
     */
    fun handleDelete(view: View) {
        showDialogDelete()
    }
}
package cn.starrah.thu_course_helper.activity


import android.R.id.text2
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cn.starrah.thu_course_helper.R
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


/**
 * 描述：显示某个日程和全部的activity类
 */
class ItemShowActivity : AppCompatActivity(){

    private var showItem: CalendarItemDataWithTimes? = null;
    private var showID:Int = -1;


    companion object {
        public val EXTRA_MESSAGE = "cn.starrah.thu_course_helper.extra.MESSAGE"
        public val EDIT_CODE = 1024
    }

    /**
     * 描述：初始化
     * @param savedInstanceState 存储的data，其实只有待显示活动的ID
     */
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_item_show)
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
        var item_teacher:String? = showItem!!.detail[CalendarItemLegalDetailKey.TEACHER]
        var show_teacher_place:LinearLayout = findViewById(R.id.item_show_teacher_place)
        var show_teacher: TextView = findViewById(R.id.item_show_teacher)
        if(item_teacher == null) {
            ItemEditActivity.HideItem(show_teacher_place)
        }
        else {
            ItemEditActivity.ShowItem(show_teacher_place)
            show_teacher.setText(item_teacher)
        }


        var item_course_id:String? = showItem!!.detail[CalendarItemLegalDetailKey.COURSEID]
        var show_course_id: TextView = findViewById(R.id.item_show_course_id)
        var show_course_id_place: LinearLayout = findViewById(R.id.item_show_course_id_place)

        if(item_course_id == null) {
            ItemEditActivity.HideItem(show_course_id_place)
        }
        else {
            ItemEditActivity.ShowItem(show_course_id_place)
            show_course_id.setText(item_course_id)
        }

        var item_association: String? =
            showItem!!.detail[CalendarItemLegalDetailKey.ORGANIZATION]
        var show_association: TextView = findViewById(R.id.item_show_association)
        var show_association_place: LinearLayout = findViewById(R.id.item_show_association_place)

        if (item_association == null) {
            ItemEditActivity.HideItem(show_association_place)
        }
        else {
            ItemEditActivity.ShowItem(show_association_place)
            show_association.setText(item_association)
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
            var week_show = ItemEditActivity.getWeeksString(week_list)
            show_date.setText(week_show)

        }
        else if(time.type == CalendarTimeType.REPEAT_HOUR) {
            //时间---周三9:00-10:00
            var schedule = time.timeInHour
            var start_time:String = ItemEditActivity.getTimeString(schedule!!.startTime)
            var end_time:String = ItemEditActivity.getTimeString(schedule!!.endTime)
            var week_day = schedule!!.dayOfWeek
            var week_day_string:String = week_day!!.chineseName
            var day_time:String = week_day_string + start_time  + "-" + end_time
            show_time.setText(day_time)

            //日期--后八周
            var week_list = time.repeatWeeks
            var week_show = ItemEditActivity.getWeeksString(week_list)
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
            var start_time:String = ItemEditActivity.getTimeString(schedule!!.startTime)
            var end_time:String = ItemEditActivity.getTimeString(schedule!!.endTime)
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
            var time:String = ItemEditActivity.getTimeString(schedule!!.startTime)
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
        var id: Int = showID
        var intent = Intent(this, ItemEditActivity::class.java)
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
        /*lifecycleScope.launch{
            CREP.deleteItem(showItem!!)
            finish()
        }*/
        //TODO
    }
}
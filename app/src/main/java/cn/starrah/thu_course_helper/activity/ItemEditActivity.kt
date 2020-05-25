package cn.starrah.thu_course_helper.activity

//import butterknife.Bind
//import butterknife.ButterKnife

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarItemDataWithTimes
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeData
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarItemLegalDetailKey
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarItemType
import cn.starrah.thu_course_helper.data.utils.getNotNullValue
import kotlinx.coroutines.launch
import java.time.LocalTime


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
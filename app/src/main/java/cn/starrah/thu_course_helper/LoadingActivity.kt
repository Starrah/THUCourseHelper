package cn.starrah.thu_course_helper;

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import cn.starrah.thu_course_helper.data.SPRING2019TERMJSON
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarItemData
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeData
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarTimeType
import cn.starrah.thu_course_helper.data.declares.school.SchoolTerm
import cn.starrah.thu_course_helper.data.declares.time.TimeInCourseSchedule
import com.alibaba.fastjson.JSON
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Thread.sleep
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * 描述：显示一张图片，在这里完成各种加载，然后再跳转到主界面
 */
class LoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loading)


        lifecycleScope.launch{
            val term = JSON.parseObject(SPRING2019TERMJSON, SchoolTerm::class.java)
            CREP.initializeTerm(this@LoadingActivity, term)
            loadTestData()
            delay(8000);


            var the_intent: Intent = Intent()
            the_intent.setClass(this@LoadingActivity, MainActivity::class.java)
            startActivity(the_intent);
            finish();
        }

    }


    /**
    描述:测试函数，上传初始化日程
     */
    suspend fun loadTestData() {

        val item1:CalendarItemData = CalendarItemData(id = 1, name = "数据库")

        val item2:CalendarItemData = CalendarItemData(id = 2, name = "移动软件开发")

        val item3:CalendarItemData = CalendarItemData(id = 3, name = "数据结构")

        val time1: TimeInCourseSchedule = TimeInCourseSchedule(
            startBig = 6, startOffsetSmall = 0.0f,
            lengthSmall = 2.0f, date = LocalDate.parse("2020-05-19")
        )
        val data1: CalendarTimeData = CalendarTimeData(
            name = "摸鱼", type = CalendarTimeType.SINGLE_COURSE
            , timeInCourseSchedule = time1, item_id = 1
        )

        val time2: TimeInCourseSchedule = TimeInCourseSchedule(
            dayOfWeek = DayOfWeek.TUESDAY, startBig = 2, startOffsetSmall = 0.0f,
            lengthSmall = 3.0f
        )
        val data2: CalendarTimeData = CalendarTimeData(
            name = "上课", type = CalendarTimeType.REPEAT_COURSE
            , timeInCourseSchedule = time2, repeatWeeks = mutableListOf(14,15,16), item_id = 1
        )

        val list1 = listOf<CalendarTimeData>(data1, data2)


        val time3: TimeInCourseSchedule = TimeInCourseSchedule(
            startBig = 5, startOffsetSmall = 0.0f,
            lengthSmall = 2.0f, date = LocalDate.parse("2020-05-20")
        )
        val data3: CalendarTimeData = CalendarTimeData(
            name = "摸鱼", type = CalendarTimeType.SINGLE_COURSE
            , timeInCourseSchedule = time3, item_id = 2
        )

        val time4: TimeInCourseSchedule = TimeInCourseSchedule(
            dayOfWeek = DayOfWeek.WEDNESDAY, startBig = 2, startOffsetSmall = 0.0f,
            lengthSmall = 2.0f
        )
        val data4: CalendarTimeData = CalendarTimeData(
            name = "上课", type = CalendarTimeType.REPEAT_COURSE
            , timeInCourseSchedule = time4, repeatWeeks = mutableListOf(14,15,16), item_id = 2
        )

        val list2 = listOf<CalendarTimeData>(data3, data4)


        val time5: TimeInCourseSchedule = TimeInCourseSchedule(
            startBig = 6, startOffsetSmall = 0.0f,
            lengthSmall = 3.0f, date = LocalDate.parse("2020-05-21")
        )
        val data5: CalendarTimeData = CalendarTimeData(
            name = "摸鱼", type = CalendarTimeType.SINGLE_COURSE
            , timeInCourseSchedule = time5, item_id = 3
        )

        val time6: TimeInCourseSchedule = TimeInCourseSchedule(
            dayOfWeek = DayOfWeek.THURSDAY, startBig = 3, startOffsetSmall = 0.0f,
            lengthSmall = 3.0f
        )
        val data6: CalendarTimeData = CalendarTimeData(
            name = "上课", type = CalendarTimeType.REPEAT_COURSE
            , timeInCourseSchedule = time6, repeatWeeks = mutableListOf(12, 14, 16), item_id = 3
        )
        val list3 = listOf<CalendarTimeData>(data5, data6)
        CREP.updateItemAndTimes(item1, list1)
        CREP.updateItemAndTimes(item2, list2)
        CREP.updateItemAndTimes(item3, list3)



    }


}
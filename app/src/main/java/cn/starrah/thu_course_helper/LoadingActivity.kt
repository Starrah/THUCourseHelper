package cn.starrah.thu_course_helper

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.database.CalendarRepository
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarItemData
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeData
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarItemLegalDetailKey
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarItemType
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarTimeType
import cn.starrah.thu_course_helper.data.declares.time.TimeInCourseSchedule
import cn.starrah.thu_course_helper.data.declares.time.TimeInHour
import cn.starrah.thu_course_helper.onlinedata.backend.BACKEND_SITE
import cn.starrah.thu_course_helper.service.initializeAllTimelyIntents
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import kotlin.system.exitProcess

/**
 * 描述：显示一张图片，在这里完成各种加载，然后再跳转到主界面
 */
class LoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loading)

        BACKEND_SITE = try {
            Properties().apply { load(applicationContext.assets.open("backend.properties")) }
                .getProperty("BACKEND_SITE")
        }
        catch (e: Exception) {
            ""
        }

        lifecycleScope.launch {
            val sp = PreferenceManager.getDefaultSharedPreferences(this@LoadingActivity)
            sp.edit {
                //进入时清空当前周
                putInt("currentWeekCourseTable", 0)
                putInt("currentWeekTimeTable", 0)
            }
            val term_id = sp.getString("term_id", null)

            try {
                val currentTerm = CalendarRepository.requestDefaultTerm(this@LoadingActivity)
                CREP.initializeTerm(this@LoadingActivity, currentTerm)
            }
            catch (e: Exception) {
                Toast.makeText(
                    this@LoadingActivity,
                    R.string.errmsg_change_term_fail_exit,
                    Toast.LENGTH_SHORT
                ).show()
                delay(3000)
                exitProcess(0)
            }
            loadTestData()

            initializeAllTimelyIntents(this@LoadingActivity, false)

            val the_intent = Intent()
            the_intent.setClass(this@LoadingActivity, MainActivity::class.java)

            if (CREP.term.termId != term_id) the_intent.putExtra(
                "SHOW_TOAST",
                "${resources.getText(R.string.info_auto_term)}${CREP.term.chineseName}"
            )
            startActivity(the_intent)
            finish()
        }

    }


    //TODO 生成模拟的作业，考试数据，正式版应该删除
    /**
    描述:测试函数，上传初始化日程
     */
    suspend fun loadTestData() {

        val detail1: MutableMap<CalendarItemLegalDetailKey, String> =
            mutableMapOf<CalendarItemLegalDetailKey, String>()
        detail1[CalendarItemLegalDetailKey.FROM_WEB] = "23,learn"
        detail1[CalendarItemLegalDetailKey.COMMENT] = "网络学堂作业"
        val item1: CalendarItemData = CalendarItemData(id = 301, name = "展示视频提交", type = CalendarItemType.OTHER, detail = detail1)

        val detail2: MutableMap<CalendarItemLegalDetailKey, String> =
            mutableMapOf<CalendarItemLegalDetailKey, String>()
        detail2[CalendarItemLegalDetailKey.FROM_WEB] = "21,learn"
        detail2[CalendarItemLegalDetailKey.COMMENT] = "网络学堂作业"
        val item2: CalendarItemData = CalendarItemData(id = 302, name = "最终提交", type = CalendarItemType.OTHER, detail = detail1)

        val detail3: MutableMap<CalendarItemLegalDetailKey, String> =
            mutableMapOf<CalendarItemLegalDetailKey, String>()
        detail3[CalendarItemLegalDetailKey.FROM_WEB] = "25,learn"
        detail3[CalendarItemLegalDetailKey.COMMENT] = "网络学堂作业"
        val item3: CalendarItemData = CalendarItemData(id = 303, name = "最终提交", type = CalendarItemType.OTHER, detail = detail1)

        val detail4: MutableMap<CalendarItemLegalDetailKey, String> =
            mutableMapOf<CalendarItemLegalDetailKey, String>()
        detail4[CalendarItemLegalDetailKey.FROM_WEB] = "29,learn"
        detail4[CalendarItemLegalDetailKey.COMMENT] = "网络学堂作业"
        val item4: CalendarItemData = CalendarItemData(id = 304, name = "作业补交", type = CalendarItemType.OTHER, detail = detail1)

        val time1: TimeInHour = TimeInHour(
            startTime = LocalTime.parse("14:53"),
            endTime = LocalTime.parse("14:53"),
            dayOfWeek = DayOfWeek.SUNDAY,
            date = LocalDate.parse("2020-06-21")
        )
        val data1: CalendarTimeData = CalendarTimeData(
            name = "移动软件开发", type = CalendarTimeType.POINT
            , timeInHour = time1, item_id = 301, comment = "未提交"
        )

        val time2: TimeInHour = TimeInHour(
            startTime = LocalTime.parse("23:59"),
            endTime = LocalTime.parse("23:59"),
            dayOfWeek = DayOfWeek.SUNDAY,
            date = LocalDate.parse("2020-06-21")
        )
        val data2: CalendarTimeData = CalendarTimeData(
            name = "移动软件开发", type = CalendarTimeType.POINT
            , timeInHour = time2, item_id = 302, comment = "未提交"
        )
        val time3: TimeInHour = TimeInHour(
            startTime = LocalTime.parse("14:53"),
            endTime = LocalTime.parse("14:53"),
            dayOfWeek = DayOfWeek.SATURDAY,
            date = LocalDate.parse("2020-06-20")
        )
        val data3: CalendarTimeData = CalendarTimeData(
            name = "数据库原理", type = CalendarTimeType.POINT
            , timeInHour = time3, item_id = 303, comment = "已提交"
        )

        val time4: TimeInHour = TimeInHour(
            startTime = LocalTime.parse("23:59"),
            endTime = LocalTime.parse("23:59"),
            dayOfWeek = DayOfWeek.SATURDAY,
            date = LocalDate.parse("2020-06-20")
        )
        val data4: CalendarTimeData = CalendarTimeData(
            name = "人工智能导论", type = CalendarTimeType.POINT
            , timeInHour = time4, item_id = 304, comment = "已提交"
        )

        val list1 = listOf<CalendarTimeData>(data1)
        var list2 = listOf<CalendarTimeData>(data2)
        var list3 = listOf<CalendarTimeData>(data3)
        var list4 = listOf<CalendarTimeData>(data4)


        CREP.updateItemAndTimes(item1, list1)
        CREP.updateItemAndTimes(item2, list2)
        CREP.updateItemAndTimes(item3, list3)
        CREP.updateItemAndTimes(item4, list4)


    }


}
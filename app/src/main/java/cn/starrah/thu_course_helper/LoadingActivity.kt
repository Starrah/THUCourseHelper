package cn.starrah.thu_course_helper;

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import cn.starrah.thu_course_helper.data.SPRING2019TERMJSON
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarItemData
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeData
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarItemLegalDetailKey
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarItemType
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarTimeType
import cn.starrah.thu_course_helper.data.declares.school.SchoolTerm
import cn.starrah.thu_course_helper.data.declares.school.TermAPIResp
import cn.starrah.thu_course_helper.data.declares.school.TermDescription
import cn.starrah.thu_course_helper.data.declares.time.TimeInCourseSchedule
import cn.starrah.thu_course_helper.data.declares.time.TimeInHour
import cn.starrah.thu_course_helper.onlinedata.backend.BACKEND_SITE
import com.alibaba.fastjson.JSON
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
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
        } catch (e: Exception) { "" }

        lifecycleScope.launch{
                val sp = PreferenceManager.getDefaultSharedPreferences(this@LoadingActivity)
                val version = packageManager.getPackageInfo(packageName, 0).versionName
                val lastStartVersion = sp.getString("lastStartVersion", null)
                val lastSyncDataTime = sp.getString("lastSyncDataTime", null)
                    ?.let { LocalDate.parse(it, DateTimeFormatter.ISO_DATE) }
                val lastSyncDataTimePassed = lastSyncDataTime?.let {
                    Period.between(it, LocalDate.now()).get(ChronoUnit.DAYS).toInt()
                }?: 100000
                val lastHandChangeTermTime = sp.getString("lastHandChangeTermTime", null)
                    ?.let { LocalDate.parse(it, DateTimeFormatter.ISO_DATE) }
                val lastHandChangeTermTimePassed = lastHandChangeTermTime?.let {
                    Period.between(it, LocalDate.now()).get(ChronoUnit.DAYS).toInt()
                }?: 100000
                var currentTerm = sp.getString("currentTerm", null)
                    ?.let { JSON.parseObject(it, SchoolTerm::class.java) }
                val term_id = sp.getString("term_id", null)

                val needRequestOnlineData: Int =
                    // 请求学期数据，触发条件：
                    if ((currentTerm == null || currentTerm.termId != term_id) || //本地无有效学期数据
                        version != lastStartVersion || // 发生了版本更新
                        (currentTerm.endInclusiveDate < LocalDate.now() && lastHandChangeTermTimePassed >= 7) || // 当前学期已结束，并且距离上一次手动修改学期的时间已经超过7天
                        (lastSyncDataTimePassed >= 7 && lastHandChangeTermTimePassed >= 7) // 距离上次刷新数据和距离上次手动更改学期都过去了7天
                    ) 2 // 应当更新学期列表和当前学期数据
                    else if (lastSyncDataTimePassed >= 7 && lastHandChangeTermTimePassed < 7) 1 // 应当只更新学期列表、不更新学期数据
                    else 0 // 不应当更新

                if (BACKEND_SITE == "") {
                    // 在开发状态下、没有后端服务器的情况，就读取本地字符串数据
                    // TODO 正式版应当删掉此处
                    sp.edit {
                        putString("available_terms", JSON.toJSONString(listOf<TermDescription>()))
                    }
                    currentTerm = JSON.parseObject(SPRING2019TERMJSON, SchoolTerm::class.java)
                }
                else if (needRequestOnlineData > 0) {
                    try {
                        val s = Fuel.get("$BACKEND_SITE/term").awaitString()
                        val resp = JSON.parseObject(s, TermAPIResp::class.java)
                        sp.edit {
                            // 任何needRequestOnlineData非0的情况下，均需要刷新available_terms列表
                            putString("available_terms", JSON.toJSONString(resp.termList))
                            // 只有在needRequestOnlineData值为2的情况下要根据网络数据更新当前学期选择
                            if (needRequestOnlineData >= 2) {
                                putString("term_id", resp.currentTermId)
                                putString("currentTerm", JSON.toJSONString(resp.termData))
                                currentTerm = resp.termData
                            }
                            putString("lastSyncDataTime", LocalDate.now().format(DateTimeFormatter.ISO_DATE))
                        }
                    }
                    catch (e: Exception) {
                        e.printStackTrace()
                        //操作失败，显示错误提示；如果currentTerm为空，则应当同时退出程序。
                        if (currentTerm != null) {
                            Toast.makeText(this@LoadingActivity, R.string.errmsg_change_term_fail, Toast.LENGTH_SHORT).show()
                        }
                        else {
                            Toast.makeText(this@LoadingActivity, R.string.errmsg_change_term_fail_exit, Toast.LENGTH_SHORT).show()
                            delay(3000)
                            exitProcess(0)
                        }
                    }
                }

                CREP.initializeTerm(this@LoadingActivity, currentTerm!!)

                var the_intent: Intent = Intent()
                the_intent.setClass(this@LoadingActivity, MainActivity::class.java)
                if (currentTerm!!.termId != term_id) the_intent.putExtra("SHOW_TOAST",
                    "${resources.getText(R.string.info_auto_term)}${currentTerm!!.chineseName}")
                startActivity(the_intent);
                finish();
            }

    }


    /**
    描述:测试函数，上传初始化日程
     */
    suspend fun loadTestData() {

        val detail1 : MutableMap<CalendarItemLegalDetailKey, String> = mutableMapOf<CalendarItemLegalDetailKey, String>()
        detail1[CalendarItemLegalDetailKey.COURSEID] = "114514"
        detail1[CalendarItemLegalDetailKey.COMMENT] = "3学分硬课"
        val item1:CalendarItemData = CalendarItemData(id = 1, name = "数据库", detail = detail1)

        val detail2 : MutableMap<CalendarItemLegalDetailKey, String> = mutableMapOf<CalendarItemLegalDetailKey, String>()
        detail2[CalendarItemLegalDetailKey.TEACHER] = "王继良"
        detail2[CalendarItemLegalDetailKey.COMMENT] = "大作业写不完了qwqwqwqwqwqwqwq，sgltcltcl"

        val item2:CalendarItemData = CalendarItemData(id = 2, name = "移动软件开发", detail = detail2)

        val detail3 : MutableMap<CalendarItemLegalDetailKey, String> = mutableMapOf<CalendarItemLegalDetailKey, String>()
        detail3[CalendarItemLegalDetailKey.ORGANIZATION] = "软件学院学生会"
        detail3[CalendarItemLegalDetailKey.COMMENT] = "并没有这种时间的社工，我单纯测试一下"

        val item3:CalendarItemData = CalendarItemData(id = 3, name = "社工", type = CalendarItemType.SOCIALWORK, detail = detail3)

        val time1: TimeInCourseSchedule = TimeInCourseSchedule(
            startBig = 6, startOffsetSmall = 0.0f,
            lengthSmall = 2.0f, date = LocalDate.parse("2020-05-19"), dayOfWeek = LocalDate.parse("2020-05-19").dayOfWeek
        )
        val data1: CalendarTimeData = CalendarTimeData(
            name = "摸鱼", type = CalendarTimeType.SINGLE_COURSE
            , timeInCourseSchedule = time1, item_id = 1, place = "李文正馆"
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
            lengthSmall = 2.0f, date = LocalDate.parse("2020-05-20"), dayOfWeek = LocalDate.parse("2020-05-20").dayOfWeek
        )
        val data3: CalendarTimeData = CalendarTimeData(
            name = "摸鱼", type = CalendarTimeType.SINGLE_COURSE
            , timeInCourseSchedule = time3, item_id = 2, comment = "不能再摸鱼了qwqquq"
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
            lengthSmall = 3.0f, date = LocalDate.parse("2020-05-21"), dayOfWeek = LocalDate.parse("2020-05-21").dayOfWeek
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
            name = "开会", type = CalendarTimeType.REPEAT_COURSE
            , timeInCourseSchedule = time6, repeatWeeks = mutableListOf(12, 14, 16), item_id = 3
        )

        val time7: TimeInHour = TimeInHour(
            startTime = LocalTime.parse("08:00"), endTime = LocalTime.parse("09:30"),
                    dayOfWeek = LocalDate.parse("2020-05-24").dayOfWeek
        )
        val data7: CalendarTimeData = CalendarTimeData(
            name = "吃kebab", type = CalendarTimeType.REPEAT_HOUR
            , timeInHour = time7, item_id = 3, repeatWeeks = mutableListOf(12, 14, 15, 16)
        )
        val list3 = listOf<CalendarTimeData>(data5, data6, data7)


        CREP.updateItemAndTimes(item1, list1)
        CREP.updateItemAndTimes(item2, list2)
        CREP.updateItemAndTimes(item3, list3)



    }


}
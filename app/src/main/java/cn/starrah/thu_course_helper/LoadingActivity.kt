package cn.starrah.thu_course_helper

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import cn.starrah.thu_course_helper.data.SPRING2019TERMJSON
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.declares.school.SchoolTerm
import cn.starrah.thu_course_helper.onlinedata.backend.BACKEND_SITE
import cn.starrah.thu_course_helper.onlinedata.backend.BackendAPICheckVersion
import cn.starrah.thu_course_helper.onlinedata.backend.BackendAPITermData
import cn.starrah.thu_course_helper.onlinedata.backend.TermDescription
import com.alibaba.fastjson.JSON
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
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
        }
        catch (e: Exception) {
            ""
        }

        lifecycleScope.launch {
            val sp = PreferenceManager.getDefaultSharedPreferences(this@LoadingActivity)
            sp.edit{
                //进入时清空当前周
                putInt("currentWeekCourseTable", 0)
                putInt("currentWeekTimeTable", 0)
            }

            val version = packageManager.getPackageInfo(packageName, 0).versionName
            val lastStartVersion = sp.getString("lastStartVersion", null)
            val lastSyncDataTime = sp.getString("lastSyncDataTime", null)
                ?.let { LocalDate.parse(it, DateTimeFormatter.ISO_DATE) }
            val lastSyncDataTimePassed = lastSyncDataTime?.let {
                Period.between(it, LocalDate.now()).get(ChronoUnit.DAYS).toInt()
            } ?: 100000
            val lastHandChangeTermTime = sp.getString("lastHandChangeTermTime", null)
                ?.let { LocalDate.parse(it, DateTimeFormatter.ISO_DATE) }
            val lastHandChangeTermTimePassed = lastHandChangeTermTime?.let {
                Period.between(it, LocalDate.now()).get(ChronoUnit.DAYS).toInt()
            } ?: 100000
            var currentTerm = sp.getString("currentTerm", null)
                ?.let { JSON.parseObject(it, SchoolTerm::class.java) }
            val term_id = sp.getString("term_id", null)

            val SYNC_TERM_INTERVAL_DAYS = 7
            val HAND_CHANGE_KEEP_DAYS = 7
            val needRequestOnlineData: Int =
                // 请求学期数据，触发条件：
                if ((currentTerm == null || currentTerm.termId != term_id) || //本地无有效学期数据
                    version != lastStartVersion || // 发生了版本更新
                    (currentTerm.endInclusiveDate < LocalDate.now() && lastHandChangeTermTimePassed >= HAND_CHANGE_KEEP_DAYS) || // 当前学期已结束，并且距离上一次手动修改学期的时间已经超过7天
                    (lastSyncDataTimePassed >= SYNC_TERM_INTERVAL_DAYS && lastHandChangeTermTimePassed >= HAND_CHANGE_KEEP_DAYS) // 距离上次刷新数据和距离上次手动更改学期都过去了7天
                ) 2 // 应当更新学期列表和当前学期数据
                else if (lastSyncDataTimePassed >= SYNC_TERM_INTERVAL_DAYS && lastHandChangeTermTimePassed < HAND_CHANGE_KEEP_DAYS) 1 // 应当只更新学期列表、不更新学期数据
                else 0 // 不应当更新

            if (BACKEND_SITE == "") {
                // TODO 在开发状态下、没有后端服务器的情况，就读取本地字符串数据.正式版应当删掉此处
                sp.edit {
                    putString("available_terms", JSON.toJSONString(listOf<TermDescription>()))
                }
                currentTerm = JSON.parseObject(SPRING2019TERMJSON, SchoolTerm::class.java)
            }
            else if (needRequestOnlineData > 0) {
                try {
                    val resp = BackendAPITermData()
                    val respVersion = BackendAPICheckVersion() // 捎带检查版本
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
                        putString("lastStartVersion", version)
                        putString("latest_version", respVersion.versionName)
                        putString("latest_version_url", respVersion.url)


                    }
                }
                catch (e: Exception) {
                    e.printStackTrace()
                    //操作失败，显示错误提示；如果currentTerm为空，则应当同时退出程序。
                    if (currentTerm != null) {
                        Toast.makeText(
                            this@LoadingActivity,
                            R.string.errmsg_change_term_fail,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else {
                        Toast.makeText(
                            this@LoadingActivity,
                            R.string.errmsg_change_term_fail_exit,
                            Toast.LENGTH_SHORT
                        ).show()
                        delay(3000)
                        exitProcess(0)
                    }
                }
            }

            CREP.initializeTerm(this@LoadingActivity, currentTerm!!)

//            loadTestData()

//            THUCourseDataSouce.loadData(CREP.term, mapOf(
//                "homework" to true,
//                "activity" to this@LoadingActivity,
//                "username" to sp.getString("login_name", null)!!,
//                "password" to CREP.getUserPassword(this@LoadingActivity)
//            ))

            val the_intent = Intent()
            the_intent.setClass(this@LoadingActivity, MainActivity::class.java)
            if (currentTerm!!.termId != term_id) the_intent.putExtra(
                "SHOW_TOAST",
                "${resources.getText(R.string.info_auto_term)}${currentTerm!!.chineseName}"
            )
            startActivity(the_intent)
            finish()
        }

    }

}
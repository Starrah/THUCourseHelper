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
            sp.edit {
                //进入时清空当前周
                putInt("currentWeekCourseTable", 0)
                putInt("currentWeekTimeTable", 0)
            }
            val term_id = sp.getString("term_id", null)

            try {
                val errMsg = CREP.initializeDefault(this@LoadingActivity)
                if (errMsg != null) Toast.makeText(this@LoadingActivity, errMsg, Toast.LENGTH_SHORT)
                    .show()
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

//            loadTestData()

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

}
package cn.starrah.thu_course_helper

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.fragment.CourseTable
import cn.starrah.thu_course_helper.fragment.Information
import cn.starrah.thu_course_helper.fragment.SettingsFragment
import cn.starrah.thu_course_helper.fragment.TimeTable
import cn.starrah.thu_course_helper.onlinedata.thu.THUCourseDataSouce
import cn.starrah.thu_course_helper.utils.setLastSyncExamDate
import cn.starrah.thu_course_helper.utils.setLastSyncHomeworkDatetime
import cn.starrah.thu_course_helper.utils.shouldSyncExam
import cn.starrah.thu_course_helper.utils.shouldSyncHomework
import cn.starrah.thu_course_helper.widget.AppWidgetCourse
import cn.starrah.thu_course_helper.widget.AppWidgetTime
import cn.starrah.thu_course_helper.widget.NotificationCourse
import cn.starrah.thu_course_helper.widget.NotificationTime
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch


class MainActivity : FragmentActivity() {

    //显示布局变量
    var courseTableType: String = "course"
    var showDays = 5


    inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> Unit) {
        val fragmentTransaction = beginTransaction()
        fragmentTransaction.func()
        fragmentTransaction.commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById(R.id.navigation) as BottomNavigationView
        supportFragmentManager.inTransaction {
            replace(R.id.frame_page, CourseTable())
        }
        //bottomNavigationView Item 选择监听
        bottomNavigationView.setOnNavigationItemSelectedListener(object :
            BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.getItemId()) {
                    R.id.navigation_course_table -> {
                        supportFragmentManager.inTransaction {
                            replace(R.id.frame_page, CourseTable())
                        }
                        return true
                    }

                    R.id.navigation_time_table   -> {
                        supportFragmentManager.inTransaction {
                            replace(R.id.frame_page, TimeTable())
                        }
                        return true
                    }

                    R.id.navigation_information  -> {
                        supportFragmentManager.inTransaction {
                            replace(R.id.frame_page, Information())
                        }
                        return true
                    }
                    R.id.navigation_settings     -> {
                        supportFragmentManager.inTransaction {
                            replace(R.id.frame_page, SettingsFragment())
                        }
                        return true
                    }
                }
                return false
            }
        })

        if (intent.getStringExtra("SHOW_TOAST") != null)
            Toast.makeText(this, intent.getStringExtra("SHOW_TOAST")!!, Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            val sp = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
            if (shouldSyncHomework(this@MainActivity)) {
                CREP.onlineCourseDataSource!!.loadData(
                    CREP.term, mapOf(
                        "homework" to true,
                        "activity" to this@MainActivity,
                        "username" to sp.getString("login_name", null)!!,
                        "password" to CREP.getUserPassword(this@MainActivity),
                        "onlyUnsubmitted" to true,
                        "apply" to true
                    )
                )
                setLastSyncHomeworkDatetime(this@MainActivity)
            }
            if (shouldSyncExam(this@MainActivity)) {
                CREP.onlineCourseDataSource!!.loadData(
                    CREP.term, mapOf(
                        "exam" to true,
                        "username" to sp.getString("login_name", null)!!,
                        "password" to CREP.getUserPassword(this@MainActivity),
                        "apply" to true
                    )
                )
                setLastSyncExamDate(this@MainActivity)
            }
            val onlineSource = CREP.onlineCourseDataSource
            if (onlineSource is THUCourseDataSouce) {
                onlineSource.tryShouldFixDataFromBackendLaterAfterWrittenToDB(this@MainActivity)
            }
        }
    }

    //小部件-通知栏更新函数
    /**
     * 描述：更新小部件
     * 参数：无
     * 返回：无
     */
    private fun updateAppWidgets() {
        val intent_widget_time = Intent(this, AppWidgetTime::class.java)
        intent_widget_time.setAction("update_action")
        val intent_widget_course = Intent(this, AppWidgetCourse::class.java)
        intent_widget_course.setAction("update_action")
        sendBroadcast(intent_widget_time)
        sendBroadcast(intent_widget_course)
    }

    /**
     * 描述：更新通知栏(日程）
     * 参数：无
     * 返回：无
     */
    private fun updateNotificationTime() {
        val intent_notify_time = Intent(this, NotificationTime::class.java)
        intent_notify_time.setAction("update_action")
        sendBroadcast(intent_notify_time)
    }

    /**
     * 描述：更新通知栏(课程）
     * 参数：无
     * 返回：无
     */
    private fun updateNotificationCourse() {
        val intent_notify_course = Intent(this, NotificationCourse::class.java)
        intent_notify_course.setAction("update_action")
        sendBroadcast(intent_notify_course)
    }

    /**
     * 描述：取消通知栏（日程）
     * 参数：无
     * 返回：无
     */
    private fun cancelNotificationTime() {
        val intent_notify_time = Intent(this, NotificationTime::class.java)
        intent_notify_time.setAction("delete_action")
        sendBroadcast(intent_notify_time)
    }

    /**
     * 描述：取消通知栏（课程）
     * 参数：无
     * 返回：无
     */
    private fun cancelNotificationCourse() {
        val intent_notify_course = Intent(this, NotificationCourse::class.java)
        intent_notify_course.setAction("delete_action")
        sendBroadcast(intent_notify_course)
    }

    /**
     * 实现intent更新
     */
    override fun onStart() {
        super.onStart()

        //更新小部件
        updateAppWidgets()

        //按照设置更新通知栏
        val sp = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
        val notifications_settings = sp.getString("stay_notice", null)
        if(notifications_settings == resources.getString(R.string.settings_stay_notice_no)) {
            cancelNotificationCourse()
            cancelNotificationTime()
        }
        else if(notifications_settings == resources.getString(R.string.settings_stay_notice_course)) {
            updateNotificationCourse()
            cancelNotificationTime()
        }
        else {
            updateNotificationCourse()
            updateNotificationTime()
        }
    }


}


package cn.starrah.thu_course_helper

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
import cn.starrah.thu_course_helper.service.allAppTask
import cn.starrah.thu_course_helper.utils.*
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
            allAppTask(this@MainActivity)
            val onlineSource = CREP.onlineCourseDataSource
            if (onlineSource is THUCourseDataSouce) {
                onlineSource.tryShouldFixDataFromBackendLaterAfterWrittenToDB(this@MainActivity)
            }

//            val time = withContext(Dispatchers.IO) { CREP.DAO.findAllItems() }.first().let { CalendarTimeDataWithItem(it.times[0], it) }
////            showRemindNotification(this@MainActivity, time)
//            val alarmIntent = Intent(this@MainActivity, RemindReceiver::class.java).apply {
//                action = "remind"
//                addCategory("timeId:${time.id}")
//            }
//            sendBroadcast(alarmIntent)
        }
    }

    /**
     * 实现intent更新
     */
    override fun onStart() {
        super.onStart()

        updateWidgetsAndNotification()
    }


}


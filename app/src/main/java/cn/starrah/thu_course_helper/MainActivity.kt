package cn.starrah.thu_course_helper

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
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
import cn.starrah.thu_course_helper.widget.updateWidgetsAndNotification
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.lang.Exception


class MainActivity : FragmentActivity() {

    //显示布局变量
    var courseTableType: String = "course"
    var showDays = 5

    //字符串和背景图片的对应关系
    companion object {
        var mapBackground: MutableMap<String, Drawable> = mutableMapOf()
    }

    inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> Unit) {
        val fragmentTransaction = beginTransaction()
        fragmentTransaction.func()
        fragmentTransaction.commit()
    }

    /**
     * 描述：加载背景图片，以及与设置的对应关系
     * 参数：无
     * 返回：无
     */
    fun loadMapBackground() {
        mapBackground.clear()
        var string1 = resources.getString(R.string.bg_blank)
        var bg1 = resources.getDrawable(R.color.colorWhite)
        mapBackground.put(string1, bg1)
        var string2 = resources.getString(R.string.bg_autumn)
        var bg2 = resources.getDrawable(R.drawable.bg_autumn)
        mapBackground.put(string2, bg2)
        var string3 = resources.getString(R.string.bg_desert)
        var bg3 = resources.getDrawable(R.drawable.bg_desert)
        mapBackground.put(string3, bg3)
        var string4 = resources.getString(R.string.bg_forest)
        var bg4 = resources.getDrawable(R.drawable.bg_forest)
        mapBackground.put(string4, bg4)
        var string5 = resources.getString(R.string.bg_grass)
        var bg5 = resources.getDrawable(R.drawable.bg_grass)
        mapBackground.put(string5, bg5)
        var string6 = resources.getString(R.string.bg_river)
        var bg6 = resources.getDrawable(R.drawable.bg_river)
        mapBackground.put(string6, bg6)
        var string7 = resources.getString(R.string.bg_sea)
        var bg7 = resources.getDrawable(R.drawable.bg_sea)
        mapBackground.put(string7, bg7)
        var string8 = resources.getString(R.string.bg_winter)
        var bg8 = resources.getDrawable(R.drawable.bg_winter)
        mapBackground.put(string8, bg8)
    }

    /**
     * 描述：根据设置信息，加载初始背景图片
     * 参数：无
     * 返回：无
     */
    fun setInitialBackground() {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val settings = sp.getString("background_choice", resources.getString(R.string.bg_blank))
        var background: Drawable? = null
        try {
            background = mapBackground.get(settings)
        }
        catch (e: Exception) {
            background = null
        }
        if(background == null) {
            background = resources.getDrawable(R.color.colorWhite)
        }

        var layout = findViewById<FrameLayout>(R.id.frame_page)
        layout.background = background
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadMapBackground()
        setInitialBackground()

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


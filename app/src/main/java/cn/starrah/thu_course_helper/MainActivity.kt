@file:Suppress("DEPRECATION")

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


class MainActivity : FragmentActivity() {

    //显示布局变量
    var courseTableType: String = "course"
    var showDays = 5

    //字符串和背景图片，虚线颜色的对应关系
    companion object {
        var bgSettings:Drawable? = null
        var mapBackground: MutableMap<String, Drawable> = mutableMapOf()
        var mapBackgroundLine: MutableMap<String, String> = mutableMapOf()

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
        bgSettings = resources.getDrawable(R.color.colorWhite)
        mapBackground.clear()
        mapBackgroundLine.clear()

        val string1 = resources.getString(R.string.bg_blank)
        val bg1 = resources.getDrawable(R.color.colorWhite)
        mapBackground.put(string1, bg1)
        val line1 = resources.getString(R.string.bg_stroke_black)
        mapBackgroundLine.put(string1, line1)


        val string3 = resources.getString(R.string.bg_desert)
        val bg3 = resources.getDrawable(R.drawable.bg_desert)
        mapBackground.put(string3, bg3)
        val line3 = resources.getString(R.string.bg_stroke_white)
        mapBackgroundLine.put(string3, line3)


        val string5 = resources.getString(R.string.bg_grass)
        val bg5 = resources.getDrawable(R.drawable.bg_grass)
        mapBackground.put(string5, bg5)
        val line5 = resources.getString(R.string.bg_stroke_white)
        mapBackgroundLine.put(string5, line5)


        val string7 = resources.getString(R.string.bg_sea)
        val bg7 = resources.getDrawable(R.drawable.bg_sea)
        mapBackground.put(string7, bg7)
        val line7 = resources.getString(R.string.bg_stroke_white)
        mapBackgroundLine.put(string7, line7)

        val string8 = resources.getString(R.string.bg_winter)
        val bg8 = resources.getDrawable(R.drawable.bg_winter)
        mapBackground.put(string8, bg8)
        val line8 = resources.getString(R.string.bg_stroke_black)
        mapBackgroundLine.put(string8, line8)

        val string9 = resources.getString(R.string.bg_109_1)
        val bg9 = resources.getDrawable(R.drawable.bg_109_1)
        mapBackground.put(string9, bg9)
        val line9 = resources.getString(R.string.bg_stroke_white)
        mapBackgroundLine.put(string9, line9)

        val string10 = resources.getString(R.string.bg_109_2)
        val bg10 = resources.getDrawable(R.drawable.bg_109_2)
        mapBackground.put(string10, bg10)
        val line10 = resources.getString(R.string.bg_stroke_white)
        mapBackgroundLine.put(string10, line10)

        val string11 = resources.getString(R.string.bg_109_3)
        val bg11 = resources.getDrawable(R.drawable.bg_109_3)
        mapBackground.put(string11, bg11)
        val line11 = resources.getString(R.string.bg_stroke_black)
        mapBackgroundLine.put(string11, line11)

        val string12 = resources.getString(R.string.bg_blue)
        val bg12 = resources.getDrawable(R.drawable.bg_blue)
        mapBackground.put(string12, bg12)
        val line12 = resources.getString(R.string.bg_stroke_black)
        mapBackgroundLine.put(string12, line12)

        val string13 = resources.getString(R.string.bg_pink)
        val bg13 = resources.getDrawable(R.drawable.bg_pink)
        mapBackground.put(string13, bg13)
        val line13 = resources.getString(R.string.bg_stroke_white)
        mapBackgroundLine.put(string13, line13)

        val string14 = resources.getString(R.string.bg_orange)
        val bg14 = resources.getDrawable(R.drawable.bg_orange)
        mapBackground.put(string14, bg14)
        val line14 = resources.getString(R.string.bg_stroke_black)
        mapBackgroundLine.put(string14, line14)

        val string15 = resources.getString(R.string.bg_yellow_purple)
        val bg15 = resources.getDrawable(R.drawable.bg_morning_mountain)
        mapBackground.put(string15, bg15)
        val line15 = resources.getString(R.string.bg_stroke_white)
        mapBackgroundLine.put(string15, line15)
        mapBackgroundLine.put(string15, line15)
    }

    /**
     * 描述：根据设置信息，加载初始背景图片
     * 参数：无
     * 返回：无
     */
    fun setInitialBackground() {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val settings = sp.getString("background_choice", resources.getString(R.string.bg_blank))
        var background: Drawable?
        try {
            background = mapBackground.get(settings)
        }
        catch (e: Exception) {
            background = null
        }
        if(background == null) {
            background = resources.getDrawable(R.color.colorWhite)
        }

        val layout = findViewById<FrameLayout>(R.id.frame_page)
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
                            setInitialBackground()
                            replace(R.id.frame_page, CourseTable())
                        }
                        return true
                    }

                    R.id.navigation_time_table   -> {
                        supportFragmentManager.inTransaction {
                            setInitialBackground()
                            replace(R.id.frame_page, TimeTable())
                        }
                        return true
                    }

                    R.id.navigation_information  -> {
                        supportFragmentManager.inTransaction {
                            setInitialBackground()
                            replace(R.id.frame_page, Information())
                        }
                        return true
                    }
                    R.id.navigation_settings     -> {
                        supportFragmentManager.inTransaction {
                            val layout = findViewById<FrameLayout>(R.id.frame_page)
                            layout.background = bgSettings
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
            allAppTask(this@MainActivity)
            val onlineSource = CREP.onlineCourseDataSource
            if (onlineSource is THUCourseDataSouce) {
                onlineSource.tryShouldFixDataFromBackendLaterAfterWrittenToDB(this@MainActivity)
            }

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


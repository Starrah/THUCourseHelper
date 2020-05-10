package cn.starrah.thu_course_helper

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : FragmentActivity() {

    //显示布局变量
    var courseTableType:String = "course"
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

                    R.id.navigation_time_table -> {
                    supportFragmentManager.inTransaction {
                            replace(R.id.frame_page, TimeTable())
                        }
                        return true
                    }

                    R.id.navigation_information -> {
                        supportFragmentManager.inTransaction {
                            replace(R.id.frame_page, Information())
                        }
                        return true
                    }
                    R.id.navigation_settings -> {
                        supportFragmentManager.inTransaction {
                            replace(R.id.frame_page, Settings())
                        }
                        return true
                    }
                }
                return false
            }
        })
    }



}


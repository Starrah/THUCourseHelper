package cn.starrah.thu_course_helper

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import cn.starrah.thu_course_helper.data.SPRING2019TERMJSON
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.declares.school.SchoolTerm
import com.alibaba.fastjson.JSON
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.runBlocking


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
        /*
        TODO
        这里是我临时写的代码、用来预先加载设置数据的（目前只有学期数据需要加载、打开对应的数据库）
        上述行为均被封装为挂起函数。按道理上，挂起函数应该通过[lifeCycleScope.launch]调用从而避免阻塞主线程，
        但现在后面的数据（课程表等加载）在时序上必须是在数据库完成之后才可以进行。
        之后需要稍微重构一下代码、把渲染fragment的过程放在launch里面、在预加载阶段完成后在进行
        （可能需要先给用户显示一个加载画面，就像微信那个人在月亮下面那种之类的）
        目前还没有实现这个功能，所以我只能先用阻塞主线程的方式完成数据预加载，否则后面会直接报错。
         */
        runBlocking {
//            val yyy = TimeInHour(LocalTime.now(), LocalTime.now(), null, LocalDate.now())
//            val a = JSON.toJSONString(yyy)
//            val ppp = JSON.parseObject(a, TimeInHour::class.java)
//            println(yyy == ppp)
//            val qwq = JSON.parseObject(SPRING2019TERMJSON, SchoolTimeRule::class.java)
            val term = JSON.parseObject(SPRING2019TERMJSON, SchoolTerm::class.java)
            CREP.initializeTerm(this@MainActivity, term)
        }
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


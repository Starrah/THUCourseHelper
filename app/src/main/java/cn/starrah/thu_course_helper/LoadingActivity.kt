package cn.starrah.thu_course_helper;

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import cn.starrah.thu_course_helper.data.SPRING2019TERMJSON
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.declares.school.SchoolTerm
import com.alibaba.fastjson.JSON
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Thread.sleep

/**
 * 描述：显示一张图片，在这里完成各种加载，然后再跳转到主界面
 */
class LoadingActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            //TODO:背景图片比较大，考虑点其他的方法
            setContentView(R.layout.loading)


            lifecycleScope.launch{
//            val yyy = TimeInHour(LocalTime.now(), LocalTime.now(), null, LocalDate.now())
//            val a = JSON.toJSONString(yyy)
//            val ppp = JSON.parseObject(a, TimeInHour::class.java)
//            println(yyy == ppp)
//            val qwq = JSON.parseObject(SPRING2019TERMJSON, SchoolTimeRule::class.java)
                val term = JSON.parseObject(SPRING2019TERMJSON, SchoolTerm::class.java)
                CREP.initializeTerm(this@LoadingActivity, term)
                System.out.println("finished")
                delay(8000);
                var the_intent: Intent = Intent();

                the_intent.setClass(this@LoadingActivity, MainActivity::class.java)
                startActivity(the_intent);
                finish();
            }

        }
}
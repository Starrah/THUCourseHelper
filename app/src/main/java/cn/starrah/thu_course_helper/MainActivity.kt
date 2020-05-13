package cn.starrah.thu_course_helper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import cn.starrah.thu_course_helper.data.declares.*
import com.alibaba.fastjson.JSON
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lifecycleScope
    }

    override fun onResume() {
        super.onResume()
//        val db = Room.databaseBuilder(
//            applicationContext,
//            AppDatabase::class.java, "database-name"
//        ).build()
//        lifecycleScope.launch {
//            withContext(Dispatchers.IO) {
//                db.userDao().insertAll(CalendarItemData(detail = mutableMapOf(CalendarItemLegalDetailKey.COMMENT to "aaa")))
//                db.userDao().insertAll(CalendarItemData(detail = mutableMapOf(CalendarItemLegalDetailKey.COMMENT to "yyy")))
//                db.userDao().insertAll(CalendarItemData(detail = mutableMapOf(CalendarItemLegalDetailKey.COMMENT to "qwq")))
//                delay(1000)
//                val res = db.userDao().getAll()
//                for (qwq in res) {
//                    Log.d("qwq", JSON.toJSONString(qwq))
//                }
//            }
//        }


    }
}

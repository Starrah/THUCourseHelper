@file:Suppress("UNUSED_PARAMETER")

package cn.starrah.thu_course_helper.information

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.fragment.Information
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject

class ClassroomShowActivity : AppCompatActivity(){

    private var showItem: JSONArray = JSONArray()
    private lateinit var searchPlace: EditText
    private lateinit var showPlace: GridLayout
    //搜索框监听器
    public lateinit var searchChanger:TextWatcher
    /**
     * 描述：初始化
     * @param savedInstanceState 存储的data
     */
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.classroom_show)
        searchPlace = findViewById(R.id.search_place)
        showPlace = findViewById<GridLayout>(R.id.button_place)

        //获取intent数据
        val intent = intent
        val message = intent.getStringExtra(Information.INTENT_JSON)
        try {
            showItem = JSONArray.parseArray(message)
        }
        catch(e:Exception) {
            showItem = JSONArray()
        }

        //绑定searchplace监听器
        initSearchChanger()
        searchPlace.addTextChangedListener(searchChanger)

        //初始化显示
        updateShow("")
    }

    /**
     * 描述：根据搜索框的输入来更新button
     */
    fun updateShow(search_text:String) {
        showPlace.removeAllViews()
        for(item in showItem) {
            var item_json:JSONObject = item as JSONObject
            var name:String? = item_json["name"] as? String
            var url:String? = item_json["url"] as? String
            if(name != null && url != null) {
                if(name.contains(search_text)) {
                    addOneButton(name, url)
                }
            }
        }
    }

    /**
     * 描述：根据json结果，建立一个按钮并且绑定跳转事件
     * 参数：名称，url
     * 返回：无
     */
    fun addOneButton(name:String, url:String) {
        var button_place: View = LayoutInflater.from(this).inflate(R.layout.classroom_show_button, null)
        var button:Button = button_place.findViewById(R.id.button_this)
        button.setText(name)
        button.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data = Uri.parse(url)
            startActivity(intent)
        })
        showPlace.addView(button_place)
    }

    /**
     * 描述：初始化搜索框监听器
     * 参数：无
     * 返回：无
     */
    private fun initSearchChanger() {
        searchChanger = (object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(editable: Editable) {
                var search_content = editable.toString()
                updateShow(search_content)
            }
        })
    }

    /**
     * 描述：处理返回按钮的事件--返回
     * 参数：无
     * 返回：无
     */
    fun handleReturn(view: View) {
        finish()
    }


}
package cn.starrah.thu_course_helper.fragment

import android.app.Dialog
import android.content.Context
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.onlinedata.thu.THUCourseDataSouce
import kotlinx.coroutines.launch

class LoginDialog(context: Context) : Dialog(context) {
    private var theContext: Context? = null
    private fun initDialog(context: Context) {
        theContext = context
        buildDialog()
    }

    private fun buildDialog() {
        val layout = LayoutInflater.from(theContext!!).inflate(R.layout.login, null)
        PreferenceManager.getDefaultSharedPreferences(theContext).let {
            if (it.getInt("login_status", 0) > 0) {
                it.getString("login_name", null)?.let {
                    layout.findViewById<EditText>(R.id.login_id).setText(it)
                }
            }
        }
        layout.findViewById<Button>(R.id.login_ok).setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                (theContext as FragmentActivity).lifecycleScope.launch {
                    var login_id_place: EditText = layout.findViewById(R.id.login_id)
                    var login_pass_place: EditText = layout.findViewById(R.id.login_password)

                    var login_id: String = login_id_place.text.toString()
                    var login_pass: String = login_pass_place.text.toString()

                    if (login_id.isEmpty()) {
                        Toast.makeText(theContext!!, "账号不能为空！", Toast.LENGTH_SHORT).show()
                    } else if (login_pass.isEmpty()) {
                        Toast.makeText(theContext!!, "密码不能为空！", Toast.LENGTH_SHORT).show()
                    } else {
                        try {
                            CREP.onlineCourseDataSource?.doSomething(theContext as FragmentActivity)
                            CREP.onlineCourseDataSource?.login(
                                login_id,
                                login_pass,
                                mapOf("old" to true)
                            )
                            PreferenceManager.getDefaultSharedPreferences(theContext).edit {
                                putInt("login_status", 1)
                                putString("login_name", login_id)
                                putInt("login_force_update", (System.currentTimeMillis() / 1000).toInt())
                            }
                            Toast.makeText(theContext, R.string.login_success, Toast.LENGTH_SHORT).show()
                            dismiss()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            //TODO 更详细的异常处理
                            Toast.makeText(theContext!!, "账号或密码错误，登陆失败！", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
        layout.findViewById<Button>(R.id.login_no).setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                dismiss()
            }
        })
        setContentView(layout)
    }

    init {
        initDialog(context)
    }
}
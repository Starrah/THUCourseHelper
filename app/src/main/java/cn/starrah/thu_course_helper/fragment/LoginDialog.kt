package cn.starrah.thu_course_helper.fragment

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import cn.starrah.thu_course_helper.R
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
                            var show: String = "账号：" + login_id + "\n" + "密码：" + login_pass
                            Toast.makeText(theContext!!, show, Toast.LENGTH_SHORT).show()
                            THUCourseDataSouce().login(
                                theContext as FragmentActivity, login_id,
                                login_pass
                            )
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
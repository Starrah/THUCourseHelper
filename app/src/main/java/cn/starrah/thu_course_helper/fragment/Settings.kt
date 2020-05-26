package cn.starrah.thu_course_helper

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import cn.starrah.thu_course_helper.fragment.LoginDialog


class Settings : Fragment() {

    //父类activity
    private var theActivity: FragmentActivity? = null

    private lateinit var loginDialog:LoginDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        theActivity = requireActivity()

        var view:View =  inflater.inflate(R.layout.settings, container, false)

        return view
    }


    override fun onStart() {
        super.onStart()
        loginDialog = LoginDialog(theActivity!!)
        var login_button = theActivity!!.findViewById<Button>(R.id.login_button)
        login_button.setOnClickListener(View.OnClickListener() {
            handleLogin(login_button)
        })

    }
    /*
    private fun showLoginDialog() {
        /* @setView 装入自定义View ==> R.layout.dialog_customize
     * 由于dialog_customize.xml只放置了一个EditView，因此和图8一样
     * dialog_customize.xml可自定义更复杂的View
     */
        val login_dialog: AlertDialog.Builder = AlertDialog.Builder(theActivity!!)
        val dialog_view:View = LayoutInflater.from(theActivity!!).inflate(R.layout.login, null);
        login_dialog.setTitle("使用清华info账号密码登录")
        login_dialog.setView(dialog_view)
        login_dialog.setPositiveButton("确定",
            DialogInterface.OnClickListener { dialog, which -> // 获取EditView中的输入内容
                var login_id = dialog_view.findViewById<EditText>(R.id.login_id).text.trim() //去掉空格
                var login_pass = dialog_view.findViewById<EditText>(R.id.login_password).text.trim() //去掉空格

                if (login_id.isEmpty()) {

                    Toast.makeText(theActivity!!, "账号不能为空！", Toast.LENGTH_SHORT).show()
                } else if (login_pass.isEmpty()) {
                    Toast.makeText(theActivity!!, "密码不能为空！", Toast.LENGTH_SHORT).show()
                } else {
                    try {
                        var show: String = "账号：" + login_id + "\n" + "密码：" + login_pass
                        Toast.makeText(theActivity!!, show, Toast.LENGTH_SHORT).show()
                        //TODO 登录
                    } catch (e: Exception) {
                        //TODO 更详细的异常处理
                        Toast.makeText(theActivity!!, "账号或密码错误，登陆失败！", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        login_dialog.setNegativeButton("取消",
            DialogInterface.OnClickListener { dialog, which ->
            })
        login_dialog.show()
    }*/

    fun handleLogin(view:View?) {
        loginDialog.show()
    }











}
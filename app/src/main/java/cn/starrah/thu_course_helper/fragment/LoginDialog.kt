package cn.starrah.thu_course_helper.fragment

import android.app.Dialog
import android.content.Context
import android.text.Editable
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.onlinedata.thu.THUCourseDataSouce
import kotlinx.coroutines.launch

class LoginDialog(context: Context) : Dialog(context){
    private var theContext: Context? = null
    private lateinit var idPlace: EditText
    private lateinit var passPlace:EditText
    private lateinit var loginBar:ProgressBar
    private lateinit var loginBarPlace:LinearLayout
    private lateinit var savePassCheck:CheckBox

    private fun initDialog(context: Context) {
        theContext = context
        buildDialog()
    }

    private fun buildDialog() {
        val layout = LayoutInflater.from(theContext!!).inflate(R.layout.login, null)

        idPlace = layout.findViewById(R.id.login_id)
        passPlace = layout.findViewById(R.id.login_password)
        loginBar = layout.findViewById(R.id.login_bar)
        loginBarPlace = layout.findViewById(R.id.login_bar_place)
        savePassCheck = layout.findViewById(R.id.login_save_pass)

        PreferenceManager.getDefaultSharedPreferences(theContext).let {
            //加载账号

            it.getString("login_name", "")?.let {
                idPlace.setText(it)
            }

            //如果登录状态为2,3，加载密码
            if (it.getInt("login_status", 0) >= 2) {
                it.getString("login_pass", "")?.let {
                    if(it != null&& it.isEmpty() == false) {
                        //设置保存密码选项为true
                        savePassCheck.isChecked = true
                    }
                    passPlace.setText(it)
                }
            }
        }


        //隐藏progressbar
        var params_hide: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
        loginBarPlace.setLayoutParams(params_hide);
        loginBar.isVisible = false

        layout.findViewById<Button>(R.id.login_ok)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View?) {
                    (theContext as FragmentActivity).lifecycleScope.launch {


                        var login_id: String = idPlace.text.toString()
                        var login_pass: String = passPlace.text.toString()

                        if (login_id.isEmpty()) {
                            Toast.makeText(theContext!!, "账号不能为空！", Toast.LENGTH_SHORT).show()
                        }
                        else if (login_pass.isEmpty()) {
                            Toast.makeText(theContext!!, "密码不能为空！", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            try {
                                //显示progressbar
                                var params_show: LinearLayout.LayoutParams =
                                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                                loginBarPlace.setLayoutParams(params_show);
                                loginBar.isVisible = true

                                //登录
                                THUCourseDataSouce.login(
                                    theContext as FragmentActivity, login_id,
                                    login_pass
                                )

                                //登录成功后保存账号，如果选择了保存密码就保存密码，否则保存密码为空串
                                var saved_password: String = login_pass
                                if(savePassCheck.isChecked == false) {
                                    saved_password = ""
                                }

                                PreferenceManager.getDefaultSharedPreferences(theContext).edit {
                                    putInt("login_status", 1)
                                    putString("login_name", login_id)
                                    putString("login_pass", saved_password)
                                    putInt(
                                        "login_force_update",
                                        (System.currentTimeMillis() / 1000).toInt()
                                    )
                                }

                                //隐藏progressbar
                                var params_hide: LinearLayout.LayoutParams =
                                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
                                loginBarPlace.setLayoutParams(params_hide);
                                loginBar.isVisible = false

                                Toast.makeText(
                                    theContext,
                                    R.string.login_success,
                                    Toast.LENGTH_SHORT
                                ).show()
                                dismiss()
                            }
                            catch (e: Exception) {
                                e.printStackTrace()

                                //隐藏progressbar
                                var params_hide: LinearLayout.LayoutParams =
                                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
                                loginBarPlace.setLayoutParams(params_hide);
                                loginBar.isVisible = false

                                Toast.makeText(theContext!!, "登陆失败！", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }
            })
        layout.findViewById<Button>(R.id.login_no)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View?) {
                    dismiss()
                }
            })

        //显示/隐藏密码
        layout.findViewById<ImageButton>(R.id.show_hide_button).setOnTouchListener(View.OnTouchListener { view, motionEvent ->
            when (motionEvent.action){
                MotionEvent.ACTION_DOWN -> {
                    passPlace.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    view.background = context.getDrawable(R.drawable.icon_show_pass)
                }
                MotionEvent.ACTION_UP -> {
                    passPlace.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    view.background = context.getDrawable(R.drawable.icon_hide_pass)
                }
            }
            return@OnTouchListener true
        })



        setContentView(layout)
    }

    init {
        initDialog(context)
    }
}


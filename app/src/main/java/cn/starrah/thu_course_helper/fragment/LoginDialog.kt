package cn.starrah.thu_course_helper.fragment

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
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
    private lateinit var captchaPlace:EditText
    private lateinit var captchaView:ImageView
    private lateinit var loginBar:ProgressBar
    private lateinit var loginBarPlace:LinearLayout
    private lateinit var savePassCheck:CheckBox


    suspend fun initDialog(context: Context) {
        theContext = context
        buildDialog()
    }

    private suspend fun buildDialog(){
        val layout = LayoutInflater.from(theContext!!).inflate(R.layout.login, null)

        idPlace = layout.findViewById(R.id.login_id)
        passPlace = layout.findViewById(R.id.login_password)
        captchaPlace = layout.findViewById(R.id.login_captcha)
        captchaView = layout.findViewById(R.id.captcha_view)
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
                runCatching { CREP.getUserPassword(context) }.getOrNull()?.let {
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
        loginBarPlace.setLayoutParams(params_hide)
        loginBar.isVisible = false


        //点击验证码图片重新生成验证码
        captchaView.setOnClickListener({
            (theContext as FragmentActivity).lifecycleScope.launch {
                try {
                    val map_captcha = mapOf<String, Any>("requireCaptcha" to true)
                    val captcha_bitmap: Bitmap? = THUCourseDataSouce.login("", "", map_captcha)
                    captchaView.setImageBitmap(captcha_bitmap!!)
                }
                catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(theContext!! as FragmentActivity, e.message, Toast.LENGTH_LONG)
                        .show()
                    dismiss()
                }
            }
        })

        //加载验证码
        (theContext as FragmentActivity).lifecycleScope.launch {
            try {
                val map_captcha = mapOf<String, Any>("requireCaptcha" to true)
                val captcha_bitmap: Bitmap? = THUCourseDataSouce.login("", "", map_captcha)
                captchaView.setImageBitmap(captcha_bitmap!!)
            }
            catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(theContext!! as FragmentActivity, e.message, Toast.LENGTH_LONG)
                    .show()
                dismiss()
            }
        }



        layout.findViewById<Button>(R.id.login_ok)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View?) {
                    (theContext as FragmentActivity).lifecycleScope.launch {

                        var login_id: String = idPlace.text.toString()
                        var login_pass: String = passPlace.text.toString()
                        var login_captcha: String = captchaPlace.text.toString().toUpperCase()
                        if (login_id.isEmpty()) {
                            Toast.makeText(theContext!!, "账号不能为空！", Toast.LENGTH_SHORT).show()
                        }
                        else if (login_pass.isEmpty()) {
                            Toast.makeText(theContext!!, "密码不能为空！", Toast.LENGTH_SHORT).show()
                        }
                        else if(login_captcha.isEmpty()) {
                            Toast.makeText(theContext!!, "验证码不能为空！", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            try {

                                //显示progressbar
                                var params_show: LinearLayout.LayoutParams =
                                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                                loginBarPlace.setLayoutParams(params_show);
                                loginBar.isVisible = true

                                var map_captcha = mapOf<String, Any>("captcha" to login_captcha)
                                //登录
                                THUCourseDataSouce.login(
                                    login_id,
                                    login_pass,
                                    map_captcha
                                )

                                //登录成功后保存账号，如果选择了保存密码就保存密码，否则保存密码为空串
                                var saved_password: String = login_pass
                                var login_status:Int = 2

                                if(savePassCheck.isChecked == false) {
                                    saved_password = ""
                                    login_status = 1
                                }

                                PreferenceManager.getDefaultSharedPreferences(theContext).edit {
                                    putInt("login_status", login_status)
                                    putString("login_name", login_id)
                                    GlobalScope.launch { CREP.setUserPassword(theContext!!, saved_password) }
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

                                Toast.makeText(theContext!!, e.message, Toast.LENGTH_SHORT)
                                    .show()
                                // dismiss() // 登录发生异常不关闭窗口
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

}


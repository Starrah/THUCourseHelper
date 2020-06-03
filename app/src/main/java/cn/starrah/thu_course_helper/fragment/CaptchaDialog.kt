package cn.starrah.thu_course_helper.fragment

import android.app.Dialog
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import cn.starrah.thu_course_helper.R
import kotlinx.android.synthetic.main.captcha.*

/**
 * @param [context] Activity
 * @param [captchaBitmap] 验证码Bitmap
 * @param [callback] 点击确定或取消的回调
 */
class CaptchaDialog(context: FragmentActivity, val captchaBitmap: Bitmap, val callback: (String?) -> Unit) : Dialog(context) {
    private var theContext: FragmentActivity? = null
    private fun initDialog(context: FragmentActivity) {
        theContext = context
        buildDialog()
    }

    private fun buildDialog() {
        val layout = LayoutInflater.from(theContext!!).inflate(R.layout.captcha, null)
        layout.findViewById<ImageView>(R.id.captchaView).setImageBitmap(captchaBitmap)

        layout.findViewById<Button>(R.id.login_ok).setOnClickListener {
            callback(capt_input.text.toString())
            dismiss()
        }
        layout.findViewById<Button>(R.id.login_no).setOnClickListener {
            callback(null)
            dismiss()
        }
        setContentView(layout)
    }

    init {
        initDialog(context)
    }
}
package cn.starrah.thu_course_helper.onlinedata.thu

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.FragmentActivity
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarItemDataWithTimes
import cn.starrah.thu_course_helper.data.declares.school.SchoolTerm
import cn.starrah.thu_course_helper.data.utils.COOKIEJAR
import cn.starrah.thu_course_helper.data.utils.CookiedFuel
import cn.starrah.thu_course_helper.data.utils.DataInvalidException
import cn.starrah.thu_course_helper.fragment.CaptchaDialog
import cn.starrah.thu_course_helper.onlinedata.AbstractCourseDataSource
import com.github.kittinunf.fuel.coroutines.awaitByteArray
import com.github.kittinunf.fuel.coroutines.awaitString
import com.github.kittinunf.fuel.coroutines.awaitStringResponse
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.nio.charset.Charset
import java.util.regex.Pattern
import kotlin.coroutines.resume

class THUCourseDataSouce : AbstractCourseDataSource() {
    private val CAPTCHA_PATTERN = Pattern.compile("<img id=\"captcha\" src=\"(.*?)\".*?>")
    private val BASE_URL = "http://zhjwxk.cic.tsinghua.edu.cn"
    override val schoolName: String = "清华大学"
    override suspend fun login(
        activity: FragmentActivity,
        username: String,
        password: String,
        extra: Map<String, Any>?
    ): String {
        val resStr = CookiedFuel.get("$BASE_URL/xklogin.do")
            .awaitString(Charset.forName("GBK"))
        println(COOKIEJAR.cookieMap)
        val captchaUrl = BASE_URL + CAPTCHA_PATTERN.matcher(resStr).run { find(); group(1) }
        val captchaParam = captchaUrl.split("=")[1]
        val jpgBytes = CookiedFuel.get(captchaUrl).awaitByteArray()
        val bitmap = BitmapFactory.decodeByteArray(jpgBytes, 0, jpgBytes.size)
        val captchaInput = suspendCancellableCoroutine<String?> { continuation ->
            CaptchaDialog(activity, bitmap) {
                continuation.resume(it)
            }.show()
        } ?: throw DataInvalidException("用户未输入验证码")
        val (_, loginResp, _) = CookiedFuel.post(
            "https://zhjwxk.cic.tsinghua.edu.cn/j_acegi_formlogin_xsxk.do",
            listOf(
                "j_username" to username,
                "j_password" to password,
                "captchaflag" to captchaParam,
                "_login_image_" to captchaInput
            )
        ).awaitStringResponse(Charset.forName("GBK"))
        if ("login_error" in loginResp.url.toString()) {
            if ("code_error" in loginResp.url.toString()) throw DataInvalidException("验证码错误！")
        }

        val rawStr = CookiedFuel.get("http://zhjwxk.cic.tsinghua.edu.cn/syxk.vsyxkKcapb.do?m=ztkbSearch&p_xnxq=2019-2020-2&pathContent=整体课表")
            .awaitString(Charset.forName("GBK"))

        println(rawStr)

        TODO("Not yet implemented")
    }

    override suspend fun loadAllCourse(term: SchoolTerm): List<CalendarItemDataWithTimes> {
        TODO("Not yet implemented")
    }



    @SuppressLint("InflateParams", "SetJavaScriptEnabled")
    suspend fun refreshHomework(activity: Activity, username: String, password: String, extra: Map<String, Any>? = null): String {
        val webView = LayoutInflater.from(activity).inflate(R.layout.homework_webview,  null) as WebView
        WebView.setWebContentsDebuggingEnabled(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        webView.settings.javaScriptEnabled = true
        webView.settings.allowUniversalAccessFromFileURLs = true

        var loadFirstSuccess = false
        webView.webViewClient = object: WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (!loadFirstSuccess) {
                    loadFirstSuccess = true
                    webView.evaluateJavascript("getHomework(\"$username\", \"$password\")", null)
                }
            }
        }

        val rawHomeworkData = suspendCancellableCoroutine<String> { continuation ->
            val javaObj = object {
                @JavascriptInterface
                fun homeworkData(data: String) {
                    continuation.resume(data)
                }
            }
            webView.addJavascriptInterface(javaObj, "java")
            webView.loadUrl("file:///android_asset/www/homework-index.html")
        }

        webView.destroy()
        return rawHomeworkData
    }
}
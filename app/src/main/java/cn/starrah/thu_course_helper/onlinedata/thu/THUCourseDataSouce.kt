package cn.starrah.thu_course_helper.onlinedata.thu

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.data.constants.THE_ZONE
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarItemData
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarItemDataWithTimes
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeData
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeDataWithItem
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarItemLegalDetailKey
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarItemType
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarTimeType
import cn.starrah.thu_course_helper.data.declares.school.SchoolTerm
import cn.starrah.thu_course_helper.data.declares.school.SchoolTermType
import cn.starrah.thu_course_helper.data.declares.time.TimeInCourseSchedule
import cn.starrah.thu_course_helper.data.declares.time.TimeInHour
import cn.starrah.thu_course_helper.data.utils.*
import cn.starrah.thu_course_helper.onlinedata.AbstractCourseDataSource
import cn.starrah.thu_course_helper.onlinedata.backend.BACKEND_SITE
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.coroutines.awaitByteArray
import com.github.kittinunf.fuel.coroutines.awaitString
import com.github.kittinunf.fuel.coroutines.awaitStringResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.net.URI
import java.nio.charset.Charset
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.math.roundToInt

object THUCourseDataSouce : AbstractCourseDataSource() {
    private val CAPTCHA_PATTERN = Pattern.compile("<img id=\"captcha\" src=\"(.*?)\".*?>")
    private val XK_BASE_URL = "http://zhjwxk.cic.tsinghua.edu.cn"

    //    private val ACEGI_PATTERN =
//        Pattern.compile("src=\"(.*?/j_acegi_login\\.do\\?url=/jxmh\\.do&amp;m=bks_jxrl&amp;ticket=[a-zA-Z0-9]+)\"")
    override val schoolName: String = "清华大学"

    private suspend fun checkInTUNET(): Boolean {
        val notInTUNET = Fuel.get("http://info.tsinghua.edu.cn/")
            .awaitStringResponse(Charset.forName("GBK")).second.url.toString()
            .contains(Regex("[oO]ut"))
        return !notInTUNET
    }

    private suspend fun assertInTUNET() {
        if (!checkInTUNET()) throw DataInvalidException("您当前不在校园网环境，因此无法登录；请您连接到清华大学校园网，或者使用sslvpn后重试。\n若要了解使用sslvpn的方法，请访问https://sslvpn.tsinghua.edu.cn/ 。")
    }

    private var captchaParam: String? = null

    /**
     * 登录清华大学选课系统。具体使用：
     *
     * (1) 首先请求验证码：要求extra参数中，"requireCaptcha"字段的值为true。至于username和password字段，
     * 则可以随便传（比如传""）。这一过程必定返回非空的Bitmap类型，请把这个Bitmap显示给用户以便用户填写。
     *
     * (2) 之后再请求一次本函数，username和password就是用户实际的用户名和密码，而extra要求其中"captcha"字段
     * 为用户填写的验证码值。如果登录成功则必定返回null；如果登录失败则会直接抛异常。
     */
    override suspend fun login(
        username: String,
        password: String,
        extra: Map<String, Any>
    ): Bitmap? {
        assertInTUNET()
        if (extra["requireCaptcha"] == true) {
            val resStr = CookiedFuel.get("$XK_BASE_URL/xklogin.do")
                .awaitString(Charset.forName("GBK"))
            val captchaUrl = XK_BASE_URL + CAPTCHA_PATTERN.matcher(resStr).run { find(); group(1) }
            captchaParam = captchaUrl.split("=")[1]
            val jpgBytes = CookiedFuel.get(captchaUrl).awaitByteArray()
            val bitmap = BitmapFactory.decodeByteArray(jpgBytes, 0, jpgBytes.size)
            return bitmap
        }
        else {
            val (_, loginResp, _) = CookiedFuel.post(
                "https://zhjwxk.cic.tsinghua.edu.cn/j_acegi_formlogin_xsxk.do", listOf(
                    "j_username" to username,
                    "j_password" to password,
                    "captchaflag" to (captchaParam
                        ?: throw DataInvalidException("没有有效的验证码图片。请重试。")),
                    "_login_image_" to (extra["captcha"] ?: throw DataInvalidException("您没有输入验证码！"))
                )
            ).awaitStringResponse(Charset.forName("GBK"))
            if ("login_error" in loginResp.url.toString()) {
                if ("code_error" in loginResp.url.toString()) throw DataInvalidException("验证码错误！")
                else throw DataInvalidException("登录错误，可能是用户名或密码不正确！")
            }
            isSessionValid = true
            return null
        }
    }

    private fun calculateTermStrInXK(term: SchoolTerm): String? {
        return when (term.type) {
            SchoolTermType.AUTUMN -> "${term.beginYear}-${term.beginYear + 1}-1"
            SchoolTermType.SPRING -> "${term.beginYear}-${term.beginYear + 1}-2"
            SchoolTermType.SUMMER -> "${term.beginYear}-${term.beginYear + 1}-3"
            else                  -> null // 不可获取课表（如寒假）
        }
    }

    data class CourseLengthAPIBody(
        val courseId: String,
        val name: String,
        var length: Float? = null,
        var start: Float? = null
    )

    suspend fun tryShouldFixDataFromBackendLaterAfterWrittenToDB(context: Context): Boolean {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val reqStr = sp.getString("needFixXKData_itemJson", null)
        if (sp.getBoolean("needFixXKData", false) && reqStr != null) {
            try {
                withContext(Dispatchers.IO) {
                    val respBodyList = JSON.parseArray(
                        CookiedFuel.post("$BACKEND_SITE/courseLength")
                            .jsonBody(reqStr).awaitString(),
                        CourseLengthAPIBody::class.java
                    )
                    val itemList = CREP.DAO.findAllItems()
                    val toUpdateOnes = mutableListOf<CalendarTimeData>()

                    for (one in respBodyList) {
                        val item =
                            itemList.find { it.detail[CalendarItemLegalDetailKey.COURSEID] == one.courseId }!!
                        if (one.length != null) item.times.forEach {
                            it.timeInCourseSchedule!!.lengthSmall = one.length!!
                        }
                        if (one.start != null) item.times.forEach {
                            it.timeInCourseSchedule!!.startOffsetSmall = one.start!!
                        }
                        toUpdateOnes += item.times
                    }

                    CREP.DAO.updateTimes(toUpdateOnes)
                }
                sp.edit {
                    remove("needFixXKData")
                    remove("needFixXKData_itemJson")
                }
                return true // 操作成功
            }
            catch (e: Exception) {
                // 与后端连接失败也不会使得获取课程失败；只是打印一个异常。
                // 但是，sharedPreference中会记录下当前的itemList和需要稍后连接后端的事情
                // 并在MainActivity把这个任务launch出去。
                e.printStackTrace()
                sp.edit {
                    putBoolean("needFixXKData", true)
                    putString("needFixXKData_itemJson", reqStr)
                }
                return false // 操作失败
            }
        }
        return true // 不需要操作
    }

    suspend fun tryFixDataFromBackend(
        context: Context,
        itemList: List<CalendarItemDataWithTimes>
    ): Boolean {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val reqBodyList = itemList.map {
            CourseLengthAPIBody(it.detail[CalendarItemLegalDetailKey.COURSEID] ?: "", it.name)
        }
        val reqStr = JSON.toJSONString(reqBodyList)
        try {
            val respBodyList = JSON.parseArray(
                CookiedFuel.post("$BACKEND_SITE/courseLength")
                    .jsonBody(reqStr).awaitString(),
                CourseLengthAPIBody::class.java
            )
            for (one in respBodyList) {
                val item =
                    itemList.find { it.detail[CalendarItemLegalDetailKey.COURSEID] == one.courseId }!!
                if (one.length != null) item.times.forEach {
                    it.timeInCourseSchedule!!.lengthSmall = one.length!!
                }
                if (one.start != null) item.times.forEach {
                    it.timeInCourseSchedule!!.startOffsetSmall = one.start!!
                }
            }
            sp.edit {
                remove("needFixXKData")
                remove("needFixXKData_itemJson")
            }
            return true
        }
        catch (e: Exception) {
            // 与后端连接失败也不会使得获取课程失败；只是打印一个异常。
            // 但是，sharedPreference中会记录下当前的itemList和需要稍后连接后端的事情
            // 并在MainActivity把这个任务launch出去。
            e.printStackTrace()
            sp.edit {
                putBoolean("needFixXKData", true)
                putString("needFixXKData_itemJson", reqStr)
            }
            return false
        }
    }

    /**
     * 读取全部课程数据（但不处理）。
     *
     * 一般而言，调用本函数后，需要把本函数的返回值作为参数调用[applyLoadedCourses].才能把获取到的课表同步到本地数据中。
     *
     * **请注意，extra中必须提供一个"context"字段、内容为一个Context！否则会抛出异常！**
     */
    override suspend fun loadAllCourses(
        term: SchoolTerm,
        extra: Map<String, Any>
    ): List<CalendarItemDataWithTimes> {
        return withContext(Dispatchers.IO) {
            assertInTUNET()
            assertDataSystem(schoolName == term.schoolName, "学校名称错误！")
            val termStrInXK = calculateTermStrInXK(term)
                ?: return@withContext listOf<CalendarItemDataWithTimes>()
            val rawStr =
                CookiedFuel.get("$XK_BASE_URL/syxk.vsyxkKcapb.do?m=ztkbSearch&p_xnxq=$termStrInXK&pathContent=整体课表")
                    .awaitString(Charset.forName("GBK"))

            if ("用户登陆超时或访问内容不存在。" in rawStr) {
                isSessionValid = false
                PreferenceManager.getDefaultSharedPreferences(extra["context"] as Context)
                    .edit { putInt("login_status", 4) }
                throw LoginStatusTimeoutException()
            }
            if ("该学年学期的选课或退课不在选课进度表中" in rawStr) throw DataInvalidException("当前学期的课程无法从教务系统的数据库中获得。可能是所选学期是将来的学期，或过于久远的过去学期。")

            val itemList = parseRawZTKB(rawStr, term)

            tryFixDataFromBackend(extra["context"] as Context, itemList)

            itemList
        }
    }

    /**
     * 把获取到的选课系统课程数据同步到本地数据中。
     *
     * 本函数的extra不需要传。
     */
    override suspend fun applyLoadedCourses(
        courses: List<CalendarItemDataWithTimes>,
        extra: Map<String, Any>
    ) = withContext(Dispatchers.IO) {
        val oldData = CREP.DAO.findAllItems().filter { it.type == CalendarItemType.COURSE }
        val newData = courses

        val oldDataMap = oldData.associateBy { it.name }
        val oldDataCourseIDMap =
            oldData.associateBy { it.detail[CalendarItemLegalDetailKey.COURSEID] }
        val newDataNames = newData.map { it.name }.toSet()
        val newDataCourseIDs =
            newData.mapNotNull { it.detail[CalendarItemLegalDetailKey.COURSEID] }.toSet()

        val toDeleteOnes = oldData.filter {
            it.detail[CalendarItemLegalDetailKey.FROM_WEB] == XK_WEB_KEYWORD &&
                    it.name !in newDataNames &&
                    it.detail[CalendarItemLegalDetailKey.COURSEID].let { it != null && it !in newDataCourseIDs }
        }

        val newDataMatchMap = newData.associate {
            Pair(it, it.detail[CalendarItemLegalDetailKey.COURSEID]?.let { oldDataCourseIDMap[it] }
                ?: oldDataMap[it.name])
        }

        val toAddOnes = newDataMatchMap.mapNotNull { if (it.value == null) it.key else null }
        val toModifyOnes = newDataMatchMap.filter { it.value != null }

        val realModifiedOnes = toModifyOnes.filter { (newItem, old) ->
            applyWebModificationOntoOneItem(old!!, newItem)
        }.map { it.value!! }

        CREP.DAO.deleteItems(toDeleteOnes)
        for (it in toAddOnes + realModifiedOnes) {
            CREP.DAO.updateItemAndTimes(it, it.times)
        }

    }

    /** 登录info直接获得cookie。作为备用。 */
    private suspend fun getCookie(
        username: String,
        password: String
    ): String {
        assertInTUNET()
        // 登录info
        val (_, resp, _) = CookiedFuel.post(
            "https://info.tsinghua.edu.cn/Login", listOf(
                "redirect" to "NO",
                "userName" to username,
                "password" to password,
                "x" to "34",
                "y" to "4"
            )
        ).awaitStringResponse(Charset.forName("GBK"))
        if (resp.url.toString().run { substring(length - 1) } != "1")
            throw DataInvalidException("登录失败，可能是用户名或密码错误")
        return DEFAULT_COOKIEJAR.cookieMap["info.tsinghua.edu.cn"]!!["UPORTALINFONEW"]!!.first
    }

    /**
     * 读取各种数据，并且可以直接把获取到的相关数据写入(apply)到数据库中。
     *
     * （A） 获取课程作业
     *
     * 参数：{ "homework": true, "username": 用户名, "password": 密码, "activity": 当前[Activity]实例,
     *        "onlyUnsubmitted": 如果为true，则只获取未提交且未过期的作业，否则获取全部课程作业,
     *        "apply": 如果为true，则获取到的数据会直接被写入到数据库中 }
     *
     * 返回类型：[List]<[CalendarItemDataWithTimes]>，获取到的每个课程作业对应的日程
     *
     * （B） 获取期末考试数据
     *
     * 注：如果某门考试，其对应的日程并不存在于数据库中，那么这样的时间段也是会存在于返回值中的（课程Item的id为0）；
     * 但是如果设置了apply选项，那么这样的时间段并不会被添加到数据库中（因为找不到其对应的日程，即本函数不可能
     * 向数据库中添加以前不存在的日程、只会对时间段进行添加和修改。）
     *
     * 参数：{ "exam": true, "username": 用户名, "password": 密码,
     *        "apply": 如果为true，则获取到的数据会直接被写入到数据库中 }
     *
     * 返回类型：[List]<[CalendarItemDataWithTimes]>，获取到的每个期末考试，对应的时间段数据。
     */
    override suspend fun loadData(term: SchoolTerm, extra: Map<String, Any>): Any? {
        if (extra["homework"] == true) {
            assertDataSystem(
                extra["username"] is String && extra["password"] is String,
                "没有传入用户名和密码。"
            )
            assertDataSystem(extra["activity"] is Activity, "没有传入Activity引用。")
            val rawStr = getHomework(
                extra["activity"] as Activity,
                extra["username"] as String,
                extra["password"] as String
            )
            val onlyUnsubmitted: Boolean = extra["onlyUnsubmitted"] as? Boolean ?: true
            val apply: Boolean = extra["apply"] as? Boolean ?: false
            val parsedData = parseHomework(rawStr, onlyUnsubmitted)
            if (apply) applyHomework(parsedData)
            return parsedData
        }
        else if (extra["exam"] == true) {
            assertDataSystem(
                extra["username"] is String && extra["password"] is String,
                "没有传入用户名和密码。"
            )
            val apply: Boolean = extra["apply"] as? Boolean ?: false
            val data = getExam(
                extra["username"] as String,
                extra["password"] as String,
                term,
                apply
            )
            return data
        }
        throw Exception("不支持的操作！")
    }

    /**
     * （A）获取后端验证所用的Cookie。
     *
     * 参数：("VPNCookie", 用户名, 密码)
     *
     * 返回：CookieJar类型的对象，可以直接JSON序列化后发给后端。。
     */
    override suspend fun doSomething(vararg params: Any?): Any? {
        if (params[0] as? String == "VPNCookie") {
            assertDataSystem(
                params.size >= 3 && params[1] is String && params[2] is String,
                "没有传入用户名和密码"
            )
            return getVPNCookiejar(params[1] as String, params[2] as String)
        }

        throw NotImplementedError("不支持的操作！")
    }

    private suspend fun loginWEBVPN(
        username: String,
        password: String
    ) {
        val respStr = CookiedFuel.post(
            "${WEBVPN_SITE}/do-login?local_login=true", listOf(
                "auth_type" to "local",
                "username" to username,
                "password" to password,
                "sms_code" to ""
            )
        ).awaitString()
        if ("验证码" in respStr) throw Exception("Web VPN要求验证码，请过一段时间再尝试。")
        if ("密码错误" in respStr) throw Exception("登录失败，可能是用户名或密码错误")
    }

    val XK_WEB_KEYWORD = "XK"

    private suspend fun getExam(
        username: String,
        password: String,
        term: SchoolTerm,
        apply: Boolean = false
    ): List<CalendarTimeDataWithItem> {
        return withContext(Dispatchers.IO) {
            val ACADEMIC_SITE = "http://academic.tsinghua.edu.cn"
            val ACADEMIC_VPN_PREFIX =
                "$WEBVPN_SITE/http/77726476706e69737468656265737421f1f44098223d6153301c9aa596522b2027124ec39c5debe6"
            val ZHJW_SITE = "http://zhjw.cic.tsinghua.edu.cn"
            val ZHJW_VPN_PREFIX =
                "$WEBVPN_SITE/http/77726476706e69737468656265737421eaff4b8b69336153301c9aa596522b20bc86e6e559a9b290"

            val needVPN = !checkInTUNET()
            if (needVPN) loginWEBVPN(username, password)

            // 登录academic
            CookiedFuel.post(
                "${if (needVPN) ACADEMIC_VPN_PREFIX else "https://academic.tsinghua.edu.cn"}/Login",
                listOf(
                    "userName" to username,
                    "password" to password
                )
            ).awaitStringResponse(GBKCharset)

            val J_ACEGI_PATTERN =
                Pattern.compile("(?:src|href)=\"(.*?/j_acegi_login\\.do\\?url=/zhjw\\.do&amp;m=jxmh_show&amp;.*?ticket=[a-zA-Z0-9]+)\"")
            //访问rootNode，获取j_acegi
            val rootNodeString = CookiedFuel.get(
                "${if (needVPN) ACADEMIC_VPN_PREFIX else ACADEMIC_SITE}/render.userLayoutRootNode.uP"
            ).awaitString(GBKCharset)
            val matcher = J_ACEGI_PATTERN.matcher(rootNodeString)
            val acegi_url = if (matcher.find()) matcher.group(1)!!.replace("&amp;", "&")
            else throw throw Exception("登录失败，可能是用户名或密码错误")
            //登录j_acegi
            CookiedFuel.get(acegi_url).awaitString(GBKCharset)

            //获取本学期课程列表，存储为课程号和课序号的列表
            val courseListHtml =
                CookiedFuel.get("${if (needVPN) ZHJW_VPN_PREFIX else ZHJW_SITE}/portal3rd.do?m=bks_bxqkc&version=1")
                    .awaitString(GBKCharset)
            val KSSJ_PATTERN = Pattern.compile("<a id=\"kssj-(\\d{8})-(\\d{1,3})\">")
            val matcherKSSJ = KSSJ_PATTERN.matcher(courseListHtml)
            val kchList = mutableListOf<Pair<String, String>>()
            val jsonObjList = mutableListOf<JSONObject>()
            while (matcherKSSJ.find()) {
                kchList.add(Pair(matcherKSSJ.group(1), matcherKSSJ.group(2)))
            }

            val JSONCALLBACK = "jQuery${System.currentTimeMillis()}"
            @Suppress("RegExpRedundantEscape") val JSONP_PATTERN =
                Pattern.compile("$JSONCALLBACK\\(\\[.*?(\\{.*\\}).*?\\]\\)", Pattern.DOTALL)

            val termStr =
                calculateTermStrInXK(term) ?: return@withContext listOf<CalendarTimeDataWithItem>()
            for ((kch, kxh) in kchList) {
                //对每个课程号，逐个获取考试信息，存为JSONObject
                val respRawStr = CookiedFuel.get(
                    "${if (needVPN) ZHJW_VPN_PREFIX else ZHJW_SITE}/jxmh.do?m=bks_ksSearch", listOf(
                        "kch" to kch,
                        "kxh" to kxh,
                        "p_xnxq" to termStr,
                        "jsoncallback" to JSONCALLBACK
                    )
                ).awaitString(GBKCharset)
                val matcherJsonp = JSONP_PATTERN.matcher(respRawStr)
                val respJsonObj =
                    if (matcherJsonp.find()) JSON.parseObject(matcherJsonp.group(1)) else null
                respJsonObj?.let { jsonObjList.add(it) }
            }

            //获取原始的课程item信息
            val itemsList = CREP.DAO.findItemsSpecifiedTypeNotLive(
                CalendarItemType.COURSE.name
            )
            val itemKCHMap =
                itemsList.associateBy { it.detail[CalendarItemLegalDetailKey.COURSEID] }
            val itemNameMap = itemsList.associateBy { it.name }

            val toUpdateTimes = mutableListOf<CalendarTimeDataWithItem>()
            val toAddTimes = mutableListOf<CalendarTimeDataWithItem>()
            val toReturnButNoItemTimes = mutableListOf<CalendarTimeDataWithItem>()

            val KSRQ_PATTERN = Pattern.compile("(\\d{2})\\.(\\d{2})\\S (\\S{2})")
            fun parseKsrq(ksrq: String): TimeInHour? {
                val matcherKsrq = KSRQ_PATTERN.matcher(ksrq)
                if (matcherKsrq.find()) {
                    val (startTime, endTime) = when (matcherKsrq.group(3)) {
                        "上午" -> Pair(LocalTime.of(8, 0), LocalTime.of(10, 0))
                        "下午" -> Pair(LocalTime.of(14, 30), LocalTime.of(16, 30))
                        "晚上" -> Pair(LocalTime.of(19, 0), LocalTime.of(21, 0))
                        else -> return null
                    }
                    val date: LocalDate = LocalDate.of(
                        LocalDate.now().year + (if (term.type == SchoolTermType.AUTUMN) 1 else 0),
                        matcherKsrq.group(1)!!.toInt(),
                        matcherKsrq.group(2)!!.toInt()
                    )
                    return TimeInHour(startTime, endTime, date = date)
                }
                else return null
            }

            for (jsonObj in jsonObjList) {
                //JSONObject转为TimeInHour、并根据Item的情况，重用修改或者新增TimeData。
                val item = itemKCHMap[jsonObj["kch"] as String]
                    ?: itemNameMap[jsonObj["kcm"] as String]
                val newTimeInHour = parseKsrq(jsonObj["ksrq"] as String) ?: continue

                if (item == null) {
                    // 找不到日程。那么要编一日程共返回使用；但是不写入数据库
                    val newItem = CalendarItemData(
                        name = jsonObj["kcm"] as String,
                        type = CalendarItemType.COURSE
                    ).apply {
                        detail[CalendarItemLegalDetailKey.COURSEID] = jsonObj["kch"] as String
                    }
                    val timeWithItem = CalendarTimeDataWithItem().apply {
                        name = "期末考试"
                        type = CalendarTimeType.SINGLE_HOUR
                        timeInHour = newTimeInHour
                        place = jsonObj["ksdd"] as String
                        item_id = newItem.id
                        calendarItem = newItem
                    }
                    toReturnButNoItemTimes.add(timeWithItem)
                    continue
                }

                //找得到日程。接下来根据是否找得到时间段，决定是编辑还是新增。
                var time = item.times.find { it.name == "期末考试" }
                if (time != null) {
                    time.timeInHour = newTimeInHour
                    time.type = CalendarTimeType.SINGLE_HOUR
                    time.place = jsonObj["ksdd"] as String
                    toUpdateTimes.add(CalendarTimeDataWithItem(time).apply { calendarItem = item })
                }
                else {
                    time = CalendarTimeData(
                        name = "期末考试",
                        type = CalendarTimeType.SINGLE_HOUR,
                        timeInHour = newTimeInHour,
                        place = jsonObj["ksdd"] as String,
                        item_id = item.id
                    )
                    item.times.add(time)
                    toAddTimes.add(CalendarTimeDataWithItem(time).apply { calendarItem = item })
                }
            }

            val toApplyList = toAddTimes + toUpdateTimes
            if (apply) {
                CREP.DAO.updateTimes(toApplyList)
            }

            val toReturnList = toApplyList + toReturnButNoItemTimes
            toReturnList
        }
    }

    val WEBVPN_SITE = "https://webvpn.tsinghua.edu.cn"
    val GBKCharset = Charset.forName("GBK")

    // 下面是调用的私有函数。外部使用者不需要看这些内容。
    private suspend fun getVPNCookiejar(
        username: String,
        password: String
    ): CookieJar {
        val J_ACEGI_PATTERN =
            Pattern.compile("(?:src|href)=\"(.*?/j_acegi_login\\.do\\?url=/jxmh\\.do&amp;m=bks_jxrl&amp;ticket=[a-zA-Z0-9]+)\"")

        val INFO_VPN_PREFIX =
            "$WEBVPN_SITE/http/77726476706e69737468656265737421f9f9479369247b59700f81b9991b2631506205de"

        loginWEBVPN(username, password)

        val (_, resp, _) = CookiedFuel.post(
            "${INFO_VPN_PREFIX}/Login", listOf(
                "redirect" to "NO",
                "userName" to username,
                "password" to password,
                "x" to "34",
                "y" to "4"
            )
        ).awaitStringResponse(GBKCharset)
        if (resp.url.toString().run { substring(length - 1) } != "1")
            throw Exception("登录失败，可能是用户名或密码错误")

        val rootNodeString = CookiedFuel.get(
            "${INFO_VPN_PREFIX}/render.userLayoutRootNode.uP"
        ).awaitString(GBKCharset)
        val matcher = J_ACEGI_PATTERN.matcher(rootNodeString)
        if (!matcher.find()) throw throw Exception("登录失败，可能是用户名或密码错误")

        val webvpnHost = URI(WEBVPN_SITE).host
        return CookieJar().apply {
            DEFAULT_COOKIEJAR.cookieMap[webvpnHost]?.let {
                cookieMap[webvpnHost] = it
            }
        }
    }
    
    class HomeWorkData(
        val courseNames: List<_ID_Name>,
        val homeworks: Map<String, List<JSONObject>>
    ) {
        class _ID_Name(val id: String, val name: String)
        class _HomeWork(
            val title: String,
            val deadline: LocalDateTime,
            val submitted: Boolean,
            val courseName: String,
            val id: String
        ) {
            val fromNowtoDeadline: Duration
                get() = Duration.between(LocalDateTime.now(), deadline)

            companion object {
                fun of(jsonObject: JSONObject, courseName: String): _HomeWork {
                    return _HomeWork(
                        jsonObject["title"] as String,
                        ZonedDateTime.parse(
                            jsonObject["deadline"] as String,
                            DateTimeFormatter.ISO_DATE_TIME
                        ).withZoneSameInstant(THE_ZONE).toLocalDateTime(),
                        jsonObject["submitted"] as Boolean,
                        courseName,
                        jsonObject["id"] as String
                    )
                }
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @SuppressLint("InflateParams", "SetJavaScriptEnabled")
    private suspend fun getHomework(
        activity: Activity,
        username: String,
        password: String,
        extra: Map<String, Any>? = null
    ): String {
        val webView =
            LayoutInflater.from(activity).inflate(R.layout.homework_webview, null) as WebView
        WebView.setWebContentsDebuggingEnabled(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        webView.settings.javaScriptEnabled = true
        webView.settings.allowUniversalAccessFromFileURLs = true

        var loadFirstSuccess = false
        webView.webViewClient = object : WebViewClient() {
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

    suspend fun parseHomework(
        raw: String,
        onlyUnsubmitted: Boolean = false
    ): MutableList<CalendarItemDataWithTimes> {
        fun parseCalendarItem(
            homework: HomeWorkData._HomeWork,
            onlyUnsubmitted: Boolean = false
        ): CalendarItemDataWithTimes? { // 返回值表示要新增的对象
            if (onlyUnsubmitted && (homework.submitted || homework.deadline < LocalDateTime.now()))
                return null // 对于已提交或已过期的作业不予处理

            // 生成时间段数据
            val timeInHour = TimeInHour(
                homework.deadline.toLocalTime(),
                homework.deadline.toLocalTime(),
                date = homework.deadline.toLocalDate()
            )
            val time = CalendarTimeData(
                name = "(${homework.courseName})",
                type = CalendarTimeType.POINT,
                timeInHour = timeInHour,
                comment = if (homework.submitted) "已提交" else "未提交"
            )
            val item = CalendarItemDataWithTimes(mutableListOf(time))
            item.name = homework.title
            item.type = CalendarItemType.OTHER
            item.detail[CalendarItemLegalDetailKey.COMMENT] = "网络学堂作业"
            item.detail[CalendarItemLegalDetailKey.FROM_WEB] = "${homework.id},learn"
            return item
        }

        return withContext(Dispatchers.IO) {
            val obj = JSON.parseObject(raw, HomeWorkData::class.java)
            val courseNameMap = obj.courseNames.associate { Pair(it.id, it.name) }

            val resultList = mutableListOf<CalendarItemDataWithTimes>()
            for ((id, homeworks) in obj.homeworks) {
                for (homeworkJsonobj in homeworks) {
                    val homework = HomeWorkData._HomeWork.of(homeworkJsonobj, courseNameMap[id]!!)
                    val item = parseCalendarItem(homework, onlyUnsubmitted)
                    if (item != null) resultList.add(item)
                }
            }
            return@withContext resultList
        }
    }

    suspend fun applyHomework(list: MutableList<CalendarItemDataWithTimes>) {
        fun tryReuseCalendarItem(
            item: CalendarItemDataWithTimes,
            // 最终oldItemsToDeleteIdMap里剩下的所有日程会被删掉的。
            oldItemsToDeleteFromWebMap: MutableMap<String, CalendarItemDataWithTimes>? = null
        ): Boolean { // 返回值false重用失败，应当新增新的对象；true表示重用成功，不需要新增这个对象
            // 尝试查找可复用的数据对象
            val key = item.detail[CalendarItemLegalDetailKey.FROM_WEB]
            val oldItem = oldItemsToDeleteFromWebMap?.get(key)
            val oldTime = oldItem?.times?.get(0)
            val time = item.times[0]
            if (oldItem != null && oldTime != null && oldItem.name == item.name && oldItem.type == item.type &&
                oldTime.name == time.name && oldTime.type == time.type && oldTime.timeInHour == time.timeInHour) {
                // 找到则复用对象即可。那么既不要删除，也不要新增。
                // 从删除列表里移除原日程的引用，而返回false表示重用成功、当前的item对象不要添加到数据库中。
                oldItemsToDeleteFromWebMap.remove(key)
                return true
            }

            //没找到，则返回false表示重用失败、应当添加当前的新对象到数据库中。
            return false
        }

        withContext(Dispatchers.IO) {
            val oldHomework = CREP.helper_findHomeworkItems()
            val toDeleteMap = oldHomework.associateBy {
                it.detail[CalendarItemLegalDetailKey.FROM_WEB] ?: ""
            }.toMutableMap()
            val toAddList = list.filter { !tryReuseCalendarItem(it, toDeleteMap) }

            CREP.DAO.deleteItems(toDeleteMap.values.toList())
            for (toAddOne in toAddList) {
                try {
                    CREP.updateItemAndTimes(toAddOne, toAddOne.times)
                }
                catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * @return 是否对old进行了修改（决定了要不要存回去）
     */
    private fun applyWebModificationOntoOneItem(
        old: CalendarItemDataWithTimes,
        newItem: CalendarItemDataWithTimes
    ): Boolean {
        var modifiedOld = false
        val newTimeMatchMap = newItem.times.associate { n ->
            Pair(n, old.times.find {
                n.timeInCourseSchedule == it.timeInCourseSchedule && n.repeatWeeks == it.repeatWeeks &&
                        n.type == it.type
            })
        }

//        val toAddTimes =
        newTimeMatchMap.filter { (n, o) ->
            if (o != null && o.place.indexOf(n.place) == -1) {
                //只修改地点
                o.place = n.place
                modifiedOld = true
                false // 不增加新的
            }
            else true
        }.map { it.key }

        /* 考虑到实际情况，对时间段和合并策略如下：不处理任何新增或删除（因为很可能是老师的特殊处理
        导致与选课系统不符）；
        但是，仅对一种最常见的情况作出处理，即：能够建立新老时间段的映射关系，但是新的place字段不是
        老的place字段的子字符串（在老place里indexOf找不到新place），
        此时用新获取的place字段替代旧的，因为这对应着一种常见情况即由于扩容等原因，在学期初一门课程
        更换上课教室的情况。 */
        /* 下面两行代码是处理增加时间段的情况，暂不开启。 */
//        if (toAddTimes.size != 0) modifiedOld = true
//        old.times.plusAssign(toAddTimes)

        return modifiedOld
    }

    private val PAT_C1_LINK =
        Pattern.compile("strHTML \\+= \"<a class='mainHref' href='.*?;(\\d{8})'.*?>\"")
    private val PAT_C1_TITLE = Pattern.compile("strHTML \\+= \"<b>(.*)</b>\"")
    private val PAT_C1_DATA = Pattern.compile("strHTML1 \\+= ?\"；(.*)\"")
    private val PAT_C1_WEEKBIG =
        Pattern.compile("getElementById\\('a(\\d)_(\\d)'\\).innerHTML \\+= strHTML")
    private val PAT_C1_WEEKDES = Pattern.compile("(\\d+)-(\\d+)")
    private val PAT_C2 =
        Pattern.compile("strHTML = \"<a class='blue_red_none' href='.*?p_id=(\\d+)'.*?><b><font color='blue'>(.*?)</font></b></a><font color='blue'>(.*?)</font>\";")
    private val PAT_NUMBER = Pattern.compile("\\d+")
    private val PAT_C2_DET = Pattern.compile("(.*?)\\((.*)\\)")
    private val PAT_C2_TIMESTR = Pattern.compile("时间：(\\d+):(\\d+)-(\\d+):(\\d+)")

    private val XKTYPE_LIST = listOf("必修", "限选", "任选")

    fun parseRawZTKB(raw: String, term: SchoolTerm): List<CalendarItemDataWithTimes> {
        val startIndex = "function setInitValue()".let { raw.indexOf(it) + it.length }
        val endIndex = raw.indexOf("Event.observe")
        val exraw = raw.substring(startIndex, endIndex)
        val lines = exraw.lines().filter { "strHTML" in it }
        val lineIterator = lines.listIterator()

        val foundCourseDict = mutableMapOf<String, CalendarItemDataWithTimes>()

        outer@ while (true) {
            var tempCourseNumber: String? = null
            lateinit var title: String
            lateinit var item: CalendarItemDataWithTimes
            lateinit var time: CalendarTimeData
            var detStr: String? = null
            var curStatus = 0
            while (true) {
                if (!lineIterator.hasNext()) break@outer
                val line = lineIterator.next()
                if (curStatus < 1) { // 初始状态
                    val matcher = PAT_C1_LINK.matcher(line)
                    if (matcher.find()) {
                        val courseNumber = matcher.group(1)
                        tempCourseNumber = courseNumber
                        curStatus = 1
                        continue
                    }
                    val c2Matcher = PAT_C2.matcher(line)
                    if (c2Matcher.find()) {
                        tempCourseNumber = c2Matcher.group(1)!!.substring(0, 8)
                        title = c2Matcher.group(2)!!
                        detStr = c2Matcher.group(3)!!
                        item = foundCourseDict[title] ?: CalendarItemDataWithTimes().apply {
                            name = title
                            type = CalendarItemType.COURSE
                            tempCourseNumber.let {
                                detail[CalendarItemLegalDetailKey.COURSEID] = it
                            }
                        }
                        time = CalendarTimeData(item_id = item.id)
                        curStatus = 22
                    }
                }
                else if (curStatus == 1) { // 识别课程名
                    val matcher = PAT_C1_TITLE.matcher(line)
                    if (matcher.find()) {
                        title = matcher.group(1)!!
                        item = foundCourseDict[title] ?: CalendarItemDataWithTimes().apply {
                            name = title
                            type = CalendarItemType.COURSE
                            tempCourseNumber?.let {
                                detail[CalendarItemLegalDetailKey.COURSEID] = it
                            }
                        }
                        time = CalendarTimeData(
                            item_id = item.id,
                            timeInCourseSchedule = TimeInCourseSchedule()
                        )
                        curStatus = 2
                    }
                }
                else if (curStatus == 2) { //识别教师
                    val matcher = PAT_C1_DATA.matcher(line)
                    if (matcher.find()) {
                        val teacher = matcher.group(1)!!
                        item.detail[CalendarItemLegalDetailKey.TEACHER] = teacher
                        curStatus = 3
                    }
                }
                else if (curStatus == 3) { //识别课程属性
                    val matcher = PAT_C1_DATA.matcher(line)
                    if (matcher.find()) {
                        val str = matcher.group(1)!!
                        if (str !in XKTYPE_LIST) lineIterator.previous()
                        curStatus = 4
                    }
                }
                else if (curStatus == 4) { //识别周安排
                    val matcher = PAT_C1_DATA.matcher(line)
                    if (matcher.find()) {
                        val weekRawStr = matcher.group(1)!!
                        val repeatWeek = dealWithWeekStr(weekRawStr)
                        time.repeatWeeks = repeatWeek
                        time.type =
                            if (repeatWeek.size > 1) CalendarTimeType.REPEAT_COURSE else CalendarTimeType.SINGLE_COURSE
                        curStatus = 5
                    }
                }
                else if (curStatus == 5) { //识别地点
                    val matcher = PAT_C1_DATA.matcher(line)
                    if (matcher.find()) {
                        val place = matcher.group(1)!!
                        time.place = place
                        curStatus = 6
                    }
                }
                else if (curStatus == 6) { //识别上课时间
                    val matcher = PAT_C1_WEEKBIG.matcher(line)
                    if (matcher.find()) {
                        val big = matcher.group(1)!!.toInt()
                        val day = matcher.group(2)!!.toInt()
                        time.timeInCourseSchedule!!.dayOfWeek = DayOfWeek.of(day)
                        time.timeInCourseSchedule!!.startBig = big
                        if (time.type == CalendarTimeType.SINGLE_COURSE) {
                            time.timeInCourseSchedule!!.date =
                                term.datesInAWeek(time.repeatWeeks.single())[day - 1]
                        }
                        break
                    }
                }
                else if (curStatus == 22) { //二级选课识别上课时间
                    val matcher = PAT_C1_WEEKBIG.matcher(line)
                    if (matcher.find()) {
                        val big = matcher.group(1)!!.toInt()
                        val day = matcher.group(2)!!.toInt()
                        dealWithC2Class(detStr!!, item, time, day, big, term)
                        break
                    }
                }
            }
            item.times.add(time)
            foundCourseDict[title] = item
        }

        return postProcess(foundCourseDict.map { (_, v) -> v }.toMutableList(), term)
    }

    private fun getCreditCount(item: CalendarItemData): Int? {
        return item.detail[CalendarItemLegalDetailKey.COURSEID]?.run {
            substring(
                length - 1,
                length
            )
        }
            ?.toInt()
    }

    private fun tryTurnTimeIntoCourse(time: CalendarTimeData, term: SchoolTerm): CalendarTimeData {
        if (time.timeInHour == null || time.timeInCourseSchedule != null) return time
        val timeInHour = time.timeInHour!!
        var startMatched = false
        var endMatched = false
        outer@ for (big in term.timeRule.bigs) {
            for (small in big.smalls) {
                if (small.startTime == timeInHour.startTime) startMatched = true
                if (small.endTime == timeInHour.endTime) endMatched = true
                if (startMatched && endMatched) break@outer
            }
        }
        if (startMatched && endMatched) {
            time.timeInCourseSchedule = timeInHour.toTimeInCourseSchedule()
            time.timeInHour = null
            time.type =
                if (time.type == CalendarTimeType.REPEAT_HOUR) CalendarTimeType.REPEAT_COURSE else CalendarTimeType.SINGLE_COURSE
        }
        return time
    }

    private fun postProcess(
        itemList: MutableList<CalendarItemDataWithTimes>,
        term: SchoolTerm
    ): MutableList<CalendarItemDataWithTimes> {
        for (item in itemList) {
            val res = mutableListOf<CalendarTimeData>()
            // 主时间段数据、横跨大节
            val toArrange = mutableListOf<Pair<CalendarTimeData, List<Int>>>()
            for (dayOfWeek in DayOfWeek.values()) {
                val list =
                    item.times.filter { it.timeInCourseSchedule?.dayOfWeek == dayOfWeek || it.timeInHour?.dayOfWeek == dayOfWeek }
                if (list.isEmpty()) continue
                val timeList =
                    list.filter { it.type == CalendarTimeType.SINGLE_HOUR || it.type == CalendarTimeType.REPEAT_HOUR }
                val courseList = (list - timeList).toMutableList()

                if (timeList.isNotEmpty()) {
                    //清除重复的时间段类型
                    val uniqueTimeList = timeList.toSet()
                    for (time in uniqueTimeList) {
                        //看看是否应该转为course格式，然后存入res
                        res.add(tryTurnTimeIntoCourse(time, term))
                    }
                }

                //处理课程类型
                while (courseList.size > 0) {
                    val mainCourse = courseList[0]
                    val sameCourseBigs = courseList.filter {
                        it.name == mainCourse.name && it.type == mainCourse.type && it.place == mainCourse.place &&
                                it.repeatWeeks == mainCourse.repeatWeeks
                    }

                    val otherMainCourses = mutableListOf(mainCourse)
                    val crossBigList = mutableListOf(mainCourse.timeInCourseSchedule!!.startBig)

                    var here = mainCourse.timeInCourseSchedule!!.startBig - 1
                    while (true) {
                        val cc = sameCourseBigs.find { it.timeInCourseSchedule!!.startBig == here }
                            ?: break
                        otherMainCourses.add(cc)
                        crossBigList.add(here)
                        here--
                    }

                    here = mainCourse.timeInCourseSchedule!!.startBig + 1
                    while (true) {
                        val cc = sameCourseBigs.find { it.timeInCourseSchedule!!.startBig == here }
                            ?: break
                        otherMainCourses.add(cc)
                        crossBigList.add(here)
                        here++
                    }

                    courseList -= otherMainCourses
                    toArrange.add(Pair(mainCourse, crossBigList.apply { sort() }))
                }
            }

            if (toArrange.isNotEmpty()) {
                val credit = getCreditCount(item)
                var normalDealed = false
                val autumnspring = listOf(SchoolTermType.AUTUMN, SchoolTermType.SPRING)
                if (credit != null && term.type in autumnspring && toArrange.size <= 2) {
                    // 获取得到学分，是正常课；不是夏季学期；一周至多上两次，才适用学分处理逻辑。
                    // 要根据平均周学时等因素判断。
                    val smallPerWeek =
                        (credit * 16f / toArrange[0].first.repeatWeeks.size).roundToInt()

                    if (toArrange.size == 1) {
                        val finalTime = toArrange.single().first
                        finalTime.timeInCourseSchedule!!.startBig = toArrange.single().second[0]
                        finalTime.timeInCourseSchedule!!.startOffsetSmall = 0f
                        finalTime.timeInCourseSchedule!!.lengthSmall = smallPerWeek.toFloat()
                        res.add(finalTime)
                        normalDealed = true
                    }
                    else if (toArrange.size == 2) {
                        val ttsms = mutableListOf<Int>() //每个待分配时间段、可用的最大小节数
                        for (one in toArrange) {
                            one.first.timeInCourseSchedule!!.startBig = one.second[0]
                            one.first.timeInCourseSchedule!!.startOffsetSmall = 0f
                            ttsms.add(one.second.fold(0) { acc, v ->
                                acc + term.timeRule.getBigByNumber(v).smallsCount
                            })
                        }
                        val arrangeSmallResult = when (smallPerWeek) {
                            3 -> if (ttsms[0] == 2 && ttsms[1] == 2) listOf(2, 2) else null
                            4 -> listOf(2, 2)
                            5 -> if (ttsms[0] < 3) listOf(2, 3) else listOf(3, 2)
                            6 -> {
                                if (ttsms[0] < 3) listOf(2, 4)
                                else if (ttsms[1] < 3) listOf(4, 2)
                                else listOf(3, 3)
                            }
                            7 -> {
                                if (ttsms[0] >= 3 && ttsms[0] < ttsms[1]) listOf(3, 4)
                                else if (ttsms[1] >= 3 && ttsms[0] > ttsms[1]) listOf(4, 3)
                                else null
                            }
                            8 -> if (ttsms[0] >= 4 && ttsms[1] >= 4) listOf(4, 4) else null
                            else -> null
                        }

                        if (arrangeSmallResult != null) {
                            normalDealed = true
                            for (i in 0..1) {
                                toArrange[i].first.timeInCourseSchedule!!.lengthSmall =
                                    arrangeSmallResult[i].toFloat()
                                res.add(toArrange[i].first)
                            }
                        }
                    }
                }
                if (!normalDealed) {
                    //获取不到学分，很可能是实验课，一连一上午或一下午的那种。
                    //夏季学期，也大多数是实验课、一连上一上午或一下午的那种。
                    //上述两种情况，就直接让lengthSmall占据所有大节即可。
                    //而对于一周上课次数大于等于三次的情况，十分罕见、也不知道怎么做学分分配，因此也不处理了。
                    //总之凡是无法完成按学分分配学时的情况，则全部填充lengthSmall为占据所有大节。
                    for ((finalTime, crossList) in toArrange) {
                        finalTime.timeInCourseSchedule!!.startBig = crossList[0]
                        finalTime.timeInCourseSchedule!!.startOffsetSmall = 0f
                        //小节数，是横跨的所有大节的总小节数之和
                        finalTime.timeInCourseSchedule!!.lengthSmall =
                            crossList.fold(0f) { acc, v -> acc + term.timeRule.getBigByNumber(v).smallsCount }
                        res.add(finalTime)
                    }
                }
            }

            item.times = res
            item.detail[CalendarItemLegalDetailKey.FROM_WEB] = XK_WEB_KEYWORD
        }

        return itemList
    }

    @Suppress("UNUSED_PARAMETER")
    private fun dealWithC2Class(
        detStr: String,
        item: CalendarItemData,
        time: CalendarTimeData,
        day: Int,
        big: Int,
        term: SchoolTerm
    ) {
        val detMatcher = PAT_C2_DET.matcher(detStr)
        if (detMatcher.find()) {
            val groupName = detMatcher.group(1)!!
            val detList = detMatcher.group(2)!!.split("；")
            time.name = groupName
            if (detList.isNotEmpty()) time.place = detList[0]
            if (detList.size >= 2) time.repeatWeeks = dealWithWeekStr(detList[1])
            var haveTimeStr = false
            if (detList.size >= 3) {
                val timeMatcher = PAT_C2_TIMESTR.matcher(detList[2])
                if (timeMatcher.find()) {
                    haveTimeStr = true
                    val startTime =
                        LocalTime.of(
                            timeMatcher.group(1)!!.toInt(),
                            timeMatcher.group(2)!!.toInt()
                        )
                    val endTime =
                        LocalTime.of(
                            timeMatcher.group(3)!!.toInt(),
                            timeMatcher.group(4)!!.toInt()
                        )

                    val timeInHour =
                        TimeInHour(startTime, endTime, dayOfWeek = DayOfWeek.of(day))
                    time.timeInHour = timeInHour
                    if (time.repeatWeeks.size > 1) {
                        time.type = CalendarTimeType.REPEAT_HOUR
                    }
                    else {
                        time.type = CalendarTimeType.SINGLE_HOUR
                        time.timeInHour!!.date =
                            term.datesInAWeek(time.repeatWeeks.single())[day - 1]
                    }
                }
            }
            if (!haveTimeStr) {
                //按大节处理
                time.timeInCourseSchedule = TimeInCourseSchedule(
                    dayOfWeek = DayOfWeek.of(day),
                    startBig = big,
                    startOffsetSmall = 0f,
                    lengthSmall = term.timeRule.getBigByNumber(big).smallsCount.toFloat()
                )
                if (time.repeatWeeks.size > 1) {
                    time.type = CalendarTimeType.REPEAT_COURSE
                }
                else {
                    time.type = CalendarTimeType.SINGLE_COURSE
                    time.timeInCourseSchedule!!.date =
                        term.datesInAWeek(time.repeatWeeks.single())[day - 1]
                }
            }
        }
        else {
            //无法识别。那么当成一级课处理一下吧。
            time.timeInCourseSchedule = TimeInCourseSchedule(
                dayOfWeek = DayOfWeek.of(day),
                startBig = big,
                startOffsetSmall = 0f,
                lengthSmall = term.timeRule.getBigByNumber(big).smallsCount.toFloat()
            )
            time.repeatWeeks = (1..16).toMutableList()
            time.type = CalendarTimeType.REPEAT_COURSE
        }
    }


    private fun dealWithWeekStr(str: String): MutableList<Int> {
        val res: MutableList<Int>
        if (str == "全周") {
            res = (1..16).toMutableList()
        }
        else if (str == "前八周") {
            res = (1..8).toMutableList()
        }
        else if (str == "后八周") {
            res = (9..16).toMutableList()
        }
        else {
            //先尝试第a-b周的形式
            val matcher = PAT_C1_WEEKDES.matcher(str)
            if (matcher.find()) {
                val weekStart = matcher.group(1)!!.toInt()
                val weekEnd = matcher.group(2)!!.toInt()
                res = (weekStart..weekEnd).toMutableList()
            }
            else {
                // 尝试周枚举 第a,b,c,d周的形式，提取出所有数字
                val hereRes = mutableListOf<Int>()
                val enumWeekMatcher = PAT_NUMBER.matcher(str)
                while (enumWeekMatcher.find()) {
                    val weekNumber = enumWeekMatcher.group().toInt()
                    if (weekNumber in 1..16) hereRes.add(weekNumber)
                }
                if (hereRes.size > 0) res = hereRes
                else {
                    // 无法解析（不含任何数字、也不是已知的文字），那么先默认按全周处理。
                    res = (1..16).toMutableList()
                }
            }
        }

        return res.filter { it in 1..16 }.sorted().toMutableList()
    }
}
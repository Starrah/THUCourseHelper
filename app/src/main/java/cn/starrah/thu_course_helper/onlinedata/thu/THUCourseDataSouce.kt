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
import cn.starrah.thu_course_helper.data.SPRING2019TERMJSON
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarItemData
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarItemDataWithTimes
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeData
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarItemLegalDetailKey
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarItemType
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarTimeType
import cn.starrah.thu_course_helper.data.declares.school.SchoolTerm
import cn.starrah.thu_course_helper.data.declares.school.SchoolTermType
import cn.starrah.thu_course_helper.data.declares.time.TimeInCourseSchedule
import cn.starrah.thu_course_helper.data.declares.time.TimeInHour
import cn.starrah.thu_course_helper.data.utils.*
import cn.starrah.thu_course_helper.fragment.CaptchaDialog
import cn.starrah.thu_course_helper.onlinedata.AbstractCourseDataSource
import com.alibaba.fastjson.JSON
import com.github.kittinunf.fuel.coroutines.awaitByteArray
import com.github.kittinunf.fuel.coroutines.awaitString
import com.github.kittinunf.fuel.coroutines.awaitStringResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import java.nio.charset.Charset
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.math.roundToInt

class THUCourseDataSouce : AbstractCourseDataSource() {
    private val CAPTCHA_PATTERN = Pattern.compile("<img id=\"captcha\" src=\"(.*?)\".*?>")
    private val XK_BASE_URL = "http://zhjwxk.cic.tsinghua.edu.cn"
//    private val ACEGI_PATTERN =
//        Pattern.compile("src=\"(.*?/j_acegi_login\\.do\\?url=/jxmh\\.do&amp;m=bks_jxrl&amp;ticket=[a-zA-Z0-9]+)\"")
    override val schoolName: String = "清华大学"

    override suspend fun login(
        activity: FragmentActivity,
        username: String,
        password: String,
        extra: Map<String, Any>?
    ) {
        val resStr = CookiedFuel.get("$XK_BASE_URL/xklogin.do")
            .awaitString(Charset.forName("GBK"))
        println(COOKIEJAR.cookieMap)
        val captchaUrl = XK_BASE_URL + CAPTCHA_PATTERN.matcher(resStr).run { find(); group(1) }
        val captchaParam = captchaUrl.split("=")[1]
        val jpgBytes = CookiedFuel.get(captchaUrl).awaitByteArray()
        val bitmap = BitmapFactory.decodeByteArray(jpgBytes, 0, jpgBytes.size)
        val captchaInput = suspendCancellableCoroutine<String?> { continuation ->
            CaptchaDialog(activity, bitmap) {
                continuation.resume(it)
            }.show()
        } ?: throw DataInvalidException("用户未输入验证码")
        val (_, loginResp, _) = CookiedFuel.post(
            "https://zhjwxk.cic.tsinghua.edu.cn/j_acegi_formlogin_xsxk.do", listOf(
                "j_username" to username,
                "j_password" to password,
                "captchaflag" to captchaParam,
                "_login_image_" to captchaInput
            )
        ).awaitStringResponse(Charset.forName("GBK"))
        if ("login_error" in loginResp.url.toString()) {
            if ("code_error" in loginResp.url.toString()) throw DataInvalidException("验证码错误！")
        }
    }

    override suspend fun loadAllCourse(term: SchoolTerm): List<CalendarItemDataWithTimes> {
        assertDataSystem(schoolName == term.schoolName, "学校名称错误！")
        val termStrInXK = when (term.type) {
            SchoolTermType.AUTUMN -> "${term.beginYear}-${term.beginYear + 1}-1"
            SchoolTermType.SPRING -> "${term.beginYear}-${term.beginYear + 1}-2"
            SchoolTermType.SUMMER -> "${term.beginYear}-${term.beginYear + 1}-3"
            else                  -> return listOf() // 不可获取课表（如寒假）
        }
        val rawStr =
            CookiedFuel.get("$XK_BASE_URL/syxk.vsyxkKcapb.do?m=ztkbSearch&p_xnxq=$termStrInXK&pathContent=整体课表")
                .awaitString(Charset.forName("GBK"))

        return parseRawZTKB(rawStr, term)
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
                            item_id = item.id, name = "上课",
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
        return item.detail[CalendarItemLegalDetailKey.COURSEID]?.run { substring(length - 1, length) }
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
            time.type = if (time.type == CalendarTimeType.REPEAT_HOUR) CalendarTimeType.REPEAT_COURSE else CalendarTimeType.SINGLE_COURSE
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
                                else if (ttsms[1] >= 3 && ttsms[0] > ttsms[1]) listOf(4,3)
                                else null
                            }
                            8 -> if (ttsms[0] >= 4 && ttsms[1] >= 4) listOf(4, 4) else null
                            else -> null
                        }

                        if (arrangeSmallResult != null) {
                            normalDealed = true
                            for (i in 0..1) {
                                toArrange[i].first.timeInCourseSchedule!!.lengthSmall = arrangeSmallResult[i].toFloat()
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
            time.name = "上课"
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

    override suspend fun getCookie(
        username: String,
        password: String
    ): String {
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
        if (resp.url.toString().run { substring(length - 1..length) } != "1")
            throw DataInvalidException("登录失败，可能是用户名或密码错误")
        return COOKIEJAR.cookieMap["info.tsinghua.edu.cn"]!!["UPORTALINFONEW"]!!.first
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
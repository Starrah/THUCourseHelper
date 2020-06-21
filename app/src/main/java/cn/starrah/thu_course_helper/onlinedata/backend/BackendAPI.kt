package cn.starrah.thu_course_helper.onlinedata.backend

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarItemDataWithTimes
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarRemindType
import cn.starrah.thu_course_helper.data.declares.school.SchoolTerm
import cn.starrah.thu_course_helper.data.utils.CookiedFuel
import cn.starrah.thu_course_helper.data.utils.DataInvalidException
import cn.starrah.thu_course_helper.remind.setRemindTimerService
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.coroutines.awaitString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.PrintWriter
import java.io.StringWriter

data class TermAPIResp(
    val termData: SchoolTerm,
    val termList: List<TermDescription>? = null,
    val currentTermId: String? = null
)

data class TermDescription(val id: String, val name: String)

suspend fun BackendAPITermData(id: String? = null): TermAPIResp {
    val s = CookiedFuel.get("$BACKEND_SITE/term", id?.let { listOf("id" to id) }).awaitString()
    return JSON.parseObject(s, TermAPIResp::class.java)
}

data class CheckVersionAPIResp(val versionName: String, val url: String)

suspend fun BackendAPICheckVersion(): CheckVersionAPIResp {
    val s = CookiedFuel.get("$BACKEND_SITE/version_check").awaitString()
    return JSON.parseObject(s, CheckVersionAPIResp::class.java)
}

val UPLOAD_PREFERENCE_KEY_LIST = listOf(
    "sync_hmex",
    "course_show_type",
    "course_show_days",
    "time_show_days",
    "stay_notice"
)

suspend fun BackendAPIUploadMyData(context: Context, authentication: Any?) {
    val sp = PreferenceManager.getDefaultSharedPreferences(context)

    withContext(Dispatchers.IO) {
        val calendarData = CREP.DAO.findAllItems()

        val preferenceMap = mutableMapOf<String, Any?>()
        val spMap = sp.all
        for (key in UPLOAD_PREFERENCE_KEY_LIST) {
            if (key in spMap) preferenceMap[key] = spMap[key]
        }

        CookiedFuel.post("$BACKEND_SITE/uploadUserData")
            .jsonBody(
                JSON.toJSONString(
                    mapOf(
                        "authentication" to authentication,
                        "termId" to CREP.term.termId,
                        "calendarData" to calendarData,
                        "preference" to preferenceMap
                    )
                )
            ).awaitString()
    }
}

suspend fun BackendAPIDownloadMyData(context: Context, authentication: Any?) {
    val sp = PreferenceManager.getDefaultSharedPreferences(context)

    withContext(Dispatchers.IO) {
        val respStr = CookiedFuel.post("$BACKEND_SITE/downloadUserData")
            .jsonBody(
                JSON.toJSONString(
                    mapOf(
                        "authentication" to authentication,
                        "termId" to CREP.term.termId
                    )
                )
            ).awaitString()

        data class _resp(
            val calendarData: List<CalendarItemDataWithTimes>?,
            val preference: MutableMap<String, Any?>?
        )

        val respObj = JSON.parseObject(respStr, _resp::class.java)

        if (respObj.preference != null) {
            sp.edit {
                for ((key, value) in respObj.preference) {
                    if (key in UPLOAD_PREFERENCE_KEY_LIST) {
                        @Suppress("UNCHECKED_CAST")
                        when (value) {
                            is String  -> putString(key, value)
                            is Int     -> putInt(key, value)
                            is Boolean -> putBoolean(key, value)
                            is Long    -> putLong(key, value)
                            is Float   -> putFloat(key, value)
                            is Set<*>  -> putStringSet(key, value as Set<String>)
                            null       -> remove(key)
                        }
                    }
                }
            }
        }

        if (respObj.calendarData == null) throw DataInvalidException("服务器上不存在备份的数据！")
        else if (respObj.calendarData.isEmpty()) return@withContext

        CREP.DAO.findAllTimes().forEach {
            it.remindData.type = CalendarRemindType.NONE
            setRemindTimerService(context, it, shouldCancel = true)
        }
        CREP.DAO.dropAllTables()
        for (one in respObj.calendarData) {
            CREP.DAO.updateItemAndTimes(one, one.times)
            one.times.forEach {
                setRemindTimerService(context, it, shouldCancel = true)
            }
        }
    }
}

suspend fun BackendAPISubmitLog(message: String) {
    CookiedFuel.post("$BACKEND_SITE/log").header(Headers.CONTENT_TYPE, "text/plain")
        .body(message).awaitString()
}

suspend fun BackendAPISubmitLog(e: Throwable) =
    BackendAPISubmitLog(StringWriter().also { e.printStackTrace(PrintWriter(it)) }.toString())

suspend fun BackendAPIFeedback(message: String, contact: String?) {
    CookiedFuel.post("$BACKEND_SITE/feedback").header(Headers.CONTENT_TYPE, "text/plain")
        .body("message${if (contact != null) ", 联系方式：${contact}" else ""}").awaitString()
}

/**
 * 获取信息页面的资源列表。
 *
 * 返回的是[List]<[JSONObject]>，其中每个[JSONObject]，要么形如：
 * {
"name": "选课时间安排",
"url": "https://xxxxx"
}
 *
 * 要么形如：
 * {
"name": "教室情况",
"children": \[
{
"name": "六教",
"url": "https://xxxxx"
},
{
"name": "五教",
"url": "https://xxxxx"
},
{
"name": "四教",
"url": "https://xxxxx"
}
]
}
 *
 * 分别表示有二级选项和无二级选项的情况。
 */
suspend fun BackendAPIInfo(): List<JSONObject> {
    val s = CookiedFuel.get("$BACKEND_SITE/infoList").awaitString()
    return JSON.parseArray(s).map { it as JSONObject }
}
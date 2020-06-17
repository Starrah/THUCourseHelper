package cn.starrah.thu_course_helper.data.utils

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import java.util.regex.Pattern
import com.github.kittinunf.fuel.core.interceptors.redirectResponseInterceptor

val CookiedFuel = FuelManager().apply {
    addRequestInterceptor { next -> {req ->
        next(req.enableCookie(DEFAULT_COOKIEJAR))
    } }
    removeAllResponseInterceptors()
    addResponseInterceptor { next -> {req, resp ->
        next(req, resp.saveCookie(DEFAULT_COOKIEJAR))
    } }
    addResponseInterceptor(redirectResponseInterceptor(this))

    // 设置超时
    timeoutInMillisecond = 5000
    FuelManager.instance.timeoutInMillisecond = 5000
}

private val SAVECOOKIE_KV_PATTERN = Pattern.compile("^(.+)=(.*)$")
private val SAVECOOKIE_PATH_PATTERN = Pattern.compile("^Path=(.*)$", Pattern.CASE_INSENSITIVE)

fun Response.saveCookie(cookieJar: CookieJar): Response {
    val domain = this.url.host

    val SCs = this[Headers.SET_COOKIE]
    for (SC in SCs) {
        val items = SC.split("; ")
        val main = items[0]
        val remainItems = items.drop(1)

        val mainMatcher = SAVECOOKIE_KV_PATTERN.matcher(main)
        if (!mainMatcher.matches()) continue
        val name = mainMatcher.group(1)!!
        val cookieValue = mainMatcher.group(2)!!

        var path: String? = null
        for (ss in remainItems) {
            val subMatcher = SAVECOOKIE_PATH_PATTERN.matcher(ss)
            if (subMatcher.matches()) path = subMatcher.group(1)
        }

        if (domain !in cookieJar.cookieMap) cookieJar.cookieMap[domain] = mutableMapOf()
        cookieJar.cookieMap[domain]!![name] = Pair(cookieValue, path)
    }
    return this
}

fun Triple<*, Response, *>.saveCookie(cookieJar: CookieJar): Triple<*, Response, *> {
    second.saveCookie(cookieJar)
    return this
}

fun Request.enableCookie(cookieJar: CookieJar): Request {
    val domain = this.url.host
    val cookies = cookieJar.cookieMap[domain]
    val cookieString = cookies?.filter {
        it.value.second?.let { request.url.path?.ifEmpty { "/" }?.startsWith(it) } != false
    } ?.map { "${it.key}=${it.value.first}" } ?.joinToString("; ")
    if (cookieString != null) {
        request[Headers.COOKIE] = cookieString
    }
    return this
}

class CookieJar {
    /**
     * cookie的储存位置。
     * 结构是{ 域名: [{ cookie key: [ cookie value, path ] }] }
     * （path可能为空）
     */
    val cookieMap: MutableMap<String, MutableMap<String, Pair<String, String?>>> = mutableMapOf()
}

val DEFAULT_COOKIEJAR = CookieJar()


package cn.starrah.thu_course_helper.data.utils

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import java.util.regex.Pattern
import com.github.kittinunf.fuel.core.interceptors.redirectResponseInterceptor

val CookiedFuel = FuelManager().apply {
    addRequestInterceptor { next -> {req ->
        next(req.enableCookie())
    } }
    removeAllResponseInterceptors()
    addResponseInterceptor { next -> {req, resp ->
        next(req, resp.saveCookie())
    } }
    addResponseInterceptor(redirectResponseInterceptor(this))
}

private val SAVECOOKIE_KV_PATTERN = Pattern.compile("^(.+)=(.*)$")
private val SAVECOOKIE_PATH_PATTERN = Pattern.compile("^Path=(.*)$", Pattern.CASE_INSENSITIVE)

private fun Response.saveCookie(): Response {
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

        if (domain !in COOKIEJAR.cookieMap) COOKIEJAR.cookieMap[domain] = mutableMapOf()
        COOKIEJAR.cookieMap[domain]!![name] = Pair(cookieValue, path)
    }
    return this
}

private fun Triple<*, Response, *>.saveCookie(): Triple<*, Response, *> {
    second.saveCookie()
    return this
}

private fun Request.enableCookie(): Request {
    val domain = this.url.host
    val cookies = COOKIEJAR.cookieMap[domain]
    val cookieString = cookies?.filter {
        it.value.second?.let { request.url.path?.ifEmpty { "/" }?.startsWith(it) } != false
    } ?.map { "${it.key}=${it.value.first}" } ?.joinToString("; ")
    if (cookieString != null) {
        request[Headers.COOKIE] = cookieString
    }
    return this
}

object COOKIEJAR{
    /**
     * cookie的储存位置。
     * 结构是{ 域名: [{ cookie key: [ cookie value, path ] }] }
     * （path可能为空）
     */
    val cookieMap: MutableMap<String, MutableMap<String, Pair<String, String?>>> = mutableMapOf()

}
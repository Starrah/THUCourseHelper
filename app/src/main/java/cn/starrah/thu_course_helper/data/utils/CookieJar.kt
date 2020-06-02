package cn.starrah.thu_course_helper.data.utils

import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import java.util.regex.Pattern


private val SAVECOOKIE_KV_PATTERN = Pattern.compile("^(.+)=(.*)$")
private val SAVECOOKIE_PATH_PATTERN = Pattern.compile("^Path=(.*)$")

fun Response.saveCookie(): Response {
    val domain = this.url.host

    val SCs = this[Headers.SET_COOKIE]
    for (SC in SCs) {
        val items = SC.split("; ")
        val main = items[0]
        val remainItems = items.drop(1)

        val mainMatcher = SAVECOOKIE_KV_PATTERN.matcher(main)
        if (mainMatcher.groupCount() != 2) continue
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

fun Triple<*, Response, *>.saveCookie(): Triple<*, Response, *> {
    second.saveCookie()
    return this
}

fun Request.enableCookie(): Request {
    val domain = this.url.host
    val cookies = COOKIEJAR.cookieMap[domain]
    if (cookies != null) {
        for (cookie in cookies){
            if (cookie.value.second?.let { request.url.path.startsWith(it) } == false) continue
            request[cookie.key] = cookie.value.first
        }
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
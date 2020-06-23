package cn.starrah.thu_course_helper.data.utils

import com.alibaba.fastjson.JSON
import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.core.deserializers.StringDeserializer
import com.github.kittinunf.fuel.core.interceptors.redirectResponseInterceptor
import java.util.regex.Pattern

// 用于后端处理返回json中的errMsg字段，以保证错误消息能够被作为Exception的message。
private val backendErrMsgHandler: FoldableResponseInterceptor = { next ->
    { req, resp ->
        if (!req.executionOptions.responseValidator(resp) && "application/json" in resp.headers[Headers.CONTENT_TYPE]) {
            val jsonObj =
                runCatching { JSON.parseObject(StringDeserializer().deserialize(resp)) }.getOrNull()
            if (jsonObj != null && !(jsonObj["errMsg"] as? String).isNullOrEmpty()) {
                throw DataInvalidException(jsonObj["errMsg"] as String)
            }
        }
        next(req, resp)
    }
}

val CookiedFuel = FuelManager().apply {
    addRequestInterceptor { next ->
        { req ->
            next(req.enableCookie(DEFAULT_COOKIEJAR))
        }
    }

    // 为了确保重定向期间的cookie也能被获取，各个Interceptor的顺序就十分重要。
    // 必须保证获取cookie的Interceptor是位于重定向的Interceptor之前的。
    removeAllResponseInterceptors()

    // 获取cookie，存到默认DEFAULT_COOKIEJAR
    addResponseInterceptor { next ->
        { req, resp ->
            next(req, resp.saveCookie(DEFAULT_COOKIEJAR))
        }
    }

    // 处理重定向
    addResponseInterceptor(redirectResponseInterceptor(this))

    // 用于后端处理返回json中的errMsg字段，以保证错误消息能够被作为Exception的message。
    addResponseInterceptor(backendErrMsgHandler)

    // 设置超时
    timeoutInMillisecond = 5000

    // 对于Fuel对象，也需要设置一下超时。在这里一并处理了。
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
    }?.map { "${it.key}=${it.value.first}" }?.joinToString("; ")
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


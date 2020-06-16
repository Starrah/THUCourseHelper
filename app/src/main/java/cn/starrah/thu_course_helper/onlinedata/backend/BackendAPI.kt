package cn.starrah.thu_course_helper.onlinedata.backend

import cn.starrah.thu_course_helper.data.declares.school.SchoolTerm
import com.alibaba.fastjson.JSON
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString

data class TermAPIResp(val termData: SchoolTerm, val termList: List<TermDescription>? = null, val currentTermId: String? = null)
data class TermDescription(val id: String, val name: String)
suspend fun BackendAPITermData(id: String? = null): TermAPIResp {
    val s = Fuel.get("$BACKEND_SITE/term", id?.let { listOf("id" to id) }).awaitString()
    return JSON.parseObject(s, TermAPIResp::class.java)
}

data class CheckVersionAPIResp(val versionName: String, val url: String)
suspend fun BackendAPICheckVersion(): CheckVersionAPIResp {
    val s = Fuel.get("$BACKEND_SITE/version_check").awaitString()
    return JSON.parseObject(s, CheckVersionAPIResp::class.java)
}
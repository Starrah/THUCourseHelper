package cn.starrah.thu_course_helper.data.declares.school


data class TermAPIResp(val termData: SchoolTerm, val termList: List<TermDescription>? = null, val currentTermId: String? = null)
data class TermDescription(val id: String, val name: String)
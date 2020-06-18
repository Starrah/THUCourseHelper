package cn.starrah.thu_course_helper.data.utils

import com.alibaba.fastjson.annotation.JSONField
import java.lang.Exception

interface Verifiable {
    fun assertValid()

    fun assertValidResetMsg(transform: (String) -> String) {
        try {
            assertValid()
        } catch (e: Exception) {
            throw DataInvalidException(e.message?.let(transform), e)
        }
    }

    @JSONField(serialize = false)
    fun isValid(): Boolean {
        return try {
            assertValid()
            true
        } catch (e: Exception) {
            false
        }
    }
}
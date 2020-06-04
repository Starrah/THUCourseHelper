package cn.starrah.thu_course_helper.picker

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import android.util.TypedValue
import androidx.annotation.NonNull

/**
 * Created by ramiz on 2/1/18.
 */
object CommonUtils {
    @ColorRes
    fun resolveResource(@NonNull context: Context, resId: Int): Int {
        val outValue = TypedValue()
        context.theme.resolveAttribute(resId, outValue, true)
        return outValue.resourceId
    }
}
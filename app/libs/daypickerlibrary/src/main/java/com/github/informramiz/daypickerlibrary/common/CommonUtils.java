package com.github.informramiz.daypickerlibrary.common;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.util.TypedValue;

/**
 * Created by ramiz on 2/1/18.
 */

public class CommonUtils {
    public static @ColorRes int resolveResource(@NonNull Context context, int resId) {
        final TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(resId, outValue, true);
        return outValue.resourceId;
    }
}

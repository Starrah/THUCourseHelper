package com.github.informramiz.daypickerlibrary.views;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.content.res.AppCompatResources;

import com.github.informramiz.daypickerlibrary.R;
import com.github.informramiz.daypickerlibrary.common.CommonUtils;

/**
 * A custom implementation of StateListDrawable to properly apply
 * theme colors on Android version below Lollipop
 * Created by ramiz on 2/2/18.
 */
public class CustomStateListDrawable extends StateListDrawable {
    private Context context;

    public CustomStateListDrawable(@NonNull Context context) {
        this.context = context;
        GradientDrawable normalGradientDrawable = getNormalGradientDrawable();
        GradientDrawable selectedGradientDrawable = getSelectedGradientDrawable();
        GradientDrawable pressedGradientDrawable = getPressedGradientDrawable();

        addState(new int[]{android.R.attr.state_selected}, selectedGradientDrawable);
        addState(new int[]{android.R.attr.state_pressed}, pressedGradientDrawable);
        addState(new int[]
                        {-android.R.attr.state_selected,
                        -android.R.attr.state_pressed},
                normalGradientDrawable);
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    private Context getContext() {
        return context;
    }

    private GradientDrawable getNormalGradientDrawable() {
        GradientDrawable gradientDrawable = (GradientDrawable) AppCompatResources.getDrawable(getContext(), R.drawable.shape_circle_holo);
        gradientDrawable.setStroke(getContext().getResources().getDimensionPixelSize(R.dimen.circle_stroke_width), getColorAccent());
        return gradientDrawable;
    }

    private GradientDrawable getSelectedGradientDrawable() {
        GradientDrawable gradientDrawable = (GradientDrawable) AppCompatResources.getDrawable(getContext(), R.drawable.shape_circle_filled);
        gradientDrawable.setColor(getColorAccent());
        return gradientDrawable;
    }

    private GradientDrawable getPressedGradientDrawable() {
        GradientDrawable gradientDrawable = (GradientDrawable) AppCompatResources.getDrawable(getContext(), R.drawable.shape_circle_filled_light);
        gradientDrawable.setColor(getColorHighlight());
        return gradientDrawable;
    }

    private @ColorInt int getColorAccent() {
        return ContextCompat.getColor(getContext(), CommonUtils.resolveResource(getContext(), R.attr.colorAccent));
    }

    private @ColorInt int getColorHighlight() {
        return ContextCompat.getColor(getContext(), CommonUtils.resolveResource(getContext(), R.attr.colorControlHighlight));
    }
}

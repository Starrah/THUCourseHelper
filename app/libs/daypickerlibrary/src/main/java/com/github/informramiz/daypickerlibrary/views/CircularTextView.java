package com.github.informramiz.daypickerlibrary.views;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import com.github.informramiz.daypickerlibrary.R;

/**
 * Created by ramiz on 1/31/18.
 */

public class CircularTextView extends AppCompatTextView implements View.OnClickListener {
    @Nullable
    private OnClickListener onClickListener = null;
    private boolean isAutoSelectEnabled = true;

    public CircularTextView(Context context) {
        super(context);
        init();
    }

    public CircularTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircularTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        setGravity(Gravity.CENTER);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //We can't access theme attributes in @DrawableRes below Android Lollipop
            //so to properly support theme colors let's use a custom StateListDrawable
            setBackgroundDrawable(new CustomStateListDrawable(getContext()));
        } else {
            setBackgroundDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.circular_text_view_background_selector));
        }

        setTextColor(AppCompatResources.getColorStateList(getContext(), R.color.circular_text_view_text_color_selector));
        super.setOnClickListener(this);
    }

    /**
     * Returns true if auto-select background change is enabled
     * @return true of auto-select background change is enabled, false otherwise
     */
    public boolean isAutoSelectEnabled() {
        return isAutoSelectEnabled;
    }

    /**
     * If you want to disable auto-select background change
     * of this view then you can set pass false, true otherwise
     * @param autoSelectEnabled false if you want to disable auto background change, true otherwise
     */
    public void setAutoSelectEnabled(boolean autoSelectEnabled) {
        isAutoSelectEnabled = autoSelectEnabled;
    }

    @Override
    public void onClick(View v) {
        if (isAutoSelectEnabled()) {
            handleAutoSelectEvent();
        }

        if (onClickListener != null) {
            onClickListener.onClick(v);
        }
    }

    private void handleAutoSelectEvent() {
        setSelected(!isSelected());
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}

package cn.starrah.thu_course_helper.picker

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.annotation.Nullable
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import cn.starrah.thu_course_helper.R

/**
 * Created by ramiz on 1/31/18.
 */
class CircularTextView : AppCompatTextView, View.OnClickListener {
    @Nullable
    private var onClickListener: OnClickListener? = null
    /**
     * Returns true if auto-select background change is enabled
     * @return true of auto-select background change is enabled, false otherwise
     */
    /**
     * If you want to disable auto-select background change
     * of this view then you can set pass false, true otherwise
     * @param autoSelectEnabled false if you want to disable auto background change, true otherwise
     */
    var isAutoSelectEnabled = true

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(
        context: Context?,
        @Nullable attrs: AttributeSet?
    ) : super(context!!, attrs) {
        init()
    }

    constructor(
        context: Context?,
        @Nullable attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context!!, attrs, defStyleAttr) {
        init()
    }

    @Suppress("DEPRECATION")
    fun init() {
        setGravity(Gravity.CENTER)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //We can't access theme attributes in @DrawableRes below Android Lollipop
            //so to properly support theme colors let's use a custom StateListDrawable
            setBackgroundDrawable(CustomStateListDrawable(getContext()))
        }
        else {
            setBackgroundDrawable(
                AppCompatResources.getDrawable(
                    getContext(),
                    R.drawable.circular_text_view_background_selector
                )
            )
        }
        setTextColor(
            AppCompatResources.getColorStateList(
                getContext(),
                R.color.circular_text_view_text_color_selector
            )
        )
        super.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (isAutoSelectEnabled) {
            handleAutoSelectEvent()
        }
        onClickListener?.onClick(v)
    }

    private fun handleAutoSelectEvent() {
        setSelected(!isSelected())
    }

    override fun setOnClickListener(@Nullable onClickListener: OnClickListener?) {
        this.onClickListener = onClickListener
    }
}
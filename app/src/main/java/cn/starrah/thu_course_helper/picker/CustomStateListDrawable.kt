package cn.starrah.thu_course_helper.picker

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import androidx.annotation.ColorInt
import androidx.annotation.NonNull
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import cn.starrah.thu_course_helper.R

/**
 * A custom implementation of StateListDrawable to properly apply
 * theme colors on Android version below Lollipop
 * Created by ramiz on 2/2/18.
 */
class CustomStateListDrawable(@param:NonNull private val context: Context) :
    StateListDrawable() {
    override fun isStateful(): Boolean {
        return true
    }

    private val normalGradientDrawable: GradientDrawable
        get() {
            val gradientDrawable = AppCompatResources.getDrawable(
                context,
                R.drawable.shape_circle_holo
            ) as GradientDrawable
            gradientDrawable.setStroke(
                context.resources.getDimensionPixelSize(R.dimen.circle_stroke_width),
                colorAccent
            )
            return gradientDrawable
        }

    private val selectedGradientDrawable: GradientDrawable
        get() {
            val gradientDrawable = AppCompatResources.getDrawable(
                context,
                R.drawable.shape_circle_filled
            ) as GradientDrawable
            gradientDrawable.setColor(colorAccent)
            return gradientDrawable
        }

    private val pressedGradientDrawable: GradientDrawable
        get() {
            val gradientDrawable = AppCompatResources.getDrawable(
                context,
                R.drawable.shape_circle_filled_light
            ) as GradientDrawable
            gradientDrawable.setColor(colorHighlight)
            return gradientDrawable
        }

    @get:ColorInt
    private val colorAccent: Int
        get() = ContextCompat.getColor(
            context,
            CommonUtils.resolveResource(context, R.attr.colorAccent)
        )

    @get:ColorInt
    private val colorHighlight: Int
        get() = ContextCompat.getColor(
            context,
            CommonUtils.resolveResource(context, R.attr.colorControlHighlight)
        )

    init {
        val normalGradientDrawable = normalGradientDrawable
        val selectedGradientDrawable = selectedGradientDrawable
        val pressedGradientDrawable = pressedGradientDrawable
        addState(intArrayOf(android.R.attr.state_selected), selectedGradientDrawable)
        addState(intArrayOf(android.R.attr.state_pressed), pressedGradientDrawable)
        addState(
            intArrayOf(
                -android.R.attr.state_selected,
                -android.R.attr.state_pressed
            ),
            normalGradientDrawable
        )
    }
}
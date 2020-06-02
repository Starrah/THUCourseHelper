package cn.starrah.thu_course_helper.picker

import cn.starrah.thu_course_helper.R
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.*

/**
 * Created by ramiz on 1/31/18.
 */
class PickerView : FrameLayout, View.OnClickListener {
    private var normalWeeks = 0
    private var examWeeks = 0

    @Retention(RetentionPolicy.SOURCE)
    annotation class PickerDay

    private val weekViews: ArrayList<CircularTextView?> =
        ArrayList()

    //private CircularTextView[] dayViews = new CircularTextView[TOTAL_DAYS];
    @get:Nullable
    @Nullable
    var onDaysSelectionChangedListener: OnWeeksSelectionChangedListener? = null

    /**
     * Callback to communicate day selection back
     *
     */
    interface OnWeeksSelectionChangedListener {
        /**
         * Callback to be called when day selection is changed
         * @param pickerView View associated with this listener
         * @param selectedWeeks 7-element array(SUNDAY=0, SATURDAY=6) with days that are selected
         * set as true
         */
        fun onWeeksSelectionChange(
            pickerView: PickerView?,
            selectedWeeks: ArrayList<Int?>?
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    constructor(@NonNull context: Context?) : super(context!!) {
        LayoutInflater.from(getContext()).inflate(
            R.layout.layout_picker_view,
            this,
            true
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    constructor(
        @NonNull context: Context?,
        @Nullable attrs: AttributeSet?
    ) : super(context!!, attrs) {
        val typedArray =
            getContext().obtainStyledAttributes(attrs, R.styleable.PickerView)
        LayoutInflater.from(getContext()).inflate(
            R.layout.layout_picker_view,
            this,
            true
        )
    }

    /**
     * 描述：初始化数据存储和显示
     * 参数：当前context，正常周数目，考试周数目，先前选取过的周
     * 返回：无
     */
    fun initContent(
        context: Context?,
        normalWeeks: Int,
        examWeeks: Int,
        selectedWeeks: ArrayList<Int>
    ) {
        //初始化显示和绑定的数据
        val currentLayout =
            findViewById<GridLayout>(R.id.week_select_place)
        this.normalWeeks = normalWeeks
        this.examWeeks = examWeeks
        for (i in 0 until this.normalWeeks + this.examWeeks) {
            val item = CircularTextView(context)
            item.setText("" + (i + 1))
            val layoutParams = LinearLayout.LayoutParams(100, 100)
            layoutParams.setMargins(10, 10, 10, 10)
            item.setLayoutParams(layoutParams)
            item.setClickable(true)
            item.setFocusable(true)
            weekViews.add(item)
            currentLayout.addView(item)
        }

        //给每个view绑定button click
        for (weekView in weekViews) {
            if (weekView != null) {
                weekView.setOnClickListener(this)
            }
        }

        //初始化显示
        setWeeksSelected(selectedWeeks)
    }

    override fun onClick(v: View) {
        if (onDaysSelectionChangedListener != null) {
            onDaysSelectionChangedListener!!.onWeeksSelectionChange(this, selectedWeeks)
        }
    }

    /**
     * 描述：选中某个周
     * 参数：被选中的第几周（从1开始），是否选中
     */
    fun setWeekSelected(@PickerDay week: Int, isSelected: Boolean) {
        weekViews[week - 1]?.setSelected(isSelected)
    }

    /**
     * 描述：设置哪些周被选中，用于初始化
     * 参数：一个数组，代表被选中的周的编号
     * 返回：无
     */
    fun setWeeksSelected(selectedWeeks: ArrayList<Int>) {
        val whether_show =
            BooleanArray(examWeeks + normalWeeks + 1)
        for (weeks in selectedWeeks) {
            whether_show[weeks] = true
        }
        for (i in 1..examWeeks + normalWeeks) {
            setWeekSelected(i, whether_show[i])
        }
    }

    /**
     * 描述：返回选中的weeks
     * 参数：无
     * 返回：一个列表，代表选中的所有周（从1开始）
     */
    @get:NonNull
    val selectedWeeks: ArrayList<Int?>
        get() {
            val selectedWeeks = ArrayList<Int?>()
            for (i in 0 until examWeeks + normalWeeks) {
                if (weekViews[i]?.isSelected()!!) {
                    selectedWeeks.add(i + 1)
                }
            }
            return selectedWeeks
        }

    companion object {
        private val LOG_TAG = PickerView::class.java.simpleName
    }
}
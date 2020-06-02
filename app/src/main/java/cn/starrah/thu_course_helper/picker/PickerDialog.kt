package cn.starrah.thu_course_helper.picker


import cn.starrah.thu_course_helper.R
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import cn.starrah.thu_course_helper.picker.PickerView
import java.util.*

/**
 * Created by ramiz on 1/31/18.
 */
class PickerDialog(builder: Builder) :
    AlertDialog(builder.context, builder.themeResId),
    DialogInterface.OnClickListener {
    private var mNormalWeeks = 0
    private var mExamWeeks = 0
    private val mPickerView: PickerView
    private val mHeaderTextView: TextView

    @Nullable
    private var mInitialSelectedWeeks: ArrayList<Int>? = ArrayList()

    @Nullable
    private val mOnWeeksSelectedListener: OnWeeksSelectedListener?

    /**
     * Callback to indicate that user is done selecting days
     */
    interface OnWeeksSelectedListener {
        fun onWeeksSelected(
            pickerView: PickerView?,
            selectedWeeks: ArrayList<Int?>?
        )
    }

    override fun show() {
        super.show()
        getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            onClick(this@PickerDialog, DialogInterface.BUTTON_POSITIVE)
            dismiss()
        }
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> mOnWeeksSelectedListener?.onWeeksSelected(
                mPickerView,
                mPickerView.selectedWeeks
            )
            DialogInterface.BUTTON_NEGATIVE -> cancel()
        }
    }

    /**
     * 描述：保存选中周
     * 参数：无
     * 返回：保存的state
     */
    @NonNull
    override fun onSaveInstanceState(): Bundle {
        val state = super.onSaveInstanceState()
        state.putIntegerArrayList(
            KEY_SELECTED_WEEKS,
            mPickerView.selectedWeeks
        )
        return state
    }

    /**
     * 描述：恢复选中周
     * 参数：保存到state
     * 返回：无
     */
    override fun onRestoreInstanceState(@NonNull savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val selectedWeeks =
            savedInstanceState.getIntegerArrayList(KEY_SELECTED_WEEKS)
        if (selectedWeeks != null) {
            mPickerView.setWeeksSelected(selectedWeeks)
        }
    }

    /**
     * 描述：构造器
     * 构造方法：输入context
     * 必须构造的：考试周数，一般周数
     * 可以构造的：初始选中的周，选中周监听器
     */
    class Builder(@field:NonNull @param:NonNull var context: Context) {
        @NonNull
        var examWeeks = 0

        @NonNull
        var normalWeeks = 0

        @Nullable
        var initialSelectedWeeks: ArrayList<Int>? = null

        @Nullable
        var onWeeksSelectedListener: OnWeeksSelectedListener? = null

        @StyleRes
        var themeResId = 0
        fun setWeeks(normalWeeks: Int, examWeeks: Int): Builder {
            this.normalWeeks = normalWeeks
            this.examWeeks = examWeeks
            return this
        }

        fun setInitialSelectedWeeks(@Nullable initialSelectedWeeks: ArrayList<Int>?): Builder {
            this.initialSelectedWeeks = initialSelectedWeeks
            return this
        }

        fun setOnWeeksSelectedListener(@Nullable onWeeksSelectedListener: OnWeeksSelectedListener?): Builder {
            this.onWeeksSelectedListener = onWeeksSelectedListener
            return this
        }

        fun setThemeResId(themeResId: Int): Builder {
            this.themeResId = themeResId
            return this
        }

        fun build(): PickerDialog {
            return PickerDialog(this)
        }

        fun show() {
            build().show()
        }

    }

    companion object {
        private const val KEY_SELECTED_WEEKS = "selectedWeeks"
    }

    init {
        mNormalWeeks = builder.normalWeeks
        mExamWeeks = builder.examWeeks
        mInitialSelectedWeeks = builder.initialSelectedWeeks
        mOnWeeksSelectedListener = builder.onWeeksSelectedListener
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.picker_dialog, null)
        setView(view)
        setButton(
            DialogInterface.BUTTON_POSITIVE,
            context.getString(android.R.string.ok),
            this
        )
        setButton(
            DialogInterface.BUTTON_NEGATIVE,
            context.getString(android.R.string.cancel),
            this
        )
        mHeaderTextView = view.findViewById(R.id.pick_dialog_header)
        mHeaderTextView.text = context.getString(R.string.picker_dialog_title)
        mPickerView = view.findViewById(R.id.pickerView)
        mInitialSelectedWeeks?.let {
            mPickerView.initContent(
                context,
                mNormalWeeks,
                mExamWeeks,
                it
            )
        }
    }
}
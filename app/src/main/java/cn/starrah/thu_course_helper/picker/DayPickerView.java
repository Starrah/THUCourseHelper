package com.github.informramiz.daypickerlibrary.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.Size;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.informramiz.daypickerlibrary.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by ramiz on 1/31/18.
 */

public class DayPickerView extends FrameLayout implements View.OnClickListener {
    private static final String LOG_TAG = DayPickerView.class.getSimpleName();

    private int normalWeeks = 0;
    private int examWeeks = 0;

    @Retention(RetentionPolicy.SOURCE)
    public @interface PickerDay{}


    private ArrayList<CircularTextView> weekViews = new ArrayList();
    //private CircularTextView[] dayViews = new CircularTextView[TOTAL_DAYS];
    @Nullable
    private OnWeeksSelectionChangedListener onWeeksSelectionChangedListener;

    /**
     * Callback to communicate day selection back
     *
     */
    public interface OnWeeksSelectionChangedListener {
        /**
         * Callback to be called when day selection is changed
         * @param dayPickerView View associated with this listener
         * @param selectedWeeks 7-element array(SUNDAY=0, SATURDAY=6) with days that are selected
         * set as true
         */
        void onWeeksSelectionChange(DayPickerView dayPickerView, ArrayList<Integer> selectedWeeks);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public DayPickerView(@NonNull Context context) {
        super(context);
        LayoutInflater.from(getContext()).inflate(R.layout.layout_day_picker_view,
                this,
                true);    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public DayPickerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.DayPickerView);
        LayoutInflater.from(getContext()).inflate(R.layout.layout_day_picker_view,
                this,
                true);    }

    /**
     * 描述：初始化数据存储和显示
     * 参数：当前context，正常周数目，考试周数目，先前选取过的周
     * 返回：无
     */
    public void initContent(Context context, int normalWeeks, int examWeeks, ArrayList<Integer> selectedWeeks) {
        //初始化显示和绑定的数据
        GridLayout currentLayout = findViewById(R.id.week_select_place);
        this.normalWeeks = normalWeeks;
        this.examWeeks = examWeeks;
        for(int i = 0; i < this.normalWeeks + this.examWeeks; i ++) {
            CircularTextView item = new CircularTextView(context);
            item.setText("" + (i + 1));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(100, 100);
            layoutParams.setMargins(10, 10, 10, 10);
            item.setLayoutParams(layoutParams);
            item.setClickable(true);
            item.setFocusable(true);
            weekViews.add(item);
            currentLayout.addView(item);
        }

        //给每个view绑定button click
        for (CircularTextView weekView : weekViews) {
            weekView.setOnClickListener(this);
        }

        //初始化显示
        setWeeksSelected(selectedWeeks);
    }

    @Override
    public void onClick(View v) {
        if (onWeeksSelectionChangedListener != null) {
            onWeeksSelectionChangedListener.onWeeksSelectionChange(this, getSelectedWeeks());
        }
    }

    /**
     * 描述：选中某个周
     * 参数：被选中的第几周（从1开始），是否选中
     */
    public void setWeekSelected(@PickerDay int week, boolean isSelected) {
        weekViews.get(week - 1).setSelected(isSelected);
    }

    /**
     * 描述：设置哪些周被选中，用于初始化
     * 参数：一个数组，代表被选中的周的编号
     * 返回：无
     */
    public void setWeeksSelected(ArrayList<Integer> selectedWeeks) {
        boolean[] whether_show = new boolean[this.examWeeks + this.normalWeeks + 1];
        for(Integer weeks :selectedWeeks) {
            whether_show[weeks.intValue()] = true;
        }
        for(int i = 1; i <= this.examWeeks + this.normalWeeks; i ++) {
            setWeekSelected(i, whether_show[i]);
        }
    }

    /**
     * 描述：返回选中的weeks
     * 参数：无
     * 返回：一个列表，代表选中的所有周（从1开始）
     */
    @NonNull
    public ArrayList<Integer> getSelectedWeeks() {
        ArrayList<Integer> selectedWeeks = new ArrayList<>();
        for (int i = 0; i < this.examWeeks + this.normalWeeks; i++) {
            if(weekViews.get(i).isSelected()) {
                selectedWeeks.add(i + 1);
            }
        }
        return selectedWeeks;
    }

    @Nullable
    public OnWeeksSelectionChangedListener getOnDaysSelectionChangedListener() {
        return onWeeksSelectionChangedListener;
    }

    public void setOnDaysSelectionChangedListener(@Nullable OnWeeksSelectionChangedListener onWeeksSelectionChangedListener) {
        this.onWeeksSelectionChangedListener = onWeeksSelectionChangedListener;
    }

}

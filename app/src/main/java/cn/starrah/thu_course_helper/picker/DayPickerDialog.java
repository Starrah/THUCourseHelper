package com.github.informramiz.daypickerlibrary.views;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.informramiz.daypickerlibrary.R;

import java.util.ArrayList;

/**
 * Created by ramiz on 1/31/18.
 */

public class DayPickerDialog extends AlertDialog implements DialogInterface.OnClickListener {
    private static final String KEY_SELECTED_WEEKS = "selectedWeeks";
    private int mNormalWeeks = 0;
    private int mExamWeeks = 0;
    private DayPickerView mDayPickerView;
    private TextView mHeaderTextView;
    @Nullable
    private ArrayList<Integer> mInitialSelectedWeeks = new ArrayList<>();
    @Nullable
    private OnWeeksSelectedListener mOnWeeksSelectedListener;

    /**
     * Callback to indicate that user is done selecting days
     */
    public interface OnWeeksSelectedListener {
        void onWeeksSelected(DayPickerView dayPickerView, ArrayList<Integer> selectedWeeks);
    }


    public DayPickerDialog(Builder builder) {
        super(builder.context, builder.themeResId);
        this.mNormalWeeks = builder.normalWeeks;
        this.mExamWeeks = builder.examWeeks;
        this.mInitialSelectedWeeks = builder.initialSelectedWeeks;
        this.mOnWeeksSelectedListener = builder.onWeeksSelectedListener;
        View view = LayoutInflater.from(getContext()).inflate(R.layout.day_picker_dialog, null);
        setView(view);

        setButton(BUTTON_POSITIVE, getContext().getString(android.R.string.ok), this);
        setButton(BUTTON_NEGATIVE, getContext().getString(android.R.string.cancel), this);

        mHeaderTextView = view.findViewById(R.id.day_pick_dialog_header);

        mHeaderTextView.setText(getContext().getResources().getQuantityString(R.plurals.day_picker_dialog_title, 7));


        mDayPickerView = view.findViewById(R.id.dayPickerView);
        mDayPickerView.initContent(getContext(), this.mNormalWeeks, this.mExamWeeks, this.mInitialSelectedWeeks);
    }

    @Override
    public void show() {
        super.show();

        getButton(BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DayPickerDialog.this.onClick(DayPickerDialog.this, BUTTON_POSITIVE);
                dismiss();
            }
        });
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case BUTTON_POSITIVE:
                if (mOnWeeksSelectedListener != null) {
                    mOnWeeksSelectedListener.onWeeksSelected(mDayPickerView, mDayPickerView.getSelectedWeeks());
                }
                break;

            case BUTTON_NEGATIVE:
                cancel();
                break;
        }
    }

    /**
     * 描述：保存选中周
     * 参数：无
     * 返回：保存的state
     */
    @NonNull
    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putIntegerArrayList(KEY_SELECTED_WEEKS, mDayPickerView.getSelectedWeeks());
        return state;
    }

    /**
     * 描述：恢复选中周
     * 参数：保存到state
     * 返回：无
     */
    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList<Integer> selectedWeeks = savedInstanceState.getIntegerArrayList(KEY_SELECTED_WEEKS);
        if (selectedWeeks != null) {
            mDayPickerView.setWeeksSelected(selectedWeeks);
        }
    }

    public static class Builder {
        @NonNull
        int examWeeks = 0;
        @NonNull
        int normalWeeks = 0;
        @NonNull
        Context context;
        @Nullable
        ArrayList<Integer> initialSelectedWeeks = null;
        @Nullable
        OnWeeksSelectedListener onWeeksSelectedListener = null;
        @StyleRes
        int themeResId = 0;

        public Builder(@NonNull Context context) {
            this.context = context;
        }

        public Builder setWeeks(int normalWeeks, int examWeeks) {
            this.normalWeeks = normalWeeks;
            this.examWeeks = examWeeks;
            return this;
        }

        public Builder setInitialSelectedWeeks(@Nullable ArrayList<Integer> initialSelectedWeeks) {
            this.initialSelectedWeeks = initialSelectedWeeks;
            return this;
        }


        public Builder setOnWeeksSelectedListener(@Nullable OnWeeksSelectedListener onWeeksSelectedListener) {
            this.onWeeksSelectedListener = onWeeksSelectedListener;
            return this;
        }

        public Builder setThemeResId(int themeResId) {
            this.themeResId = themeResId;
            return this;
        }

        public DayPickerDialog build() {
            return new DayPickerDialog(this);
        }

        public void show() {
            build().show();
        }
     }

}

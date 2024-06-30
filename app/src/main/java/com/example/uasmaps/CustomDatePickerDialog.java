package com.example.uasmaps;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.DatePicker;

import java.util.Calendar;

public class CustomDatePickerDialog extends DatePickerDialog {

    private final Calendar calendar;
    private final OnInvalidDateSelectedListener onInvalidDateSelectedListener;

    public CustomDatePickerDialog(Context context, OnDateSetListener listener, Calendar calendar, OnInvalidDateSelectedListener invalidDateListener) {
        super(context, listener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        this.calendar = calendar;
        this.onInvalidDateSelectedListener = invalidDateListener;
        this.getDatePicker().setMaxDate(System.currentTimeMillis());
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(year, month, dayOfMonth);
        int dayOfWeek = selectedDate.get(Calendar.DAY_OF_WEEK);

        // Check if selected date is weekend or future date
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY || selectedDate.after(Calendar.getInstance())) {
            if (onInvalidDateSelectedListener != null) {
                onInvalidDateSelectedListener.onInvalidDateSelected(dayOfWeek);
            }
            // Check if the date is different before calling updateDate to avoid infinite loop
            if (year != calendar.get(Calendar.YEAR) || month != calendar.get(Calendar.MONTH) || dayOfMonth != calendar.get(Calendar.DAY_OF_MONTH)) {
                updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            }
        } else {
            super.onDateChanged(view, year, month, dayOfMonth);
        }
    }

    @Override
    protected void onStop() {
        // Override to avoid calling listener if the date is invalid
    }

    public interface OnInvalidDateSelectedListener {
        void onInvalidDateSelected(int dayOfWeek);
    }
}

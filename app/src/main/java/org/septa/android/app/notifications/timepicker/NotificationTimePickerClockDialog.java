package org.septa.android.app.notifications.timepicker;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import org.septa.android.app.support.GeneralUtils;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Android's TimePickerDialog defaults to timePickerModer "clock" starting with sdkVersion L
 */
public class NotificationTimePickerClockDialog extends TimePickerDialog {

    private static final String TAG = NotificationTimePickerClockDialog.class.getSimpleName();

    private final static int TIME_PICKER_INTERVAL = 15;
    private Context context;
    private TimePicker mTimePicker;
    private final NotificationTimePickerDialogListener mListener;
    private boolean isStartTime;

    private static final DecimalFormat FORMATTER = new DecimalFormat("00");

    private boolean mIgnoreEvent = false;

    public NotificationTimePickerClockDialog(Context context, NotificationTimePickerDialogListener listener, int hourOfDay, int minute, boolean is24HourView, boolean isStartTime) {
        super(context, TimePickerDialog.THEME_HOLO_LIGHT, null, hourOfDay, minute, is24HourView);
        this.context = context;
        this.mListener = listener;
        this.isStartTime = isStartTime;
    }

    @Override
    public void updateTime(int hourOfDay, int minuteOfHour) {
        mTimePicker.setCurrentHour(hourOfDay);
        mTimePicker.setCurrentMinute(minuteOfHour / TIME_PICKER_INTERVAL);
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        super.onTimeChanged(view, hourOfDay, minute);

        if (mIgnoreEvent) {
            return;
        }

        if (minute % TIME_PICKER_INTERVAL != 0) {
            minute = GeneralUtils.roundUpToNearestInterval(minute, TIME_PICKER_INTERVAL);
            mIgnoreEvent = true;
            view.setCurrentMinute(minute);
            mIgnoreEvent = false;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case BUTTON_POSITIVE:
                if (mListener != null) {
                    if (isStartTime) {
                        mListener.onStartTimeSet(mTimePicker, mTimePicker.getCurrentHour(), mTimePicker.getCurrentMinute());
                    } else {
                        mListener.onEndTimeSet(mTimePicker, mTimePicker.getCurrentHour(), mTimePicker.getCurrentMinute());
                    }
                }
                break;
            case BUTTON_NEGATIVE:
                cancel();
                break;
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            Class<?> classForid = Class.forName("com.android.internal.R$id");
            Field timePickerField = classForid.getField("timePicker");
            mTimePicker = findViewById(timePickerField.getInt(null));

            Field field = classForid.getField("minute");

            NumberPicker minuteSpinner = mTimePicker.findViewById(field.getInt(null));
            minuteSpinner.setMinValue(0);
            minuteSpinner.setMaxValue((60 / TIME_PICKER_INTERVAL) - 1);
            List<String> displayedValues = new ArrayList<>();
            for (int i = 0; i < 60; i += TIME_PICKER_INTERVAL) {
                displayedValues.add(FORMATTER.format(i));
            }
            minuteSpinner.setDisplayedValues(displayedValues.toArray(new String[displayedValues.size()]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
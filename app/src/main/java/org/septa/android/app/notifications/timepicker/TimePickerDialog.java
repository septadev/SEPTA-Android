package org.septa.android.app.notifications.timepicker;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import org.septa.android.app.R;
import org.septa.android.app.support.CrashlyticsManager;
import org.septa.android.app.view.TextView;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class TimePickerDialog extends Dialog implements View.OnClickListener {

    private static final String TAG = TimePickerDialog.class.getSimpleName();

    private TextView cancelButton, saveButton;
    private TimePicker mTimePicker;

    private final static int TIME_PICKER_INTERVAL = 15;
    private final NotificationTimePickerDialogListener mListener;
    private boolean isStartTime;
    private int position, hour, minute;

    private static final DecimalFormat FORMATTER = new DecimalFormat("00");

    public TimePickerDialog(Context context, NotificationTimePickerDialogListener listener, int hourOfDay, int minute, boolean isStartTime, int position) {
        super(context);
        this.mListener = listener;
        this.isStartTime = isStartTime;
        this.position = position;
        this.hour = hourOfDay;
        this.minute = minute;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.view_time_picker_dialog);
        mTimePicker = findViewById(R.id.time_picker);
        cancelButton = findViewById(R.id.cancel_button);
        saveButton = findViewById(R.id.save_button);

        setTimePickerInterval(mTimePicker);
        mTimePicker.setIs24HourView(false);
        mTimePicker.setCurrentHour(hour);
        mTimePicker.setCurrentMinute(minute / TIME_PICKER_INTERVAL);
        cancelButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_button:
                saveChanges();
                break;
            case R.id.cancel_button:
            default:
                break;
        }
        dismiss();
    }

    @SuppressLint("NewApi")
    private void setTimePickerInterval(TimePicker timePicker) {
        try {
            Class<?> classForid = Class.forName("com.android.internal.R$id");

            Field field = classForid.getField("minute");
            NumberPicker minutePicker = timePicker.findViewById(field.getInt(null));

            minutePicker.setMinValue(0);
            minutePicker.setMaxValue((60 / TIME_PICKER_INTERVAL) - 1);
            ArrayList<String> displayedValues = new ArrayList<>();
            for (int i = 0; i < 60; i += TIME_PICKER_INTERVAL) {
                displayedValues.add(FORMATTER.format(i));
            }
            for (int i = 0; i < 60; i += TIME_PICKER_INTERVAL) {
                displayedValues.add(FORMATTER.format(i));
            }
            minutePicker.setDisplayedValues(displayedValues
                    .toArray(new String[0]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void saveChanges() {
        if (mListener != null) {
            if (isStartTime) {
                mListener.onStartTimeSet(mTimePicker, mTimePicker.getCurrentHour(), mTimePicker.getCurrentMinute() * TIME_PICKER_INTERVAL, position);
            } else {
                mListener.onEndTimeSet(mTimePicker, mTimePicker.getCurrentHour(), mTimePicker.getCurrentMinute() * TIME_PICKER_INTERVAL, position);
            }
        } else {
            CrashlyticsManager.log(Log.ERROR, TAG, "Could not save changes to time because listener null");
        }
    }

}
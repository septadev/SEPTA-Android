package org.septa.android.app.notifications.timepicker;

import android.widget.TimePicker;

public interface NotificationTimePickerDialogListener {
    void onStartTimeSet(TimePicker view, int hourOfDay, int minute, int position);

    void onEndTimeSet(TimePicker view, int hourOfDay, int minute, int position);
}
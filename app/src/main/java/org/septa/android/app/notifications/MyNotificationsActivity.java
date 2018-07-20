package org.septa.android.app.notifications;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TimePicker;

import org.septa.android.app.BaseActivity;
import org.septa.android.app.R;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.support.GeneralUtils;
import org.septa.android.app.view.TextView;

import java.util.Calendar;
import java.util.List;

import static org.septa.android.app.notifications.NotificationTimePickerDialog.TIME_PICKER_INTERVAL;

public class MyNotificationsActivity extends BaseActivity implements NotificationTimePickerDialog.NotificationTimePickerDialogListener {

    private static final String TAG = MyNotificationsActivity.class.getSimpleName();

    // layout variables
    TextView startTime, endTime, addButton, editButton;
    SparseArray<ImageView> daysOfWeekButtons;
    boolean[] daysOfWeekEnabled;
    RecyclerView notificationRecyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeActivity(savedInstanceState);

        setContentView(R.layout.activity_my_notifications);
        setTitle(R.string.my_notifications_heading);

        initializeView();

        // day of week buttons are clickable
        for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
            final ImageView button = daysOfWeekButtons.get(i);

            final int dayOfWeek = i;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isEnabled = daysOfWeekEnabled[dayOfWeek - 1];

                    // toggle icon
                    button.setImageResource(getDayOfWeekImageResId(dayOfWeek, !isEnabled));

                    if (isEnabled) {
                        // disable notifications for that day
                        SeptaServiceFactory.getNotificationsService().removeDayOfWeekFromSchedule(MyNotificationsActivity.this, dayOfWeek);
                    } else {
                        // enable notifications for that day
                        SeptaServiceFactory.getNotificationsService().addDayOfWeekToSchedule(MyNotificationsActivity.this, dayOfWeek);
                    }

                    daysOfWeekEnabled[dayOfWeek - 1] = !isEnabled;
                }
            });
        }

        // start time picker
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] strTime = startTime.getText().toString().split("[: ]");

                // parse 12H time to 24H time
                int hour = Integer.parseInt(strTime[0]);
                String amPm = strTime[2];
                if (GeneralUtils.TIME_PM.equalsIgnoreCase(amPm)) {
                    hour += 12;
                }

                // round up to nearest minute interval
                int minute = Integer.parseInt(strTime[1]);
                minute = GeneralUtils.roundUpToNearestInterval(minute, TIME_PICKER_INTERVAL);

                NotificationTimePickerDialog timePickerDialog = new NotificationTimePickerDialog(MyNotificationsActivity.this, MyNotificationsActivity.this, hour, minute, false, true);
                timePickerDialog.show();
            }
        });

        // end time picker
        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] strTime = endTime.getText().toString().split("[: ]");

                // parse 12H time to 24H time
                int hour = Integer.parseInt(strTime[0]);
                String amPm = strTime[2];
                if (GeneralUtils.TIME_PM.equalsIgnoreCase(amPm)) {
                    hour += 12;
                }

                // round up to nearest minute interval
                int minute = Integer.parseInt(strTime[1]);
                minute = GeneralUtils.roundUpToNearestInterval(minute, TIME_PICKER_INTERVAL);

                NotificationTimePickerDialog timePickerDialog = new NotificationTimePickerDialog(MyNotificationsActivity.this, MyNotificationsActivity.this, hour, minute, false, false);
                timePickerDialog.show();
            }
        });
    }

    @Override
    public void onStartTimeSet(TimePicker view, int hourOfDay, int minute) {
        String newStartTime = GeneralUtils.getTimeFromInt(hourOfDay, minute);
        startTime.setText(newStartTime);

        // save start time
        SeptaServiceFactory.getNotificationsService().setNotificationsStartTime(this, newStartTime);
    }

    @Override
    public void onEndTimeSet(TimePicker view, int hourOfDay, int minute) {
        // TODO: end time must be > start time

        String newEndTime = GeneralUtils.getTimeFromInt(hourOfDay, minute);
        endTime.setText(newEndTime);

        // save end time
        SeptaServiceFactory.getNotificationsService().setNotificationsEndTime(this, newEndTime);
    }

    private void initializeActivity(@Nullable Bundle savedInstanceState) {
        // TODO: back button doesn't work
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Bundle bundle = getIntent().getExtras();

        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        } else {
            restoreState(bundle);
        }
    }

    private void restoreState(Bundle bundle) {
        // TODO: restore any variables needed
//        firstRoute = (RouteDirectionModel) bundle.get(TransitViewFragment.TRANSITVIEW_ROUTE_FIRST);
    }

    private void initializeView() {
        startTime = findViewById(R.id.notification_schedule_start_picker);
        endTime = findViewById(R.id.notification_schedule_end_picker);
        addButton = findViewById(R.id.add_notifications);
        editButton = findViewById(R.id.edit_notifications);

        daysOfWeekButtons = new SparseArray<>();
        daysOfWeekButtons.put(Calendar.SUNDAY, (ImageView) findViewById(R.id.button_sunday));
        daysOfWeekButtons.put(Calendar.MONDAY, (ImageView) findViewById(R.id.button_monday));
        daysOfWeekButtons.put(Calendar.TUESDAY, (ImageView) findViewById(R.id.button_tuesday));
        daysOfWeekButtons.put(Calendar.WEDNESDAY, (ImageView) findViewById(R.id.button_wednesday));
        daysOfWeekButtons.put(Calendar.THURSDAY, (ImageView) findViewById(R.id.button_thursday));
        daysOfWeekButtons.put(Calendar.FRIDAY, (ImageView) findViewById(R.id.button_friday));
        daysOfWeekButtons.put(Calendar.SATURDAY, (ImageView) findViewById(R.id.button_saturday));

        // enable button if that day is saved
        daysOfWeekEnabled = new boolean[]{false, false, false, false, false, false, false};
        List<Integer> daysEnabled = SeptaServiceFactory.getNotificationsService().getNotificationsSchedule(this);
        for (Integer dayofWeek : daysEnabled) {
            daysOfWeekEnabled[dayofWeek - 1] = true;
            daysOfWeekButtons.get(dayofWeek).setImageResource(getDayOfWeekImageResId(dayofWeek, true));
        }

        // set initial start and end time
        startTime.setText(SeptaServiceFactory.getNotificationsService().getNotificationStartTime(this));
        endTime.setText(SeptaServiceFactory.getNotificationsService().getNotificationEndTime(this));

        // TODO: recyclerview of notifications
        notificationRecyclerView = findViewById(R.id.notification_recyclerview);
    }

    private int getDayOfWeekImageResId(int dayOfWeek, boolean enabled) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                if (enabled) {
                    return R.drawable.ic_sunday_enabled;
                } else {
                    return R.drawable.ic_sunday_disabled;
                }
            case Calendar.MONDAY:
                if (enabled) {
                    return R.drawable.ic_monday_enabled;
                } else {
                    return R.drawable.ic_monday_disabled;
                }
            case Calendar.TUESDAY:
                if (enabled) {
                    return R.drawable.ic_tuesday_enabled;
                } else {
                    return R.drawable.ic_tuesday_disabled;
                }
            case Calendar.WEDNESDAY:
                if (enabled) {
                    return R.drawable.ic_wednesday_enabled;
                } else {
                    return R.drawable.ic_wednesday_disabled;
                }
            case Calendar.THURSDAY:
                if (enabled) {
                    return R.drawable.ic_thursday_enabled;
                } else {
                    return R.drawable.ic_thursday_disabled;
                }
            case Calendar.FRIDAY:
                if (enabled) {
                    return R.drawable.ic_friday_enabled;
                } else {
                    return R.drawable.ic_friday_disabled;
                }
            case Calendar.SATURDAY:
            default:
                if (enabled) {
                    return R.drawable.ic_saturday_enabled;
                } else {
                    return R.drawable.ic_saturday_disabled;
                }
        }
    }

}

package org.septa.android.app.notifications;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.septa.android.app.BaseActivity;
import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.notifications.edit.EditNotificationsFragment;
import org.septa.android.app.notifications.timepicker.NotificationTimePickerClockDialog;
import org.septa.android.app.notifications.timepicker.NotificationTimePickerDialogListener;
import org.septa.android.app.notifications.timepicker.NotificationTimePickerSpinnerDialog;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.RouteNotificationSubscription;
import org.septa.android.app.support.GeneralUtils;
import org.septa.android.app.view.TextView;

import java.util.Calendar;
import java.util.List;

public class MyNotificationsActivity extends BaseActivity implements NotificationTimePickerDialogListener, EditNotificationsFragment.EditNotificationsFragmentListener, NotificationItemAdapter.NotificationItemListener {

    private static final String TAG = MyNotificationsActivity.class.getSimpleName();

    private boolean[] daysOfWeekEnabled = new boolean[]{false, false, false, false, false, false, false};
    private int startTime, endTime;
    boolean isInEditMode;

    // layout variables
    private TextView startTimePicker, endTimePicker, addButton, editButton;
    private SparseArray<ImageView> daysOfWeekButtons;
    private android.support.v4.app.Fragment activeFragment;

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
        startTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = startTime / 100;
                int minute = startTime % 100;

                TimePickerDialog timePickerDialog;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // android:timePickerMode spinner and clock began in Lollipop
                    timePickerDialog = new NotificationTimePickerClockDialog(MyNotificationsActivity.this, MyNotificationsActivity.this, hour, minute, false, true);
                } else {
                    timePickerDialog = new NotificationTimePickerSpinnerDialog(MyNotificationsActivity.this, MyNotificationsActivity.this, hour, minute, false, true);
                }
                timePickerDialog.show();
            }
        });

        // end time picker
        endTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = endTime / 100;
                int minute = endTime % 100;

                TimePickerDialog timePickerDialog;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // android:timePickerMode spinner and clock began in Lollipop
                    timePickerDialog = new NotificationTimePickerClockDialog(MyNotificationsActivity.this, MyNotificationsActivity.this, hour, minute, false, false);
                } else {
                    timePickerDialog = new NotificationTimePickerSpinnerDialog(MyNotificationsActivity.this, MyNotificationsActivity.this, hour, minute, false, false);
                }
                timePickerDialog.show();
            }
        });

        // add button links to system status picker
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSystemStatusPicker();
            }
        });

        // open edit notifications mode
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInEditMode) {
                    closeEditMode();
                } else {
                    List<RouteNotificationSubscription> routesList = SeptaServiceFactory.getNotificationsService().getRoutesSubscribedTo(MyNotificationsActivity.this);
                    if (routesList.isEmpty()) {
                        // user must have some notifications in order to switch to edit mode
                        Toast.makeText(MyNotificationsActivity.this, R.string.no_notifications_to_edit, Toast.LENGTH_SHORT).show();
                    } else {
                        openEditMode();
                    }
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        try {
            onBackPressed();
        } catch (Exception e) {
            Log.w(TAG, "Exception on Backpress", e);
        }
        return true;
    }

    @Override
    public void onStartTimeSet(TimePicker view, int hourOfDay, int minute) {
        int newStartTime = hourOfDay * 100 + minute;

        // start time must be before end time
        if (newStartTime < endTime) {
            startTime = newStartTime;
            startTimePicker.setText(GeneralUtils.getTimeFromInt(hourOfDay, minute));

            // save start time
            SeptaServiceFactory.getNotificationsService().setNotificationsStartTime(this, startTime);
        } else {
            Toast.makeText(this, R.string.notifications_start_time_requirement, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onEndTimeSet(TimePicker view, int hourOfDay, int minute) {
        int newEndTime = hourOfDay * 100 + minute;

        // end time must be after start time
        if (newEndTime > startTime) {
            endTime = newEndTime;
            endTimePicker.setText(GeneralUtils.getTimeFromInt(hourOfDay, minute));

            // save end time
            SeptaServiceFactory.getNotificationsService().setNotificationsEndTime(this, endTime);
        } else {
            Toast.makeText(this, R.string.notifications_end_time_requirement, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void promptToDeleteNotification(final int position, final String routeId, final String routeName, final TransitType transitType) {
        if (isInEditMode) {
            // prompt to confirm deletion
            new AlertDialog.Builder(MyNotificationsActivity.this).setCancelable(true).setTitle(R.string.delete_notif_title)
                    .setMessage(getString(R.string.delete_notif_text, routeName))

                    // confirm to delete
                    .setPositiveButton(R.string.delete_fav_pos_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                            deleteNotificationForRoute(position, routeId, transitType);
                        }
                    })

                    // cancel deletion
                    .setNegativeButton(R.string.delete_fav_neg_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            Log.e(TAG, "Invalid attempt to delete a notification subscription");
        }
    }

    @Override
    public void closeEditMode() {
        // switch to viewing fragment
        activeFragment = new ViewNotificationsFragment();
        isInEditMode = false;
        getSupportFragmentManager().beginTransaction().replace(R.id.my_notifications_container, activeFragment).commit();
        editButton.setText(R.string.notifications_edit);
    }

    public void openEditMode() {
        // switch to editing notifications fragment
        activeFragment = new EditNotificationsFragment();
        isInEditMode = true;
        getSupportFragmentManager().beginTransaction().replace(R.id.my_notifications_container, activeFragment).commit();
        editButton.setText(R.string.notifications_done);
    }

    private void initializeActivity(@Nullable Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        startTime = SeptaServiceFactory.getNotificationsService().getNotificationStartTime(this);
        endTime = SeptaServiceFactory.getNotificationsService().getNotificationEndTime(this);
    }

    private void initializeView() {
        startTimePicker = findViewById(R.id.notification_schedule_start_picker);
        endTimePicker = findViewById(R.id.notification_schedule_end_picker);
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
        List<Integer> daysEnabled = SeptaServiceFactory.getNotificationsService().getNotificationsSchedule(this);
        for (Integer dayofWeek : daysEnabled) {
            daysOfWeekEnabled[dayofWeek - 1] = true;
            daysOfWeekButtons.get(dayofWeek).setImageResource(getDayOfWeekImageResId(dayofWeek, true));
        }

        // set initial start and end time
        startTimePicker.setText(GeneralUtils.getTimeFromInt(startTime));
        endTimePicker.setText(GeneralUtils.getTimeFromInt(endTime));

        // default to viewing fragment
        activeFragment = new ViewNotificationsFragment();
        isInEditMode = false;
        getSupportFragmentManager().beginTransaction().replace(R.id.my_notifications_container, activeFragment).commit();
    }

    private void goToSystemStatusPicker() {
        setResult(Constants.VIEW_SYSTEM_STATUS_PICKER, new Intent());
        finish();
    }

    private void deleteNotificationForRoute(int position, String routeId, TransitType transitType) {
        PushNotificationManager.getInstance(MyNotificationsActivity.this).deleteNotificationForRoute(routeId, transitType);

        // update view
        EditNotificationsFragment editFragment = (EditNotificationsFragment) activeFragment;
        editFragment.deleteNotificationAtPosition(position);
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
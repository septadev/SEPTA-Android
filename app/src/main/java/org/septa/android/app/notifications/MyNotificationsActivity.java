package org.septa.android.app.notifications;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.septa.android.app.BaseActivity;
import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.notifications.edit.EditNotificationsFragment;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.RouteNotificationSubscription;
import org.septa.android.app.support.AnalyticsManager;
import org.septa.android.app.view.TextView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.septa.android.app.notifications.NotificationsSharedPrefsUtilsImpl.MAX_TIMEFRAMES;

public class MyNotificationsActivity extends BaseActivity implements EditNotificationsFragment.EditNotificationsFragmentListener, NotificationItemAdapter.NotificationItemListener, TimeFrameItemAdapter.TimeFrameItemListener {

    private static final String TAG = MyNotificationsActivity.class.getSimpleName();

    private boolean[] daysOfWeekEnabled = new boolean[]{false, false, false, false, false, false, false};
    boolean isInEditMode;

    // layout variables
    private TextView addTimeFramesButton, addNotifsButton, editButton;
    private SparseArray<ImageView> daysOfWeekButtons;
    private RecyclerView timeFramesRecyclerView;
    private TimeFrameItemAdapter timeFrameItemAdapter;
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

        // add time frames
        addTimeFramesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> timeFramesList = SeptaServiceFactory.getNotificationsService().addNotificationTimeFrame(MyNotificationsActivity.this);

                // only show add timeframe button if not at max
                if (timeFramesList.size() < MAX_TIMEFRAMES) {
                    addTimeFramesButton.setVisibility(View.VISIBLE);
                } else {
                    addTimeFramesButton.setVisibility(View.GONE);
                }
                timeFrameItemAdapter.updateList(timeFramesList);
            }
        });

        // add button links to system status picker
        addNotifsButton.setOnClickListener(new View.OnClickListener() {
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
    public List<String> deleteTimeFrame(int position) {
        List<String> timeFramesList = SeptaServiceFactory.getNotificationsService().removeNotificationTimeFrame(MyNotificationsActivity.this, position);

        // only show add timeframe button if not at max
        if (timeFramesList.size() < MAX_TIMEFRAMES) {
            addTimeFramesButton.setVisibility(View.VISIBLE);
        } else {
            addTimeFramesButton.setVisibility(View.GONE);
        }

        return timeFramesList;
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
    }

    private void initializeView() {
        addTimeFramesButton = findViewById(R.id.add_timeframe_button);
        addNotifsButton = findViewById(R.id.add_notifications);
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

        // default to viewing fragment
        activeFragment = new ViewNotificationsFragment();
        isInEditMode = false;
        getSupportFragmentManager().beginTransaction().replace(R.id.my_notifications_container, activeFragment).commit();

        List<String> timeFramesList = SeptaServiceFactory.getNotificationsService().getNotificationTimeFrames(MyNotificationsActivity.this);

        // only show add timeframe button if not at max
        if (timeFramesList.size() < MAX_TIMEFRAMES) {
            addTimeFramesButton.setVisibility(View.VISIBLE);
        } else {
            addTimeFramesButton.setVisibility(View.GONE);
        }

        // initialize recyclerview of timeframes
        timeFramesRecyclerView = findViewById(R.id.notification_timeframes_recyclerview);
        timeFramesRecyclerView.setLayoutManager(new LinearLayoutManager(MyNotificationsActivity.this));
        timeFrameItemAdapter = new TimeFrameItemAdapter(MyNotificationsActivity.this, timeFramesList);
        timeFramesRecyclerView.setAdapter(timeFrameItemAdapter);
        timeFrameItemAdapter.updateList(timeFramesList);
    }

    private void goToSystemStatusPicker() {
        setResult(Constants.VIEW_SYSTEM_STATUS_PICKER, new Intent());
        finish();
    }

    private void deleteNotificationForRoute(int position, String routeId, TransitType transitType) {
        Map<String, String> deletedRouteData = new HashMap<>();
        deletedRouteData.put(Constants.TRANSIT_TYPE, String.valueOf(transitType));
        deletedRouteData.put(Constants.ROUTE_ID, routeId);
        AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_ROUTE_DELETE_SUBSCRIPTION, AnalyticsManager.CUSTOM_EVENT_ID_NOTIFICATION_MANAGEMENT, deletedRouteData);

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
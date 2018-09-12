package org.septa.android.app.notifications;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import org.septa.android.app.services.apiinterfaces.model.PushNotifSubscriptionRequest;
import org.septa.android.app.services.apiinterfaces.model.RouteSubscription;
import org.septa.android.app.support.AnalyticsManager;
import org.septa.android.app.support.CrashlyticsManager;
import org.septa.android.app.support.GeneralUtils;
import org.septa.android.app.view.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.septa.android.app.services.apiinterfaces.NotificationsSharedPrefsUtilsImpl.DEFAULT_TIME_FRAME;
import static org.septa.android.app.services.apiinterfaces.NotificationsSharedPrefsUtilsImpl.MAX_TIMEFRAMES;
import static org.septa.android.app.services.apiinterfaces.NotificationsSharedPrefsUtilsImpl.START_END_TIME_DELIM;

public class MyNotificationsActivity extends BaseActivity implements EditNotificationsFragment.EditNotificationsFragmentListener, NotificationItemAdapter.NotificationItemListener, TimeFrameItemAdapter.TimeFrameItemListener {

    private static final String TAG = MyNotificationsActivity.class.getSimpleName();

    private boolean[] daysOfWeekEnabled = new boolean[]{false, false, false, false, false, false, false};
    private static final DecimalFormat FORMATTER = new DecimalFormat("0000");
    boolean isInEditMode;

    // initial days of week and timeframe
    private String[] daysOfWeekText = new String[]{"", "Su", "M", "Tu", "W", "Th", "F", "Sa"};
    private List<Integer> initialDaysOfWeek, modifiedDaysOfWeek;
    private List<String> initialTimeFrames, modifiedTimeFrames;
    private List<RouteSubscription> initialRouteSubscriptions, modifiedRouteSubscriptions;

    // layout variables
    private TextView addTimeFramesButton, addNotifsButton, editButton;
    private SparseArray<ImageView> daysOfWeekButtons;
    private RecyclerView timeFramesRecyclerView;
    private TimeFrameItemAdapter timeFrameItemAdapter;
    private android.support.v4.app.Fragment activeFragment;
    private FloatingActionButton saveButton;

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

                    // enforce that at least one DOW selected
                    if (isEnabled && modifiedDaysOfWeek.size() == 1) {
                        Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_my_notifications), R.string.notifications_days_of_week_requirement, Snackbar.LENGTH_SHORT);
                        snackbar.setAction("Show Me", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onBackPressed();
                            }
                        });
                        View snackbarView = snackbar.getView();
                        android.widget.TextView tv = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                        tv.setMaxLines(10);
                        snackbar.show();

                    } else {
                        // toggle icon
                        button.setImageResource(getDayOfWeekImageResId(dayOfWeek, !isEnabled));

                        if (isEnabled) {
                            // disable notifications for that day
                            int indexToRemove = -1;
                            for (int i = 0; i < modifiedDaysOfWeek.size(); i++) {
                                if (dayOfWeek == modifiedDaysOfWeek.get(i)) {
                                    indexToRemove = i;
                                    break;
                                }
                            }
                            if (indexToRemove != -1) {
                                modifiedDaysOfWeek.remove(indexToRemove);
                            } else {
                                Log.e(TAG, "Notifications are already disabled for " + dayOfWeek);
                            }
                        } else {
                            // enable notifications for that day
                            if (!modifiedDaysOfWeek.contains(dayOfWeek)) {
                                modifiedDaysOfWeek.add(dayOfWeek);
                                Collections.sort(modifiedDaysOfWeek);
                            }

                        }

                        daysOfWeekEnabled[dayOfWeek - 1] = !isEnabled;

                        // show changes have not been saved
                        enableSaveButton();
                    }
                }
            });
        }

        // add time frames
        addTimeFramesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifiedTimeFrames.add(DEFAULT_TIME_FRAME);

                // only show add timeframe button if not at max
                if (modifiedTimeFrames.size() < MAX_TIMEFRAMES) {
                    addTimeFramesButton.setVisibility(View.VISIBLE);
                } else {
                    addTimeFramesButton.setVisibility(View.GONE);
                }
                timeFrameItemAdapter.updateList(modifiedTimeFrames);

                // show changes have not been saved
                enableSaveButton();
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
                    if (activeFragment instanceof EditNotificationsFragment) {
                        List<RouteSubscription> routesList = ((EditNotificationsFragment) activeFragment).getRoutesList();
                        closeEditMode(routesList);
                    } else {
                        CrashlyticsManager.log(Log.WARN, TAG, "Attempting to close edit mode when already in viewing mode.");
                    }
                } else {
                    if (activeFragment instanceof ViewNotificationsFragment) {
                        List<RouteSubscription> routesList = ((ViewNotificationsFragment) activeFragment).getRoutesList();

                        if (routesList.isEmpty()) {
                            // user must have some notifications in order to switch to edit mode
                            Toast.makeText(MyNotificationsActivity.this, R.string.no_notifications_to_edit, Toast.LENGTH_SHORT).show();
                        } else {
                            openEditMode(routesList);
                        }
                    } else {
                        CrashlyticsManager.log(Log.WARN, TAG, "Attempting to open edit mode when already in edit mode.");
                    }
                }
            }
        });

        // save button is clickable
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GeneralUtils.isConnectedToInternet(MyNotificationsActivity.this)) {
                    disableView(saveButton);

                    // build request
                    PushNotifSubscriptionRequest request = PushNotificationManager.buildSubscriptionRequest(MyNotificationsActivity.this, modifiedDaysOfWeek, modifiedTimeFrames, modifiedRouteSubscriptions);

                    // send subscription update to server and handle response
                    PushNotificationManager.updateNotifSubscription(MyNotificationsActivity.this, request,
                            new Runnable() {
                                @Override
                                public void run() {
                                    enableSaveButton();
                                }
                            }, new Runnable() {
                                @Override
                                public void run() {
                                    // show success message
                                    Toast.makeText(MyNotificationsActivity.this, R.string.subscription_success, Toast.LENGTH_SHORT).show();

                                    // save settings to shared preferences
                                    savePrefsLocally();
                                }
                            });

                } else {
                    // handle no network connection
                    Toast.makeText(MyNotificationsActivity.this, R.string.subscription_need_connection, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // disable save button
        boolean notifsSaved = SeptaServiceFactory.getNotificationsService().areNotifPrefsSaved(MyNotificationsActivity.this);
        if (!notifsSaved) {
            // show message only when starting activity that there are unsaved changes
            Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_my_notifications), R.string.unsaved_notif_prefs, Snackbar.LENGTH_LONG);
            snackbar.setAction("Save", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveButton.performClick();
                }
            });
            View snackbarView = snackbar.getView();
            android.widget.TextView tv = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            tv.setMaxLines(10);
            snackbar.show();
        }
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
    public void onBackPressed() {
        boolean notifsSaved = SeptaServiceFactory.getNotificationsService().areNotifPrefsSaved(MyNotificationsActivity.this);
        if (!notifsSaved) {
            final AlertDialog dialog = new AlertDialog.Builder(MyNotificationsActivity.this).setCancelable(true).setTitle(R.string.subscription_warning_title)
                    .setMessage(R.string.subscription_warning)
                    .setPositiveButton(R.string.notif_warning_pos_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // save and go back
                            saveButton.performClick();
                            MyNotificationsActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton(R.string.notif_warning_neg_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SeptaServiceFactory.getNotificationsService().setNotifPrefsSaved(MyNotificationsActivity.this, true);
                            MyNotificationsActivity.super.onBackPressed();
                        }
                    })
                    .create();
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
                }
            });
            dialog.show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void setStartTime(int position, int newStartTime) {
        int endTime = Integer.parseInt(modifiedTimeFrames.get(position).substring(5));

        // start time must be before end time
        if (newStartTime < endTime) {
            // save new start time
            modifiedTimeFrames.set(position, FORMATTER.format(newStartTime) + START_END_TIME_DELIM + FORMATTER.format(endTime));

            // show changes in view
            timeFrameItemAdapter.updateList(modifiedTimeFrames);

            enableSaveButton();
        } else {
            Toast.makeText(MyNotificationsActivity.this, R.string.notifications_start_time_requirement, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void setEndTime(int position, int newEndTime) {
        int startTime = Integer.parseInt(modifiedTimeFrames.get(position).substring(0, 4));

        // end time must be after start time
        if (newEndTime > startTime) {
            // save new end time
            modifiedTimeFrames.set(position, FORMATTER.format(startTime) + START_END_TIME_DELIM + FORMATTER.format(newEndTime));

            // show changes in view
            timeFrameItemAdapter.updateList(modifiedTimeFrames);

            enableSaveButton();
        } else {
            Toast.makeText(MyNotificationsActivity.this, R.string.notifications_end_time_requirement, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public List<String> deleteTimeFrame(int position) {
        modifiedTimeFrames.remove(position);

        // only show add timeframe button if not at max
        if (modifiedTimeFrames.size() < MAX_TIMEFRAMES) {
            addTimeFramesButton.setVisibility(View.VISIBLE);
        } else {
            addTimeFramesButton.setVisibility(View.GONE);
        }

        // show changes have not been saved
        enableSaveButton();

        return modifiedTimeFrames;
    }

    @Override
    public void enableNotifsForRoute(int position) {
        modifiedRouteSubscriptions.get(position).setEnabled(true);
    }

    @Override
    public void disableNotifsForRoute(int position) {
        modifiedRouteSubscriptions.get(position).setEnabled(false);
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
    public void enableSaveButton() {
        if (initialDaysOfWeek == null || initialTimeFrames == null || initialRouteSubscriptions == null) {
            CrashlyticsManager.log(Log.ERROR, TAG, "Initial user notification preferences is null: ");
            if (initialDaysOfWeek == null) {
                CrashlyticsManager.log(Log.ERROR, TAG, "days of week is null");
            }
            if (initialTimeFrames == null) {
                CrashlyticsManager.log(Log.ERROR, TAG, "timeframes is null");
            }
            if (initialRouteSubscriptions == null) {
                CrashlyticsManager.log(Log.ERROR, TAG, "route subscriptions is null");
            }
        } else if (initialDaysOfWeek.equals(modifiedDaysOfWeek) && initialTimeFrames.equals(modifiedTimeFrames) && initialRouteSubscriptions.equals(modifiedRouteSubscriptions)) {
            SeptaServiceFactory.getNotificationsService().setNotifPrefsSaved(MyNotificationsActivity.this, true);
            disableView(saveButton);
        } else {
            SeptaServiceFactory.getNotificationsService().setNotifPrefsSaved(MyNotificationsActivity.this, false);
            activateView(saveButton);
        }
    }

    @Override
    public void closeEditMode(List<RouteSubscription> routeSubscriptions) {
        // switch to viewing fragment
        activeFragment = ViewNotificationsFragment.newInstance(routeSubscriptions);
        isInEditMode = false;
        getSupportFragmentManager().beginTransaction().replace(R.id.my_notifications_container, activeFragment).commit();
        editButton.setText(R.string.notifications_edit);
    }

    public void openEditMode(List<RouteSubscription> routeSubscriptions) {
        // switch to editing notifications fragment
        activeFragment = EditNotificationsFragment.newInstance(routeSubscriptions);
        isInEditMode = true;
        getSupportFragmentManager().beginTransaction().replace(R.id.my_notifications_container, activeFragment).commit();
        editButton.setText(R.string.notifications_done);
    }

    private void initializeActivity(@Nullable Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        initialDaysOfWeek = SeptaServiceFactory.getNotificationsService().getNotificationsSchedule(this);
        initialTimeFrames = SeptaServiceFactory.getNotificationsService().getNotificationTimeFrames(MyNotificationsActivity.this);
        initialRouteSubscriptions = SeptaServiceFactory.getNotificationsService().getRoutesSubscribedTo(MyNotificationsActivity.this);

        modifiedDaysOfWeek = new ArrayList<>(initialDaysOfWeek);
        modifiedTimeFrames = new ArrayList<>(initialTimeFrames);
        modifiedRouteSubscriptions = new ArrayList<>();
        for (RouteSubscription routeSubscription : initialRouteSubscriptions) {
            modifiedRouteSubscriptions.add(routeSubscription.clone());
        }
    }

    private void initializeView() {
        addTimeFramesButton = findViewById(R.id.add_timeframe_button);
        addNotifsButton = findViewById(R.id.add_notifications);
        editButton = findViewById(R.id.edit_notifications);
        saveButton = findViewById(R.id.save_notif_settings);

        daysOfWeekButtons = new SparseArray<>();
        daysOfWeekButtons.put(Calendar.SUNDAY, (ImageView) findViewById(R.id.button_sunday));
        daysOfWeekButtons.put(Calendar.MONDAY, (ImageView) findViewById(R.id.button_monday));
        daysOfWeekButtons.put(Calendar.TUESDAY, (ImageView) findViewById(R.id.button_tuesday));
        daysOfWeekButtons.put(Calendar.WEDNESDAY, (ImageView) findViewById(R.id.button_wednesday));
        daysOfWeekButtons.put(Calendar.THURSDAY, (ImageView) findViewById(R.id.button_thursday));
        daysOfWeekButtons.put(Calendar.FRIDAY, (ImageView) findViewById(R.id.button_friday));
        daysOfWeekButtons.put(Calendar.SATURDAY, (ImageView) findViewById(R.id.button_saturday));

        // enable button if that day is saved
        for (Integer dayofWeek : initialDaysOfWeek) {
            daysOfWeekEnabled[dayofWeek - 1] = true;
            daysOfWeekButtons.get(dayofWeek).setImageResource(getDayOfWeekImageResId(dayofWeek, true));
        }

        // default to viewing fragment
        closeEditMode(initialRouteSubscriptions);

        // only show add timeframe button if not at max
        if (initialTimeFrames.size() < MAX_TIMEFRAMES) {
            addTimeFramesButton.setVisibility(View.VISIBLE);
        } else {
            addTimeFramesButton.setVisibility(View.GONE);
        }

        // initialize recyclerview of timeframes
        timeFramesRecyclerView = findViewById(R.id.notification_timeframes_recyclerview);
        timeFramesRecyclerView.setLayoutManager(new LinearLayoutManager(MyNotificationsActivity.this));
        timeFrameItemAdapter = new TimeFrameItemAdapter(MyNotificationsActivity.this, initialTimeFrames);
        timeFramesRecyclerView.setAdapter(timeFrameItemAdapter);
        timeFrameItemAdapter.updateList(initialTimeFrames);

        // disable save button
        boolean notifsSaved = SeptaServiceFactory.getNotificationsService().areNotifPrefsSaved(MyNotificationsActivity.this);
        if (notifsSaved) {
            disableView(saveButton);
        } else {
            activateView(saveButton);

            // show message only when starting activity that there are unsaved changes
            Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_my_notifications), R.string.unsaved_notif_prefs, Snackbar.LENGTH_LONG);
            snackbar.setAction("Save", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveButton.performClick();
                }
            });
            View snackbarView = snackbar.getView();
            android.widget.TextView tv = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            tv.setMaxLines(10);
            snackbar.show();
        }
    }

    private void goToSystemStatusPicker() {
        setResult(Constants.VIEW_SYSTEM_STATUS_PICKER, new Intent());
        finish();
    }

    private void deleteNotificationForRoute(int position, String routeId, TransitType transitType) {
        Map<String, String> deletedRouteData = new HashMap<>();
        deletedRouteData.put("Deleted Subscription - Transit Type", String.valueOf(transitType));
        deletedRouteData.put("Deleted Subscription - Route ID", routeId);
        AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_ROUTE_DELETE_SUBSCRIPTION, AnalyticsManager.CUSTOM_EVENT_ID_NOTIFICATION_MANAGEMENT, deletedRouteData);

        modifiedRouteSubscriptions.remove(position);

        // update view
        EditNotificationsFragment editFragment = (EditNotificationsFragment) activeFragment;
        editFragment.deleteNotificationAtPosition(position);

        // show changes have not been saved
        enableSaveButton();
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

    private void disableView(View view) {
        view.setAlpha((float) .5);
        view.setEnabled(false);
        view.setClickable(false);
    }

    private void activateView(View view) {
        view.setAlpha(1);
        view.setEnabled(true);
        view.setClickable(true);
    }

    private void savePrefsLocally() {
        if (initialDaysOfWeek != null && !initialDaysOfWeek.equals(modifiedDaysOfWeek)) {
            // save changes to days of week
            SeptaServiceFactory.getNotificationsService().setNotificationsSchedule(MyNotificationsActivity.this, modifiedDaysOfWeek);

            // reset initial list of days of week
            initialDaysOfWeek = new ArrayList<>(modifiedDaysOfWeek);

            // analytics around days of week
            StringBuilder daysOfWeek = new StringBuilder();
            for (Integer day : modifiedDaysOfWeek) {
                daysOfWeek.append(daysOfWeekText[day]);
            }
            Map<String, String> data = new HashMap<>();
            data.put("Days of Week", daysOfWeek.toString());
            AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_DAYS_OF_WEEK, AnalyticsManager.CUSTOM_EVENT_ID_NOTIFICATION_MANAGEMENT, data);
        }

        if (initialTimeFrames != null && !initialTimeFrames.equals(modifiedTimeFrames)) {
            // save changes to timeframes
            SeptaServiceFactory.getNotificationsService().setNotificationTimeFrames(MyNotificationsActivity.this, modifiedTimeFrames);

            // reset initial list of time frames
            initialTimeFrames = new ArrayList<>(modifiedTimeFrames);

            // analytics around timeframes
            Map<String, String> data = new HashMap<>();
            data.put("Timeframe(s)", modifiedTimeFrames.toString());
            AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_TIMEFRAMES, AnalyticsManager.CUSTOM_EVENT_ID_NOTIFICATION_MANAGEMENT, data);
        }

        if (!initialRouteSubscriptions.equals(modifiedRouteSubscriptions)) {
            // save changes to route subscriptions
            SeptaServiceFactory.getNotificationsService().setRoutesSubscribedTo(MyNotificationsActivity.this, modifiedRouteSubscriptions);

            // reset initial list of route subscriptions
            initialRouteSubscriptions.clear();
            for (RouteSubscription routeSubscription : modifiedRouteSubscriptions) {
                initialRouteSubscriptions.add(routeSubscription.clone());
            }

            // TODO: analytics around route subscriptions
//            Map<String, String> data = new HashMap<>();
//            data.put("Route Subscriptions", modifiedRouteSubscriptions.toString());
//            AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_ROUTE_UPDATES, AnalyticsManager.CUSTOM_EVENT_ID_NOTIFICATION_MANAGEMENT, data);
        }
    }

}
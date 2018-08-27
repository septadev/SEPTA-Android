package org.septa.android.app.notifications;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.support.AnalyticsManager;
import org.septa.android.app.support.CrashlyticsManager;
import org.septa.android.app.support.GeneralUtils;
import org.septa.android.app.view.TextView;

public class NotificationsManagementFragment extends Fragment {

    private static final String TAG = NotificationsManagementFragment.class.getSimpleName(), TOOLBAR_TITLE = "TOOLBAR_TITLE";

    private Context context;

    // used to ignore switch toggles for analytics
    private boolean ignoreGlobalSwitch = false, ignoreSpecialSwitch = false;

    // layout variables
    private View containerView;
    private TextView systemSettings, myNotifs;
    private SwitchCompat enableNotifs, specialAnnouncements;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_notifications, container, false);

        context = getContext();
        if (context == null) {
            return rootView;
        }

        initializeView(rootView);

        containerView = rootView;

        // enable or disable push notifications
        enableNotifs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (GeneralUtils.isConnectedToInternet(context)) {
                    if (!ignoreGlobalSwitch) {
                        if (isChecked) {
                            // turn on notifications
                            toggleNotifications(true);

                        } else {
                            // turn off notifications
                            toggleNotifications(false);
                        }
                    } else if (!isChecked) {
                        specialAnnouncements.setEnabled(false);
                    } else {
                        specialAnnouncements.setEnabled(true);
                    }
                } else {
                    Toast.makeText(context, R.string.subscription_need_connection, Toast.LENGTH_SHORT).show();

                    // handle no network connection
                    ignoreGlobalSwitch = true;
                    enableNotifs.setChecked(!isChecked);
                    ignoreGlobalSwitch = false;
                }
            }
        });

        // enable or disable special announcements
        specialAnnouncements.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (GeneralUtils.isConnectedToInternet(context)) {
                    if (!ignoreSpecialSwitch) {
                        if (isChecked) {
                            subscribeToSpecialAnnouncements();
                        } else {
                            unsubscribeFromSpecialAnnouncements();
                        }
                    }
                } else {
                    Toast.makeText(context, R.string.subscription_need_connection, Toast.LENGTH_SHORT).show();

                    // handle no network connection
                    ignoreSpecialSwitch = true;
                    specialAnnouncements.setChecked(!isChecked);
                    ignoreSpecialSwitch = false;
                }
            }
        });

        // link to system settings
        systemSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSystemNotificationSettings(context);
            }
        });

        myNotifs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMyNotifications();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // recheck device permissions and only enable switch if allowed
        boolean notifsAllowed = NotificationManagerCompat.from(context).areNotificationsEnabled();

        enableNotifs.setEnabled(notifsAllowed);
        specialAnnouncements.setEnabled(notifsAllowed && enableNotifs.isChecked());

        if (!notifsAllowed) {
            // show message that device permissions must be enabled in order to toggle preferences
            Snackbar snackbar = Snackbar.make(containerView, R.string.notifications_permission_required, Snackbar.LENGTH_LONG);
            snackbar.setAction("Settings", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // link to system notification settings
                    openSystemNotificationSettings(context);
                }
            });

            View snackbarView = snackbar.getView();
            android.widget.TextView tv = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            tv.setMaxLines(10);
            snackbar.show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TOOLBAR_TITLE, getActivity().getTitle().toString());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            String title = savedInstanceState.getString(TOOLBAR_TITLE);
            if (title != null && getActivity() != null) {
                getActivity().setTitle(title);
            }
        }
    }

    private void initializeView(View rootView) {
        systemSettings = rootView.findViewById(R.id.system_notification_settings);
        myNotifs = rootView.findViewById(R.id.my_notifications_link);
        enableNotifs = rootView.findViewById(R.id.enable_notifications_switch);
        specialAnnouncements = rootView.findViewById(R.id.special_announcements_switch);

        // set initial checked state of toggles based on shared preferences
        ignoreGlobalSwitch = true;
        ignoreSpecialSwitch = true;
        enableNotifs.setChecked(SeptaServiceFactory.getNotificationsService().areNotificationsEnabled(context));
        specialAnnouncements.setChecked(SeptaServiceFactory.getNotificationsService().areSpecialAnnouncementsEnabled(context));
        ignoreSpecialSwitch = false;
        ignoreGlobalSwitch = false;
    }

    private void toggleNotifications(boolean isChecked) {
        // disable other switches but remember their value
        specialAnnouncements.setEnabled(isChecked);

        if (isChecked) {
            SeptaServiceFactory.getNotificationsService().setNotificationsEnabled(context, true);

            // send subscription update to server and handle response
            PushNotificationManager.updateNotifSubscription(context, new Runnable() {
                @Override
                public void run() {
                    String deviceId = SeptaServiceFactory.getNotificationsService().getDeviceId(context);
                    CrashlyticsManager.log(Log.ERROR, TAG, "Unable to turn on notifications for device ID: " + deviceId);

                    failureToToggleNotifSubscription(true);
                }
            });

            AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_ENABLE_NOTIFS, AnalyticsManager.CUSTOM_EVENT_ID_NOTIFICATION_MANAGEMENT, null);
        } else {
            SeptaServiceFactory.getNotificationsService().setNotificationsEnabled(context, false);

            // send subscription removal to server and handle response
            PushNotificationManager.removeNotifSubscription(context, new Runnable() {
                @Override
                public void run() {
                    String deviceId = SeptaServiceFactory.getNotificationsService().getDeviceId(context);
                    CrashlyticsManager.log(Log.ERROR, TAG, "Unable to turn off notifications for device ID: " + deviceId);

                    failureToToggleNotifSubscription(false);
                }
            });

            AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_DISABLE_NOTIFS, AnalyticsManager.CUSTOM_EVENT_ID_NOTIFICATION_MANAGEMENT, null);
        }
    }

    private void subscribeToSpecialAnnouncements() {
        Log.d(TAG, "Subscribing to SEPTA Special Announcements");

        SeptaServiceFactory.getNotificationsService().setSpecialAnnouncementsEnabled(context, true);

        // send subscription update to server and handle response
        PushNotificationManager.updateNotifSubscription(context, new Runnable() {
            @Override
            public void run() {
                String deviceId = SeptaServiceFactory.getNotificationsService().getDeviceId(context);
                CrashlyticsManager.log(Log.ERROR, TAG, "Unable to subscribe to special announcements for device ID: " + deviceId);

                failureToToggleSpecialAnnouncements(true);
            }
        });

        AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_ENABLE_SPECIAL_ANNOUNCEMENTS, AnalyticsManager.CUSTOM_EVENT_ID_NOTIFICATION_MANAGEMENT, null);
    }

    private void unsubscribeFromSpecialAnnouncements() {
        SeptaServiceFactory.getNotificationsService().setSpecialAnnouncementsEnabled(context, false);

        // send subscription update to server and handle response
        PushNotificationManager.updateNotifSubscription(context, new Runnable() {
            @Override
            public void run() {
                String deviceId = SeptaServiceFactory.getNotificationsService().getDeviceId(context);
                CrashlyticsManager.log(Log.ERROR, TAG, "Unable to unsubscribe from special announcements for device ID: " + deviceId);

                failureToToggleSpecialAnnouncements(false);
            }
        });

        AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_DISABLE_SPECIAL_ANNOUNCEMENTS, AnalyticsManager.CUSTOM_EVENT_ID_NOTIFICATION_MANAGEMENT, null);
    }

    private void failureToToggleNotifSubscription(boolean isChecked) {
        ignoreGlobalSwitch = true;
        enableNotifs.setChecked(!isChecked);
        SeptaServiceFactory.getNotificationsService().setNotificationsEnabled(context, !isChecked);
        ignoreGlobalSwitch = false;
    }

    private void failureToToggleSpecialAnnouncements(boolean isChecked) {
        ignoreSpecialSwitch = true;
        specialAnnouncements.setChecked(!isChecked);
        SeptaServiceFactory.getNotificationsService().setSpecialAnnouncementsEnabled(context, !isChecked);
        ignoreSpecialSwitch = false;
    }

    public static void openSystemNotificationSettings(Context context) {
        Intent intent = new Intent();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // open notification settings for SEPTA app if on 26+
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());

        } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // notification settings for 21+
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", context.getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);

        } else {
            // notification settings for <20
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
        }
        context.startActivity(intent);
    }

    private void openMyNotifications() {
        Intent intent = new Intent(context, MyNotificationsActivity.class);
        startActivityForResult(intent, Constants.NOTIFICATIONS_REQUEST);
    }
}
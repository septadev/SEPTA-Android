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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.support.AnalyticsManager;
import org.septa.android.app.view.TextView;

public class NotificationsManagementFragment extends Fragment {

    private static final String TAG = NotificationsManagementFragment.class.getSimpleName(), TOOLBAR_TITLE = "TOOLBAR_TITLE";

    private Context context;

    // used to ignore switch toggles for analytics
    private boolean ignoreGlobalSwitch = false, ignoreSpecialSwitch = false;

    // layout variables
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

        final View containerView = rootView;

        // enable or disable push notifications
        enableNotifs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // check for device permission to send notifications about the app before enabling
                boolean notifsAllowed = NotificationManagerCompat.from(context).areNotificationsEnabled();

                if (!notifsAllowed && isChecked) {
                    // user cannot enable notifs without permissions
                    ignoreGlobalSwitch = true;
                    enableNotifs.setChecked(false);
                    toggleNotifications(false);
                    ignoreGlobalSwitch = false;

                    // show message that device permissions must be enabled
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

                } else if (notifsAllowed && isChecked) {
                    // turn on notifications
                    toggleNotifications(true);

                } else {
                    // turn off notifications
                    toggleNotifications(false);
                }
            }
        });

        // enable or disable special announcements
        specialAnnouncements.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!ignoreSpecialSwitch) {
                    if (isChecked) {
                        PushNotificationManager.getInstance(context).subscribeToSpecialAnnouncements();

                        AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_ENABLE_SPECIAL_ANNOUNCEMENTS, AnalyticsManager.CUSTOM_EVENT_ID_NOTIFICATION_MANAGEMENT, null);
                    } else {
                        PushNotificationManager.getInstance(context).unsubscribeFromSpecialAnnouncements();

                        AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_DISABLE_SPECIAL_ANNOUNCEMENTS, AnalyticsManager.CUSTOM_EVENT_ID_NOTIFICATION_MANAGEMENT, null);
                    }
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
        boolean notifsEnabled = SeptaServiceFactory.getNotificationsService().areNotificationsEnabled(context),
                notifsAllowed = NotificationManagerCompat.from(context).areNotificationsEnabled();
        ignoreGlobalSwitch = true;
        enableNotifs.setChecked(notifsEnabled && notifsAllowed);
        ignoreGlobalSwitch = false;
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

        // set initial checked state of special announcements based on shared preferences
        ignoreSpecialSwitch = true;
        specialAnnouncements.setChecked(SeptaServiceFactory.getNotificationsService().areSpecialAnnouncementsEnabled(context));
        ignoreSpecialSwitch = false;
    }

    private void toggleNotifications(boolean isChecked) {
        // disable other switches but remember their value
        specialAnnouncements.setEnabled(isChecked);

        if (!ignoreGlobalSwitch) {
            if (isChecked) {
                PushNotificationManager.getInstance(context).resubscribeToPushNotifs();
                AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_ENABLE_NOTIFS, AnalyticsManager.CUSTOM_EVENT_ID_NOTIFICATION_MANAGEMENT, null);
            } else {
                PushNotificationManager.getInstance(context).unsubscribeFromPushNotifs();
                AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_DISABLE_NOTIFS, AnalyticsManager.CUSTOM_EVENT_ID_NOTIFICATION_MANAGEMENT, null);
            }
        }
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
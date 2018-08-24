package org.septa.android.app.services.apiinterfaces;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.septa.android.app.TransitType;
import org.septa.android.app.notifications.DelayNotificationType;
import org.septa.android.app.notifications.NotificationType;
import org.septa.android.app.notifications.PushNotificationManager;
import org.septa.android.app.support.AnalyticsManager;
import org.septa.android.app.support.CrashlyticsManager;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PushNotificationService extends FirebaseMessagingService {

    private static final String TAG = PushNotificationService.class.getSimpleName();

    private static final String NOTIFICATION_KEY_TYPE = "notificationType",
            NOTIFICATION_KEY_TRANSIT_TYPE = "routeType",
            NOTIFICATION_KEY_ROUTE_ID = "routeId",
            NOTIFICATION_KEY_MESSAGE = "message",
            NOTIFICATION_KEY_VEHICLE_ID = "vehicleId",
            NOTIFICATION_KEY_DESTINATION_STOP_ID = "destinationStopId",
            NOTIFICATION_KEY_DELAY_TYPE = "delayType",
            NOTIFICATION_KEY_EXPIRES = "expires";

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                final String newToken = instanceIdResult.getToken();
                String tokenId = instanceIdResult.getId();

                // usually new token is only generated when the app is reinstalled or the data is cleared
                Log.d(TAG, "FCM Token: " + newToken);

                // a new FCM token was generated
                final String oldToken = SeptaServiceFactory.getNotificationsService().getRegistrationToken(getApplicationContext());
                if (!oldToken.isEmpty() && !oldToken.equals(newToken)) {

                    // save registration token
                    SeptaServiceFactory.getNotificationsService().setRegistrationToken(getApplicationContext(), newToken);

                    // register new FCM token
                    PushNotificationManager.updateNotifSubscription(getApplicationContext(), new Runnable() {
                        @Override
                        public void run() {
                            CrashlyticsManager.log(Log.ERROR, TAG, "Could not register new FCM token: " + newToken + " and unregister old FCM token: " + oldToken);

                            SeptaServiceFactory.getNotificationsService().setNotifPrefsSaved(getApplicationContext(), false);
                        }
                    });

                } else if (oldToken.isEmpty()) {
                    Log.d(TAG, "Saving FCM token for first time");

                    // save registration token
                    SeptaServiceFactory.getNotificationsService().setRegistrationToken(getApplicationContext(), newToken);
                }

                Log.d(TAG, "Instance ID: " + tokenId); // this is a shortcutted version of the token
            }
        });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, remoteMessage.toString());
        Map<String, String> notifData = new HashMap<>();

        if (remoteMessage.getData() != null && remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();

            // get notification title / body
            final NotificationType notificationType = NotificationType.valueOf(data.get(NOTIFICATION_KEY_TYPE));
            final String message = data.get(NOTIFICATION_KEY_MESSAGE);

            // send notification if special announcement
            if (notificationType == NotificationType.SPECIAL_ANNOUNCEMENT) {
                PushNotificationManager.getInstance(getApplicationContext()).buildSpecialAnnouncementNotification(getApplicationContext(), message);

            } else {
                // get route ID for route specific alerts
                final String routeId = data.get(NOTIFICATION_KEY_ROUTE_ID);
                notifData.put("Push Notif Shown - Route ID", routeId);

                // get transit type
                TransitType transitType = TransitType.valueOf(data.get(NOTIFICATION_KEY_TRANSIT_TYPE));
                notifData.put("Push Notif Shown - Transit Type", String.valueOf(transitType));

                // send notification if rail delay
                if (notificationType == NotificationType.DELAY) {
                    // handle the data message
                    final String vehicleId = data.get(NOTIFICATION_KEY_VEHICLE_ID);
                    final String destinationStopId = data.get(NOTIFICATION_KEY_DESTINATION_STOP_ID);
                    final DelayNotificationType delayType = DelayNotificationType.valueOf(data.get(NOTIFICATION_KEY_DELAY_TYPE));

                    PushNotificationManager.getInstance(getApplicationContext()).buildRailDelayNotification(getApplicationContext(), message, routeId, vehicleId, destinationStopId, delayType);
                } else {
                    // send system status push notification
                    if (notificationType == NotificationType.ALERT || notificationType == NotificationType.DETOUR) {
                        PushNotificationManager.getInstance(getApplicationContext()).buildSystemStatusNotification(getApplicationContext(), notificationType, message, transitType, routeId);
                    }
                }
            }

            // analytics push notif shown to user
            notifData.put("Push Notif Shown - Notification Type", String.valueOf(notificationType));
            AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_PUSH_NOTIF_SHOWN_TO_USER, AnalyticsManager.CUSTOM_EVENT_ID_NOTIFICATION_ENGAGEMENT, notifData);
        }

        // analytics push notif received
        AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_PUSH_NOTIF_RECEIVED, AnalyticsManager.CUSTOM_EVENT_ID_NOTIFICATION_ENGAGEMENT, null);
    }

    private boolean isAppInForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private Date addHoursToDate(Date date, int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, hours);
        return calendar.getTime();
    }
}
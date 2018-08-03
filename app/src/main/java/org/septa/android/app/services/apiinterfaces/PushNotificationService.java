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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
                String token = instanceIdResult.getToken();
                // Do whatever you want with your token now
                // i.e. store it on SharedPreferences or DB
                // or directly send it to server


                //for now we are displaying the token in the log
                //copy it as this method is called only when the new token is generated
                //and usually new token is only generated when the app is reinstalled or the data is cleared
                Log.d(TAG, "TOKEN: " + token);
            }
        });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, remoteMessage.toString());

        // check notification subscription window
        if (PushNotificationManager.isWithinNotificationWindow(getApplicationContext())) {

            if (remoteMessage.getData().size() > 0) {
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

                    // send notification if rail delay
                    if (notificationType == NotificationType.DELAY) {
                        // handle the data message
                        final String vehicleId = data.get(NOTIFICATION_KEY_VEHICLE_ID);
                        final String destinationStopId = data.get(NOTIFICATION_KEY_DESTINATION_STOP_ID);
                        final DelayNotificationType delayType = DelayNotificationType.valueOf(data.get(NOTIFICATION_KEY_DELAY_TYPE));

                        // default expiration date 3 hours from now
                        Date expirationTimeStamp = addHoursToDate(new Date(), 3);

                        // parse expiration date
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.US);
                        try {
                            String expires = data.get(NOTIFICATION_KEY_EXPIRES);
                            expirationTimeStamp = df.parse(expires);
                            Log.d(TAG, "Parsing Expiration Time Stamp: " + expirationTimeStamp);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        PushNotificationManager.getInstance(getApplicationContext()).buildDelayNotification(getApplicationContext(), message, routeId, vehicleId, destinationStopId, delayType, expirationTimeStamp);
                    }

                    // get transit type
                    TransitType transitType = TransitType.valueOf(data.get(NOTIFICATION_KEY_TRANSIT_TYPE));

                    if (notificationType == NotificationType.ALERT) {
                        PushNotificationManager.getInstance(getApplicationContext()).buildServiceAlertNotification(getApplicationContext(), message, transitType, routeId);
                    } else if (notificationType == NotificationType.DETOUR) {
                        // TODO: parse out other values for detour
                        PushNotificationManager.getInstance(getApplicationContext()).buildDetourNotification(getApplicationContext(), message, transitType, routeId);
                    }
                }
            }
        }
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
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
import org.septa.android.app.notifications.NotificationType;
import org.septa.android.app.notifications.PushNotificationManager;

import java.util.List;
import java.util.Map;

public class PushNotificationService extends FirebaseMessagingService {

    private static final String TAG = PushNotificationService.class.getSimpleName();

    private static final String NOTIFICATION_KEY_TYPE = "notificationType",
            NOTIFICATION_KEY_ROUTE_ID = "routeId",
            NOTIFICATION_KEY_TRANSIT_TYPE = "transitType",
            NOTIFICATION_KEY_MESSAGE = "message";

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
                final String routeId = data.get(NOTIFICATION_KEY_ROUTE_ID);

                TransitType transitType = null;
                if (notificationType == NotificationType.DELAY) {
                    transitType = TransitType.RAIL;
                } else if (notificationType == NotificationType.ALERT || notificationType == NotificationType.DETOUR) {
                    transitType = TransitType.valueOf(data.get(NOTIFICATION_KEY_TRANSIT_TYPE));
                }
                // TODO: handle the data message here

                // send notification
                PushNotificationManager.getInstance(getApplicationContext()).displayNotification(getApplicationContext(), notificationType, transitType, message, routeId);
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
}
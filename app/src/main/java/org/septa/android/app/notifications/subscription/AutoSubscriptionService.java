package org.septa.android.app.notifications.subscription;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.septa.android.app.notifications.PushNotificationManager;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.support.CrashlyticsManager;

public class AutoSubscriptionService extends IntentService {

    private static final String TAG = AutoSubscriptionService.class.getSimpleName();

    public AutoSubscriptionService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PushNotificationManager.updateNotifSubscription(getApplicationContext(), new Runnable() {
            @Override
            public void run() {
                String regToken = SeptaServiceFactory.getNotificationsService().getRegistrationToken(getApplicationContext());
                String deviceId = SeptaServiceFactory.getNotificationsService().getDeviceId(getApplicationContext());

                CrashlyticsManager.log(Log.ERROR, TAG, "Unable to auto subscribe device ID: " + deviceId + " with FCM token: " + regToken);
            }
        });
    }
}

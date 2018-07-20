package org.septa.android.app.services.apiinterfaces;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.septa.android.app.notifications.PushNotificationManager;

import java.util.List;

public class PushNotificationService extends FirebaseMessagingService {

    private static final String TAG = PushNotificationService.class.getSimpleName();

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

        Log.d(TAG, remoteMessage.getNotification().toString());

        //if the message contains data payload
        //It is a map of custom keyvalues
        //we can read it easily

        if (remoteMessage.getData().size() > 0) {
            // TODO: handle the data message here
        }

        // get notification title / body
        final String title = remoteMessage.getNotification().getTitle();
        final String body = remoteMessage.getNotification().getBody();

        // check notification subscription window
        if (PushNotificationManager.isWithinNotificationWindow(getApplicationContext())) {

            // display notification
            if (isAppInForeground(getPackageName())) {
                // show toast if app open
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Notification: " + body, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                // send notification if app backgrounded
                PushNotificationManager.getInstance(getApplicationContext()).displayNotification(title, body);

            }
        }

    }

    public boolean isAppInForeground(String packageName) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        return componentInfo.getPackageName().equals(packageName);
    }
}
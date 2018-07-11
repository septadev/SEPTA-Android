package org.septa.android.app.services.apiinterfaces;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.septa.android.app.notifications.PushNotificationManager;

import java.util.List;

public class SEPTAFirebaseMessagingService extends FirebaseMessagingService {

    //this method will be called
    //when the token is generated
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        //now we will have the token
        String token = FirebaseInstanceId.getInstance().getToken();

        //for now we are displaying the token in the log
        //copy it as this method is called only when the new token is generated
        //and usually new token is only generated when the app is reinstalled or the data is cleared
        Log.d("MyRefreshedToken", token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d("MyNotification", remoteMessage.getNotification().toString());
        Log.d("MyNotification", remoteMessage.getNotification().toString());

        //if the message contains data payload
        //It is a map of custom keyvalues
        //we can read it easily

        if (remoteMessage.getData().size() > 0) {
            //handle the data message here
        }

        //getting the title and the body
        final String title = remoteMessage.getNotification().getTitle();
        final String body = remoteMessage.getNotification().getBody();

        //then here we can use the title and body to build a notification

        Log.d("MyNotification", remoteMessage.getNotification().toString());
        Log.d("MyNotification", remoteMessage.getNotification().toString());

        if (!isForeground("org.septa.android.app")) {
            PushNotificationManager.getInstance(getApplicationContext()).displayNotification(title, body);
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), "Notification:" + body, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public boolean isForeground(String myPackage) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        return componentInfo.getPackageName().equals(myPackage);
    }
}
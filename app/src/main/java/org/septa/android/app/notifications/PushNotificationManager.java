package org.septa.android.app.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import org.septa.android.app.MainActivity;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;

import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;

public class PushNotificationManager {

    private static final String TAG = PushNotificationManager.class.getSimpleName();

    private Context context;
    private static PushNotificationManager mInstance;

    private static final String CHANNEL_ID = "SEPTA_PUSH_NOTIFICATIONS";
    private static final String TOPIC_PREFIX = "TOPIC_";
    private static final String TOPIC_DELIM = "_";

    private static final String SPECIAL_ANNOUNCEMENTS = "SPECIAL_ANNOUNCEMENTS";
    private static final String SERVICE_ALERT_SUFFIX = "_ALERT";
    private static final String RAIL_DELAY_SUFFIX = "_DELAY";
    private static final String DETOUR_SUFFIX = "_DETOUR";

    private PushNotificationManager(Context context) {
        this.context = context;
    }

    public static synchronized PushNotificationManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PushNotificationManager(context);
        }
        return mInstance;
    }

    public void displayNotification(String title, String body) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(body);

        /*
         *  Clicking on the notification will take us to this intent
         *  Right now we are using the MainActivity as this is the only activity we have in our application
         *  But for your project you can customize it as you want
         * */

        Intent resultIntent = new Intent(context, MainActivity.class);

        /*
         *  Now we will create a pending intent
         *  The method getActivity is taking 4 parameters
         *  All paramters are describing themselves
         *  0 is the request code (the second parameter)
         *  We can detect this code in the activity that will open by this we can get
         *  Which notification opened the activity
         * */
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        /*
         *  Setting the pending intent to notification builder
         */
        mBuilder.setContentIntent(pendingIntent);

        // add sound / vibrate based on current user settings
        mBuilder.setDefaults(Notification.DEFAULT_ALL);

        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        /*
         * The first parameter is the notification id
         * better don't give a literal here (right now we are giving a int literal)
         * because using this id we can modify it later
         * */
        if (mNotifyMgr != null) {
            mNotifyMgr.notify(1, mBuilder.build());
        }
    }

    public void unsubscribeFromAllTopics() {
        SeptaServiceFactory.getNotificationsService().setNotificationsEnabled(context, false);

        Toast.makeText(context, "Unsubscribing from Push Notifs", Toast.LENGTH_SHORT).show(); // TODO: remove

        // unsubscribe from all topics
        List<String> topicsToUnsubscribeFrom = SeptaServiceFactory.getNotificationsService().getTopicsSubscribedTo(context);
        for (String topicId : topicsToUnsubscribeFrom) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topicId);
        }

        // topics subscribed to are still remembered because shared preferences untouched

        // unsubscribe from test push notifications // TODO: remove
        FirebaseMessaging.getInstance().unsubscribeFromTopic("notifications");
    }

    public void resubscribeToTopics() {
        SeptaServiceFactory.getNotificationsService().setNotificationsEnabled(context, true);

        // TODO: check system permissions for receiving notifications from the app
        // TODO: if permissions not granted show pop up saying to enable in settings
        // TODO: give button to link to notification settings

        Toast.makeText(context, "Resubscribing to Push Notifs", Toast.LENGTH_SHORT).show(); // TODO: remove

        // resubscribe user to their previously saved topics
        List<String> topicsToSubscribeTo = SeptaServiceFactory.getNotificationsService().getTopicsSubscribedTo(context);
        for (String topicId : topicsToSubscribeTo) {
            FirebaseMessaging.getInstance().subscribeToTopic(topicId);
        }

        // subscribe to test push notifications // TODO: remove
        FirebaseMessaging.getInstance().subscribeToTopic("notifications");

    }

    public void subscribeToSpecialAnnouncements() {
        Log.d(TAG, "Subscribing to SEPTA Special Announcements");
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_PREFIX + SPECIAL_ANNOUNCEMENTS);
        SeptaServiceFactory.getNotificationsService().setSpecialAnnouncementsEnabled(context, true);
    }

    public void unsubscribeFromSpecialAnnouncements() {
        Log.d(TAG, "Unsubscribing from SEPTA Special Announcements");
        FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC_PREFIX + SPECIAL_ANNOUNCEMENTS);
        SeptaServiceFactory.getNotificationsService().setSpecialAnnouncementsEnabled(context, false);
    }

    public void subscribeToRoute(String routeId, TransitType transitType) {
        routeId = routeId.toUpperCase();
        Log.d(TAG, "Subscribing to alerts for route: " + routeId);

        if (transitType == TransitType.RAIL) {
            // subscribe to rail delays
            String railDelayTopicId = TOPIC_PREFIX + routeId + RAIL_DELAY_SUFFIX;
            subscribeToTopic(railDelayTopicId);

        } else if (transitType == TransitType.BUS || transitType == TransitType.TROLLEY) {
            // subscribe to bus or trolley detours
            String detourTopicId = TOPIC_PREFIX + routeId + DETOUR_SUFFIX;
            subscribeToTopic(detourTopicId);
        }

        // subscribe to all service alerts
        String serviceAlertTopicId = TOPIC_PREFIX + routeId + SERVICE_ALERT_SUFFIX;
        subscribeToTopic(serviceAlertTopicId);
    }

    public void unsubscribeFromRoute(String routeId, TransitType transitType) {
        routeId = routeId.toUpperCase();
        Log.d(TAG, "Unsubscribing from alerts for route: " + routeId);

        if (transitType == TransitType.RAIL) {
            // unsubscribe from rail delays
            String railDelayTopicId = TOPIC_PREFIX + routeId + RAIL_DELAY_SUFFIX;
            unsubscribeFromTopic(railDelayTopicId);

        } else if (transitType == TransitType.BUS || transitType == TransitType.TROLLEY) {
            // unsubscribe from bus or trolley detours
            String detourTopicId = TOPIC_PREFIX + routeId + DETOUR_SUFFIX;
            unsubscribeFromTopic(detourTopicId);
        }

        // unsubscribe from all service alerts
        String serviceAlertTopicId = TOPIC_PREFIX + routeId + SERVICE_ALERT_SUFFIX;
        unsubscribeFromTopic(serviceAlertTopicId);
    }

    private void unsubscribeFromTopic(String topicId) {
        // unsubscribe from Firebase topic
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topicId);

        // remove topic from shared preferences
        SeptaServiceFactory.getNotificationsService().removeTopicSubscription(context, topicId);
    }

    private void subscribeToTopic(String topicId) {
        // subscribe to firebase topic
        FirebaseMessaging.getInstance().subscribeToTopic(topicId);

        // add topic to shared preferences
        SeptaServiceFactory.getNotificationsService().addTopicSubscription(context, topicId);
    }
}
package org.septa.android.app.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

import org.septa.android.app.Constants;
import org.septa.android.app.MainActivity;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.RouteNotificationSubscription;
import org.septa.android.app.systemstatus.SystemStatusResultsActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;

public class PushNotificationManager {

    private static final String TAG = PushNotificationManager.class.getSimpleName();

    private Context context;
    private static PushNotificationManager mInstance;

    private static final String CHANNEL_ID = "SEPTA_PUSH_NOTIFICATIONS";
    private static final String TOPIC_PREFIX = "TOPIC_";

    private static final String SPECIAL_ANNOUNCEMENTS = "SPECIAL_ANNOUNCEMENTS";
    private static final String SERVICE_ALERT_SUFFIX = "_ALERT";
    private static final String RAIL_DELAY_SUFFIX = "_DELAY";
    private static final String DETOUR_SUFFIX = "_DETOUR";

    private static final DateFormat timeFormat = new SimpleDateFormat("HHmm");

    private PushNotificationManager(Context context) {
        this.context = context;
    }

    public static synchronized PushNotificationManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PushNotificationManager(context);
        }
        return mInstance;
    }

    public static boolean isWithinNotificationWindow(Context context) {
        List<Integer> daysEnabled = SeptaServiceFactory.getNotificationsService().getNotificationsSchedule(context);

        // check day of week
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        if (daysEnabled.contains(day)) {

            // parse multiple timeframes
            List<String> timeFrames = SeptaServiceFactory.getNotificationsService().getNotificationTimeFrames(context);

            // check against time frames
            for (String window : timeFrames) {
                String[] startEndTimes = window.split(NotificationsSharedPrefsUtilsImpl.START_END_TIME_DELIM);
                int startTime = Integer.parseInt(startEndTimes[0]);
                int endTime = Integer.parseInt(startEndTimes[1]);

                // get current time in 24H format
                int currentTime = Integer.parseInt(timeFormat.format(calendar.getTime()));
                if (currentTime >= startTime && currentTime <= endTime) {
                    return true;
                }
            }
        }

        return false;
    }

    public void displayNotification(Context context, NotificationType notificationType, TransitType transitType, String message, String routeId) {
        /*
         *  Clicking on the notification will take us to this intent
         *  Right now we are using the MainActivity as this is the only activity we have in our application
         *  But for your project you can customize it as you want
         * */

        Intent resultIntent = new Intent(context, MainActivity.class);
        int requestCode = 0;

        // TODO: set click action based on notificationType
        // TODO: fix request code
        String title = null;
        switch (notificationType) {
            case SPECIAL_ANNOUNCEMENT:
                title = context.getString(R.string.push_notif_special_announcement_title);
                break;

            case ALERT:
                title = context.getString(R.string.push_notif_alert_title, routeId);

                String routeName = routeId;
                if (transitType == TransitType.RAIL) {
                    // TODO: look up line name for rail
                }

                // tapping on service alert will open system status page
                resultIntent = new Intent(context, SystemStatusResultsActivity.class);
                resultIntent.putExtra(Constants.ROUTE_NAME, routeName);
                resultIntent.putExtra(Constants.ROUTE_ID, routeId);
                resultIntent.putExtra(Constants.TRANSIT_TYPE, transitType);
                resultIntent.putExtra(Constants.SERVICE_ALERT_EXPANDED, Boolean.TRUE);
                requestCode = Constants.SYSTEM_STATUS_REQUEST;
                break;

            case DELAY:
                title = context.getString(R.string.push_notif_rail_delay_title, routeId);
                break;

            case DETOUR:
                title = context.getString(R.string.push_notif_detour_title, routeId);

                // tapping on service alert will open system status page
                resultIntent = new Intent(context, SystemStatusResultsActivity.class);
                resultIntent.putExtra(Constants.ROUTE_NAME, routeId);
                resultIntent.putExtra(Constants.ROUTE_ID, routeId);
                resultIntent.putExtra(Constants.TRANSIT_TYPE, transitType);
                resultIntent.putExtra(Constants.ACTIVE_DETOUR_EXPANDED, Boolean.TRUE);
                requestCode = Constants.SYSTEM_STATUS_REQUEST;
                break;
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(message);

        // tapping special announcements doesn't open app
        if (notificationType != NotificationType.SPECIAL_ANNOUNCEMENT) {
            /*
             *  Now we will create a pending intent
             *  The method getActivity is taking 4 parameters
             *  All paramters are describing themselves
             *  0 is the request code (the second parameter)
             *  We can detect this code in the activity that will open by this we can get
             *  Which notification opened the activity
             * */

            // build back stack
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            // This uses android:parentActivityName and
            // android.support.PARENT_ACTIVITY meta-data by default
            stackBuilder.addNextIntentWithParentStack(resultIntent);

            PendingIntent pendingIntent = stackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT);

            /*
             *  Setting the pending intent to notification builder
             */
            mBuilder.setContentIntent(pendingIntent);
        }

        // dismiss notification on click
        mBuilder.setAutoCancel(true);

        // add sound / vibrate based on current user settings
        mBuilder.setDefaults(Notification.DEFAULT_ALL);

        // use timestamp to create unique notif ID
        int notifId = (int) System.currentTimeMillis();

        /*
         * The first parameter is the notification id
         * better don't give a literal here (right now we are giving a int literal)
         * because using this id we can modify it later
         * */
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (mNotifyMgr != null) {
            mNotifyMgr.notify(notifId, mBuilder.build());
        }
    }

    public void unsubscribeFromAllTopics() {
        SeptaServiceFactory.getNotificationsService().setNotificationsEnabled(context, false);

        // unsubscribe from all topics
        List<RouteNotificationSubscription> routesToUnsubscribeFrom = SeptaServiceFactory.getNotificationsService().getRoutesSubscribedTo(context);
        for (RouteNotificationSubscription route : routesToUnsubscribeFrom) {
            unsubscribeFromRoute(route.getRouteId(), route.getTransitType());
        }

        // topics subscribed to are still remembered because shared preferences untouched

        // unsubscribe from test push notifications // TODO: remove
        FirebaseMessaging.getInstance().unsubscribeFromTopic("notifications");
    }

    public void resubscribeToTopics() {
        SeptaServiceFactory.getNotificationsService().setNotificationsEnabled(context, true);

        // resubscribe user to their previously saved topics
        List<RouteNotificationSubscription> routesSubscribedTo = SeptaServiceFactory.getNotificationsService().getRoutesSubscribedTo(context);
        for (RouteNotificationSubscription route : routesSubscribedTo) {
            if (route.isEnabled()) {
                subscribeToRoute(route.getRouteId(), route.getTransitType());
            }
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

    private void subscribeToRoute(String routeId, TransitType transitType) {
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

    private void unsubscribeFromRoute(String routeId, TransitType transitType) {
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
    }

    private void subscribeToTopic(String topicId) {
        // subscribe to firebase topic
        FirebaseMessaging.getInstance().subscribeToTopic(topicId);
    }

    public void createNotificationForRoute(String routeId, String routeName, TransitType transitType) {
        // make NHSL chosen from trolley picker have NHSL transittype
        if ("NHSL".equalsIgnoreCase(routeId)) {
            transitType = TransitType.NHSL;
        }

        // add route to subscription list
        List<RouteNotificationSubscription> notificationSubscriptions = SeptaServiceFactory.getNotificationsService().getRoutesSubscribedTo(context);
        RouteNotificationSubscription routeToSubscribeTo = new RouteNotificationSubscription(routeId, routeName, transitType);

        if (notificationSubscriptions.contains(routeToSubscribeTo)) {
            // turn notifications on for that route
            SeptaServiceFactory.getNotificationsService().toggleRouteSubscription(context, routeId, true);

        } else {
            // create new route subscription
            SeptaServiceFactory.getNotificationsService().addRouteSubscription(context, routeToSubscribeTo);
        }

        subscribeToRoute(routeId, transitType);
    }

    public void removeNotificationForRoute(String routeId, TransitType transitType) {
        // make NHSL chosen from trolley picker have NHSL transittype
        if ("NHSL".equalsIgnoreCase(routeId)) {
            transitType = TransitType.NHSL;
        }

        // remember route but toggle notifications off
        SeptaServiceFactory.getNotificationsService().toggleRouteSubscription(context, routeId, false);

        unsubscribeFromRoute(routeId, transitType);
    }

    public void deleteNotificationForRoute(String routeId, TransitType transitType) {
        // make NHSL chosen from trolley picker have NHSL transittype
        if ("NHSL".equalsIgnoreCase(routeId)) {
            transitType = TransitType.NHSL;
        }

        // delete notification
        SeptaServiceFactory.getNotificationsService().removeRouteSubscription(context, routeId);

        unsubscribeFromRoute(routeId, transitType);
    }

}
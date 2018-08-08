package org.septa.android.app.notifications;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import org.septa.android.app.Constants;
import org.septa.android.app.MainActivity;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.database.DatabaseManager;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.nextarrive.NextToArriveTripDetailActivity;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalDetails;
import org.septa.android.app.services.apiinterfaces.model.RouteNotificationSubscription;
import org.septa.android.app.support.CrashlyticsManager;
import org.septa.android.app.support.Criteria;
import org.septa.android.app.support.CursorAdapterSupplier;
import org.septa.android.app.systemstatus.SystemStatusResultsActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.NOTIFICATION_SERVICE;

public class PushNotificationManager {

    private static final String TAG = PushNotificationManager.class.getSimpleName();

    private Context context;
    private static PushNotificationManager mInstance;

    private static final String CHANNEL_ID = "SEPTA_PUSH_NOTIFICATIONS";
    private static final String TOPIC_PREFIX = "TOPIC_";

    private static final String SEPTA_ANNOUNCEMENTS = "SEPTA_ANNOUNCEMENTS";
    private static final String SERVICE_ALERT_SUFFIX = "_ALERT";
    private static final String RAIL_DELAY_SUFFIX = "_DELAY";
    private static final String DETOUR_SUFFIX = "_DETOUR";

    private static final DateFormat timeFormat = new SimpleDateFormat("HHmm", Locale.US);

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

    public void buildSpecialAnnouncementNotification(Context context, String message) {
        String title = context.getString(R.string.push_notif_special_announcement_title);
        displayNotification(context, title, message, null);
    }

    public void buildSystemStatusNotification(Context context, NotificationType notificationType, String message, TransitType transitType, String routeId) {
        String title = null;
        String routeName = routeId;

        Intent resultIntent = new Intent(context, MainActivity.class);

        if (notificationType == NotificationType.ALERT) {
            // build service alert
            title = context.getString(R.string.push_notif_alert_title, routeId);
            resultIntent.putExtra(Constants.REQUEST_CODE, Constants.PUSH_NOTIF_REQUEST_SERVICE_ALERT);

            // look up line name for rail
            if (transitType == TransitType.RAIL) {
                routeName = getRailRouteName(context, routeId);
            }
        } else if (notificationType == NotificationType.DETOUR) {
            // build detour
            title = context.getString(R.string.push_notif_detour_title, routeId);
            resultIntent.putExtra(Constants.REQUEST_CODE, Constants.PUSH_NOTIF_REQUEST_DETOUR);
        }

        // tapping on push notification will open main activity
        resultIntent.putExtra(Constants.ROUTE_NAME, routeName);
        resultIntent.putExtra(Constants.ROUTE_ID, routeId);
        resultIntent.putExtra(Constants.TRANSIT_TYPE, transitType);

        // build back stack for click action
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        displayNotification(context, title, message, pendingIntent);
    }

    public void buildRailDelayNotification(final Context context, String message, final String routeId, final String vehicleId, String destinationStopId, DelayNotificationType delayType, Date expirationTimeStamp) {
        String title = context.getString(R.string.push_notif_rail_delay_title, routeId);

        // estimated delay notifications don't link anywhere in app, actual ones do
        PendingIntent pendingIntent = null;

        if (delayType == DelayNotificationType.ACTUAL) {
            // all delay notifications are rail
            final TransitType transitType = TransitType.RAIL;

            // look up destination stop
            StopModel destStop = getRailStop(context, destinationStopId);

            // look up routeName
            String routeName = getRailRouteName(context, routeId);

            // build intent for click action
            final Intent resultIntent = new Intent(context, MainActivity.class);
            resultIntent.putExtra(Constants.REQUEST_CODE, Constants.PUSH_NOTIF_REQUEST_RAIL_DELAY);
            resultIntent.putExtra(Constants.DESTINATION_STATION, destStop);
            resultIntent.putExtra(Constants.DESTINATION_STOP_ID, destinationStopId);
            resultIntent.putExtra(Constants.VEHICLE_ID, vehicleId);
            resultIntent.putExtra(Constants.ROUTE_ID, routeId);
            resultIntent.putExtra(Constants.ROUTE_NAME, routeName);
            resultIntent.putExtra(Constants.TRANSIT_TYPE, transitType);
            resultIntent.putExtra(Constants.EXPIRATION_TIMESTAMP, expirationTimeStamp);

            // build back stack for click action
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntentWithParentStack(resultIntent);
            pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        displayNotification(context, title, message, pendingIntent);
    }

    private void displayNotification(Context context, String title, String message, PendingIntent pendingIntent) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        // add click action
        if (pendingIntent != null) {
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

    private static void showNotificationExpiredMessage(Context context) {
        Toast.makeText(context, R.string.notification_expired, Toast.LENGTH_LONG).show();
    }

    public static void onSystemStatusNotificationClick(Activity activity, Intent intent, String expandedAlert) {
        final String routeId = intent.getStringExtra(Constants.ROUTE_ID);
        final String routeName = intent.getStringExtra(Constants.ROUTE_NAME);

        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            final TransitType transitType = (TransitType) bundle.get(Constants.TRANSIT_TYPE);

            // tapping on service alert notif will open system status results
            Intent resultIntent = new Intent(activity, SystemStatusResultsActivity.class);
            resultIntent.putExtra(Constants.ROUTE_NAME, routeName);
            resultIntent.putExtra(Constants.ROUTE_ID, routeId);
            resultIntent.putExtra(Constants.TRANSIT_TYPE, transitType);
            resultIntent.putExtra(expandedAlert, Boolean.TRUE);

            activity.startActivityForResult(resultIntent, Constants.SYSTEM_STATUS_REQUEST);
        } else {
            CrashlyticsManager.log(Log.ERROR, TAG, "Null intent bundle after tapping system status push notification for route " + routeId);
        }
    }

    public static void onRailDelayNotificationClick(final Activity activity, Intent intent) {
        String destinationStopId = intent.getStringExtra(Constants.DESTINATION_STOP_ID);
        final String routeId = intent.getStringExtra(Constants.ROUTE_ID);
        final String vehicleId = intent.getStringExtra(Constants.VEHICLE_ID);
        final String routeName = intent.getStringExtra(Constants.ROUTE_NAME);

        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            final StopModel destStop = (StopModel) bundle.get(Constants.DESTINATION_STATION);
            final TransitType transitType = (TransitType) bundle.get(Constants.TRANSIT_TYPE);
            Date expirationTimestamp = (Date) bundle.get(Constants.EXPIRATION_TIMESTAMP);

            // show notification expired message
            if (new Date().after(expirationTimestamp)) {
                showNotificationExpiredMessage(activity);

            } else {
                // to get train details, don't pass route ID to the API call
                SeptaServiceFactory.getNextArrivalService().getNextArrivalDetails(destinationStopId, null, vehicleId).enqueue(new Callback<NextArrivalDetails>() {
                    @Override
                    public void onResponse(@NonNull Call<NextArrivalDetails> call, @NonNull Response<NextArrivalDetails> response) {
                        NextArrivalDetails responseBody = response.body();

                        if (responseBody != null && responseBody.getResults() > 0) {
                            Intent intent = new Intent(activity, NextToArriveTripDetailActivity.class);

                            intent.putExtra(Constants.DESTINATION_STATION, destStop);
                            intent.putExtra(Constants.TRANSIT_TYPE, transitType);
                            intent.putExtra(Constants.ROUTE_NAME, routeName);
                            intent.putExtra(Constants.ROUTE_ID, routeId);
                            intent.putExtra(Constants.TRIP_ID, vehicleId);
                            // startingStation, vehicle ID, routeDescription will be null coming from a rail delay push notification

                            activity.startActivity(intent);
                        } else {
                            CrashlyticsManager.log(Log.ERROR, TAG, "Null response body when attempting to jump to train details from push notification");
                            showNotificationExpiredMessage(activity);
                        }
                    }

                    @Override
                    public void onFailure(Call<NextArrivalDetails> call, Throwable t) {
                        showNotificationExpiredMessage(activity);
                    }
                });
            }
        } else {
            CrashlyticsManager.log(Log.ERROR, TAG, "Null intent bundle after tapping rail delay push notification for route " + routeId);
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
    }

    public void subscribeToSpecialAnnouncements() {
        Log.d(TAG, "Subscribing to SEPTA Special Announcements");
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_PREFIX + SEPTA_ANNOUNCEMENTS);
        SeptaServiceFactory.getNotificationsService().setSpecialAnnouncementsEnabled(context, true);
    }

    public void unsubscribeFromSpecialAnnouncements() {
        Log.d(TAG, "Unsubscribing from SEPTA Special Announcements");
        FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC_PREFIX + SEPTA_ANNOUNCEMENTS);
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

    private String getRailRouteName(Context context, String routeId) {
        DatabaseManager dbManager = DatabaseManager.getInstance(context);
        CursorAdapterSupplier<RouteDirectionModel> routeCursorAdapterSupplier = dbManager.getRailNoDirectionRouteCursorAdapterSupplier();
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(new Criteria("routeId", Criteria.Operation.EQ, routeId));
        Cursor cursor = routeCursorAdapterSupplier.getCursor(context, criteriaList);

        String routeName = routeId; // default route name to route ID
        if (cursor.moveToFirst()) {
            do {
                RouteDirectionModel route = routeCursorAdapterSupplier.getCurrentItemFromCursor(cursor);
                if (routeId.equals(route.getRouteId())) {
                    routeName = route.getRouteShortName();
                    break;
                }
            } while (cursor.moveToNext());
        } else {
            Log.e(TAG, "Could not find rail route name for route ID " + routeId);
        }
        return routeName;
    }

    private StopModel getRailStop(Context context, String stopId) {
        DatabaseManager dbManager = DatabaseManager.getInstance(context);
        CursorAdapterSupplier<StopModel> stopModelCursorAdapterSupplier = dbManager.getRailStopCursorAdapterSupplier();
        StopModel stopModel = stopModelCursorAdapterSupplier.getItemFromId(context, stopId);
        if (stopModel == null) {
            Log.e(TAG, "Could not find rail stop for stop ID " + stopId);
        }
        return stopModel;
    }

}
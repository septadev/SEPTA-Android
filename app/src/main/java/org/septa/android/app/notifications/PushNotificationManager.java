package org.septa.android.app.notifications;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.septa.android.app.Constants;
import org.septa.android.app.MainActivity;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.database.DatabaseManager;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.nextarrive.NextToArriveTripDetailActivity;
import org.septa.android.app.notifications.subscription.AutoSubscriptionReceiver;
import org.septa.android.app.notifications.subscription.RefreshAdvertisingId;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalDetails;
import org.septa.android.app.services.apiinterfaces.model.PushNotifSubscriptionRequest;
import org.septa.android.app.services.apiinterfaces.model.PushNotifSubscriptionResponse;
import org.septa.android.app.services.apiinterfaces.model.RouteNotifSubscription;
import org.septa.android.app.services.apiinterfaces.model.RouteSubscription;
import org.septa.android.app.services.apiinterfaces.model.TimeSlot;
import org.septa.android.app.support.AnalyticsManager;
import org.septa.android.app.support.CrashlyticsManager;
import org.septa.android.app.support.Criteria;
import org.septa.android.app.support.CursorAdapterSupplier;
import org.septa.android.app.support.GeneralUtils;
import org.septa.android.app.systemstatus.SystemStatusResultsActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.NOTIFICATION_SERVICE;

public class PushNotificationManager {

    private static final String TAG = PushNotificationManager.class.getSimpleName();

    private Context context;
    private static PushNotificationManager mInstance;

    private static final String CHANNEL_ID = "SEPTA_PUSH_NOTIFICATIONS";

    private PushNotificationManager(Context context) {
        this.context = context;
    }

    public static synchronized PushNotificationManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PushNotificationManager(context);
        }
        return mInstance;
    }

    public static void refreshFCMToken(final Context context) {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                final String newToken = instanceIdResult.getToken();
                String tokenId = instanceIdResult.getId();

                // usually new token is only generated when the app is reinstalled or the data is cleared
                Log.d(TAG, "FCM Token: " + newToken);

                // a new FCM token was generated
                final String oldToken = SeptaServiceFactory.getNotificationsService().getRegistrationToken(context);
                if (!oldToken.isEmpty() && !oldToken.equals(newToken)) {

                    // save registration token
                    SeptaServiceFactory.getNotificationsService().setRegistrationToken(context, newToken);

                    // register new FCM token
                    PushNotificationManager.updateNotifSubscription(context, new Runnable() {
                        @Override
                        public void run() {
                            CrashlyticsManager.log(Log.ERROR, TAG, "Could not register new FCM token: " + newToken + " and unregister old FCM token: " + oldToken);

                            SeptaServiceFactory.getNotificationsService().setNotifPrefsSaved(context, false);
                        }
                    });

                } else if (oldToken.isEmpty()) {
                    Log.d(TAG, "Saving FCM token for first time");

                    // save registration token
                    SeptaServiceFactory.getNotificationsService().setRegistrationToken(context, newToken);
                } else {
                    Log.d(TAG, "No changes to FCM token");
                }

                Log.d(TAG, "Instance ID: " + tokenId); // this is a shortcutted version of the token
            }
        });
    }

    public void buildSpecialAnnouncementNotification(Context context, String message) {
        String title = context.getString(R.string.push_notif_special_announcement_title);

        // create unique notif ID
        int notifId = (int) System.currentTimeMillis();

        // build back stack for click action
        final Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, notifId, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        displayNotification(notifId, context, title, message, pendingIntent);
    }

    public void buildSystemStatusNotification(Context context, NotificationType notificationType, String message, TransitType transitType, String routeId) {
        String title = null;
        String routeName = routeId;

        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (notificationType == NotificationType.ALERT) {
            // build service alert
            title = context.getString(R.string.push_notif_alert_title, routeId.toUpperCase());
            resultIntent.putExtra(Constants.REQUEST_CODE, Constants.PUSH_NOTIF_REQUEST_SERVICE_ALERT);

            // look up line name for rail
            if (transitType == TransitType.RAIL) {
                routeName = getRailRouteName(context, routeId);
            }
        } else if (notificationType == NotificationType.DETOUR) {
            // build detour
            title = context.getString(R.string.push_notif_detour_title, routeId.toUpperCase());
            resultIntent.putExtra(Constants.REQUEST_CODE, Constants.PUSH_NOTIF_REQUEST_DETOUR);
        } else {
            CrashlyticsManager.log(Log.ERROR, TAG, "Invalid notificationType for System Status notification: " + notificationType);
        }

        // tapping on push notification will open main activity
        resultIntent.putExtra(Constants.ROUTE_NAME, routeName);
        resultIntent.putExtra(Constants.ROUTE_ID, routeId);
        resultIntent.putExtra(Constants.TRANSIT_TYPE, transitType);

        // create unique notif ID
        int notifId = (int) System.currentTimeMillis();

        PendingIntent pendingIntent = PendingIntent.getActivity(context, notifId, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        displayNotification(notifId, context, title, message, pendingIntent);
    }

    public void buildRailDelayNotification(final Context context, String message, final String routeId, final String vehicleId, String destinationStopId, DelayNotificationType delayType) {
        String title = context.getString(R.string.push_notif_rail_delay_title, routeId.toUpperCase());

        // all delay notifications are rail
        final TransitType transitType = TransitType.RAIL;

        final Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // estimated delay notifications don't link anywhere in app, actual ones do
        if (delayType == DelayNotificationType.ACTUAL) {
            // look up destination stop
            StopModel destStop = getRailStop(context, destinationStopId);

            // look up routeName
            String routeName = getRailRouteName(context, routeId);

            // build intent for click action
            resultIntent.putExtra(Constants.REQUEST_CODE, Constants.PUSH_NOTIF_REQUEST_RAIL_DELAY);
            resultIntent.putExtra(Constants.DESTINATION_STATION, destStop);
            resultIntent.putExtra(Constants.DESTINATION_STOP_ID, destinationStopId);
            resultIntent.putExtra(Constants.VEHICLE_ID, vehicleId);
            resultIntent.putExtra(Constants.ROUTE_ID, routeId);
            resultIntent.putExtra(Constants.ROUTE_NAME, routeName);
            resultIntent.putExtra(Constants.TRANSIT_TYPE, transitType);
        }

        // create unique notif ID
        int notifId = (int) System.currentTimeMillis();

        PendingIntent pendingIntent = PendingIntent.getActivity(context, notifId, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        displayNotification(notifId, context, title, message, pendingIntent);
    }

    private void displayNotification(int notifId, Context context, String title, String message, PendingIntent pendingIntent) {

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

        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (mNotifyMgr != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = context.getString(R.string.push_notifications_channel_name);
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                mNotifyMgr.createNotificationChannel(mChannel);
            }

            // send notification to tray
            mNotifyMgr.notify(notifId, mBuilder.build());
        }
    }

    private static void showNotificationExpiredMessage(Context context) {
        Toast.makeText(context, R.string.notification_expired, Toast.LENGTH_LONG).show();
    }

    public static void onSystemStatusNotificationClick(Activity activity, Intent intent, String expandedAlert, NotificationType notificationType) {
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

            // analytics
            Map<String, String> notifData = new HashMap<>();
            notifData.put("Push Notif Clicked - Notification Type", String.valueOf(notificationType));
            notifData.put("Push Notif Clicked - Transit Type", String.valueOf(transitType));
            notifData.put("Push Notif Clicked - Route ID", routeId);
            AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_PUSH_NOTIF_CLICKED, AnalyticsManager.CUSTOM_EVENT_ID_NOTIFICATION_ENGAGEMENT, notifData);

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

            // analytics
            Map<String, String> notifData = new HashMap<>();
            notifData.put("Push Notif Clicked - Notification Type", String.valueOf(NotificationType.DELAY));
            notifData.put("Push Notif Clicked - Transit Type", String.valueOf(transitType));
            notifData.put("Push Notif Clicked - Route ID", routeId);
            AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_PUSH_NOTIF_CLICKED, AnalyticsManager.CUSTOM_EVENT_ID_NOTIFICATION_ENGAGEMENT, notifData);

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
        } else {
            CrashlyticsManager.log(Log.ERROR, TAG, "Null intent bundle after tapping rail delay push notification for route " + routeId);
        }
    }

    public static void updateNotifSubscription(Context context, Runnable failureTask) {
        final PushNotifSubscriptionRequest request = buildSubscriptionRequest(context, null, null, null);
        updateNotifSubscription(context, request, failureTask, null);
    }

    public static void updateNotifSubscription(final Context context, final PushNotifSubscriptionRequest request, final Runnable failureTask, final Runnable successTask) {
        if (SeptaServiceFactory.getNotificationsService().areNotificationsEnabled(context)) {
            RefreshAdvertisingId refreshAdvertisingId = new RefreshAdvertisingId(context, new Runnable() {
                @Override
                public void run() {
                    String deviceId = SeptaServiceFactory.getNotificationsService().getDeviceId(context);
                    if (deviceId.isEmpty()) {
                        CrashlyticsManager.log(Log.ERROR, TAG, "Failed to retrieve advertising ID");
                        displaySubscriptionFailureMessage(context);
                        if (failureTask != null) {
                            failureTask.run();
                        }

                    } else {
                        // use old device ID
                        submitNotifPrefs(context, request, failureTask, successTask);
                    }
                }
            }, new Runnable() {
                @Override
                public void run() {
                    submitNotifPrefs(context, request, failureTask, successTask);
                }
            });
            refreshAdvertisingId.execute();

            // set up auto-subscription
            AutoSubscriptionReceiver.scheduleSubscriptionUpdate(context, false);

        } else {
            removeNotifSubscription(context, failureTask, successTask);
        }
    }

    private static void submitNotifPrefs(final Context context, final PushNotifSubscriptionRequest request, final Runnable failureTask, final Runnable successTask) {
        SeptaServiceFactory.getPushNotificationService().setNotificationSubscription(request).enqueue(new Callback<PushNotifSubscriptionResponse>() {
            @Override
            public void onResponse(Call<PushNotifSubscriptionResponse> call, Response<PushNotifSubscriptionResponse> response) {
                PushNotifSubscriptionResponse responseBody = response.body();

                if (responseBody != null) {
                    boolean success = response.isSuccessful() && responseBody.isSuccess();

                    if (success) {
                        SeptaServiceFactory.getNotificationsService().setNotifPrefsSaved(context, true);

                        Toast.makeText(context, R.string.subscription_success, Toast.LENGTH_SHORT).show();

                        if (successTask != null) {
                            successTask.run();
                        }
                    } else {
                        CrashlyticsManager.log(Log.ERROR, TAG, "Could not update push notification subscription: " + response.message());
                        failureToUpdatePrefs(context, request, failureTask);
                    }
                } else {
                    CrashlyticsManager.log(Log.ERROR, TAG, "Could not update push notification subscription - response body was null: " + response.message());
                    failureToUpdatePrefs(context, request, failureTask);
                }
            }

            @Override
            public void onFailure(Call<PushNotifSubscriptionResponse> call, Throwable t) {
                CrashlyticsManager.log(Log.ERROR, TAG, "Failed to Update Push Notification Subscription Request");
                CrashlyticsManager.logException(TAG, t);
                failureToUpdatePrefs(context, request, failureTask);
            }
        });
    }

    public static void removeNotifSubscription(Context context, Runnable failureTask) {
        removeNotifSubscription(context, failureTask, null);
    }

    public static void removeNotifSubscription(final Context context, final Runnable failureTask, final Runnable successTask) {
        final PushNotifSubscriptionRequest request = buildNullSubscriptionRequest(context);

        SeptaServiceFactory.getPushNotificationService().setNotificationSubscription(request).enqueue(new Callback<PushNotifSubscriptionResponse>() {
            @Override
            public void onResponse(Call<PushNotifSubscriptionResponse> call, Response<PushNotifSubscriptionResponse> response) {
                PushNotifSubscriptionResponse responseBody = response.body();

                if (responseBody != null) {
                    boolean success = response.isSuccessful() && responseBody.isSuccess();

                    if (success) {
                        // cancel auto-subscription
                        AutoSubscriptionReceiver.cancelSubscriptionUpdate(context);

                        SeptaServiceFactory.getNotificationsService().setNotifPrefsSaved(context, true);

                        Toast.makeText(context, R.string.subscription_success, Toast.LENGTH_SHORT).show();

                        if (successTask != null) {
                            successTask.run();
                        }
                    } else {
                        CrashlyticsManager.log(Log.ERROR, TAG, "Could not remove push notification subscription: " + response.message());
                        failureToUpdatePrefs(context, request, failureTask);

                        // continue auto-subscription since this failed
                        AutoSubscriptionReceiver.scheduleSubscriptionUpdate(context, false);
                    }

                } else {
                    CrashlyticsManager.log(Log.ERROR, TAG, "Could not remove push notification subscription - response body was null");
                    failureToUpdatePrefs(context, request, failureTask);

                    // continue auto-subscription since this failed
                    AutoSubscriptionReceiver.scheduleSubscriptionUpdate(context, false);
                }
            }

            @Override
            public void onFailure(Call<PushNotifSubscriptionResponse> call, Throwable t) {
                CrashlyticsManager.log(Log.ERROR, TAG, "Failed to Remove Push Notification Subscription Request");
                CrashlyticsManager.logException(TAG, t);
                failureToUpdatePrefs(context, request, failureTask);

                // continue auto-subscription since this failed
                AutoSubscriptionReceiver.scheduleSubscriptionUpdate(context, false);
            }
        });
    }

    private static void failureToUpdatePrefs(Context context, PushNotifSubscriptionRequest request, Runnable failureTask) {
        CrashlyticsManager.log(Log.ERROR, TAG, request.toString());

        displaySubscriptionFailureMessage(context);
        if (failureTask != null) {
            failureTask.run();
        }
    }

    public static PushNotifSubscriptionRequest buildSubscriptionRequest(Context context, List<Integer> daysEnabled, List<String> timeFrames, List<RouteSubscription> routesSubscribedTo) {

        // pull days of week from shared prefs
        if (daysEnabled == null) {
            daysEnabled = SeptaServiceFactory.getNotificationsService().getNotificationsSchedule(context);
        }

        // send off signal if no days are selected
        if (daysEnabled.isEmpty()) {
            return buildNullSubscriptionRequest(context);
        }

        // generate days of week request format
        int[] daysOfWeek = new int[daysEnabled.size()];
        for (int i = 0; i < daysEnabled.size(); i++) {
            daysOfWeek[i] = daysEnabled.get(i);
        }

        // pull time frames from shared prefs
        if (timeFrames == null) {
            timeFrames = SeptaServiceFactory.getNotificationsService().getNotificationTimeFrames(context);
        }

        // generate request timestamp format
        TimeSlot[] timeSlots = new TimeSlot[timeFrames.size()];
        for (int i = 0; i < timeFrames.size(); i++) {
            String timeFrame = timeFrames.get(i);

            StringBuilder startTime = new StringBuilder(timeFrame.substring(0, 2));
            startTime.append(":").append(timeFrame.substring(2, 4)).append(":00");

            StringBuilder endTime = new StringBuilder(timeFrame.substring(5, 7));
            endTime.append(":").append(timeFrame.substring(7, 9)).append(":00");

            TimeSlot newTimeSlot = new TimeSlot(startTime.toString(), endTime.toString(), daysOfWeek);

            timeSlots[i] = newTimeSlot;
        }

        // pull route subscription list from shared prefs
        if (routesSubscribedTo == null) {
            routesSubscribedTo = SeptaServiceFactory.getNotificationsService().getRoutesSubscribedTo(context);
        }

        // generate route subscription request format
        List<RouteNotifSubscription> temp = new ArrayList<>();
        for (int i = 0; i < routesSubscribedTo.size(); i++) {
            RouteSubscription route = routesSubscribedTo.get(i);
            if (route.isEnabled()) {
                String routeId = route.getRouteId();
                RouteNotifSubscription newRouteSubscription = new RouteNotifSubscription(routeId);
                temp.add(newRouteSubscription);
            }
        }
        RouteNotifSubscription[] routeSubscriptions = new RouteNotifSubscription[temp.size()];
        routeSubscriptions = temp.toArray(routeSubscriptions);

        // pull registration token, device ID, and special announcements from shared prefs
        String regToken = SeptaServiceFactory.getNotificationsService().getRegistrationToken(context);
        String deviceId = SeptaServiceFactory.getNotificationsService().getDeviceId(context);
        boolean specialAnnouncements = SeptaServiceFactory.getNotificationsService().areSpecialAnnouncementsEnabled(context);

        return new PushNotifSubscriptionRequest(deviceId, regToken, specialAnnouncements, timeSlots, routeSubscriptions);
    }

    @NonNull
    private static PushNotifSubscriptionRequest buildNullSubscriptionRequest(Context context) {
        String deviceId = SeptaServiceFactory.getNotificationsService().getDeviceId(context);
        return new PushNotifSubscriptionRequest(deviceId);
    }

    private static void displaySubscriptionFailureMessage(Context context) {
        if (GeneralUtils.isConnectedToInternet(context)) {
            Toast.makeText(context, R.string.subscription_failed, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.subscription_failed_no_connection, Toast.LENGTH_SHORT).show();
        }
    }

    public void createNotificationForRoute(String routeId, String routeName, TransitType transitType, String requestCode) {
        // make NHSL chosen from trolley picker have NHSL transittype
        if ("NHSL".equalsIgnoreCase(routeId)) {
            transitType = TransitType.NHSL;
        }

        // add route to subscription list
        List<RouteSubscription> notificationSubscriptions = SeptaServiceFactory.getNotificationsService().getRoutesSubscribedTo(context);
        RouteSubscription routeToSubscribeTo = new RouteSubscription(routeId, routeName, transitType);

        boolean isAlreadySubscribed = false;
        for (RouteSubscription routeSubscription : notificationSubscriptions) {
            if (routeToSubscribeTo.getRouteId().equals(routeSubscription.getRouteId())) {
                isAlreadySubscribed = true;
                break;
            }
        }

        if (isAlreadySubscribed) {
            // turn notifications on for that route
            SeptaServiceFactory.getNotificationsService().toggleRouteSubscription(context, routeId, true);

        } else {
            // create new route subscription
            SeptaServiceFactory.getNotificationsService().addRouteSubscription(context, routeToSubscribeTo);
        }

        // analytics
        Map<String, String> routeSubscribedTo = new HashMap<>();
        routeSubscribedTo.put("Added Subscription - Request Code", requestCode);
        routeSubscribedTo.put("Added Subscription - Transit Type", String.valueOf(transitType));
        routeSubscribedTo.put("Added Subscription - Route ID", routeId);
        AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_ROUTE_SUBSCRIBE, AnalyticsManager.CUSTOM_EVENT_ID_NOTIFICATION_MANAGEMENT, routeSubscribedTo);
    }

    public void removeNotificationForRoute(String routeId, TransitType transitType, String requestCode) {
        // make NHSL chosen from trolley picker have NHSL transittype
        if ("NHSL".equalsIgnoreCase(routeId)) {
            transitType = TransitType.NHSL;
        }

        // remember route but toggle notifications off
        SeptaServiceFactory.getNotificationsService().toggleRouteSubscription(context, routeId, false);

        // analytics
        Map<String, String> routeSubscribedTo = new HashMap<>();
        routeSubscribedTo.put("Muted Subscription - Request Code", requestCode);
        routeSubscribedTo.put("Muted Subscription - Transit Type", String.valueOf(transitType));
        routeSubscribedTo.put("Muted Subscription - Route ID", routeId);
        AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_ROUTE_UNSUBSCRIBE, AnalyticsManager.CUSTOM_EVENT_ID_NOTIFICATION_MANAGEMENT, routeSubscribedTo);
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
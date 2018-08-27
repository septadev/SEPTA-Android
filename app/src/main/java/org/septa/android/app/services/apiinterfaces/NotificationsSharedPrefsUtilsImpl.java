package org.septa.android.app.services.apiinterfaces;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.septa.android.app.services.apiinterfaces.model.RouteSubscription;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class NotificationsSharedPrefsUtilsImpl implements NotificationsSharedPrefsUtils {

    public static final String TAG = NotificationsSharedPrefsUtils.class.getSimpleName();

    // database shared preferences
    private static final String SHARED_PREFERENCES_NOTIFICATIONS = "SHARED_PREFERENCES_NOTIFICATIONS";
    private static final String NOTIF_PREFS_SAVED = "NOTIF_PREFS_SAVED";
    private static final String NOTIFICATIONS_ENABlED = "NOTIFICATIONS_ENABlED";
    private static final String SPECIAL_ANNOUNCEMENTS = "SPECIAL_ANNOUNCEMENTS";
    private static final String ROUTE_NOTIFICATION_SUBSCRIPTION = "ROUTE_NOTIFICATION_SUBSCRIPTION";
    private static final String NOTIFICATIONS_SCHEDULE_DAYS_OF_WEEK = "NOTIFICATIONS_SCHEDULE_DAYS_OF_WEEK";
    private static final String NOTIFICATIONS_TIME_FRAME = "NOTIFICATIONS_TIME_FRAME";
    private static final String DEVICE_ID = "DEVICE_ID";
    private static final String REGISTRATION_TOKEN = "REGISTRATION_TOKEN";
    private static final String AUTO_SUBSCRIPTION_TIME = "AUTO_SUBSCRIPTION_TIME";

    // parsing time frames
    public static final String START_END_TIME_DELIM = ",";
    private static final String DEFAULT_TIME_FRAME = "0600,1800";

    public static final int MAX_TIMEFRAMES = 2;

    // NOTE: using commit() instead of apply() so changes are saved immediately

    @Override
    public boolean areNotifPrefsSaved(Context context) {
        return getSharedPreferences(context).getBoolean(NOTIF_PREFS_SAVED, false);
    }

    @Override
    public void setNotifPrefsSaved(Context context, boolean isSaved) {
        getSharedPreferences(context).edit().putBoolean(NOTIF_PREFS_SAVED, isSaved).commit();
    }

    @Override
    public boolean areNotificationsEnabled(Context context) {
        return getSharedPreferences(context).getBoolean(NOTIFICATIONS_ENABlED, false);
    }

    @Override
    public void setNotificationsEnabled(Context context, boolean isEnabled) {
        getSharedPreferences(context).edit().putBoolean(NOTIFICATIONS_ENABlED, isEnabled).commit();
    }

    @Override
    public boolean areSpecialAnnouncementsEnabled(Context context) {
        return getSharedPreferences(context).getBoolean(SPECIAL_ANNOUNCEMENTS, false);
    }

    @Override
    public void setSpecialAnnouncementsEnabled(Context context, boolean subscribed) {
        getSharedPreferences(context).edit().putBoolean(SPECIAL_ANNOUNCEMENTS, subscribed).commit();
    }

    @Override
    public List<Integer> getNotificationsSchedule(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        String preferencesJson = sharedPreferences.getString(NOTIFICATIONS_SCHEDULE_DAYS_OF_WEEK, null);

        if (preferencesJson == null) {
            return getDefaultDaysOfWeek();
        }

        Gson gson = new Gson();
        try {
            return gson.fromJson(preferencesJson, new TypeToken<List<Integer>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            Log.e(TAG, e.toString());
            sharedPreferences.edit().remove(NOTIFICATIONS_SCHEDULE_DAYS_OF_WEEK).commit();
            return getDefaultDaysOfWeek();
        }
    }

    @Override
    public void addDayOfWeekToSchedule(Context context, int dayToAdd) {
        List<Integer> daysOfWeek = getNotificationsSchedule(context);
        if (!daysOfWeek.contains(dayToAdd)) {
            daysOfWeek.add(dayToAdd);
            Collections.sort(daysOfWeek);
            storeNotificationsSchedule(context, daysOfWeek);
        } else {
            Log.e(TAG, "Notifications are already enabled for " + dayToAdd);
        }
    }

    @Override
    public void removeDayOfWeekFromSchedule(Context context, int dayToRemove) {
        List<Integer> daysOfWeek = getNotificationsSchedule(context);
        int indexToRemove = -1;
        for (int i = 0; i < daysOfWeek.size(); i++) {
            if (dayToRemove == daysOfWeek.get(i)) {
                indexToRemove = i;
                break;
            }
        }
        if (indexToRemove != -1) {
            daysOfWeek.remove(indexToRemove);
        } else {
            Log.e(TAG, "Notifications are already disabled for " + dayToRemove);
        }
        storeNotificationsSchedule(context, daysOfWeek);
    }

    private void storeNotificationsSchedule(Context context, List<Integer> schedule) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        Gson gson = new Gson();
        String scheduleJson = gson.toJson(schedule);
        sharedPreferences.edit().putString(NOTIFICATIONS_SCHEDULE_DAYS_OF_WEEK, scheduleJson).commit();
    }

    @Override
    public List<String> getNotificationTimeFrames(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        String preferencesJson = sharedPreferences.getString(NOTIFICATIONS_TIME_FRAME, null);

        if (preferencesJson == null) {
            List<String> defaultTimeFrame = new ArrayList<>();
            defaultTimeFrame.add(DEFAULT_TIME_FRAME);
            return defaultTimeFrame;
        }

        Gson gson = new Gson();
        try {
            List<String> timeFramesList = gson.fromJson(preferencesJson, new TypeToken<List<String>>() {
            }.getType());

            // drop extra time frames if there are too many
            while (timeFramesList.size() > MAX_TIMEFRAMES) {
                timeFramesList.remove(timeFramesList.size() - 1);
            }
            return timeFramesList;
        } catch (JsonSyntaxException e) {
            Log.e(TAG, e.toString());
            sharedPreferences.edit().remove(NOTIFICATIONS_TIME_FRAME).commit();
            List<String> defaultTimeFrame = new ArrayList<>();
            defaultTimeFrame.add(DEFAULT_TIME_FRAME);
            return defaultTimeFrame;
        }
    }

    private void storeNotificationTimeFrames(SharedPreferences sharedPreferences, List<String> timeFrames) {
        Gson gson = new Gson();
        String timeFramesListJson = gson.toJson(timeFrames);
        sharedPreferences.edit().putString(NOTIFICATIONS_TIME_FRAME, timeFramesListJson).commit();
    }

    @Override
    public String getNotificationTimeFrame(Context context, int windowNumber, boolean isStartTime) {
        List<String> timeFramesList = getNotificationTimeFrames(context);
        if (windowNumber >= 0 && windowNumber < timeFramesList.size()) {
            String[] timeWindow = timeFramesList.get(windowNumber).split(START_END_TIME_DELIM);

            if (isStartTime) {
                return timeWindow[0];
            } else {
                return timeWindow[1];
            }
        } else {
            Log.e(TAG, "Invalid attempt to get time frame #" + windowNumber + " but timeFrames size is only " + timeFramesList.size());
        }
        return null;
    }

    @Override
    public void changeNotificationTimeFrame(Context context, int windowNumber, boolean isStartTime, String newTime) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        List<String> timeFramesList = getNotificationTimeFrames(context);

        // change notification for time frame
        if (windowNumber >= 0 && windowNumber < timeFramesList.size()) {
            String currentTimeWindow = timeFramesList.get(windowNumber);

            if (isStartTime) {
                currentTimeWindow = newTime + currentTimeWindow.substring(4);
            } else {
                currentTimeWindow = currentTimeWindow.substring(0, 5) + newTime;
            }

            timeFramesList.set(windowNumber, currentTimeWindow);
        } else {
            Log.e(TAG, "Invalid attempt to change notification time frame at " + windowNumber + " but timeFrames size is only " + timeFramesList.size());
        }

        storeNotificationTimeFrames(sharedPreferences, timeFramesList);
    }

    @Override
    public List<String> addNotificationTimeFrame(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        List<String> timeFrames = getNotificationTimeFrames(context);
        timeFrames.add(DEFAULT_TIME_FRAME);
        storeNotificationTimeFrames(sharedPreferences, timeFrames);
        return timeFrames;
    }

    @Override
    public List<String> removeNotificationTimeFrame(Context context, int windowToDelete) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        List<String> timeFrames = getNotificationTimeFrames(context);

        if (windowToDelete >= 0 && windowToDelete < timeFrames.size()) {
            timeFrames.remove(windowToDelete);
            storeNotificationTimeFrames(sharedPreferences, timeFrames);
        } else {
            Log.e(TAG, "Could not remove notification timeframe #" + windowToDelete);
        }
        return timeFrames;
    }

    @Override
    public List<RouteSubscription> getRoutesSubscribedTo(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        String preferencesJson = sharedPreferences.getString(ROUTE_NOTIFICATION_SUBSCRIPTION, null);

        if (preferencesJson == null) {
            return new ArrayList<>();
        }

        Gson gson = new Gson();
        try {
            return gson.fromJson(preferencesJson, new TypeToken<List<RouteSubscription>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            Log.e(TAG, e.toString());
            sharedPreferences.edit().remove(ROUTE_NOTIFICATION_SUBSCRIPTION).commit();
            return new ArrayList<>();
        }
    }

    @Override
    public boolean isSubscribedToRoute(Context context, String routeId) {
        List<RouteSubscription> routeList = getRoutesSubscribedTo(context);
        for (RouteSubscription route : routeList) {
            if (routeId.equalsIgnoreCase(route.getRouteId()) && route.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addRouteSubscription(Context context, RouteSubscription routeToAdd) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        List<RouteSubscription> routeList = getRoutesSubscribedTo(context);
        boolean found = false;

        for (RouteSubscription route : routeList) {
            if (routeToAdd.getRouteId().equalsIgnoreCase(route.getRouteId())) {
                Log.e(TAG, "Already subscribed to route: " + route.getRouteId());
                found = true;
                break;
            }
        }

        if (!found) {
            routeList.add(routeToAdd);
            Collections.sort(routeList);
            storeRoutesSubscribedTo(sharedPreferences, routeList);
        }
    }

    @Override
    public void toggleRouteSubscription(Context context, String routeId, boolean isEnabled) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        List<RouteSubscription> routeList = getRoutesSubscribedTo(context);
        boolean success = false;

        for (RouteSubscription route : routeList) {
            if (routeId.equalsIgnoreCase(route.getRouteId())) {
                route.setEnabled(isEnabled);
                success = true;
                break;
            }
        }

        if (success) {
            storeRoutesSubscribedTo(sharedPreferences, routeList);
        } else {
            Log.e(TAG, "Could not toggle notification subscription for route " + routeId);
        }
    }

    @Override
    public void removeRouteSubscription(Context context, String routeId) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        List<RouteSubscription> routesList = getRoutesSubscribedTo(context);
        int indexToRemove = -1;
        for (int i = 0; i < routesList.size(); i++) {
            if (routeId.equalsIgnoreCase(routesList.get(i).getRouteId())) {
                indexToRemove = i;
                break;
            }
        }
        if (indexToRemove != -1) {
            routesList.remove(indexToRemove);
            storeRoutesSubscribedTo(sharedPreferences, routesList);
        } else {
            Log.e(TAG, "Could not remove route subscription with ID: " + routeId);
        }
    }

    @Override
    public String getDeviceId(Context context) {
        return getSharedPreferences(context).getString(DEVICE_ID, "");
    }

    @Override
    public void setDeviceId(Context context, String deviceId) {
        getSharedPreferences(context).edit().putString(DEVICE_ID, deviceId).commit();
    }

    @Override
    public String getRegistrationToken(Context context) {
        return getSharedPreferences(context).getString(REGISTRATION_TOKEN, "");
    }

    @Override
    public void setRegistrationToken(Context context, String token) {
        getSharedPreferences(context).edit().putString(REGISTRATION_TOKEN, token).commit();
    }

    @Override
    public Long getNextAutoSubscriptionTime(Context context) {
        return getSharedPreferences(context).getLong(AUTO_SUBSCRIPTION_TIME, new Date().getTime());
    }

    @Override
    public void setNextAutoSubscriptionTime(Context context, Long subscriptionTime) {
        getSharedPreferences(context).edit().putLong(AUTO_SUBSCRIPTION_TIME, subscriptionTime).commit();
    }

    private List<Integer> getDefaultDaysOfWeek() {
        List<Integer> defaultSchedule = new ArrayList<>();
        defaultSchedule.add(Calendar.MONDAY);
        defaultSchedule.add(Calendar.TUESDAY);
        defaultSchedule.add(Calendar.WEDNESDAY);
        defaultSchedule.add(Calendar.THURSDAY);
        defaultSchedule.add(Calendar.FRIDAY);
        return defaultSchedule;
    }

    private void storeRoutesSubscribedTo(SharedPreferences sharedPreferences, List<RouteSubscription> routeList) {
        Gson gson = new Gson();
        String routesListJson = gson.toJson(routeList);
        sharedPreferences.edit().putString(ROUTE_NOTIFICATION_SUBSCRIPTION, routesListJson).commit();
    }

    private SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_NOTIFICATIONS, Context.MODE_PRIVATE);
    }
}
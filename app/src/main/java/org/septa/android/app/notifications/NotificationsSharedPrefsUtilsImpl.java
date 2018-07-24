package org.septa.android.app.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.septa.android.app.services.apiinterfaces.model.RouteNotificationSubscription;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class NotificationsSharedPrefsUtilsImpl implements NotificationsSharedPrefsUtils {

    public static final String TAG = NotificationsSharedPrefsUtils.class.getSimpleName();

    // database shared preferences
    private static final String SHARED_PREFERENCES_NOTIFICATIONS = "SHARED_PREFERENCES_NOTIFICATIONS";
    private static final String NOTIFICATIONS_ENABlED = "NOTIFICATIONS_ENABlED";
    private static final String SPECIAL_ANNOUNCEMENTS = "SPECIAL_ANNOUNCEMENTS";
    private static final String ROUTE_NOTIFICATION_SUBSCRIPTION = "ROUTE_NOTIFICATION_SUBSCRIPTION";
    private static final String NOTIFICATIONS_SCHEDULE_DAYS_OF_WEEK = "NOTIFICATIONS_SCHEDULE_DAYS_OF_WEEK";
    private static final String NOTIFICATIONS_START_TIME = "NOTIFICATIONS_START_TIME";
    private static final String NOTIFICATIONS_END_TIME = "NOTIFICATIONS_END_TIME";

    @Override
    public boolean areNotificationsEnabled(Context context) {
        return getSharedPreferences(context).getBoolean(NOTIFICATIONS_ENABlED, false);
    }

    @Override
    public void setNotificationsEnabled(Context context, boolean isEnabled) {
        getSharedPreferences(context).edit().putBoolean(NOTIFICATIONS_ENABlED, isEnabled).apply();
    }

    @Override
    public boolean areSpecialAnnouncementsEnabled(Context context) {
        return getSharedPreferences(context).getBoolean(SPECIAL_ANNOUNCEMENTS, false);
    }

    @Override
    public void setSpecialAnnouncementsEnabled(Context context, boolean subscribed) {
        getSharedPreferences(context).edit().putBoolean(SPECIAL_ANNOUNCEMENTS, subscribed).apply();
    }

    @Override
    public List<Integer> getNotificationsSchedule(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        String preferencesJson = sharedPreferences.getString(NOTIFICATIONS_SCHEDULE_DAYS_OF_WEEK, null);

        if (preferencesJson == null) {
            List<Integer> defaultSchedule = new ArrayList<>();
            defaultSchedule.add(Calendar.MONDAY);
            defaultSchedule.add(Calendar.TUESDAY);
            defaultSchedule.add(Calendar.WEDNESDAY);
            defaultSchedule.add(Calendar.THURSDAY);
            defaultSchedule.add(Calendar.FRIDAY);
            return defaultSchedule;
        }

        Gson gson = new Gson();
        try {
            return gson.fromJson(preferencesJson, new TypeToken<List<Integer>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            Log.e(TAG, e.toString());
            sharedPreferences.edit().remove(NOTIFICATIONS_SCHEDULE_DAYS_OF_WEEK).apply();
            return new ArrayList<>();
        }
    }

    @Override
    public void addDayOfWeekToSchedule(Context context, int dayToAdd) {
        List<Integer> daysOfWeek = getNotificationsSchedule(context);
        if (!daysOfWeek.contains(dayToAdd)) {
            daysOfWeek.add(dayToAdd);
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
        sharedPreferences.edit().putString(NOTIFICATIONS_SCHEDULE_DAYS_OF_WEEK, scheduleJson).apply();
    }

    @Override
    public Integer getNotificationStartTime(Context context) {
        return getSharedPreferences(context).getInt(NOTIFICATIONS_START_TIME, 900);
    }

    @Override
    public void setNotificationsStartTime(Context context, Integer startTime) {
        getSharedPreferences(context).edit().putInt(NOTIFICATIONS_START_TIME, startTime).apply();
    }

    @Override
    public Integer getNotificationEndTime(Context context) {
        return getSharedPreferences(context).getInt(NOTIFICATIONS_END_TIME, 1700);
    }

    @Override
    public void setNotificationsEndTime(Context context, Integer endTime) {
        getSharedPreferences(context).edit().putInt(NOTIFICATIONS_END_TIME, endTime).apply();
    }

    @Override
    public List<RouteNotificationSubscription> getRoutesSubscribedTo(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        String preferencesJson = sharedPreferences.getString(ROUTE_NOTIFICATION_SUBSCRIPTION, null);

        if (preferencesJson == null) {
            return new ArrayList<>();
        }

        Gson gson = new Gson();
        try {
            return gson.fromJson(preferencesJson, new TypeToken<List<RouteNotificationSubscription>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            Log.e(TAG, e.toString());
            sharedPreferences.edit().remove(ROUTE_NOTIFICATION_SUBSCRIPTION).apply();
            return new ArrayList<>();
        }
    }

    @Override
    public boolean isSubscribedToRoute(Context context, String routeId) {
        List<RouteNotificationSubscription> routeList = getRoutesSubscribedTo(context);
        for (RouteNotificationSubscription route : routeList) {
            if (routeId.equalsIgnoreCase(route.getRouteId()) && route.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addRouteSubscription(Context context, RouteNotificationSubscription routeToAdd) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        List<RouteNotificationSubscription> routeList = getRoutesSubscribedTo(context);
        boolean found = false;

        for (RouteNotificationSubscription route : routeList) {
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
        List<RouteNotificationSubscription> routeList = getRoutesSubscribedTo(context);
        boolean success = false;

        for (RouteNotificationSubscription route : routeList) {
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
        List<RouteNotificationSubscription> routesList = getRoutesSubscribedTo(context);
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

    private void storeRoutesSubscribedTo(SharedPreferences sharedPreferences, List<RouteNotificationSubscription> routeList) {
        Gson gson = new Gson();
        String routesListJson = gson.toJson(routeList);
        sharedPreferences.edit().putString(ROUTE_NOTIFICATION_SUBSCRIPTION, routesListJson).apply();
    }

    private SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_NOTIFICATIONS, Context.MODE_PRIVATE);
    }
}
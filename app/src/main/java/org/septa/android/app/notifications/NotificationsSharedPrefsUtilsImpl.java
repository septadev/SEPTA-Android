package org.septa.android.app.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class NotificationsSharedPrefsUtilsImpl implements NotificationsSharedPrefsUtils {

    public static final String TAG = NotificationsSharedPrefsUtils.class.getSimpleName();

    // database shared preferences
    private static final String SHARED_PREFERENCES_NOTIFICATIONS = "SHARED_PREFERENCES_NOTIFICATIONS";
    private static final String NOTIFICATIONS_ENABlED = "NOTIFICATIONS_ENABlED"; // TODO: is this needed or should we just check device permissions each time
    private static final String SPECIAL_ANNOUNCEMENTS = "SPECIAL_ANNOUNCEMENTS";
    private static final String ROUTE_TOPICS_SUBSCRIPTION = "ROUTE_TOPICS_SUBSCRIPTION";
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
            return new ArrayList<>();
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
    public String getNotificationStartTime(Context context) {
        return getSharedPreferences(context).getString(NOTIFICATIONS_START_TIME, "9:00 am"); // TODO: default value?
    }

    @Override
    public void setNotificationsStartTime(Context context, String startTime) {
        getSharedPreferences(context).edit().putString(NOTIFICATIONS_START_TIME, startTime).apply();
    }

    @Override
    public String getNotificationEndTime(Context context) {
        return getSharedPreferences(context).getString(NOTIFICATIONS_END_TIME, "5:00 pm"); // TODO: default value?
    }

    @Override
    public void setNotificationsEndTime(Context context, String endTime) {
        getSharedPreferences(context).edit().putString(NOTIFICATIONS_END_TIME, endTime).apply();
    }

    @Override
    public List<String> getTopicsSubscribedTo(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        String preferencesJson = sharedPreferences.getString(ROUTE_TOPICS_SUBSCRIPTION, null);

        if (preferencesJson == null) {
            return new ArrayList<>();
        }

        Gson gson = new Gson();
        try {
            return gson.fromJson(preferencesJson, new TypeToken<List<String>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            Log.e(TAG, e.toString());
            sharedPreferences.edit().remove(ROUTE_TOPICS_SUBSCRIPTION).apply();
            return new ArrayList<>();
        }
    }

    @Override
    public void addTopicSubscription(Context context, String topicId) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        List<String> topicsList = getTopicsSubscribedTo(context);

        if (!topicsList.contains(topicId)) {
            topicsList.add(topicId);
            storeTopicsSubscribedTo(sharedPreferences, topicsList);
        } else {
            Log.d(TAG, "Already subscribed to topic: " + topicId);
        }
    }

    @Override
    public void removeTopicSubscription(Context context, String topicId) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        List<String> topicsList = getTopicsSubscribedTo(context);
        int indexToRemove = -1;
        for (int i = 0; i < topicsList.size(); i++) {
            if (topicId.equals(topicsList.get(i))) {
                indexToRemove = i;
                break;
            }
        }
        if (indexToRemove != -1) {
            topicsList.remove(indexToRemove);
        } else {
            Log.e(TAG, "Could not remove topic with ID: " + topicId);
        }
        storeTopicsSubscribedTo(sharedPreferences, topicsList);
    }

    private void storeTopicsSubscribedTo(SharedPreferences sharedPreferences, List<String> topicsList) {
        Gson gson = new Gson();
        String topicsListJson = gson.toJson(topicsList);
        sharedPreferences.edit().putString(ROUTE_TOPICS_SUBSCRIPTION, topicsListJson).apply();
    }

    private SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_NOTIFICATIONS, Context.MODE_PRIVATE);
    }
}
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
    private static final String TREAT_AS_PRIORITY = "TREfAT_AS_PRIORITY";
    private static final String ROUTE_TOPICS_SUBSCRIPTION = "ROUTE_TOPICS_SUBSCRIPTION";
    private static final String NOTIFICATIONS_SCHEDULE = "NOTIFICATIONS_SCHEDULE";

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
    public boolean shouldTreatAsPriority(Context context) {
        return getSharedPreferences(context).getBoolean(TREAT_AS_PRIORITY, false);
    }

    @Override
    public void setTreatAsPriority(Context context, boolean priority) {
        getSharedPreferences(context).edit().putBoolean(TREAT_AS_PRIORITY, priority).apply();
    }

    @Override
    public List<String> getNotificationsSchedule(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        String preferencesJson = sharedPreferences.getString(NOTIFICATIONS_SCHEDULE, null);

        if (preferencesJson == null) {
            return new ArrayList<>();
        }

        Gson gson = new Gson();
        try {
            return gson.fromJson(preferencesJson, new TypeToken<List<String>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            Log.e(TAG, e.toString());
            sharedPreferences.edit().remove(NOTIFICATIONS_SCHEDULE).apply();
            return new ArrayList<>();
        }
    }

    private void storeNotificationsSchedule(SharedPreferences sharedPreferences, List<String> schedule) {
        Gson gson = new Gson();
        String scheduleJson = gson.toJson(schedule);
        sharedPreferences.edit().putString(NOTIFICATIONS_SCHEDULE, scheduleJson).apply();
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
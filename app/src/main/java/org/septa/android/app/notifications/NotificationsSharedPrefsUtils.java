package org.septa.android.app.notifications;

import android.content.Context;

import java.util.List;

public interface NotificationsSharedPrefsUtils {

    boolean areNotificationsEnabled(Context context);

    void setNotificationsEnabled(Context context, boolean isEnabled);

    boolean areSpecialAnnouncementsEnabled(Context context);

    void setSpecialAnnouncementsEnabled(Context context, boolean subscribed);

    List<Integer> getNotificationsSchedule(Context context);

    void addDayOfWeekToSchedule(Context context, int dayToAdd);

    void removeDayOfWeekFromSchedule(Context context, int dayToRemove);

    Integer getNotificationStartTime(Context context);

    void setNotificationsStartTime(Context context, Integer startTime);

    Integer getNotificationEndTime(Context context);

    void setNotificationsEndTime(Context context, Integer endTime);

    List<String> getTopicsSubscribedTo(Context context);

    void addTopicSubscription(Context context, String topicId);

    void removeTopicSubscription(Context context, String topicId);

}
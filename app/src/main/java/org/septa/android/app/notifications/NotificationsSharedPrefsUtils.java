package org.septa.android.app.notifications;

import android.content.Context;

import java.util.List;

public interface NotificationsSharedPrefsUtils {

    boolean areNotificationsEnabled(Context context);

    void setNotificationsEnabled(Context context, boolean isEnabled);

    boolean areSpecialAnnouncementsEnabled(Context context);

    void setSpecialAnnouncementsEnabled(Context context, boolean subscribed);

    List<String> getNotificationsSchedule(Context context);

    List<String> getTopicsSubscribedTo(Context context);

    void addTopicSubscription(Context context, String topicId);

    void removeTopicSubscription(Context context, String topicId);

}
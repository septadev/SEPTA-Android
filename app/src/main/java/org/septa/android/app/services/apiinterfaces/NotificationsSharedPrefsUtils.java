package org.septa.android.app.services.apiinterfaces;

import android.content.Context;

import org.septa.android.app.services.apiinterfaces.model.RouteSubscription;

import java.util.List;

public interface NotificationsSharedPrefsUtils {

    boolean areNotifPrefsSaved(Context context);

    void setNotifPrefsSaved(Context context, boolean isSaved);

    boolean areNotificationsEnabled(Context context);

    void setNotificationsEnabled(Context context, boolean isEnabled);

    boolean areSpecialAnnouncementsEnabled(Context context);

    void setSpecialAnnouncementsEnabled(Context context, boolean subscribed);

    List<Integer> getNotificationsSchedule(Context context);

    void setNotificationsSchedule(Context context, List<Integer> schedule);

    List<String> getNotificationTimeFrames(Context context);

    void setNotificationTimeFrames(Context context, List<String> timeFrames);

    List<RouteSubscription> getRoutesSubscribedTo(Context context);

    void setRoutesSubscribedTo(Context context, List<RouteSubscription> routeSubscriptions);

    boolean isSubscribedToRoute(Context context, String routeId);

    void addRouteSubscription(Context context, RouteSubscription route);

    void toggleRouteSubscription(Context context, String routeId, boolean isEnabled);

    String getDeviceId(Context context);

    void setDeviceId(Context context, String deviceId);

    String getRegistrationToken(Context context);

    void setRegistrationToken(Context context, String token);

    Long getNextAutoSubscriptionTime(Context context);

    void setNextAutoSubscriptionTime(Context context, Long subscriptionTime);
}
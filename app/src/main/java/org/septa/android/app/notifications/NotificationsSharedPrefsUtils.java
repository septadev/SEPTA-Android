package org.septa.android.app.notifications;

import android.content.Context;

import org.septa.android.app.services.apiinterfaces.model.RouteNotificationSubscription;

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

    List<RouteNotificationSubscription> getRoutesSubscribedTo(Context context);

    boolean isSubscribedToRoute(Context context, String routeId);

    void addRouteSubscription(Context context, RouteNotificationSubscription route);

    void toggleRouteSubscription(Context context, String routeId, boolean isEnabled);

    void removeRouteSubscription(Context context, String routeId);

}
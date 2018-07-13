package org.septa.android.app.notifications;

import android.content.Context;

import org.septa.android.app.database.SEPTADatabase;

public abstract class NotificationsSharedPrefsUtils {

    // database shared preferences
    private static final String SHARED_PREFERENCES_NOTIFICATIONS = "SHARED_PREFERENCES_NOTIFICATIONS";
    private static final String NOTIFICATIONS_ENABlED = "NOTIFICATIONS_ENABlED"; // TODO: is this needed or should we just check device permissions each time
    private static final String SPECIAL_ANNOUNCEMENTS = "SPECIAL_ANNOUNCEMENTS";
    private static final String TREAT_AS_PRIORITY = "TREfAT_AS_PRIORITY";
    private static final String MY_NOTIFICATIONS = "MY_NOTIFICATIONS";
    private static final String NOTIFICATIONS_SCHEDULE = "NOTIFICATIONS_SCHEDULE";

    public static boolean areNotificationsEnabled(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_NOTIFICATIONS, Context.MODE_PRIVATE).getBoolean(NOTIFICATIONS_ENABlED, false);
    }

    public static void setNotificationsEnabled(Context context, boolean isEnabled) {
        context.getSharedPreferences(SHARED_PREFERENCES_NOTIFICATIONS, Context.MODE_PRIVATE).edit().putBoolean(NOTIFICATIONS_ENABlED, isEnabled).apply();
    }

    public static boolean isSubscribedToSpecialAnnouncements(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_NOTIFICATIONS, Context.MODE_PRIVATE).getBoolean(SPECIAL_ANNOUNCEMENTS, false);
    }

    public static void setSubscribedToSpecialAnnouncements(Context context, boolean subscribed) {
        context.getSharedPreferences(SHARED_PREFERENCES_NOTIFICATIONS, Context.MODE_PRIVATE).edit().putBoolean(SPECIAL_ANNOUNCEMENTS, subscribed).apply();
    }

    public static boolean shouldTreatAsPriority(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_NOTIFICATIONS, Context.MODE_PRIVATE).getBoolean(TREAT_AS_PRIORITY, false);
    }

    public static void setTreatAsPriority(Context context, boolean priority) {
        context.getSharedPreferences(SHARED_PREFERENCES_NOTIFICATIONS, Context.MODE_PRIVATE).edit().putBoolean(TREAT_AS_PRIORITY, priority).apply();
    }

    public static String getMyNotifications(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_NOTIFICATIONS, Context.MODE_PRIVATE).getString(MY_NOTIFICATIONS, SEPTADatabase.getDatabaseFileName());
    }

    public static void setMyNotifications(Context context, String myNotifs) {
        context.getSharedPreferences(SHARED_PREFERENCES_NOTIFICATIONS, Context.MODE_PRIVATE).edit().putString(MY_NOTIFICATIONS, myNotifs).apply();
    }

    public static String getNotificationsSchedule(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_NOTIFICATIONS, Context.MODE_PRIVATE).getString(NOTIFICATIONS_SCHEDULE, SEPTADatabase.getDatabaseFileName());
    }

    public static void setNotificationsSchedule(Context context, String schedule) {
        context.getSharedPreferences(SHARED_PREFERENCES_NOTIFICATIONS, Context.MODE_PRIVATE).edit().putString(NOTIFICATIONS_SCHEDULE, schedule).apply();
    }

}
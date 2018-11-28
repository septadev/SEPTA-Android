package org.septa.android.app.systemstatus;

import android.content.Context;
import android.content.SharedPreferences;

public abstract class SharedPreferencesAlertUtil {

    public static final String TAG = SharedPreferencesAlertUtil.class.getSimpleName();

    private static final String SHARED_PREFERENCES_RATING = "SHARED_PREFERENCES_ALERT";
    private static final String PREF_HIDDEN_GLOBAL_ALERT_TIMESTAMP = "PREF_HIDDEN_GLOBAL_ALERT_TIMESTAMP";
    private static final String PREF_HIDDEN_MOBILE_ALERT_TIMESTAMP = "PREF_HIDDEN_MOBILE_ALERT_TIMESTAMP";

    // using commit() instead of apply() so that the values are immediately written to memory

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_RATING, Context.MODE_PRIVATE);
    }

    public static String getHiddenGlobalAlertTimestamp(Context context) {
        return getSharedPreferences(context).getString(PREF_HIDDEN_GLOBAL_ALERT_TIMESTAMP, "");
    }

    public static void setHiddenGlobalAlertTimestamp(Context context, String alertTimestamp) {
        getSharedPreferences(context).edit().putString(PREF_HIDDEN_GLOBAL_ALERT_TIMESTAMP, alertTimestamp).commit();
    }

    public static String getHiddenMobileAlertTimestamp(Context context) {
        return getSharedPreferences(context).getString(PREF_HIDDEN_MOBILE_ALERT_TIMESTAMP, "");
    }

    public static void setHiddenMobileAlertTimestamp(Context context, String alertTimestamp) {
        getSharedPreferences(context).edit().putString(PREF_HIDDEN_MOBILE_ALERT_TIMESTAMP, alertTimestamp).commit();
    }

}
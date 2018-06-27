package org.septa.android.app.rating;

import android.content.Context;
import android.content.SharedPreferences;

public abstract class SharedPreferencesRatingUtil {

    public static final String TAG = SharedPreferencesRatingUtil.class.getSimpleName();

    private static final String SHARED_PREFERENCES_RATING = "SHARED_PREFERENCES_RATING";
    private static final String PREF_APP_RATED = "PREF_APP_RATED";
    private static final String PREF_NUMBER_USES = "PREF_NUMBER_USES";
    private static final String PREF_RATING_ID = "PREF_RATING_ID";
    private static final String PREF_APP_JUST_CRASHED = "PREF_APP_JUST_CRASHED";

    // using commit() instead of apply() so that the values are immediately written to memory before the restart

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_RATING, Context.MODE_PRIVATE);
    }

    public static boolean getAppRated(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APP_RATED, false);
    }

    public static void setAppRated(Context context, boolean appRated) {
        getSharedPreferences(context).edit().putBoolean(PREF_APP_RATED, appRated).commit();
    }

    public static int getNumberOfUses(Context context) {
        return getSharedPreferences(context).getInt(PREF_NUMBER_USES, 0);
    }

    public static void setNumberOfUses(Context context, int numberUses) {
        getSharedPreferences(context).edit().putInt(PREF_NUMBER_USES, numberUses).commit();
    }

    public static void incrementNumberOfUses(Context context) {
        getSharedPreferences(context).edit().putInt(PREF_NUMBER_USES, getNumberOfUses(context) + 1).commit();
    }

    public static int getRatingId(Context context) {
        return getSharedPreferences(context).getInt(PREF_RATING_ID, 0);
    }

    public static void setRatingId(Context context, int ratingId) {
        getSharedPreferences(context).edit().putInt(PREF_RATING_ID, ratingId).commit();
    }

    public static boolean getAppJustCrashed(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APP_JUST_CRASHED, false);
    }

    public static void setAppJustCrashed(Context context, boolean crash) {
        getSharedPreferences(context).edit().putBoolean(PREF_APP_JUST_CRASHED, crash).commit();
    }
}
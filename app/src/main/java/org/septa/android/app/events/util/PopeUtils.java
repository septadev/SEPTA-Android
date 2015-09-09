package org.septa.android.app.events.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import org.septa.android.app.BuildConfig;
import org.septa.android.app.events.EventsConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jhunchar on 9/2/15.
 */
public class PopeUtils {
    private static final String TAG = PopeUtils.class.getName();

    // 7 days * 24 hr/day * 60 min/hr * 60 sec/min * 1000 ms/sec == 604800000 ms
    private static final long WEEK_IN_MILLIS = 7 * 24 * 60 * 60 * 1000;

    public static boolean isPopeVisitingToday(Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(EventsConstants.PREFS_KEY_EVENTS, 0);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(EventsConstants.VALUE_POPE_DATE_FORMAT);

        try {

            // Get the current date
            Date currentDate = new Date();
            long currentDateMillis = currentDate.getTime();

            // Get the papal visit start date
            String startDateString = sharedPreferences.getString(
                    EventsConstants.PREFS_KEY_POPE_START_DATE,
                    EventsConstants.VALUE_POPE_DATE_SATURDAY_START
            );

            Date startDate = simpleDateFormat.parse(startDateString);
            long startDateMillis = startDate.getTime();

            // Get the papal visit end date
            String endDateString = sharedPreferences.getString(
                    EventsConstants.PREFS_KEY_POPE_END_DATE,
                    EventsConstants.VALUE_POPE_DATE_MONDAY_END
            );

            Date endDate = simpleDateFormat.parse(endDateString);
            long endDateMillis = endDate.getTime();

            if (BuildConfig.DEBUG) {
                Log.v(TAG, "isPopeVisitingToday: " + (currentDateMillis >= startDateMillis && currentDateMillis <= endDateMillis));
            }

            return currentDateMillis >= startDateMillis && currentDateMillis <= endDateMillis;
        }

        catch (ParseException e) {

            Log.w(TAG, e.getMessage());
        }

        return false;
    }

    public static boolean isRailScheduleAvailableToday() {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(EventsConstants.VALUE_POPE_DATE_FORMAT);

        try {

            // Get the current date
            Date currentDate = new Date();
            long currentDateMillis = currentDate.getTime();

            Date startDate = simpleDateFormat.parse(EventsConstants.VALUE_POPE_DATE_SATURDAY_START);
            long startDateMillis = startDate.getTime();

            Date endDate = simpleDateFormat.parse(EventsConstants.VALUE_POPE_DATE_MONDAY_END);
            long endDateMillis = endDate.getTime();

            if (BuildConfig.DEBUG) {
                Log.v(TAG, "isRailScheduleAvailableToday: " + !(currentDateMillis < startDateMillis && currentDateMillis > endDateMillis));
            }

            return currentDateMillis < startDateMillis || currentDateMillis > endDateMillis;
        }

        catch (ParseException e) {

            Log.w(TAG, e.getMessage());
        }

        return false;
    }

    public static boolean isPopeVisitingSaturday() {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(EventsConstants.VALUE_POPE_DATE_FORMAT);

        try {

            // Get the current date
            Date currentDate = new Date();
            long currentDateMillis = currentDate.getTime();

            // Get the papal visit start date
            Date saturdayEnd = simpleDateFormat.parse(EventsConstants.VALUE_POPE_DATE_SUNDAY_START);
            long saturdayEndMillis = saturdayEnd.getTime();

            // Check if saturday pope visit is within week
            long millisToSaturdayEnd = saturdayEndMillis - currentDateMillis;

            if (BuildConfig.DEBUG) {
                Log.v(TAG, "millisToSaturdayEnd = " + millisToSaturdayEnd);
            }

            return millisToSaturdayEnd > 0 && millisToSaturdayEnd < WEEK_IN_MILLIS;
        }

        catch (ParseException e) {

            Log.w(TAG, e.getMessage());
        }

        return false;
    }

    public static boolean isPopeVisitingSunday() {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(EventsConstants.VALUE_POPE_DATE_FORMAT);

        try {

            // Get the current date
            Date currentDate = new Date();
            long currentDateMillis = currentDate.getTime();

            // Get the papal visit end date
            Date sundayEnd = simpleDateFormat.parse(EventsConstants.VALUE_POPE_DATE_MONDAY_END);
            long sundayEndMillis = sundayEnd.getTime();

            // Check if sunday pope visit is within week
            long millisToSundayEnd = sundayEndMillis - currentDateMillis;

            if (BuildConfig.DEBUG) {
                Log.v(TAG, "millisToSundayEnd = " + millisToSundayEnd);
            }

            return millisToSundayEnd > 0 && millisToSundayEnd < WEEK_IN_MILLIS;
        }

        catch (ParseException e) {

            Log.w(TAG, e.getMessage());
        }

        return false;
    }

    public static boolean updatePopeVisitEndDate(Context context, String endDate) {

        if (!TextUtils.isEmpty(endDate)) {

            SharedPreferences sharedPreferences = context.getSharedPreferences(EventsConstants.PREFS_KEY_EVENTS, 0);
            String savedEndDate = sharedPreferences.getString(
                    EventsConstants.PREFS_KEY_POPE_END_DATE,
                    EventsConstants.VALUE_POPE_DATE_MONDAY_END
            );

            if (endDate != null && !endDate.equals(savedEndDate)) {

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(EventsConstants.PREFS_KEY_POPE_END_DATE, endDate).apply();

                if (BuildConfig.DEBUG) {
                    Log.v(TAG, "updatePopeVisitEndDate: " + endDate);
                }

                // Return true if the end date was updated
                return true;
            }
        }

        // Otherwise, return false
        return false;
    }

    public static boolean updatePopeVisitStartDate(Context context, String startDate) {

        if (!TextUtils.isEmpty(startDate)) {

            SharedPreferences sharedPreferences = context.getSharedPreferences(EventsConstants.PREFS_KEY_EVENTS, 0);
            String savedStartDate = sharedPreferences.getString(
                    EventsConstants.PREFS_KEY_POPE_START_DATE,
                    EventsConstants.VALUE_POPE_DATE_SATURDAY_START
            );

            if (startDate != null && !startDate.equals(savedStartDate)) {

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(EventsConstants.PREFS_KEY_POPE_START_DATE, startDate).apply();

                if (BuildConfig.DEBUG) {
                    Log.v(TAG, "updatePopeVisitStartDate: " + startDate);
                }

                // Return true if the start date was updated
                return true;
            }
        }

        // Otherwise, return false
        return false;
    }
}

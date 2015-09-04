package org.septa.android.app.PapalVisit;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import org.septa.android.app.BuildConfig;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jhunchar on 9/2/15.
 */
public class PopeUtils {
    private static final String TAG = PopeUtils.class.getName();

    public static boolean isPopeVisitingToday(Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(PopeConstants.PREFS_KEY, 0);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(PopeConstants.VALUE_POPE_DATE_FORMAT);

        try {

            // Get the current date
            Date currentDate = new Date();
            long currentDateMillis = currentDate.getTime();

            // Get the papal visit start date
            String startDateString = sharedPreferences.getString(
                    PopeConstants.PREFS_KEY_POPE_START_DATE,
                    PopeConstants.VALUE_POPE_DATE_SATURDAY_START
            );

            Date startDate = simpleDateFormat.parse(startDateString);
            long startDateMillis = startDate.getTime();

            // Get the papal visit end date
            String endDateString = sharedPreferences.getString(
                    PopeConstants.PREFS_KEY_POPE_END_DATE,
                    PopeConstants.VALUE_POPE_DATE_MONDAY_END
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

    public static boolean isPopeVisitingSaturday() {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(PopeConstants.VALUE_POPE_DATE_FORMAT);

        try {

            // Get the current date
            Date currentDate = new Date();
            long currentDateMillis = currentDate.getTime();

            // Get the papal visit start date
            Date startDate = simpleDateFormat.parse(PopeConstants.VALUE_POPE_DATE_SUNDAY_START);
            long startDateMillis = startDate.getTime();

            // Check if saturday pope visit is within week

        }

        catch (ParseException e) {

            Log.w(TAG, e.getMessage());
        }

        return false;
    }

    public static boolean isPopeVisitingSunday() {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(PopeConstants.VALUE_POPE_DATE_FORMAT);

        try {

            // Get the current date
            Date currentDate = new Date();
            long currentDateMillis = currentDate.getTime();

            // Get the papal visit end date
            Date endDate = simpleDateFormat.parse(PopeConstants.VALUE_POPE_DATE_MONDAY_END);
            long endDateMillis = endDate.getTime();

            // Check if saturday pope visit is within week

        }

        catch (ParseException e) {

            Log.w(TAG, e.getMessage());
        }

        return false;
    }

    public static boolean updatePopeVisitEndDate(Context context, String endDate) {

        if (!TextUtils.isEmpty(endDate)) {

            SharedPreferences sharedPreferences = context.getSharedPreferences(PopeConstants.PREFS_KEY, 0);
            String savedEndDate = sharedPreferences.getString(
                    PopeConstants.PREFS_KEY_POPE_END_DATE,
                    PopeConstants.VALUE_POPE_DATE_MONDAY_END
            );

            if (endDate != null && !endDate.equals(savedEndDate)) {

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(PopeConstants.PREFS_KEY_POPE_END_DATE, endDate).apply();

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

            SharedPreferences sharedPreferences = context.getSharedPreferences(PopeConstants.PREFS_KEY, 0);
            String savedStartDate = sharedPreferences.getString(
                    PopeConstants.PREFS_KEY_POPE_START_DATE,
                    PopeConstants.VALUE_POPE_DATE_SATURDAY_START
            );

            if (startDate != null && !startDate.equals(savedStartDate)) {

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(PopeConstants.PREFS_KEY_POPE_START_DATE, startDate).apply();

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

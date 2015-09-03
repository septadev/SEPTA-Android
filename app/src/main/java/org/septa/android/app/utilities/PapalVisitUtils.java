package org.septa.android.app.utilities;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jhunchar on 9/2/15.
 */
public class PapalVisitUtils {
    private static final String TAG = PapalVisitUtils.class.getName();

    private static final String PAPAL_SATURDAY_START_DATE = "Sat Sep 26 00:00:01 EDT 2015";
    private static final String PAPAL_SUNDAY_START_DATE = "Sun Sep 27 00:00:01 EDT 2015";
    private static final String PAPAL_MONDAY_END_DATE = "Mon Sep 28 00:00:01 EDT 2015";


    public static boolean isPopeVisitingToday() {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");

        try {

            // Get the current date
            Date currentDate = new Date();
            long currentDateMillis = currentDate.getTime();

            // Get the papal visit start date
            Date startDate = simpleDateFormat.parse(PAPAL_SATURDAY_START_DATE);
            long startDateMillis = startDate.getTime();

            // Get the papal visit end date
            Date endDate = simpleDateFormat.parse(PAPAL_MONDAY_END_DATE);
            long endDateMillis = endDate.getTime();

            return (currentDateMillis >= startDateMillis && currentDateMillis <= endDateMillis) ? true : false;
        }

        catch (ParseException e) {

            Log.w(TAG, e.getMessage());
        }

        return false;
    }

    // TODO: Is it better to check if week of ... or is it better to advance date value to next sat?

    public static boolean isPopeVisitingSaturday() {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");

        try {

            // Get the current date
            Date currentDate = new Date();
            long currentDateMillis = currentDate.getTime();

            // Get the papal visit start date
            Date startDate = simpleDateFormat.parse(PAPAL_SUNDAY_START_DATE);
            long startDateMillis = startDate.getTime();

            // Check if saturday pope visit is within week

        }

        catch (ParseException e) {

            Log.w(TAG, e.getMessage());
        }

        return false;
    }

    public static boolean isPopeVisitingSunday() {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");

        try {

            // Get the current date
            Date currentDate = new Date();
            long currentDateMillis = currentDate.getTime();

            // Get the papal visit end date
            Date endDate = simpleDateFormat.parse(PAPAL_MONDAY_END_DATE);
            long endDateMillis = endDate.getTime();

            // Check if saturday pope visit is within week

        }

        catch (ParseException e) {

            Log.w(TAG, e.getMessage());
        }

        return false;
    }
}

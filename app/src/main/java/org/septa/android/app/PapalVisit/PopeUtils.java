package org.septa.android.app.PapalVisit;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jhunchar on 9/2/15.
 */
public class PopeUtils {
    private static final String TAG = PopeUtils.class.getName();

    // FIXME: Set correct Papal visit dates (JCH)
    private static final String PAPAL_SATURDAY_START_DATE = "Thu Sep 03 00:00:01 EDT 2015";
    private static final String PAPAL_SUNDAY_START_DATE = "Fri Sep 04 00:00:01 EDT 2015";
    private static final String PAPAL_MONDAY_END_DATE = "Sat Sep 05 00:00:01 EDT 2015";


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

package org.septa.android.app.utilities;

import android.util.Log;

import org.septa.android.app.activities.FindNearestLocationActionBarActivity;
import org.septa.android.app.activities.NextToArriveActionBarActivity;
import org.septa.android.app.activities.TrainViewActionBarActivity;
import org.septa.android.app.activities.TransitViewActionBarActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jhunchar on 9/2/15.
 */
public class PapalVisitUtils {
    private static final String TAG = PapalVisitUtils.class.getName();

    private static final Class[] disabledMenuItems = {
            NextToArriveActionBarActivity.class,
            TrainViewActionBarActivity.class,
            TransitViewActionBarActivity.class,
            FindNearestLocationActionBarActivity.class
    };

    // TODO: Clean this up with official dates (JCH)
    private static final String START_DATE = "Wed Sep 02 15:30:00 EDT 2015";
    private static final String END_DATE = "Wed Sep 02 23:59:59 EDT 2015";

    public static boolean isPapalVisit() {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");

        try {

            // Get the current date
            Date currentDate = new Date();
            long currentDateMillis = currentDate.getTime();

            // Get the papal visit start date
            Date startDate = simpleDateFormat.parse(START_DATE);
            long startDateMillis = startDate.getTime();

            // Get the papal visit end date
            Date endDate = simpleDateFormat.parse(END_DATE);
            long endDateMillis = endDate.getTime();

            return (currentDateMillis >= startDateMillis && currentDateMillis <= endDateMillis) ? true : false;
        }

        catch (ParseException e) {

            Log.w(TAG, e.getMessage());
        }

        return false;
    }

    public static boolean isDisabledMenuItem(Class classname) {

        if (isPapalVisit()) {
            for (int i = 0; i < disabledMenuItems.length; i++) {
                if (classname.equals(disabledMenuItems[i])) {
                    return true;
                }
            }
        }

        return false;
    }
}

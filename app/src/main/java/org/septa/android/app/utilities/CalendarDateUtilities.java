package org.septa.android.app.utilities;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.septa.android.app.activities.schedules.ItinerarySelectionActionBarActivity;
import org.septa.android.app.databases.SEPTADatabase;

import java.util.Calendar;

public class CalendarDateUtilities {

    public static int getDayOfTheWeek() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        return day;
    }

    public static boolean isTodaySunday() {
        int day = getDayOfTheWeek();

        switch (day) {
            case Calendar.SUNDAY: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    // this is a short cut method to return the service id.
    // technically this data should be looked up in the SQLlite database.
    // but for now
    public int getServiceIdForNow(Context context) {
        int day = getDayOfTheWeek();

        switch (day) {
            case Calendar.SUNDAY: {
                return 3;
            }
            case Calendar.SATURDAY: {
                return 2;
            }
            case Calendar.FRIDAY: {
                return 4;
            }
            default: {      // this will cover any days not specifically called out
                return 1;
            }
        }
    }
}

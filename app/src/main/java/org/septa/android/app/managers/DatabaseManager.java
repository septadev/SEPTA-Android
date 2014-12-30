package org.septa.android.app.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.septa.android.app.databases.SEPTADatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Future handler of database interactions
 * can become singleton when all calls are changed and a database instance can live here instead
 * of being passed in
 * <p/>
 * Created by Antoine on 12/29/2014.
 */
public class DatabaseManager {

    private static final String TAG = SEPTADatabase.class.getName();
    private static final String QUERY_BUS_HOLIDAY = "SELECT service_id, date FROM holiday_bus WHERE date='%s'";
    private static final String DATE_FORMAT_HOLIDAY = "yyyyMMdd";

    /**
     * Check if the passed in date is a holiday
     *
     * @param database Database object
     * @param date     Date object
     * @return the corresponding service id, otherwise -1 if day not found
     */
    public static int isTodayBusHoliday(SQLiteDatabase database, Date date) {
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_HOLIDAY);
        String query = String.format(QUERY_BUS_HOLIDAY, dateFormat.format(date));
        int serviceId = -1;
        try {
            Cursor cursor = database.rawQuery(query, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    serviceId = cursor.getInt(0);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying holiday", e);
        }

        return serviceId;
    }
}

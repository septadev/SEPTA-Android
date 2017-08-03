package org.septa.android.app.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.septa.android.app.domain.StopModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Database manager
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private static SQLiteDatabase database;

    private DatabaseManager(Context context) {
    }

    public static synchronized DatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseManager(context);
        }
        // Initialize database
        initDatabase(context);
        return instance;
    }

    private static synchronized void initDatabase(Context context) {
        if (database == null) {
            database = new SEPTADatabase(context.getApplicationContext()).getReadableDatabase();
        }
    }

    public static synchronized SQLiteDatabase getDatabase(Context context) {
        if (database == null) {
            database = new SEPTADatabase(context.getApplicationContext()).getReadableDatabase();
        }
        return database;
    }

    public List<StopModel> getRailStops(Context context) {
        List<StopModel> railStops = new ArrayList<>();
        String queryString = "SELECT stop_id, stop_name, wheelchair_boarding, stop_lat, stop_lon FROM stops_rail ORDER BY stop_name";
        Cursor cursor = getDatabase(context).rawQuery(queryString, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    StopModel stopModel = new StopModel(cursor.getString(0), cursor.getString(1),
                            (cursor.getInt(2) == 1), cursor.getString(3), cursor.getString(4));
                    railStops.add(stopModel);
                } while (cursor.moveToNext());
            }

            cursor.close();
        }
        return railStops;
    }
}

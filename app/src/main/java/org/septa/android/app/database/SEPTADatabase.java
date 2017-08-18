package org.septa.android.app.database;

import android.content.Context;
import android.database.SQLException;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class SEPTADatabase extends SQLiteAssetHelper {

    /**
     * Current packaged DB version, update number when packaged DB changes
     */
    private static final int DATABASE_VERSION = 70;
    private static final String DATABASE_FILE_NAME = "SEPTA.sqlite";

    public SEPTADatabase(Context context) {

        super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION);
        // Causes database to overwrite when version changes
        setForcedUpgrade();

        try {
            getWritableDatabase().execSQL("CREATE INDEX tripsBUSIDX on trips_bus(trip_id)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            getWritableDatabase().execSQL("CREATE INDEX stopsBUSSTOPID on stop_times_bus(stop_id)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            getWritableDatabase().execSQL("CREATE INDEX stopsBUSSSEQ on stop_times_bus(stop_sequence)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
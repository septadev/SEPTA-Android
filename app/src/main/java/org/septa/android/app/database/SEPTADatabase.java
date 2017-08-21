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

        execSQL(new String[]{"CREATE INDEX tripsBUSIDX on trips_bus(trip_id)"});
        execSQL(new String[]{"CREATE INDEX stopsBUSSTOPID on stop_times_bus(stop_id)"});
        execSQL(new String[]{"CREATE INDEX stopsBUSSSEQ on stop_times_bus(stop_sequence)"});
    }

    private void execSQL(String statements[]) {
        try {
            for (String sql : statements)
                getWritableDatabase().execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
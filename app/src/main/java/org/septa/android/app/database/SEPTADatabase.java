package org.septa.android.app.database;

import android.content.Context;
import android.database.SQLException;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import org.septa.android.app.support.CrashlyticsManager;

public class SEPTADatabase extends SQLiteAssetHelper {
    private static String TAG = SEPTADatabase.class.getSimpleName();

    /**
     * Current packaged DB version, update number when packaged DB changes
     */
    private static final String LATEST_DATABASE_API_URL = "https://s3.amazonaws.com/mobiledb.septa.org/latest/latestDb.json";

    // these are left in case the user does not have the most up to date version of the database
    // modify databaseVersion and databaseFileName when pushing out a new release with a new DB
    private static int databaseVersion = 282;
    private static String databaseFileName = "SEPTA.sqlite";
    // TODO: have to update shared preferences when these constants are changed so that older DBs don't overwrite a newer one on the app

    public SEPTADatabase(Context context, int databaseVersion, String databaseFileName) {
        super(context, databaseFileName, null, databaseVersion);

        setDatabaseVersion(databaseVersion);

        CrashlyticsManager.log(Log.INFO, TAG, "Initializing DB: " + databaseVersion);

        // Causes database to overwrite when version changes
        setForcedUpgrade();

        // create indices on new app database
        execSQL(new String[]{
                "CREATE INDEX stop_srd_index ON stop_route_direction (stop_id);",
                "CREATE INDEX route_srd_index ON stop_route_direction (route_id);",
                "CREATE INDEX direction_srd_index ON stop_route_direction (direction_id);",
                "CREATE INDEX trips_bus_route_id_index ON trips_bus (route_id);",
                "CREATE INDEX trips_rail_route_id_trip_id_index ON trips_rail (route_id, trip_id);",
                "CREATE INDEX trips_rail_trip_id_route_id_index ON trips_rail (trip_id, route_id);",
                "CREATE INDEX routes_bus_route_id_index ON routes_bus (route_id);",
                "CREATE INDEX routes_rail_route_id_index ON routes_rail (route_id);",
                "CREATE INDEX reverseStopSearch_reverse_stop_id_stop_id_index ON reverseStopSearch (reverse_stop_id, stop_id);",
                "CREATE INDEX reverseStopSearch_stop_id_reverse_stop_id_index ON reverseStopSearch (stop_id, reverse_stop_id);",
                "CREATE INDEX bus_stop_directions_Route_index ON bus_stop_directions (Route);",
                "CREATE INDEX stop_times_rail_trip_id_stop_id_stop_sequence_index ON stop_times_rail (trip_id, stop_id, stop_sequence);",
                "CREATE INDEX stop_times_rail_stop_id_trip_id_index ON stop_times_rail (stop_id, trip_id);",
                "VACUUM;"
        });
    }

    private void execSQL(String statements[]) {
        try {
            for (String sql : statements) {
                getWritableDatabase().execSQL(sql);
            }
        } catch (SQLException e) {
            //e.printStackTrace();
        }
    }

    public static int getDatabaseVersion() {
        return databaseVersion;
    }

    public static void setDatabaseVersion(int databaseVersion) {
        SEPTADatabase.databaseVersion = databaseVersion;

        String newDatabaseFilename = new StringBuilder("SEPTA_").append(databaseVersion).append(".sqlite").toString();
        setDatabaseFileName(newDatabaseFilename);
    }

    public static String getDatabaseFileName() {
        return databaseFileName;
    }

    public static void setDatabaseFileName(String databaseFileName) {
        SEPTADatabase.databaseFileName = databaseFileName;
    }

    public static String getLatestDatabaseApiUrl() {
        return LATEST_DATABASE_API_URL;
    }
}
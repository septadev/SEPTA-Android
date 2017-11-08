package org.septa.android.app.database;

import android.content.Context;
import android.database.SQLException;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class SEPTADatabase extends SQLiteAssetHelper {

    /**
     * Current packaged DB version, update number when packaged DB changes
     */
    private static final int DATABASE_VERSION = 257;
    private static final String DATABASE_FILE_NAME = "SEPTA.sqlite";

    public SEPTADatabase(Context context) {

        super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION);
        // Causes database to overwrite when version changes
        setForcedUpgrade();

//        execSQL(new String[]{"CREATE INDEX IF NOT EXISTS tripsBUSIDX on trips_bus(trip_id)"});
//        execSQL(new String[]{"CREATE INDEX IF NOT EXISTS stopsBUSSTOPID on stop_times_bus(stop_id)"});
//        execSQL(new String[]{"CREATE INDEX IF NOT EXISTS stopsBUSSSEQ on stop_times_bus(stop_sequence)"});
//
//        //execSQL(new String[]{"drop table stop_route_direction"});
//
//        execSQL(new String[]{"CREATE TABLE stop_route_direction (stop_id INT, route_id TEXT, direction_id INT)",
//                "insert into stop_route_direction(stop_id, route_id, direction_id) SELECT DISTINCT S.stop_id, T.route_id, direction_id   FROM trips_bus T join stop_times_bus ST ON T.trip_id = ST.trip_id join stops_bus S on ST.stop_id = S.stop_id",
//                "insert into stop_route_direction(stop_id, route_id, direction_id) SELECT DISTINCT S.stop_id, T.route_id, direction_id   FROM trips_MFL T join stop_times_MFL ST ON T.trip_id = ST.trip_id join stops_bus S on ST.stop_id = S.stop_id",
//                "insert into stop_route_direction(stop_id, route_id, direction_id) SELECT DISTINCT S.stop_id, T.route_id, direction_id   FROM trips_BSL T join stop_times_BSL ST ON T.trip_id = ST.trip_id join stops_bus S on ST.stop_id = S.stop_id",
//                "CREATE INDEX stop_srd_index ON stop_route_direction (stop_id)",
//                "CREATE INDEX route_srd_index ON stop_route_direction (route_id)",
//                "CREATE INDEX direction_srd_index ON stop_route_direction (direction_id)"});

    }

    private void execSQL(String statements[]) {
        try {
            for (String sql : statements)
                getWritableDatabase().execSQL(sql);
        } catch (SQLException e) {
            //e.printStackTrace();
        }
    }
}
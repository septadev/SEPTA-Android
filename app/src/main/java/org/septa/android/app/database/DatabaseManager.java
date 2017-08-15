package org.septa.android.app.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.septa.android.app.domain.RouteModel;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.support.CursorAdapterSupplier;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Route;

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

    public CursorAdapterSupplier<StopModel> getRailStopCursorAdapterSupplier() {
        return new RailStopCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getNhslStopCursorAdapterSupplier() {
        return new NhslStopCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<RouteModel> getBusRouteCursorAdapterSupplier() {
        return new BusRouteCursorAdapterSupplier();
    }

    public class NhslStopCursorAdapterSupplier implements CursorAdapterSupplier<StopModel> {
        private static final String SELECT_CLAUSE = "SELECT DISTINCT a.stop_id, stop_name, wheelchair_boarding, stop_lat, stop_lon, a.rowid AS _id FROM stops_bus a, stop_times_NHSL b WHERE b.stop_id = a.stop_id";

        @Override
        public Cursor getCursor(Context context, String whereClause) {
            StringBuilder queryString = new StringBuilder(SELECT_CLAUSE);
            if (whereClause != null) {
                queryString.append(" AND ");
                queryString.append(whereClause);
            }
            queryString.append(" ORDER BY a.stop_name");

            Cursor cursor = getDatabase(context).rawQuery(queryString.toString(), null);

            return cursor;
        }

        @Override
        public StopModel getCurrentItemFromCursor(Cursor cursor) {
            return new StopModel(cursor.getString(0), cursor.getString(1),
                    (cursor.getInt(2) == 1), cursor.getString(3), cursor.getString(4));
        }

        @Override
        public StopModel getItemFromId(Context context, Object id) {
            Cursor cursor = getCursor(context, "a.stop_id='" + id.toString() + "'");
            StopModel stopModel = null;

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    stopModel = getCurrentItemFromCursor(cursor);
                }
                cursor.close();
            }

            return stopModel;
        }
    }

    public class RailStopCursorAdapterSupplier implements CursorAdapterSupplier<StopModel> {

        private static final String SELECT_CLAUSE = "SELECT stop_id, stop_name, wheelchair_boarding, stop_lat, stop_lon, rowid AS _id FROM stops_rail";


        @Override
        public Cursor getCursor(Context context, String whereClause) {
            StringBuilder queryString = new StringBuilder(SELECT_CLAUSE);
            if (whereClause != null) {
                queryString.append(" WHERE ");
                queryString.append(whereClause);
            }
            queryString.append(" ORDER BY stop_name");

            Cursor cursor = getDatabase(context).rawQuery(queryString.toString(), null);

            return cursor;
        }

        @Override
        public StopModel getCurrentItemFromCursor(Cursor cursor) {
            return new StopModel(cursor.getString(0), cursor.getString(1),
                    (cursor.getInt(2) == 1), cursor.getString(3), cursor.getString(4));
        }

        @Override
        public StopModel getItemFromId(Context context, Object id) {
            String queryString = SELECT_CLAUSE + " WHERE stop_id='" + id.toString() + "'";
            Cursor cursor = getCursor(context, "stop_id='" + id.toString() + "'");
            StopModel stopModel = null;

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    stopModel = getCurrentItemFromCursor(cursor);
                }
                cursor.close();
            }

            return stopModel;
        }
    }

    public class BusRouteCursorAdapterSupplier implements CursorAdapterSupplier<RouteModel> {
        private static final String SELECT_CLAUSE = "SELECT route_id, route_short_name, route_long_name, route_type, rowid AS _id from routes_bus WHERE route_type=3";

        @Override
        public Cursor getCursor(Context context, String whereClause) {
            StringBuilder queryString = new StringBuilder(SELECT_CLAUSE);
            if (whereClause != null) {
                queryString.append(" AND ");
                queryString.append(whereClause);
            }
            queryString.append(" ORDER BY route_short_name");

            Cursor cursor = getDatabase(context).rawQuery(queryString.toString(), null);

            return cursor;
        }

        @Override
        public RouteModel getCurrentItemFromCursor(Cursor cursor) {
            return new RouteModel(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3));
        }

        @Override
        public RouteModel getItemFromId(Context context, Object id) {
            Cursor cursor = getCursor(context, "route_id=" + id);
            RouteModel routeModel = null;

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    routeModel = getCurrentItemFromCursor(cursor);
                }
                cursor.close();
            }
            return routeModel;
        }
    }

}
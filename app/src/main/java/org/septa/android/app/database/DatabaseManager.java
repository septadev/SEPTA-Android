package org.septa.android.app.database;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.domain.ScheduleItem;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.support.Criteria;
import org.septa.android.app.support.CursorAdapterSupplier;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Database manager
 */
public class DatabaseManager {

    public static final String TAG = DatabaseManager.class.getSimpleName();

    private static DatabaseManager instance;
    private static SQLiteDatabase database;

    private static final int BUS = 3;
    private static final int TROLLY = 0;


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

    public CursorAdapterSupplier<StopModel> getBusStopCursorAdapterSupplier() {
        return new BusStopCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<RouteDirectionModel> getBusRouteCursorAdapterSupplier() {
        return new BusRouteCursorAdapterSupplier(BUS);
    }

    public CursorAdapterSupplier<StopModel> getBusStopAfterCursorAdapterSupplier() {
        return new BusStopAfterCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getTrolleyStopCursorAdapterSupplier() {
        return new BusStopCursorAdapterSupplier();
    }
    public CursorAdapterSupplier<StopModel> getSubwayStopCursorAdapterSupplier() {
        return new SubwayStopCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<RouteDirectionModel> getTrolleyRouteCursorAdapterSupplier() {
        return new BusRouteCursorAdapterSupplier(TROLLY);
    }

    public CursorAdapterSupplier<RouteDirectionModel> getSubwayRouteCursorAdapterSupplier() {
        return new SubwayRouteCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getTrolleyStopAfterCursorAdapterSupplier() {
        return new BusStopAfterCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getSubwayStopAfterCursorAdapterSupplier() {
        return new BusStopAfterCursorAdapterSupplier();
    }

    public class NhslStopCursorAdapterSupplier implements CursorAdapterSupplier<StopModel> {
        private static final String SELECT_CLAUSE = "SELECT DISTINCT a.stop_id, stop_name, wheelchair_boarding, stop_lat, stop_lon, a.rowid AS _id FROM stops_bus a, stop_times_NHSL b WHERE b.stop_id = a.stop_id";

        @Override
        public Cursor getCursor(Context context, List<Criteria> whereClause) {
            StringBuilder queryString = new StringBuilder(SELECT_CLAUSE);

            if (whereClause != null) {
                for (Criteria c : whereClause) {
                    queryString.append(" AND ");

                    if ("stop_lon".equals(c.getFieldName()) || "stop_lat".equals(c.getFieldName())) {
                        queryString.append("CAST(a.").append(c.getFieldName()).append(" as decimal)")
                                .append(c.getOperation()).append(c.getValue().toString());
                    } else {
                        queryString.append("a.").append(c.getFieldName()).append(c.getOperation())
                                .append("'").append(c.getValue().toString()).append("'");
                    }
                }
            }

            Log.d(TAG, "Creating cursor:" + queryString.toString());

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
            List<Criteria> criteria = new ArrayList<Criteria>(1);
            criteria.add(new Criteria("stop_id", Criteria.Operation.EQ, id.toString()));
            Cursor cursor = getCursor(context, criteria);
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

        private static final String SELECT_CLAUSE = "SELECT stop_id, stop_name, wheelchair_boarding, stop_lat, stop_lon, rowid AS _id FROM stops_rail a";


        @Override
        public Cursor getCursor(Context context, List<Criteria> whereClause) {
            StringBuilder queryString = new StringBuilder(SELECT_CLAUSE);

            if (whereClause != null) {
                boolean first = true;
                for (Criteria c : whereClause) {
                    if (!first) {
                        queryString.append(" AND ");
                    } else {
                        queryString.append(" WHERE ");
                        first = false;
                    }

                    if ("stop_lon".equals(c.getFieldName()) || "stop_lat".equals(c.getFieldName())) {
                        queryString.append("CAST(a.").append(c.getFieldName()).append(" as decimal)")
                                .append(c.getOperation()).append(c.getValue().toString());
                    } else {
                        queryString.append("a.").append(c.getFieldName())
                                .append(c.getOperation()).append("'").append(c.getValue().toString()).append("'");
                    }
                }
            }

            queryString.append(" ORDER BY stop_name");

            Log.d(TAG, "Creating cursor:" + queryString.toString());

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
            List<Criteria> criteria = new ArrayList<Criteria>(1);
            criteria.add(new Criteria("stop_id", Criteria.Operation.EQ, id.toString()));
            Cursor cursor = getCursor(context, criteria);
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

    public class BusStopCursorAdapterSupplier implements CursorAdapterSupplier<StopModel> {

        private static final String SELECT_CLAUSE = "SELECT DISTINCT a.stop_id, stop_name, wheelchair_boarding, stop_lat, stop_lon, a.rowid AS _id FROM stops_bus a, trips_bus b, stop_times_bus c WHERE c.trip_id=b.trip_id and c.stop_id=a.stop_id";


        @Override
        public Cursor getCursor(Context context, List<Criteria> whereClause) {
            StringBuilder queryString = new StringBuilder(SELECT_CLAUSE);

            if (whereClause != null)
                for (Criteria c : whereClause) {
                    queryString.append(" AND ");

                    if ("stop_lon".equals(c.getFieldName()) || "stop_lat".equals(c.getFieldName())) {
                        queryString.append("CAST(a.").append(c.getFieldName()).append(" as decimal)")
                                .append(c.getOperation()).append(c.getValue().toString());
                        continue;
                    } else if ("route_id".equals(c.getFieldName()) || "direction_id".equals(c.getFieldName())) {
                        queryString.append("b.");
                    } else queryString.append("a.");

                    queryString.append(c.getFieldName()).append(c.getOperation()).append("'").append(c.getValue().toString()).append("'");
                }

            queryString.append(" ORDER BY stop_name");

            Log.d(TAG, "BusStopCursorAdapterSupplier Creating cursor:" + queryString.toString());

            Cursor cursor = getDatabase(context).rawQuery(queryString.toString(), null);

            return cursor;
        }

        @Override
        public StopModel getCurrentItemFromCursor(Cursor cursor) {
            StopModel stopModel = new StopModel(cursor.getString(0), cursor.getString(1),
                    (cursor.getInt(2) == 1), cursor.getString(3), cursor.getString(4));
            return stopModel;
        }

        @Override
        public StopModel getItemFromId(Context context, Object id) {
            String queryString = "SELECT DISTINCT a.stop_id, stop_name, wheelchair_boarding, stop_lat, stop_lon, a.rowid AS _id FROM stops_bus a where a.stop_id='" + id.toString() + "'";

            StopModel stopModel = null;
            Cursor cursor = getDatabase(context).rawQuery(queryString.toString(), null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    stopModel = getCurrentItemFromCursor(cursor);
                }
                cursor.close();
            }

            return stopModel;
        }
    }

    public class BusStopAfterCursorAdapterSupplier implements CursorAdapterSupplier<StopModel> {

        private static final String SELECT_CLAUSE = "select distinct y.stop_id, y.stop_name, y.wheelchair_boarding, y.stop_lat, y.stop_lon, y.rowid AS _id from (SELECT a.stop_id, stop_name, wheelchair_boarding, stop_lat, stop_lon, c.stop_sequence, c.trip_id, a.rowid AS _id FROM stops_bus a, trips_bus b, stop_times_bus c WHERE c.trip_id=b.trip_id and c.stop_id=a.stop_id and a.stop_id=''{0}'' and b.route_id=''{1}'' and b.direction_id=''{2}'') x, (SELECT a.stop_id, stop_name, wheelchair_boarding, stop_lat, stop_lon, c.stop_sequence, c.trip_id, a.rowid AS _id FROM stops_bus a, trips_bus b, stop_times_bus c WHERE c.trip_id=b.trip_id and c.stop_id=a.stop_id) y where x.trip_id=y.trip_id and x.stop_sequence < y.stop_sequence";


        @Override
        public Cursor getCursor(Context context, List<Criteria> whereClause) {
            if (whereClause == null)
                throw new RuntimeException("Required where clause that includes after_stop_id, route_id and direction_id with Equals Operation");

            StringBuilder queryString = new StringBuilder(SELECT_CLAUSE);
            String afterStopId = null;
            String routeId = null;
            String directionId = null;

            for (Criteria c : whereClause) {
                if ("after_stop_id".equals(c.getFieldName()) && Criteria.Operation.EQ == c.getOperation()) {
                    afterStopId = c.getValue().toString();
                    continue;
                }

                if ("route_id".equals(c.getFieldName()) && Criteria.Operation.EQ == c.getOperation()) {
                    routeId = c.getValue().toString();
                    continue;
                }
                if ("direction_id".equals(c.getFieldName()) && Criteria.Operation.EQ == c.getOperation()) {
                    directionId = c.getValue().toString();
                    continue;
                }
            }

            if (afterStopId == null || routeId == null || directionId == null) {
                throw new RuntimeException("Requires Criteria that includes after_stop_id, route_id and direction_id with Equals Operation");
            }

            queryString.append(" ORDER BY y.stop_name");

            MessageFormat form = new MessageFormat(queryString.toString());
            String query = form.format(new Object[]{afterStopId, routeId, directionId});

            Cursor cursor = getDatabase(context).rawQuery(query, null);
            Log.d(TAG, "BusStopAfterCursorAdapterSupplier Creating cursor:" + query);

            return cursor;
        }

        @Override
        public StopModel getCurrentItemFromCursor(Cursor cursor) {
            StopModel stopModel = new StopModel(cursor.getString(0), cursor.getString(1),
                    (cursor.getInt(2) == 1), cursor.getString(3), cursor.getString(4));
            stopModel.setStopSequence(cursor.getInt(5));
            return stopModel;
        }

        @Override
        public StopModel getItemFromId(Context context, Object id) {
            String queryString = "SELECT DISTINCT a.stop_id, stop_name, wheelchair_boarding, stop_lat, stop_lon, a.rowid AS _id FROM stops_bus a where a.stop_id='" + id.toString() + "'";

            StopModel stopModel = null;
            Cursor cursor = getDatabase(context).rawQuery(queryString.toString(), null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    stopModel = getCurrentItemFromCursor(cursor);
                }
                cursor.close();
            }

            return stopModel;
        }
    }


    public class BusRouteCursorAdapterSupplier implements CursorAdapterSupplier<RouteDirectionModel> {
        String selectClause;

        BusRouteCursorAdapterSupplier(int type) {
            selectClause = "SELECT a.route_id, a.route_short_name, a.route_long_name, b.DirectionDescription, b.dircode, a.route_type, a.rowid AS _id from routes_bus a, bus_stop_directions b WHERE route_type=" + type + "  AND a.route_id=b.route";
        }

        @Override
        public Cursor getCursor(Context context, List<Criteria> whereClause) {
            StringBuilder queryString = new StringBuilder(selectClause);
            if (whereClause != null) {
                queryString.append(" AND ");
                queryString.append(whereClause);
            }
            queryString.append(" ORDER BY route_short_name");

            Cursor cursor = getDatabase(context).rawQuery(queryString.toString(), null);
            Log.d(TAG, "Creating cursor:" + queryString.toString());

            return cursor;
        }

        @Override
        public RouteDirectionModel getCurrentItemFromCursor(Cursor cursor) {
            return new RouteDirectionModel(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getInt(5));
        }

        @Override
        public RouteDirectionModel getItemFromId(Context context, Object id) {
            List<Criteria> criteria = new ArrayList<Criteria>(1);
            criteria.add(new Criteria("route_id", Criteria.Operation.EQ, id.toString()));
            Cursor cursor = getCursor(context, criteria);
            RouteDirectionModel routeDirectionModel = null;

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    routeDirectionModel = getCurrentItemFromCursor(cursor);
                }
                cursor.close();
            }
            return routeDirectionModel;
        }
    }

    public class SubwayRouteCursorAdapterSupplier implements CursorAdapterSupplier<RouteDirectionModel> {
        String selectClause;

        SubwayRouteCursorAdapterSupplier() {
            selectClause = "SELECT Route, Route, Route, DirectionDescription, dircode, routeType, rowid AS _id from bus_stop_directions WHERE RouteType='RailTransit' and Route<>'NHSL'";
        }

        @Override
        public Cursor getCursor(Context context, List<Criteria> whereClause) {


            StringBuilder queryString = new StringBuilder(selectClause);
            queryString.append(" ORDER BY Route");

            Cursor cursor = getDatabase(context).rawQuery(queryString.toString(), null);
            Log.d(TAG, "Creating cursor:" + queryString.toString());

            return cursor;
        }

        @Override
        public RouteDirectionModel getCurrentItemFromCursor(Cursor cursor) {
            return new RouteDirectionModel(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getInt(5));
        }

        @Override
        public RouteDirectionModel getItemFromId(Context context, Object id) {
            List<Criteria> criteria = new ArrayList<Criteria>(1);
            criteria.add(new Criteria("route_id", Criteria.Operation.EQ, id.toString()));
            Cursor cursor = getCursor(context, criteria);
            RouteDirectionModel routeDirectionModel = null;

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    routeDirectionModel = getCurrentItemFromCursor(cursor);
                }
                cursor.close();
            }
            return routeDirectionModel;
        }
    }


    public class SubwayStopCursorAdapterSupplier implements CursorAdapterSupplier<StopModel> {

        private static final String SELECT_CLAUSE = "SELECT DISTINCT a.stop_id, stop_name, wheelchair_boarding, stop_lat, stop_lon, a.rowid AS _id FROM stops_bus a, {0} b, {1} c WHERE c.trip_id=b.trip_id and c.stop_id=a.stop_id";


        @Override
        public Cursor getCursor(Context context, List<Criteria> whereClause) {
            StringBuilder queryString = new StringBuilder(SELECT_CLAUSE);

            String tripsTable = "trips_bus";
            String stopsTable = "stop_times_bus";

            if (whereClause != null)
                for (Criteria c : whereClause) {
                    queryString.append(" AND ");

                    if ("stop_lon".equals(c.getFieldName()) || "stop_lat".equals(c.getFieldName())) {
                        queryString.append("CAST(a.").append(c.getFieldName()).append(" as decimal)")
                                .append(c.getOperation()).append(c.getValue().toString());
                        continue;
                    } else if ("route_id".equals(c.getFieldName()) || "direction_id".equals(c.getFieldName())) {
                        queryString.append("b.");
                    } else queryString.append("a.");

                    queryString.append(c.getFieldName()).append(c.getOperation()).append("''").append(c.getValue().toString()).append("''");

                    if ("route_id".equals(c.getFieldName())) {
                        if ("MFL".equals(c.getValue())) {
                            tripsTable = "trips_MFL";
                            stopsTable = "stop_times_MFL";
                        } else if ("BSL".equals(c.getValue())) {
                            tripsTable = "trips_BSL";
                            stopsTable = "stop_times_BSL";
                        }
                    }
                }

            queryString.append(" ORDER BY stop_name");

            MessageFormat form = new MessageFormat(queryString.toString());
            String query = form.format(new Object[]{tripsTable, stopsTable});

            Log.d(TAG, "SubwayStopCursorAdapterSupplier Creating cursor:" + query);

            Cursor cursor = getDatabase(context).rawQuery(query, null);

            return cursor;
        }

        @Override
        public StopModel getCurrentItemFromCursor(Cursor cursor) {
            StopModel stopModel = new StopModel(cursor.getString(0), cursor.getString(1),
                    (cursor.getInt(2) == 1), cursor.getString(3), cursor.getString(4));
            return stopModel;
        }

        @Override
        public StopModel getItemFromId(Context context, Object id) {
            String queryString = "SELECT DISTINCT a.stop_id, stop_name, wheelchair_boarding, stop_lat, stop_lon, a.rowid AS _id FROM stops_bus a where a.stop_id='" + id.toString() + "'";

            StopModel stopModel = null;
            Cursor cursor = getDatabase(context).rawQuery(queryString.toString(), null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    stopModel = getCurrentItemFromCursor(cursor);
                }
                cursor.close();
            }

            return stopModel;
        }
    }

}
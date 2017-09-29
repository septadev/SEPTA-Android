package org.septa.android.app.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.septa.android.app.R;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.domain.ScheduleModel;
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

    public static SQLiteDatabase getDatabase() {
        if (database == null) {
            throw new RuntimeException("DB was not initialized.");
        }
        return database;
    }

    public CursorAdapterSupplier<StopModel> getRailStopCursorAdapterSupplier() {
        return new RailStopCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getLineAwareRailStopCursorAdapterSupplier() {
        return new LineAwareRailStopCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getLineAwareRailStopAfterCursorAdapterSupplier() {
        return new LineAwareRailStopAfterCursorAdapterSupplier();
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

    public CursorAdapterSupplier<RouteDirectionModel> getRailRouteCursorAdapaterSupplier() {
        return new RailRouteCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<RouteDirectionModel> getNHSLRouteCursorAdapterSupplier() {
        return new NHSLRouteCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getBusStopAfterCursorAdapterSupplier() {
        return new BusStopAfterCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getTrolleyStopCursorAdapterSupplier() {
        return new BusStopCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getSubwayStopCursorAdapterSupplier() {
        //return new SubwayStopCursorAdapterSupplier();
        return new BusStopCursorAdapterSupplier();
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

    public CursorAdapterSupplier<RouteDirectionModel> getRailNoDirectionRouteCursorAdapaterSupplier() {
        return new RailNoDirectionRouteCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<RouteDirectionModel> getBusNoDirectionRouteCursorAdapaterSupplier() {
        return new BusTrolleyNoDirectionRouteCursorAdapterSupplier(BUS);
    }

    public CursorAdapterSupplier<RouteDirectionModel> getTrolleyNoDirectionRouteCursorAdapaterSupplier() {
        return new BusTrolleyNoDirectionRouteCursorAdapterSupplier(TROLLY);
    }

    public CursorAdapterSupplier<RouteDirectionModel> getSubwayNoDirectionRouteCursorAdapaterSupplier() {
        return new SubwayNoDirectionRouteCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<ScheduleModel> getNonRegionalRailScheduleCursorAdapterSupplier() {
        return new NonRegionalRailScheduleCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<ScheduleModel> getRailScheduleCursorAdapterSupplier() {
        return new RegionalRailScheduleCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getNonRailReverseAdapterSupplier() {
        return new NonRailReverseAdapterSupplier();
    }

    public CursorAdapterSupplier<RouteDirectionModel> getNonRailReverseRouteCursorAdapterSupplier() {
        return new NonRailReverseRouteCursorAdapterSupplier();
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


    public class LineAwareRailStopCursorAdapterSupplier implements CursorAdapterSupplier<StopModel> {

        //private static final String SELECT_CLAUSE = "SELECT DISTINCT a.stop_id, a.stop_name, a.wheelchair_boarding, a.stop_lat, a.stop_lon, a.rowid AS _id FROM stops_rail a, trips_rail b, stop_times_rail c, routes_rail_boundaries r  WHERE b.trip_id=c.trip_id and c.stop_id=a.stop_id and r.route_id=b.route_id and r.direction_id=b.direction_id";


        @Override
        public Cursor getCursor(Context context, List<Criteria> whereClause) {
            StringBuilder queryString = new StringBuilder(context.getResources().getString(R.string.rail_trip_start));


            String routeId = null;
            String directionId = null;

            if (whereClause != null) {
                for (Criteria c : whereClause) {
                    if ("route_id".equals(c.getFieldName())) {
                        routeId = c.getValue().toString();
                    } else if ("direction_id".equals(c.getFieldName())) {
                        directionId = c.getValue().toString();
                    } else {
                        queryString.append(" AND ");

                        if ("stop_lat".equals(c.getFieldName())) {
                            queryString.append("stopLatitude").append(c.getOperation()).append(c.getValue().toString());
                        } else if ("stop_lon".equals(c.getFieldName())) {
                            queryString.append("stopLongitude").append(c.getOperation()).append(c.getValue().toString());
                        }
                        queryString.append(" ");
                    }
                }
            }

            queryString.append(" GROUP BY S.stop_id, S.stop_name, S.stop_lat, S.stop_lon ORDER BY S.stop_name");

            MessageFormat form = new MessageFormat(queryString.toString());
            String query = form.format(new Object[]{routeId, directionId});
            Log.d(TAG, "Creating cursor:" + query);


            Cursor cursor = getDatabase(context).rawQuery(query, null);

            return cursor;
        }

        @Override
        public StopModel getCurrentItemFromCursor(Cursor cursor) {
            return new StopModel(cursor.getString(0), cursor.getString(1),
                    (cursor.getInt(3) == 1), cursor.getString(2), cursor.getString(3));
        }

        @Override
        public StopModel getItemFromId(Context context, Object id) {
            String queryString = "SELECT DISTINCT a.stop_id, a.stop_name, a.stop_lat, a.stop_lon, a.wheelchair_boarding, a.rowid AS _id FROM stops_rail a";

            queryString += " where a.stop_id=" + id.toString();

            Cursor cursor = getDatabase(context).rawQuery(queryString, null);
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

    public class LineAwareRailStopAfterCursorAdapterSupplier implements CursorAdapterSupplier<StopModel> {

        // private static final String SELECT_CLAUSE = "select distinct y.stop_id, y.stop_name, y.wheelchair_boarding, y.stop_lat, y.stop_lon, y.rowid AS _id from (SELECT a.stop_id, stop_name, wheelchair_boarding, stop_lat, stop_lon, c.stop_sequence, c.trip_id, a.rowid AS _id FROM stops_rail a, trips_rail b, stop_times_rail c WHERE c.trip_id=b.trip_id and c.stop_id=a.stop_id and a.stop_id=''{0}'' and b.route_id=''{1}'' and b.direction_id=''{2}'') x, (SELECT a.stop_id, stop_name, wheelchair_boarding, stop_lat, stop_lon, c.stop_sequence, c.trip_id, a.rowid AS _id FROM stops_rail a, trips_rail b, stop_times_rail c WHERE c.trip_id=b.trip_id and c.stop_id=a.stop_id) y where x.trip_id=y.trip_id and x.stop_sequence < y.stop_sequence";


        @Override
        public Cursor getCursor(Context context, List<Criteria> whereClause) {
            if (whereClause == null)
                throw new RuntimeException("Required where clause that includes after_stop_id, route_id and direction_id with Equals Operation");

            StringBuilder queryString = new StringBuilder(context.getResources().getString(R.string.rail_trip_end));
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

            queryString.append(" GROUP BY S.stop_id, S.stop_name, S.stop_lat, S.stop_lon ORDER BY S.stop_name");

            MessageFormat form = new MessageFormat(queryString.toString());
            String query = form.format(new Object[]{routeId, directionId, afterStopId});

            Cursor cursor = getDatabase(context).rawQuery(query, null);
            Log.d(TAG, "BusStopAfterCursorAdapterSupplier Creating cursor:" + query);

            return cursor;
        }

        @Override
        public StopModel getCurrentItemFromCursor(Cursor cursor) {
            StopModel stopModel = new StopModel(cursor.getString(0), cursor.getString(1),
                    (cursor.getInt(4) == 1), cursor.getString(2), cursor.getString(3));
            return stopModel;
        }

        @Override
        public StopModel getItemFromId(Context context, Object id) {
            String queryString = "SELECT DISTINCT a.stop_id, stop_name,  stop_lat, stop_lon, wheelchair_boarding, a.rowid AS _id FROM stops_rail a where a.stop_id='" + id.toString() + "'";

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

    public class BusStopCursorAdapterSupplier implements CursorAdapterSupplier<StopModel> {

        // private static final String SELECT_CLAUSE = "SELECT DISTINCT a.stop_id, stop_name, wheelchair_boarding, stop_lat, stop_lon, a.rowid AS _id FROM stops_bus a, trips_bus b, stop_times_bus c WHERE c.trip_id=b.trip_id and c.stop_id=a.stop_id";
        private static final String SELECT_CLAUSE = "SELECT DISTINCT a.stop_id, stop_name, wheelchair_boarding, stop_lat, stop_lon, a.rowid AS _id FROM stops_bus a, stop_route_direction b WHERE a.stop_id=b.stop_id";


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


    public class RailRouteCursorAdapterSupplier implements CursorAdapterSupplier<RouteDirectionModel> {
        //private static final String SELECT_CLAUSE = "SELECT R.Route_id, R.route_short_name route_short_name, S.stop_name route_long_name, cast (T.direction_id  as TEXT ) dircode FROM routes_rail R JOIN trips_rail T ON R.route_id = T.route_id JOIN stop_times_rail ST ON T.trip_id = ST.trip_id JOIN stops_rail S ON ST.stop_id = S.stop_id JOIN ( SELECT R.route_id, T.direction_id, max(ST.stop_sequence) max_stop_sequence FROM routes_rail R JOIN trips_rail T ON R.route_id = T.route_id JOIN stop_times_rail ST ON T.trip_id = ST.trip_id JOIN stops_rail S ON ST.stop_id = S.stop_id GROUP BY R.route_id, T.direction_id) lastStop ON R.route_id = lastStop.route_id AND T.direction_id = lastStop.direction_id AND ST.stop_sequence = lastStop.max_stop_sequence ";
        private static final String SELECT_CLAUSE = "SELECT R.Route_id, R.route_short_name route_short_name, B.terminus_name route_long_name, cast (B.direction_id  as TEXT ) dircode FROM routes_rail R join routes_rail_boundaries B on R.route_id = b.route_id";

        @Override
        public Cursor getCursor(Context context, List<Criteria> whereClause) {

            StringBuilder queryString = new StringBuilder(SELECT_CLAUSE);
            if (whereClause != null) {
                boolean first = true;
                queryString.append(" WHERE ");
                for (Criteria c : whereClause) {
                    if (!first)
                        queryString.append(" AND ");
                    else first = false;
                    if ("route_id".equals(c.getFieldName())) {
                        queryString.append("R.");
                    }
                    queryString.append(c.getFieldName()).append(c.getOperation()).append("'").append(c.getValue().toString()).append("'").append(" ");

                }
            }

            //queryString.append(" GROUP BY R.Route_id,R.route_short_name, R.route_long_name,T.direction_id ,S.stop_name");

            Cursor cursor = getDatabase(context).rawQuery(queryString.toString(), null);
            Log.d(TAG, "Creating cursor:" + queryString.toString());

            return cursor;
        }

        @Override
        public RouteDirectionModel getCurrentItemFromCursor(Cursor cursor) {
            return new RouteDirectionModel(cursor.getString(0), cursor.getString(1), cursor.getString(1), cursor.getString(2), cursor.getString(3), null);
        }

        @Override
        public RouteDirectionModel getItemFromId(Context context, Object id) {
            throw new UnsupportedOperationException("Rail Get Item from ID not supported.");
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

    public class NHSLRouteCursorAdapterSupplier implements CursorAdapterSupplier<RouteDirectionModel> {
        String selectClause;

        NHSLRouteCursorAdapterSupplier() {
            selectClause = "SELECT Route, Route, Route, DirectionDescription, dircode, routeType, rowid AS _id from bus_stop_directions WHERE RouteType='RailTransit' and Route='NHSL'";
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
            return new RouteDirectionModel(cursor.getString(0), cursor.getString(1), "Norristown High Speed Line", cursor.getString(3), cursor.getString(4), cursor.getInt(5));
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

            String id = cursor.getString(0);

            String longName = cursor.getString(2);

            if ("BSL".equals(id))
                longName = "Broad Street Line";
            else if ("MFL".equals(id))
                longName = "Market Frankford Line";
            else if ("BSO".equals(id))
                longName = "Broad Street Overnight";
            else if ("MFO".equals(id))
                longName = "Market Frankford Overnight";

            return new RouteDirectionModel(cursor.getString(0), cursor.getString(1), longName, cursor.getString(3), cursor.getString(4), cursor.getInt(5));
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

    public class RailNoDirectionRouteCursorAdapterSupplier implements CursorAdapterSupplier<RouteDirectionModel> {
        private static final String SELECT_CLAUSE = "SELECT route_id, route_short_name, route_long_name, route_type FROM routes_rail a";

        @Override
        public Cursor getCursor(Context context, List<Criteria> whereClause) {

            Cursor cursor = getDatabase(context).rawQuery(SELECT_CLAUSE, null);
            Log.d(TAG, "Creating cursor:" + SELECT_CLAUSE);

            return cursor;
        }

        @Override
        public RouteDirectionModel getCurrentItemFromCursor(Cursor cursor) {
            return new RouteDirectionModel(cursor.getString(0), cursor.getString(1), cursor.getString(2), null, null, cursor.getInt(3));
        }

        @Override
        public RouteDirectionModel getItemFromId(Context context, Object id) {
            throw new UnsupportedOperationException("Rail Get Item from ID not supported.");
        }
    }

    public class BusTrolleyNoDirectionRouteCursorAdapterSupplier implements CursorAdapterSupplier<RouteDirectionModel> {
        String selectClause;


        public BusTrolleyNoDirectionRouteCursorAdapterSupplier(int type) {
            selectClause = "SELECT route_id, route_short_name, route_long_name, route_type FROM routes_bus a where a.route_type=" + type + " ORDER BY route_short_name";
        }

        @Override
        public Cursor getCursor(Context context, List<Criteria> whereClause) {

            Cursor cursor = getDatabase(context).rawQuery(selectClause, null);
            Log.d(TAG, "Creating cursor:" + selectClause);

            return cursor;
        }

        @Override
        public RouteDirectionModel getCurrentItemFromCursor(Cursor cursor) {
            return new RouteDirectionModel(cursor.getString(0), cursor.getString(1), cursor.getString(2), null, null, cursor.getInt(3));
        }

        @Override
        public RouteDirectionModel getItemFromId(Context context, Object id) {
            throw new UnsupportedOperationException("Rail Get Item from ID not supported.");
        }
    }

    public class SubwayNoDirectionRouteCursorAdapterSupplier implements CursorAdapterSupplier<RouteDirectionModel> {
        public static final String SELECT_CLAUSE =
                "SELECT route_id, route_short_name, route_long_name, route_type FROM routes_bus a where a.route_type=1 or route_id in ('MFO', 'BSO') ORDER BY route_short_name";


        @Override
        public Cursor getCursor(Context context, List<Criteria> whereClause) {

            Cursor cursor = getDatabase(context).rawQuery(SELECT_CLAUSE, null);
            Log.d(TAG, "Creating cursor:" + SELECT_CLAUSE);

            return cursor;
        }

        @Override
        public RouteDirectionModel getCurrentItemFromCursor(Cursor cursor) {
            return new RouteDirectionModel(cursor.getString(0), cursor.getString(1), cursor.getString(2), null, null, cursor.getInt(3));
        }

        @Override
        public RouteDirectionModel getItemFromId(Context context, Object id) {
            throw new UnsupportedOperationException("Rail Get Item from ID not supported.");
        }
    }


    public class NonRegionalRailScheduleCursorAdapterSupplier implements CursorAdapterSupplier<ScheduleModel> {

        @Override
        public Cursor getCursor(Context context, List<Criteria> whereClause) {
            String queryString = context.getResources().getString(R.string.bus_trip_schedule_query);

            String start_stop_id = null;
            String service_id = null;
            String direction_id = null;
            String end_stop_id = null;
            String route_id = null;

            if (whereClause != null) {
                for (Criteria c : whereClause) {
                    if ("start_stop_id".equals(c.getFieldName())) {
                        start_stop_id = c.getValue().toString();
                    } else if ("service_id".equals(c.getFieldName())) {
                        service_id = c.getValue().toString();
                    } else if ("direction_id".equals(c.getFieldName())) {
                        direction_id = c.getValue().toString();
                    } else if ("end_stop_id".equals(c.getFieldName())) {
                        end_stop_id = c.getValue().toString();
                    } else if ("route_id".equals(c.getFieldName())) {
                        route_id = c.getValue().toString();
                    }
                }

                String tableSuffix = "bus";

                if ("MFL".equals(route_id)) {
                    tableSuffix = "mfl";
                } else if ("BSL".equals(route_id))
                    tableSuffix = "bsl";
                else if ("NHSL".equals(route_id)) {
                    tableSuffix = "nhsl";
                }

                MessageFormat form = new MessageFormat(queryString.toString());
                String query = form.format(new Object[]{start_stop_id, service_id, direction_id, end_stop_id, tableSuffix});
                Log.d(TAG, "Creating cursor:" + query);
                Cursor cursor = getDatabase(context).rawQuery(query, null);

                return cursor;
            }

            throw new RuntimeException("Need a where clause.");
        }

        @Override
        public ScheduleModel getCurrentItemFromCursor(Cursor cursor) {
            return new ScheduleModel(cursor.getString(2), cursor.getInt(1), cursor.getInt(0));
        }

        @Override
        public ScheduleModel getItemFromId(Context context, Object id) {
            return null;
        }


    }

    public class RegionalRailScheduleCursorAdapterSupplier implements CursorAdapterSupplier<ScheduleModel> {

        @Override
        public Cursor getCursor(Context context, List<Criteria> whereClause) {
            String queryString = context.getResources().getString(R.string.rail_trip_schedule_query);

            String start_stop_id = null;
            String service_id = null;
            String direction_id = null;
            String end_stop_id = null;

            if (whereClause != null) {
                for (Criteria c : whereClause) {
                    if ("start_stop_id".equals(c.getFieldName())) {
                        start_stop_id = c.getValue().toString();
                    }
                    if ("service_id".equals(c.getFieldName())) {
                        service_id = c.getValue().toString();
                    }
                    if ("direction_id".equals(c.getFieldName())) {
                        direction_id = c.getValue().toString();
                    }
                    if ("end_stop_id".equals(c.getFieldName())) {
                        end_stop_id = c.getValue().toString();
                    }
                }

                MessageFormat form = new MessageFormat(queryString.toString());
                String query = form.format(new Object[]{start_stop_id, service_id, direction_id, end_stop_id});
                Log.d(TAG, "Creating cursor:" + query);
                Cursor cursor = getDatabase(context).rawQuery(query, null);

                return cursor;
            }

            throw new RuntimeException("Need a where clause.");
        }

        @Override
        public ScheduleModel getCurrentItemFromCursor(Cursor cursor) {
            return new ScheduleModel(cursor.getString(2), cursor.getInt(1), cursor.getInt(0));
        }

        @Override
        public ScheduleModel getItemFromId(Context context, Object id) {
            return null;
        }


    }

    public class NonRailReverseAdapterSupplier implements CursorAdapterSupplier<StopModel> {

        private static final String SELECT_CLAUSE = "  SELECT s.stop_id, stop_name, wheelchair_boarding, stop_lat, stop_lon, s.rowid AS _id FROM reverseStopSearch RSS JOIN stops_bus S ON RSS.reverse_stop_id = S.stop_id";


        @Override
        public Cursor getCursor(Context context, List<Criteria> whereClause) {
            StringBuilder queryString = new StringBuilder(SELECT_CLAUSE);

            if (whereClause != null)
                for (Criteria c : whereClause) {
                    queryString.append(" AND ");

                    if ("stop_lon".equals(c.getFieldName()) || "stop_lat".equals(c.getFieldName())) {
                        queryString.append("CAST(s.").append(c.getFieldName()).append(" as decimal)")
                                .append(c.getOperation()).append(c.getValue().toString());
                        continue;
                    } else if ("route_short_name".equals(c.getFieldName()) || "stop_id".equals(c.getFieldName())) {
                        queryString.append("RSS.");
                    } else queryString.append("s.");

                    queryString.append(c.getFieldName()).append(c.getOperation()).append("'").append(c.getValue().toString()).append("'");
                }

            Log.d(TAG, "NonRailReverseAdapterSupplier Creating cursor:" + queryString.toString());

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

    public class NonRailReverseRouteCursorAdapterSupplier implements CursorAdapterSupplier<RouteDirectionModel> {
        String SELECT_CLAUSE = "SELECT a.route_id, a.route_short_name, a.route_long_name, b.DirectionDescription, b.dircode, a.route_type, a.rowid AS _id from routes_bus a, bus_stop_directions b WHERE  a.route_id=b.route";

        @Override
        public Cursor getCursor(Context context, List<Criteria> whereClause) {
            StringBuilder queryString = new StringBuilder(SELECT_CLAUSE);


            for (Criteria c : whereClause) {
                queryString.append(" AND ");
                if ("dircode".equals(c.getFieldName())) {
                    queryString.append("b.");
                } else if ("route_id".equals(c.getFieldName())) {
                    queryString.append("a.");
                }
                queryString.append(c.getFieldName()).append(c.getOperation()).append("'").append(c.getValue().toString()).append("'");

            }

            Cursor cursor = getDatabase(context).rawQuery(queryString.toString(), null);
            Log.d(TAG, "Creating cursor:" + queryString.toString());

            return cursor;
        }

        @Override
        public RouteDirectionModel getCurrentItemFromCursor(Cursor cursor) {
            String id = cursor.getString(0);

            String longName = cursor.getString(2);

            if ("BSL".equals(id))
                longName = "Broad Street Line";
            else if ("MFL".equals(id))
                longName = "Market Frankfor d Line";
            else if ("BSO".equals(id))
                longName = "Broad Street Overnight";
            else if ("MFO".equals(id))
                longName = "Market Frankford Overnight";

            return new RouteDirectionModel(cursor.getString(0), cursor.getString(1), longName, cursor.getString(3), cursor.getString(4), cursor.getInt(5));

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


    public class RailReverseRouteCursorAdapterSupplier implements CursorAdapterSupplier<RouteDirectionModel> {
        String SELECT_CLAUSE = "SELECT a.route_id, a.route_short_name, a.route_long_name, b.DirectionDescription, b.dircode, a.route_type, a.rowid AS _id from routes_bus a, bus_stop_directions b WHERE  a.route_id=b.route";

        @Override
        public Cursor getCursor(Context context, List<Criteria> whereClause) {
            StringBuilder queryString = new StringBuilder(SELECT_CLAUSE);


            for (Criteria c : whereClause) {
                queryString.append(" AND ");
                if ("dircode".equals(c.getFieldName())) {
                    queryString.append("b.");
                } else if ("route_id".equals(c.getFieldName())) {
                    queryString.append("a.");
                }
                queryString.append(c.getFieldName()).append(c.getOperation()).append("'").append(c.getValue().toString()).append("'");

            }

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
}
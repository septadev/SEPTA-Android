package org.septa.android.app.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.database.update.DatabaseSharedPrefsUtils;
import org.septa.android.app.database.update.TempDatabaseManager;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.domain.ScheduleModel;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.support.Criteria;
import org.septa.android.app.support.CursorAdapterSupplier;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

class CursorSuppliers implements Serializable {
    private static final String TAG = CursorSuppliers.class.getSimpleName();

    private static SQLiteDatabase getDatabase(Context context) {
        return DatabaseManager.getDatabase(context);
    }

    public static class DatabaseVersionCursorAdapterSupplier implements CursorAdapterSupplier<Integer> {
        String SELECT_CLAUSE = "SELECT version FROM dbVersion";

        @Override
        public Cursor getCursor(Context context, List<Criteria> whereClause) {
            return TempDatabaseManager.getDatabaseWithVersion(context, DatabaseSharedPrefsUtils.getVersionDownloaded(context)).rawQuery(SELECT_CLAUSE, null);
        }

        @Override
        public Integer getCurrentItemFromCursor(Cursor cursor) {
            Integer versionNumber = Integer.parseInt(cursor.getString(0));
            return versionNumber;
        }

        @Override
        public Integer getItemFromId(Context context, Object id) {
            throw new RuntimeException("Not supported by getVersionOfDatabase");
        }
    }

    /**
     * get all the stops for rail next to arrive picker (no line specified)
     */
    static class RailStopCursorAdapterSupplier implements CursorAdapterSupplier<StopModel> {

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

            return getDatabase(context).rawQuery(queryString.toString(), null);
        }

        @Override
        public StopModel getCurrentItemFromCursor(Cursor cursor) {
            return new StopModel(cursor.getString(0), cursor.getString(1),
                    (cursor.getInt(2) == 1), cursor.getString(3), cursor.getString(4));
        }

        @Override
        public StopModel getItemFromId(Context context, Object id) {
            List<Criteria> criteria = new ArrayList<>(1);
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

    /**
     * gets the starting stops for rail schedule picker on a line
     */
    static class LineAwareRailStopCursorAdapterSupplier implements CursorAdapterSupplier<StopModel> {

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

            return getDatabase(context).rawQuery(query, null);
        }

        @Override
        public StopModel getCurrentItemFromCursor(Cursor cursor) {
            StopModel stopModel = new StopModel(cursor.getString(0), cursor.getString(1),
                    (cursor.getInt(3) == 1), cursor.getString(2), cursor.getString(3));
            stopModel.setStopSequence(cursor.getInt(5));
            return stopModel;
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

    /**
     * gets the destination stops for rail schedule picker on a line
     */
    static class LineAwareRailStopAfterCursorAdapterSupplier implements CursorAdapterSupplier<StopModel> {

        @Override
        public Cursor getCursor(Context context, List<Criteria> whereClause) {
            if (whereClause == null) {
                throw new RuntimeException("Required where clause that includes after_stop_id, route_id and direction_id with Equals Operation");
            }

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
            Log.d(TAG, "Creating cursor:" + query);

            return cursor;
        }

        @Override
        public StopModel getCurrentItemFromCursor(Cursor cursor) {
            StopModel stopModel = new StopModel(cursor.getString(0), cursor.getString(1),
                    (cursor.getInt(4) == 1), cursor.getString(2), cursor.getString(3));
            stopModel.setStopSequence(cursor.getInt(5));
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

    /**
     * gets the starting stops for bus, subway, trolley, or NHSL on a route
     */
    static class TransitStopCursorAdapterSupplier implements CursorAdapterSupplier<StopModel> {

        private static final String SELECT_CLAUSE = "SELECT DISTINCT a.stop_id, stop_name, wheelchair_boarding, stop_lat, stop_lon, b.route_sequence, a.rowid AS _id FROM stops_bus a, stop_route_direction b WHERE a.stop_id=b.stop_id";

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

            Log.d(TAG, "TransitStopCursorAdapterSupplier Creating cursor:" + queryString.toString());

            Cursor cursor = getDatabase(context).rawQuery(queryString.toString(), null);

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

    /**
     * gets the destination stops for bus, subway, trolley, or NHSL on a route
     */
    static class TransitStopAfterCursorAdapterSupplier implements CursorAdapterSupplier<StopModel> {

        private static final String SELECT_CLAUSE = "select distinct y.stop_id, y.stop_name, y.wheelchair_boarding, y.stop_lat, y.stop_lon, b.route_sequence, y.rowid AS _id from\n" +
                "(select distinct stop_id, route_sequence from stop_route_direction where stop_id=''{0}'' and route_id=''{1}'' and direction_id=''{2}'' ) a,\n" +
                "(select distinct stop_id, route_sequence from stop_route_direction where stop_id<>''{0}'' and route_id=''{1}'' and direction_id=''{2}'' ) b,\n" +
                "stops_bus y where a.route_sequence < b.route_sequence and y.stop_id=b.stop_id";

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
            Log.d(TAG, "TransitStopAfterCursorAdapterSupplier Creating cursor:" + query);

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
            String queryString = "SELECT a.stop_id, stop_name, wheelchair_boarding, stop_lat, stop_lon, a.rowid AS _id FROM stops_bus a where a.stop_id='" + id.toString() + "'";

            StopModel stopModel = null;
            Cursor cursor = getDatabase(context).rawQuery(queryString, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    stopModel = getCurrentItemFromCursor(cursor);
                }
                cursor.close();
            }

            return stopModel;
        }
    }

    static class RailRouteCursorAdapterSupplier implements CursorAdapterSupplier<RouteDirectionModel> {

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

    static class BusRouteCursorAdapterSupplier implements CursorAdapterSupplier<RouteDirectionModel> {
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
            List<Criteria> criteria = new ArrayList<>(1);
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

    static class NHSLRouteCursorAdapterSupplier implements CursorAdapterSupplier<RouteDirectionModel> {
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
            List<Criteria> criteria = new ArrayList<>(1);
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

    static class SubwayRouteCursorAdapterSupplier implements CursorAdapterSupplier<RouteDirectionModel> {
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

            if ("BSL".equals(id)) {
                longName = "Broad Street Line";
            } else if ("MFL".equals(id)) {
                longName = "Market Frankford Line";
            } else if ("BSO".equals(id)) {
                longName = "Broad Street Overnight";
            } else if ("MFO".equals(id)) {
                longName = "Market Frankford Overnight";
            }

            return new RouteDirectionModel(cursor.getString(0), cursor.getString(1), longName, cursor.getString(3), cursor.getString(4), cursor.getInt(5));
        }

        @Override
        public RouteDirectionModel getItemFromId(Context context, Object id) {
            List<Criteria> criteria = new ArrayList<>(1);
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

    static class RailNoDirectionRouteCursorAdapterSupplier implements CursorAdapterSupplier<RouteDirectionModel> {
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

    static class BusTrolleyNoDirectionRouteCursorAdapterSupplier implements CursorAdapterSupplier<RouteDirectionModel> {
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

    static class SubwayNoDirectionRouteCursorAdapterSupplier implements CursorAdapterSupplier<RouteDirectionModel> {
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

    static class NonRegionalRailScheduleCursorAdapterSupplier implements CursorAdapterSupplier<ScheduleModel> {

        @Override
        public Cursor getCursor(Context context, List<Criteria> whereClause) {
            String queryString = context.getResources().getString(R.string.bus_trip_schedule_query);

            String start_stop_id = null;
            String service_id = null;
            String direction_id = null;
            String end_stop_id = null;

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
                    }
                }

                String tableSuffix = "bus";

                MessageFormat form = new MessageFormat(queryString);
                String query = form.format(new Object[]{start_stop_id, service_id, direction_id, end_stop_id, tableSuffix});
                Log.d(TAG, "Creating cursor:" + query);

                return getDatabase(context).rawQuery(query, null);
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

    static class RegionalRailScheduleCursorAdapterSupplier implements CursorAdapterSupplier<ScheduleModel> {

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

                MessageFormat form = new MessageFormat(queryString);
                String query = form.format(new Object[]{start_stop_id, service_id, direction_id, end_stop_id});
                Log.d(TAG, "Creating cursor:" + query);

                return getDatabase(context).rawQuery(query, null);
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

    static class NonRailReverseAdapterSupplier implements CursorAdapterSupplier<StopModel> {

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

            return getDatabase(context).rawQuery(queryString.toString(), null);
        }

        @Override
        public StopModel getCurrentItemFromCursor(Cursor cursor) {
            return new StopModel(cursor.getString(0), cursor.getString(1),
                    (cursor.getInt(2) == 1), cursor.getString(3), cursor.getString(4));
        }

        @Override
        public StopModel getItemFromId(Context context, Object id) {
            String queryString = "SELECT DISTINCT a.stop_id, stop_name, wheelchair_boarding, stop_lat, stop_lon, a.rowid AS _id FROM stops_bus a where a.stop_id='" + id.toString() + "'";

            StopModel stopModel = null;
            Cursor cursor = getDatabase(context).rawQuery(queryString, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    stopModel = getCurrentItemFromCursor(cursor);
                }
                cursor.close();
            }

            return stopModel;
        }
    }

    static class NonRailReverseRouteCursorAdapterSupplier implements CursorAdapterSupplier<RouteDirectionModel> {
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

            switch (id) {
                case "BSL":
                    longName = "Broad Street Line";
                    break;
                case "MFL":
                    longName = "Market Frankford Line";
                    break;
                case "BSO":
                    longName = "Broad Street Overnight";
                    break;
                case "MFO":
                    longName = "Market Frankford Overnight";
                    break;
            }

            return new RouteDirectionModel(cursor.getString(0), cursor.getString(1), longName, cursor.getString(3), cursor.getString(4), cursor.getInt(5));

        }

        @Override
        public RouteDirectionModel getItemFromId(Context context, Object id) {
            List<Criteria> criteria = new ArrayList<>(1);
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

    static class TransitTypeHolidayIndicatorCursorAdapterSupplier implements CursorAdapterSupplier<Boolean> {

        String tableName;

        TransitTypeHolidayIndicatorCursorAdapterSupplier(TransitType transitType) {
            if (transitType == TransitType.RAIL) {
                tableName = "holiday_rail";
            } else {
                tableName = "holiday_bus";
            }
        }

        @Override
        public Cursor getCursor(Context context, List<Criteria> whereClause) {
            String dateString = null;

            for (Criteria c : whereClause) {
                if ("date".equalsIgnoreCase(c.getFieldName()) && c.getOperation() == Criteria.Operation.EQ && c.getValue() instanceof Date) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(((Date) c.getValue()).getTime());
                    StringBuilder builder = new StringBuilder();
                    builder.append(String.format("%04d", cal.get(Calendar.YEAR)))
                            .append(String.format("%02d", cal.get(Calendar.MONTH)))
                            .append(String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)));

                    dateString = builder.toString();
                }
            }

            if (dateString == null) {
                throw new RuntimeException("Missing date where clause.");
            }

            String queryString = "SELECT CAST(CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END AS BIT) FROM " + tableName + " WHERE date = '" + dateString + "'";
            Log.d(TAG, "Creating cursor:" + queryString.toString());

            return getDatabase(context).rawQuery(queryString, null);
        }

        @Override
        public Boolean getCurrentItemFromCursor(Cursor cursor) {
            if (cursor.getInt(0) != 0)
                return Boolean.FALSE;
            else return Boolean.TRUE;
        }

        @Override
        public Boolean getItemFromId(Context context, Object id) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(((Date) id).getTime());
            StringBuilder builder = new StringBuilder();
            builder.append(String.format("%04d", cal.get(Calendar.YEAR)));
            builder.append(String.format("%02d", cal.get(Calendar.MONTH) + 1));
            builder.append(String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)));
            String queryString = "SELECT CAST(CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END AS BIT) FROM " + tableName + " WHERE date = '" + builder.toString() + "'";

            Cursor cursor = getDatabase(context).rawQuery(queryString, null);

            if (cursor.moveToFirst()) {
                if (cursor.getInt(0) == 0)
                    return Boolean.FALSE;
                else return Boolean.TRUE;
            } else
                return Boolean.FALSE;
        }
    }

}

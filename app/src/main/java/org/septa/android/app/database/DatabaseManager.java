package org.septa.android.app.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.septa.android.app.TransitType;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.domain.ScheduleModel;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.support.Criteria;
import org.septa.android.app.support.CursorAdapterSupplier;

import java.util.List;

/**
 * Database manager
 */
public class DatabaseManager {

    public static final String TAG = DatabaseManager.class.getSimpleName();

    private static DatabaseManager instance;
    private static SQLiteDatabase database;

    private static final int BUS = 3;
    private static final int TROLLEY = 0;

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
            database = new SEPTADatabase(context, SEPTADatabaseUtils.getVersionInstalled(context), SEPTADatabaseUtils.getDatabaseFilename(context)).getReadableDatabase();
        }
    }

    public static synchronized void reinitDatabase(Context context) {
        if (database != null) {
            database.close();
            database = new SEPTADatabase(context, SEPTADatabaseUtils.getVersionInstalled(context), SEPTADatabaseUtils.getDatabaseFilename(context)).getReadableDatabase();
        }
    }

    public static synchronized SQLiteDatabase getDatabase(Context context) {
        if (database == null) {
            database = new SEPTADatabase(context, SEPTADatabaseUtils.getVersionInstalled(context), SEPTADatabaseUtils.getDatabaseFilename(context)).getReadableDatabase();
        }
        return database;
    }

    public static SQLiteDatabase getDatabase() {
        if (database == null) {
            throw new RuntimeException("DB was not initialized.");
        }
        return database;
    }

    public CursorAdapterSupplier<Integer> getVersionOfDatabase() {
        return new CursorSuppliers.DatabaseVersionCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getRailStopCursorAdapterSupplier() {
        return new CursorSuppliers.RailStopCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getLineAwareRailStopCursorAdapterSupplier() {
        return new CursorSuppliers.LineAwareRailStopCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getLineAwareRailStopAfterCursorAdapterSupplier() {
        return new CursorSuppliers.LineAwareRailStopAfterCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getBusStopCursorAdapterSupplier() {
        return new CursorSuppliers.TransitStopCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<RouteDirectionModel> getBusRouteCursorAdapterSupplier() {
        return new CursorSuppliers.BusRouteCursorAdapterSupplier(BUS);
    }

    public CursorAdapterSupplier<RouteDirectionModel> getRailRouteCursorAdapterSupplier() {
        return new CursorSuppliers.RailRouteCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<RouteDirectionModel> getNHSLRouteCursorAdapterSupplier() {
        return new CursorSuppliers.NHSLRouteCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getBusStopAfterCursorAdapterSupplier() {
        return new CursorSuppliers.TransitStopAfterCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getTrolleyStopCursorAdapterSupplier() {
        return new CursorSuppliers.TransitStopCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getSubwayStopCursorAdapterSupplier() {
        return new CursorSuppliers.TransitStopCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<RouteDirectionModel> getTrolleyRouteCursorAdapterSupplier() {
        return new CursorSuppliers.BusRouteCursorAdapterSupplier(TROLLEY);
    }

    public CursorAdapterSupplier<RouteDirectionModel> getSubwayRouteCursorAdapterSupplier() {
        return new CursorSuppliers.SubwayRouteCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getTrolleyStopAfterCursorAdapterSupplier() {
        return new CursorSuppliers.TransitStopAfterCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getSubwayStopAfterCursorAdapterSupplier() {
        return new CursorSuppliers.TransitStopAfterCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<RouteDirectionModel> getRailNoDirectionRouteCursorAdapterSupplier() {
        return new CursorSuppliers.RailNoDirectionRouteCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<RouteDirectionModel> getBusNoDirectionRouteCursorAdapterSupplier() {
        return new CursorSuppliers.BusTrolleyNoDirectionRouteCursorAdapterSupplier(BUS);
    }

    public CursorAdapterSupplier<RouteDirectionModel> getTrolleyNoDirectionRouteCursorAdapterSupplier() {
        return new CursorSuppliers.BusTrolleyNoDirectionRouteCursorAdapterSupplier(TROLLEY);
    }

    public CursorAdapterSupplier<RouteDirectionModel> getSubwayNoDirectionRouteCursorAdapterSupplier() {
        return new CursorSuppliers.SubwayNoDirectionRouteCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<ScheduleModel> getNonRegionalRailScheduleCursorAdapterSupplier() {
        return new CursorSuppliers.NonRegionalRailScheduleCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<ScheduleModel> getRailScheduleCursorAdapterSupplier() {
        return new CursorSuppliers.RegionalRailScheduleCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getNonRailReverseAdapterSupplier() {
        return new CursorSuppliers.NonRailReverseAdapterSupplier();
    }

    public CursorAdapterSupplier<RouteDirectionModel> getNonRailReverseRouteCursorAdapterSupplier() {
        return new CursorSuppliers.NonRailReverseRouteCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<String> getDirectionCodeForTrainOnRoute() {
        return new CursorAdapterSupplier<String>() {
            String SELECT_CLAUSE = "select distinct direction_id from trips_rail T where ";

            @Override
            public Cursor getCursor(Context context, List<Criteria> whereClause) {
                StringBuilder queryString = new StringBuilder(SELECT_CLAUSE);

                boolean first = true;
                for (Criteria c : whereClause) {
                    if (!first)
                        queryString.append(" AND ");
                    first = false;

                    if ("trainId".equals(c.getFieldName())) {
                        queryString.append("T.block_id=").append(c.getValue());
                    } else if ("routeId".equals(c.getFieldName())) {
                        queryString.append("T.route_id='").append(c.getValue()).append("'");
                    }

                }
                String query = queryString.toString();
                Log.d(TAG, "Creating cursor:" + query);

                return DatabaseManager.getDatabase().rawQuery(query, null);
            }

            @Override
            public String getCurrentItemFromCursor(Cursor cursor) {
                String directionCode = cursor.getString(0);
                return directionCode;
            }

            @Override
            public String getItemFromId(Context context, Object id) {
                throw new RuntimeException("Not supported by getDirectionCodeForTrainOnRoute");
            }
        };
    }

    public CursorAdapterSupplier<Boolean> getHolidayIndicatorCursorAdapterSupplier(TransitType transitType) {
        return new CursorSuppliers.TransitTypeHolidayIndicatorCursorAdapterSupplier(transitType);
    }
}
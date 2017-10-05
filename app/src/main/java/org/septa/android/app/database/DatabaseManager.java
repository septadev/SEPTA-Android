package org.septa.android.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.domain.ScheduleModel;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.support.CursorAdapterSupplier;

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
        return new CursorSuppliers.RailStopCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getLineAwareRailStopCursorAdapterSupplier() {
        return new CursorSuppliers.LineAwareRailStopCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getLineAwareRailStopAfterCursorAdapterSupplier() {
        return new CursorSuppliers.LineAwareRailStopAfterCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getNhslStopCursorAdapterSupplier() {
        return new CursorSuppliers.NhslStopCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getBusStopCursorAdapterSupplier() {
        return new CursorSuppliers.BusStopCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<RouteDirectionModel> getBusRouteCursorAdapterSupplier() {
        return new CursorSuppliers.BusRouteCursorAdapterSupplier(BUS);
    }

    public CursorAdapterSupplier<RouteDirectionModel> getRailRouteCursorAdapaterSupplier() {
        return new CursorSuppliers.RailRouteCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<RouteDirectionModel> getNHSLRouteCursorAdapterSupplier() {
        return new CursorSuppliers.NHSLRouteCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getBusStopAfterCursorAdapterSupplier() {
        return new CursorSuppliers.BusStopAfterCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getTrolleyStopCursorAdapterSupplier() {
        return new CursorSuppliers.BusStopCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getSubwayStopCursorAdapterSupplier() {
        //return new CursorSuppliers.SubwayStopCursorAdapterSupplier();
        return new CursorSuppliers.BusStopCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<RouteDirectionModel> getTrolleyRouteCursorAdapterSupplier() {
        return new CursorSuppliers.BusRouteCursorAdapterSupplier(TROLLY);
    }

    public CursorAdapterSupplier<RouteDirectionModel> getSubwayRouteCursorAdapterSupplier() {
        return new CursorSuppliers.SubwayRouteCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getTrolleyStopAfterCursorAdapterSupplier() {
        return new CursorSuppliers.BusStopAfterCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<StopModel> getSubwayStopAfterCursorAdapterSupplier() {
        return new CursorSuppliers.BusStopAfterCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<RouteDirectionModel> getRailNoDirectionRouteCursorAdapaterSupplier() {
        return new CursorSuppliers.RailNoDirectionRouteCursorAdapterSupplier();
    }

    public CursorAdapterSupplier<RouteDirectionModel> getBusNoDirectionRouteCursorAdapaterSupplier() {
        return new CursorSuppliers.BusTrolleyNoDirectionRouteCursorAdapterSupplier(BUS);
    }

    public CursorAdapterSupplier<RouteDirectionModel> getTrolleyNoDirectionRouteCursorAdapaterSupplier() {
        return new CursorSuppliers.BusTrolleyNoDirectionRouteCursorAdapterSupplier(TROLLY);
    }

    public CursorAdapterSupplier<RouteDirectionModel> getSubwayNoDirectionRouteCursorAdapaterSupplier() {
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


}
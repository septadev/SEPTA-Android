/*
 * BusRoutesModel.java
 * Last modified on 04-21-2014 16:25-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.TimingLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.septa.android.app.databases.SEPTADatabase;

public class RoutesModel {
    private static final String TAG = RoutesModel.class.getName();
    private HashMap<String, RouteModel> busRoutesByRouteId;
    private HashMap<String, RouteModel> busRoutesByRouteShortName;
    private RouteType routeType;

    public enum RouteType { BUS_ROUTE, RAIL_ROUTE }

    public RoutesModel(RouteType routeType) {

        this.busRoutesByRouteId = new HashMap<String, RouteModel>();
        this.busRoutesByRouteShortName = new HashMap<String, RouteModel>();

        this.routeType = routeType;
    }

    public RoutesModel(RouteType routeType, int size) {
        this.busRoutesByRouteId = new HashMap<String, RouteModel>(size);
        this.busRoutesByRouteShortName = new HashMap<String, RouteModel>(size);

        this.routeType = routeType;
    }

    public List<RouteModel>getBusRouteModels() {

        ArrayList<RouteModel> arrayList = new ArrayList<RouteModel>(this.busRoutesByRouteId.values());

        return arrayList;
    }

    public RouteModel getBusRouteByRouteShortName(String routeShortName) {

        return busRoutesByRouteShortName.get(routeShortName);
    }

    public void setBusRouteByRouteShortName(String routeShortName, RouteModel busRoute) {

        busRoutesByRouteShortName.put(routeShortName, busRoute);
    }

    public RouteModel getBusRouteByRouteId(String routeId) {

        return busRoutesByRouteId.get(routeId);
    }

    public void setBusRouteByRouteId(String routeId, RouteModel busRoute) {

        busRoutesByRouteId.put(routeId, busRoute);
    }

    public int getBusRoutesCount() {

        return busRoutesByRouteId.size();
    }

    public void loadRoutes(Context context) {
        TimingLogger timings = new TimingLogger("RoutesModel", "loadRoutes");

        SEPTADatabase septaDatabase = new SEPTADatabase(context);
        SQLiteDatabase database = septaDatabase.getReadableDatabase();

        timings.addSplit("  opened the database...");

        // given the route type included in the call, alter the select to
        String queryString = "SELECT s.route_id, r.route_short_name, r.route_long_name, r.route_type, s.service_id, MIN(min) as min, MAX(max) as max FROM serviceHours s JOIN routes_XXXXX r ON r.route_short_name = s.route_short_name GROUP BY s.route_id, service_id ORDER BY s.route_id";
        if (routeType == RouteType.BUS_ROUTE) {
            queryString = queryString.replace("XXXXX", "bus");
        } else {
            queryString = queryString.replace("XXXXX", "rail");
        }

        Cursor cursor = database.rawQuery(queryString, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    RouteModel busRoute = getBusRouteByRouteId(cursor.getString(0));

                    if (busRoute == null) {
                        busRoute = new RouteModel();

                        busRoute.setRouteShortName(cursor.getString(1));
                        busRoute.setRouteLongName(cursor.getString(2));
                        busRoute.setRouteId(cursor.getString(0));
                        busRoute.setRouteType(Integer.valueOf(cursor.getString(3)));
                    }

                    MinMaxHoursModel minMaxHours = new MinMaxHoursModel(Integer.valueOf(cursor.getString(5)), Integer.valueOf(cursor.getString(6)));

                    busRoute.addMinMaxHoursToRoute(cursor.getString(3), minMaxHours);

                    setBusRouteByRouteId(cursor.getString(0), busRoute);
                    setBusRouteByRouteShortName(cursor.getString(1), busRoute);
                    timings.addSplit("      processed a record...");
                } while (cursor.moveToNext());
            }

            cursor.close();
        } else {
            Log.d(TAG, "cursor is null");
        }

        database.close();

        timings.dumpToLog();
    }
}

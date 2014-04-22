/*
 * TransitViewListFragment.java
 * Last modified on 04-21-2014 21:52-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.septa.android.app.adapters.TransitView_ListViewItem_ArrayAdapter;
import org.septa.android.app.databases.SEPTADatabase;
import org.septa.android.app.models.BusRouteModel;
import org.septa.android.app.models.MinMaxHoursModel;
import org.septa.android.app.models.servicemodels.BusRoutesModel;

import java.util.Collections;
import java.util.List;

public class TransitViewListFragment extends ListFragment {
    private static final String TAG = SettingsListFragment.class.getName();

    ArrayAdapter<BusRouteModel> _adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set the divider to null in order to allow the gradient to work
        getListView().setDivider(null);
        getListView().setPadding(0, 5, 0, 5);
        getListView().setDividerHeight(0);

        // TODO: fix this, this is not the right way to perform a database query.  Make it async.
        BusRoutesModel busRoutesModel = loadTransitViewData();

        List<BusRouteModel>busRouteModels = busRoutesModel.getBusRouteModels();
        Collections.sort(busRouteModels);
        _adapter = new TransitView_ListViewItem_ArrayAdapter(getActivity(), busRouteModels);

        setListAdapter(_adapter);
    }

    private BusRoutesModel loadTransitViewData() {
        BusRoutesModel busRoutes = null;

        SEPTADatabase septaDatabase = new SEPTADatabase(getActivity());
        SQLiteDatabase database = septaDatabase.getReadableDatabase();

        Cursor cursor = database.rawQuery("SELECT s.route_id, r.route_short_name, r.route_type, s.service_id, MIN(min) as min, MAX(max) as max FROM serviceHours s JOIN routes_bus r ON r.route_short_name = s.route_short_name GROUP BY s.route_id, service_id ORDER BY s.route_id", null);
        if (cursor != null) {
            busRoutes = new BusRoutesModel(cursor.getCount());
            Log.d(TAG, "the cursor is not null");
            if (cursor.moveToFirst()) {
                Log.d(TAG, "moved the first");
                do {
                    BusRouteModel busRoute = busRoutes.getBusRouteByRouteId(cursor.getString(0));

                    if (busRoute == null) {
                        Log.d(TAG, "first time seeing this route id, make a new bus route");
                        busRoute = new BusRouteModel();

                        busRoute.setRouteShortName(cursor.getString(1));
                        busRoute.setRouteId(cursor.getString(0));
                        busRoute.setRouteType(Integer.valueOf(cursor.getString(2)));
                    }

                    MinMaxHoursModel minMaxHours = new MinMaxHoursModel(Integer.valueOf(cursor.getString(4)), Integer.valueOf(cursor.getString(5)));

                    busRoute.addMinMaxHoursToRoute(cursor.getString(3), minMaxHours);

                    busRoutes.setBusRouteByRouteId(cursor.getString(0), busRoute);
                } while (cursor.moveToNext());
            }
        } else {
            Log.d(TAG, "cursor is null");
        }

        Log.d(TAG, "the count of bus routes is " + busRoutes.getBusRoutesCount());

        return busRoutes;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        FragmentTransaction fragmentTransaction;

        switch(position) {
            case 0:
                Log.d(TAG, "transition 1");

                break;

            case 1:
                Log.d(TAG, "transition 2");

                break;

            default:
                Log.d(TAG, "transition default");

                break;
        }
    }
}

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
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.septa.android.app.R;
import org.septa.android.app.activities.TransitViewMapAndRouteListActionBarActivity;
import org.septa.android.app.adapters.TransitView_ListViewItem_ArrayAdapter;
import org.septa.android.app.databases.SEPTADatabase;
import org.septa.android.app.models.BusRouteModel;
import org.septa.android.app.models.MinMaxHoursModel;
import org.septa.android.app.models.BusRoutesModel;

import java.util.Collections;
import java.util.List;

public class TransitViewListFragment extends ListFragment {
    private static final String TAG = SettingsListFragment.class.getName();

    ArrayAdapter<BusRouteModel> _adapter;
    List<BusRouteModel> busRouteModelList;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set the divider to null in order to allow the gradient to work
        getListView().setDivider(null);
        getListView().setPadding(0, 5, 0, 5);
        getListView().setDividerHeight(0);

        // TODO: fix this, this is not the right way to perform a database query.  Make it async.
        BusRoutesModel busRoutesModel = loadTransitViewData();

        busRouteModelList = busRoutesModel.getBusRouteModels();
        Collections.sort(busRouteModelList);
        _adapter = new TransitView_ListViewItem_ArrayAdapter(getActivity(), busRouteModelList);

        setListAdapter(_adapter);
    }

    private BusRoutesModel loadTransitViewData() {
        BusRoutesModel busRoutes = null;

        SEPTADatabase septaDatabase = new SEPTADatabase(getActivity());
        SQLiteDatabase database = septaDatabase.getReadableDatabase();

        Cursor cursor = database.rawQuery("SELECT s.route_id, r.route_short_name, r.route_type, s.service_id, MIN(min) as min, MAX(max) as max FROM serviceHours s JOIN routes_bus r ON r.route_short_name = s.route_short_name GROUP BY s.route_id, service_id ORDER BY s.route_id", null);
        if (cursor != null) {
            busRoutes = new BusRoutesModel(cursor.getCount());
            if (cursor.moveToFirst()) {
                do {
                    BusRouteModel busRoute = busRoutes.getBusRouteByRouteId(cursor.getString(0));

                    if (busRoute == null) {
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

        return busRoutes;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final String[] realtime_menu_icons = getResources().getStringArray(R.array.realtime_menu_icons_inorder);

        Log.d(TAG, "onListItemClick in transitviewlistfragment");
        BusRouteModel busRouteModel = busRouteModelList.get(position);

        Log.d(TAG, "clicked on the route with short name "+busRouteModel.getRouteShortName());

        Intent intent = new Intent(getActivity(), TransitViewMapAndRouteListActionBarActivity.class);
        intent.putExtra(getString(R.string.actionbar_titletext_key), getActivity().getTitle());

        // 2 is the position for the transitview
        intent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), realtime_menu_icons[2]);

        intent.putExtra("route_short_name", busRouteModel.getRouteShortName());
        startActivity(intent);
    }
}

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
import org.septa.android.app.models.ObjectFactory;
import org.septa.android.app.models.RouteModel;
import org.septa.android.app.models.MinMaxHoursModel;
import org.septa.android.app.models.RoutesModel;

import java.util.Collections;
import java.util.List;

public class TransitViewListFragment extends ListFragment {
    private static final String TAG = TransitViewListFragment.class.getName();

    ArrayAdapter<RouteModel> _adapter;
    List<RouteModel> busRouteModelList;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set the divider to null in order to allow the gradient to work
        getListView().setDivider(null);
        getListView().setPadding(0, 5, 0, 5);
        getListView().setDividerHeight(0);

        RoutesModel busRoutesModel = ObjectFactory.getInstance().getBusRoutes();

        busRoutesModel.loadRoutes(getActivity());

        busRouteModelList = busRoutesModel.getRouteModels();
        Collections.sort(busRouteModelList);

        _adapter = new TransitView_ListViewItem_ArrayAdapter(getActivity(), busRouteModelList);

        setListAdapter(_adapter);

        getListView().setFastScrollEnabled(true);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final String[] realtime_menu_icons = getResources().getStringArray(R.array.realtime_menu_icons_inorder);
        final String[] realtime_menu_titles = getResources().getStringArray(R.array.realtime_menu_strings_inorder);

        RouteModel routeModel = busRouteModelList.get(position);

        Intent intent = new Intent(getActivity(), TransitViewMapAndRouteListActionBarActivity.class);

        // 2 is the position for the transitview
        intent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), realtime_menu_icons[2]);
        intent.putExtra(getString(R.string.actionbar_titletext_key), "| "+realtime_menu_titles[2]);

        intent.putExtra("route_short_name", routeModel.getRouteShortName());
        startActivity(intent);
    }
}

/*
 * StopSelectionListFragment.java
 * Last modified on 06-04-2014 19:02-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import org.septa.android.app.R;
import org.septa.android.app.adapters.RegionalRail_StopSelection_ListViewItem_ArrayAdapter;
import org.septa.android.app.models.StopModel;

import java.util.ArrayList;
import java.util.List;

public class StopSelectionListFragment extends ListFragment implements View.OnClickListener, LocationListener {
    public static final String TAG = StopSelectionListFragment.class.getName();

    private List<StopModel> stopModelList;

    private String startOrDestinationSelectionMode;

    private LocationManager locationManager;
    private RegionalRail_StopSelection_ListViewItem_ArrayAdapter adapter;

    Menu optionsMenu;

    public StopSelectionListFragment() {
        // instantiate an empty array list for the TripDataModel
        stopModelList = new ArrayList<StopModel>(0);
        startOrDestinationSelectionMode = "start";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_sort, menu);
        optionsMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort_123:
                adapter.sortByStop();
                break;
            case R.id.menu_sort_abc:
                adapter.sortByName();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        swapSortOptions();
        return true;
    }

    public void swapSortOptions() {
        MenuItem sortStop = optionsMenu.findItem(R.id.menu_sort_123);
        MenuItem sortName = optionsMenu.findItem(R.id.menu_sort_abc);
        if(sortName != null && sortStop != null) {
            sortName.setVisible(!sortName.isVisible());
            sortStop.setVisible(!sortStop.isVisible());
            onPrepareOptionsMenu(optionsMenu);
        }
    }

    public void removeSortOptions() {
        MenuItem sortStop = optionsMenu.findItem(R.id.menu_sort_123);
        MenuItem sortName = optionsMenu.findItem(R.id.menu_sort_abc);
        if(sortName != null && sortStop != null) {
            sortName.setVisible(false);
            sortStop.setVisible(false);
            onPrepareOptionsMenu(optionsMenu);
        }
    }

    public void setStartOrDestinationSelectionMode(String startOrDestinationSelectionMode) {
        if (startOrDestinationSelectionMode == null) {
            this.startOrDestinationSelectionMode = "start";
        } else {
            this.startOrDestinationSelectionMode = startOrDestinationSelectionMode.toLowerCase();
        }
    }

    public void setStopList(List<StopModel> stopModelList) {
        this.stopModelList = stopModelList;

        getListView().setFastScrollEnabled(false);
        adapter = new RegionalRail_StopSelection_ListViewItem_ArrayAdapter(getActivity(), stopModelList);
        setListAdapter(adapter);
        getListView().setFastScrollEnabled(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setRetainInstance(true);

        ListView lv = getListView();
        lv.setDivider(getActivity().getResources().getDrawable(R.drawable.list_item_separator_gradient));
        lv.setDividerHeight(3);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * Called to instantiate the view. Creates and returns the WebView.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = super.onCreateView(inflater, container, savedInstanceState);
        ListView lv = (ListView) view.findViewById(android.R.id.list);
        View headerView = getActivity().getLayoutInflater().inflate(
                R.layout.headerview_route_selection, lv, false);
        headerView.findViewById(R.id.headerview_textview_current_location).setOnClickListener(this);
        lv.addHeaderView(headerView);

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent returnIntent = new Intent();
        position -= getListView().getHeaderViewsCount();
        returnIntent.putExtra("stop_name", stopModelList.get(position).getStopName());
        returnIntent.putExtra("stop_id", stopModelList.get(position).getStopId());
        returnIntent.putExtra("selection_mode", startOrDestinationSelectionMode);
        getActivity().setResult(Activity.RESULT_OK,returnIntent);
        getActivity().finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.headerview_textview_current_location:
                getUserLocation();
                break;
        }
    }

    private void getUserLocation() {
        Location userLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        locationManager.removeUpdates(this);
        if(userLocation == null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getActivity(), getString(R.string.error_location_disabled),
                    Toast.LENGTH_SHORT).show();
        }  else if(userLocation != null) {
            Log.i(TAG, "Using cached location: " + String.valueOf(userLocation));
            sortByLocations(userLocation);
        }  else {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this,
                    Looper.myLooper());
        }
    }

    private void sortByLocations(Location userLocation) {
        if(userLocation != null) {
            adapter.sortByLocation(userLocation);
            removeSortOptions();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Location: " + location);
        sortByLocations(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.i(TAG, "Provider: " + s + " Status: " + i);
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.i(TAG, "Enabled: " + s);
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.i(TAG, "Disabled: " + s);
    }
}

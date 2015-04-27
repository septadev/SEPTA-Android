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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import org.septa.android.app.BuildConfig;
import org.septa.android.app.R;
import org.septa.android.app.activities.GeocoderActivity;
import org.septa.android.app.adapters.RegionalRail_StopSelection_ListViewItem_ArrayAdapter;
import org.septa.android.app.models.StopModel;
import org.septa.android.app.utilities.Constants;

import java.util.ArrayList;
import java.util.List;

public class StopSelectionListFragment extends ListFragment implements View.OnClickListener, LocationListener {
    public static final String TAG = StopSelectionListFragment.class.getName();

    private static final int REQUEST_CODE_GECODER = 1000;
    private List<StopModel> stopModelList;
    private String startOrDestinationSelectionMode;
    private LocationManager locationManager;
    private RegionalRail_StopSelection_ListViewItem_ArrayAdapter adapter;
    private Location addressLocation;

    public StopSelectionListFragment() {
        // instantiate an empty array list for the TripDataModel
        stopModelList = new ArrayList<StopModel>(0);
        startOrDestinationSelectionMode = "start";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreate");
        }
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
        if(addressLocation != null) {
            sortByLocations(addressLocation);
        }
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
        headerView.findViewById(R.id.headerview_textview_enter_address).setOnClickListener(this);
        lv.addHeaderView(headerView);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(REQUEST_CODE_GECODER == requestCode && resultCode == Activity.RESULT_OK) {
            Location addressLocation = data.getParcelableExtra(Constants.KEY_LOCATION);
            if(addressLocation != null) {
                this.addressLocation = addressLocation;
                sortByLocations(addressLocation);
            }
        }
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
            case R.id.headerview_textview_enter_address:
                Intent intent = new Intent(getActivity(), GeocoderActivity.class);
                startActivityForResult(intent, REQUEST_CODE_GECODER);
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
        if(userLocation != null && adapter != null) {
            adapter.sortByLocation(userLocation);
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

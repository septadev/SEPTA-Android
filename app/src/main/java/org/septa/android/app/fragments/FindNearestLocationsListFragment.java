/*
 * FindNearestLocationsListFragment.java
 * Last modified on 03-27-2014 18:24-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import org.septa.android.app.R;
import org.septa.android.app.activities.FindNearestLocationRouteDetailsActionBarActivity;
import org.septa.android.app.adapters.FindNearestLocation_ListViewItem_ArrayAdapter;
import org.septa.android.app.databases.SEPTADatabase;
import org.septa.android.app.models.LocationBasedRouteModel;
import org.septa.android.app.models.LocationModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FindNearestLocationsListFragment extends ListFragment implements OnClickListener {
    public static final String TAG = FindNearestLocationsListFragment.class.getName();

    public interface OnRetryLocationSearch {
        public void onRetryLocationSearch();
    }

    private OnRetryLocationSearch mLocationSearchCallback;

    private List<LocationModel> mLocationList;
    private  FindNearestLocation_ListViewItem_ArrayAdapter mAdapter;

    private RouteStopIdLoader routeStopIdLoader;

    private RelativeLayout mListContainer;
    private TextView mErrorView;

    public FindNearestLocationsListFragment() {
        this.mLocationList = Collections.synchronizedList(new ArrayList<LocationModel>());
    }

    public void clearLocationLists() {
        this.mLocationList = Collections.synchronizedList(new ArrayList<LocationModel>());
        mAdapter.notifyDataSetChanged();

    }

    public void setLocationList(List<LocationModel> mLocationList) {
        // If GPS is available and enabled, show listview
        if (hasGpsSensor() && isGpsEnabled()) {
            mListContainer.setVisibility(View.VISIBLE);
            mErrorView.setVisibility(View.GONE);

            this.mLocationList = Collections.synchronizedList(mLocationList);

            Collections.sort(this.mLocationList, new Comparator<LocationModel>() {
                public int compare(LocationModel location1, LocationModel location2) {
                    return new Float(location1.getDistance()).compareTo(new Float(location2.getDistance()));
                }
            });


            if(routeStopIdLoader != null){
                routeStopIdLoader.cancel(true);
            }
            routeStopIdLoader = new RouteStopIdLoader();
            routeStopIdLoader.execute(mLocationList);
        }
        // Otherwise, show the error view
        else {
            mListContainer.setVisibility(View.GONE);
            mErrorView.setText(!hasGpsSensor() ? getString(R.string.find_nearest_location_gps_unavailable) : getString(R.string.find_nearest_location_gps_not_enabled));
            mErrorView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mLocationSearchCallback = (OnRetryLocationSearch) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setRetainInstance(true);

        ListView lv = getListView();
        lv.setFastScrollEnabled(true);
        lv.setScrollingCacheEnabled(false);
        lv.setSmoothScrollbarEnabled(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Setup views
        View view = inflater.inflate(R.layout.findnearestlocation_fragment_listview, null);

        mListContainer = (RelativeLayout) view.findViewById(R.id.list_container);
        mErrorView = (TextView) view.findViewById(R.id.empty_text);

        mErrorView.setOnClickListener(this);

        // If GPS is available and enabled, show listview
        if (hasGpsSensor() && isGpsEnabled()) {
            mListContainer.setVisibility(View.VISIBLE);
            mErrorView.setVisibility(View.GONE);
        }
        // Otherwise, show the error view
        else {
            mListContainer.setVisibility(View.GONE);
            mErrorView.setText(!hasGpsSensor() ? getString(R.string.find_nearest_location_gps_unavailable) : getString(R.string.find_nearest_location_gps_not_enabled));
            mErrorView.setVisibility(View.VISIBLE);
        }

        mAdapter = new FindNearestLocation_ListViewItem_ArrayAdapter(inflater.getContext(), new ArrayList<LocationModel>());
        setListAdapter(mAdapter);

        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        LocationModel locationModel = mLocationList.get(position);

        //GSon the locationModel to pass to the activity
        Intent findNearestLocationRouteDetailsIntent = null;

        findNearestLocationRouteDetailsIntent = new Intent(getActivity(), FindNearestLocationRouteDetailsActionBarActivity.class);

        Gson gson = new Gson();
        String locationRouteModelJSONString = gson.toJson(locationModel);
        findNearestLocationRouteDetailsIntent.putExtra(getString(R.string.findNearestLocation_locationRouteModel), locationRouteModelJSONString);

        startActivity(findNearestLocationRouteDetailsIntent);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.empty_text:
                // Retry nearest location search
                if (mLocationSearchCallback != null) {
                    mListContainer.setVisibility(View.VISIBLE);
                    mErrorView.setVisibility(View.GONE);
                    mLocationSearchCallback.onRetryLocationSearch();
                }
                break;
        }
    }

    private class RouteStopIdLoader extends AsyncTask<List<LocationModel>, Integer, Boolean> {


        public RouteStopIdLoader() {

        }

        private void loadRoutesPerStop(List<LocationModel> locationList) {
            Log.d(TAG, "processing routes per stop with a location list size of " + locationList.size());
            if(getActivity() == null) {
                this.cancel(true);
                return;
            }
            SEPTADatabase septaDatabase = new SEPTADatabase(getActivity());
            SQLiteDatabase database = septaDatabase.getReadableDatabase();

            SparseArray<LocationModel> locations = new SparseArray<LocationModel>();
            String queryString = "SELECT route_short_name, stop_id, Direction, dircode, route_type FROM stopIDRouteLookup WHERE stop_id in (";

            for (LocationModel location : locationList) {
                queryString += location.getLocationId();
                locations.put(location.getLocationId(), location);

                if(locationList.indexOf(location) < locationList.size()-1)
                    queryString += ",";

            }

            queryString += ")";

            Cursor cursor = database.rawQuery(queryString, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        locations.get(cursor.getInt(1)).addRoute(cursor.getString(0), LocationBasedRouteModel.DirectionCode.valueOf(cursor.getString(2)), cursor.getInt(3), cursor.getInt(4));
                    } while (cursor.moveToNext());
                }

                cursor.close();
            } else {
                Log.d(TAG, "cursor is null");
            }
            database.close();

        }

        @Override
        protected Boolean doInBackground(List<LocationModel>... params) {
            loadRoutesPerStop(params[0]);

            return false;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            mAdapter.setList(mLocationList);
            mAdapter.notifyDataSetChanged();
        }
    }

    private boolean hasGpsSensor(){
        PackageManager packageManager = getActivity().getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }

    private boolean isGpsEnabled(){
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}

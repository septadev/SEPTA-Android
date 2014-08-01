/*
 * FindNearestLocationsListFragment.java
 * Last modified on 03-27-2014 18:24-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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

public class FindNearestLocationsListFragment extends ListFragment {
    public static final String TAG = FindNearestLocationsListFragment.class.getName();

    private List<LocationModel> mLocationList;
    private  FindNearestLocation_ListViewItem_ArrayAdapter mAdapter;

    private RouteStopIdLoader routeStopIdLoader;

    public FindNearestLocationsListFragment() {
        mLocationList = new ArrayList<LocationModel>(0);
    }

    public void clearLocationLists() {

        this.mLocationList = new ArrayList<LocationModel>(0);
        this.getListView().invalidate();

    }

    @Override
    public void onStop() {
        super.onStop();
        if(routeStopIdLoader != null) {
            routeStopIdLoader.cancel(true);
        }
    }

    public void setLocationList(List<LocationModel> mLocationList) {
        this.mLocationList = mLocationList;

        Collections.sort(this.mLocationList, new Comparator<LocationModel>() {
            public int compare(LocationModel location1, LocationModel location2) {
                return new Float(location1.getDistance()).compareTo(new Float(location2.getDistance()));
            }
        });


        routeStopIdLoader = new RouteStopIdLoader();
        routeStopIdLoader.execute(mLocationList);

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

        View view = inflater.inflate(R.layout.findnearestlocation_fragment_listview, null);

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

    private class RouteStopIdLoader extends AsyncTask<List<LocationModel>, Integer, Boolean> {


        public RouteStopIdLoader() {

        }

        private void loadRoutesPerStop(List<LocationModel> locationList) {
            Log.d(TAG, "processing routes per stop with a location list size of " + locationList.size());
            SEPTADatabase septaDatabase = new SEPTADatabase(getActivity());
            SQLiteDatabase database = septaDatabase.getReadableDatabase();

            for (LocationModel location : locationList) {
                String queryString = "SELECT route_short_name, stop_id, Direction, dircode, route_type FROM stopIDRouteLookup WHERE stop_id=" + location.getLocationId();
                Cursor cursor = database.rawQuery(queryString, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            location.addRoute(cursor.getString(0), LocationBasedRouteModel.DirectionCode.valueOf(cursor.getString(2)), cursor.getInt(3), cursor.getInt(4));
                        } while (cursor.moveToNext());
                    }

                    cursor.close();
                } else {
                    Log.d(TAG, "cursor is null");
                }
            }

            database.close();
        }

        @Override
        protected Boolean doInBackground(List<LocationModel>... params) {
            List<LocationModel> locationList = params[0];
            loadRoutesPerStop(locationList);

            return false;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            mAdapter.setList(mLocationList);
            mAdapter.notifyDataSetChanged();
        }
    }
}

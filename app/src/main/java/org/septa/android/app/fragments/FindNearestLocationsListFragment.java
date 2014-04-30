/*
 * FindNearestLocationsListFragment.java
 * Last modified on 03-27-2014 18:24-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.septa.android.app.R;
import org.septa.android.app.adapters.FindNearestLocation_ListViewItem_ArrayAdapter;
import org.septa.android.app.databases.SEPTADatabase;
import org.septa.android.app.models.LocationModel;
import org.septa.android.app.models.ObjectFactory;

import java.util.ArrayList;
import java.util.List;

public class FindNearestLocationsListFragment extends ListFragment {
    public static final String TAG = FindNearestLocationsListFragment.class.getName();

    private List<LocationModel> locationList;

    public FindNearestLocationsListFragment() {
        // instantiate an empty array list for the TrainViewModels
        locationList = new ArrayList<LocationModel>(0);
    }

    public void setLocationList(List<LocationModel>locationList) {
        this.locationList = locationList;

        RouteStopIdLoader routeStopIdLoader = new RouteStopIdLoader(this.getListView());
        routeStopIdLoader.execute(locationList);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setRetainInstance(true);

        ListView lv = getListView();
        lv.setFastScrollEnabled(true);

        lv.setEmptyView(lv.findViewById(R.id.row_empty_view));

        lv.setScrollingCacheEnabled(false);
        lv.setSmoothScrollbarEnabled(false);
    }

    /**
     * Called to instantiate the view. Creates and returns the WebView.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ArrayAdapter<LocationModel> adapter = new FindNearestLocation_ListViewItem_ArrayAdapter(inflater.getContext(), locationList);
        setListAdapter(adapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.d(TAG, "detected a listfragment item being clicked");
    }

    @Override
    public void onStart() {
        super.onStart();
//        ListView lv = getListView();
//        lv.setEmptyView(View.inflate(getActivity(), R.layout.row_empty_view, (ViewGroup)getListView().getParent()));
    }

    /**
     * Called when the fragment is visible to the user and actively running. Resumes the WebView.
     */
    @Override
    public void onPause() {

        super.onPause();
    }

    /**
     * Called when the fragment is no longer resumed. Pauses the WebView.
     */
    @Override
    public void onResume() {

        super.onResume();
    }

    /**
     * Called when the WebView has been detached from the fragment.
     * The WebView is no longer available after this time.
     */
    @Override
    public void onDestroyView() {

        super.onDestroyView();
    }

    /**
     * Called when the fragment is no longer in use. Destroys the internal state of the WebView.
     */
    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    private class RouteStopIdLoader extends AsyncTask<List<LocationModel>, Integer, Boolean> {
        ListView listView = null;

        public RouteStopIdLoader(ListView listView) {

            this.listView = listView;
        }

        private void loadRoutesPerStop(List<LocationModel>locationList) {
            Log.d(TAG, "processing routes per stop with a location list size of "+locationList.size());
            SEPTADatabase septaDatabase = new SEPTADatabase(getActivity());
            SQLiteDatabase database = septaDatabase.getReadableDatabase();

            for (LocationModel location : locationList) {
                String queryString = "SELECT route_short_name, stop_id FROM stopIDRouteLookup WHERE stop_id=" + location.getLocationId();
                Cursor cursor = database.rawQuery(queryString, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            location.addRoute(cursor.getString(0));
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
            List<LocationModel>locationList = params[0];

            loadRoutesPerStop(locationList);

            ObjectFactory.getInstance().getBusRoutes().loadRoutes(getActivity());

            return false;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);

            ArrayAdapter<LocationModel> adapter = new FindNearestLocation_ListViewItem_ArrayAdapter(getActivity(), locationList);
            setListAdapter(adapter);

            // after the list has been updated, invalidate the list view to re-render
            listView.invalidate();
        }
    }
}

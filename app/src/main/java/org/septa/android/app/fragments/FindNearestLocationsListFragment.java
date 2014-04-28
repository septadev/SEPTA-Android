/*
 * FindNearestLocationsListFragment.java
 * Last modified on 03-27-2014 18:24-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.septa.android.app.R;
import org.septa.android.app.adapters.FindNearestLocation_ListViewItem_ArrayAdapter;
import org.septa.android.app.adapters.TransitView_RouteList_ListViewItem_ArrayAdapter;
import org.septa.android.app.models.LocationModel;
import org.septa.android.app.models.servicemodels.TransitViewVehicleModel;

import java.util.ArrayList;
import java.util.List;

public class FindNearestLocationsListFragment extends ListFragment {
    public static final String TAG = FindNearestLocationsListFragment.class.getName();

    private List<LocationModel> locationList;

    public FindNearestLocationsListFragment() {
        // instanciate an empty array list for the TrainViewModels
        locationList = new ArrayList<LocationModel>(0);
    }

    public void setLocationList(List<LocationModel>locationList) {
        this.locationList = locationList;

        ArrayAdapter<LocationModel> adapter = new FindNearestLocation_ListViewItem_ArrayAdapter(getActivity(), locationList);
        setListAdapter(adapter);

        // after the list has been update, invalidate the list view to re-render
        this.getListView().invalidate();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setRetainInstance(true);

        ListView lv = getListView();
        lv.setFastScrollEnabled(true);

        lv.setDivider(getActivity().getResources().getDrawable(R.drawable.list_item_separator_gradient));
        lv.setDividerHeight(3);

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

        Log.d(TAG, "about to call super from the onCreateView in TrainViewListFragment");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.d(TAG, "detected a listfragment item being clicked");
    }

    @Override
    public void onStart() {
        super.onStart();
        ListView lv = getListView();
        lv.setEmptyView(View.inflate(getActivity(), R.layout.row_empty_view, (ViewGroup)getListView().getParent()));
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
}

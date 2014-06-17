/*
 * StopSelectionListFragment.java
 * Last modified on 06-04-2014 19:02-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.septa.android.app.R;
import org.septa.android.app.adapters.RegionalRail_StopSelection_ListViewItem_ArrayAdapter;
import org.septa.android.app.models.StopModel;

import java.util.ArrayList;
import java.util.List;

public class StopSelectionListFragment extends ListFragment {
    public static final String TAG = StopSelectionListFragment.class.getName();

    private List<StopModel> stopModelList;

    private String startOrDestinationSelectionMode;

    public StopSelectionListFragment() {
        // instantiate an empty array list for the TripDataModel
        stopModelList = new ArrayList<StopModel>(0);
        startOrDestinationSelectionMode = "start";
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

        ArrayAdapter<StopModel> adapter = new RegionalRail_StopSelection_ListViewItem_ArrayAdapter(getActivity(), stopModelList);
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

        ArrayAdapter<StopModel> adapter = new RegionalRail_StopSelection_ListViewItem_ArrayAdapter(inflater.getContext(), stopModelList);
        setListAdapter(adapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("stop_name", stopModelList.get(position).getStopName());
        returnIntent.putExtra("stop_id", stopModelList.get(position).getStopId());
        returnIntent.putExtra("selection_mode", startOrDestinationSelectionMode);
        getActivity().setResult(getActivity().RESULT_OK,returnIntent);
        getActivity().finish();
    }
}

/*
 * SchedulesFragment.java
 * Last modified on 05-05-2014 15:39-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.septa.android.app.R;
import org.septa.android.app.activities.schedules.SchedulesRouteSelectionActionBarActivity;
import org.septa.android.app.adapters.schedules.Schedules_ListFragment_ArrayAdapter;
import org.septa.android.app.utilities.Constants;

public class SchedulesListFragment extends ListFragment {
    private static final String TAG = SchedulesListFragment.class.getName();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set the divider to null in order to allow the gradient to work
        getListView().setDivider(null);
        getListView().setPadding(0,5,0,5);
        getListView().setDividerHeight(5);

        // programmatically set the background to the main background
        getListView().setBackgroundResource(R.drawable.main_background);

        String[] values = getResources().getStringArray(R.array.schedulesfragment_listview_items);
        ArrayAdapter<String> adapter = new Schedules_ListFragment_ArrayAdapter(getActivity(), values);

        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent schedulesRouteSelectionIntent = null;

        switch(position) {
            case 0: // regional rail line

                schedulesRouteSelectionIntent = new Intent(getActivity(), SchedulesRouteSelectionActionBarActivity.class);
                schedulesRouteSelectionIntent.putExtra(getString(R.string.actionbar_titletext_key), Constants.VALUE_REGIONAL_RAIL_LINE);
                schedulesRouteSelectionIntent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), Constants.ACTION_BAR_IMAGE_SUFFIX_RRL);
                schedulesRouteSelectionIntent.putExtra(getString(R.string.schedules_routeselect_travelType),
                        Constants.VALUE_TRAVEL_TYPE_RRL);

                startActivity(schedulesRouteSelectionIntent);

                break;

            case 1: // mfl
                schedulesRouteSelectionIntent = new Intent(getActivity(), SchedulesRouteSelectionActionBarActivity.class);
                schedulesRouteSelectionIntent.putExtra(getString(R.string.actionbar_titletext_key), Constants.VALUE_MARKET_FRANKFORD_LINE);
                schedulesRouteSelectionIntent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), Constants.ACTION_BAR_IMAGE_SUFFIX_MFL);
                schedulesRouteSelectionIntent.putExtra(getString(R.string.schedules_routeselect_travelType),
                        Constants.VALUE_TRAVEL_TYPE_MFL);

                startActivity(schedulesRouteSelectionIntent);

                break;

            case 2: // bsl
                schedulesRouteSelectionIntent = new Intent(getActivity(), SchedulesRouteSelectionActionBarActivity.class);
                schedulesRouteSelectionIntent.putExtra(getString(R.string.actionbar_titletext_key), Constants.VALUE_BROAD_STREET_LINE);
                schedulesRouteSelectionIntent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), Constants.ACTION_BAR_IMAGE_SUFFIX_BSL);
                schedulesRouteSelectionIntent.putExtra(getString(R.string.schedules_routeselect_travelType),
                        Constants.VALUE_TRAVEL_TYPE_BSL);

                startActivity(schedulesRouteSelectionIntent);

                break;

            case 3: // trolley
                schedulesRouteSelectionIntent = new Intent(getActivity(), SchedulesRouteSelectionActionBarActivity.class);
                schedulesRouteSelectionIntent.putExtra(getString(R.string.actionbar_titletext_key), Constants.VALUE_TROLLEY);
                schedulesRouteSelectionIntent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), Constants.ACTION_BAR_IMAGE_SUFFIX_TROLLEY);
                schedulesRouteSelectionIntent.putExtra(getString(R.string.schedules_routeselect_travelType),
                        Constants.VALUE_TRAVEL_TYPE_TROLLEY);

                startActivity(schedulesRouteSelectionIntent);

                break;

            case 4: // nhsl
                schedulesRouteSelectionIntent = new Intent(getActivity(), SchedulesRouteSelectionActionBarActivity.class);
                schedulesRouteSelectionIntent.putExtra(getString(R.string.actionbar_titletext_key), Constants.VALUE_NHSL);
                schedulesRouteSelectionIntent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), Constants.ACTION_BAR_IMAGE_SUFFIX_NHSL);
                schedulesRouteSelectionIntent.putExtra(getString(R.string.schedules_routeselect_travelType),
                        Constants.VALUE_TRAVEL_TYPE_NHSL);

                startActivity(schedulesRouteSelectionIntent);

                break;

            case 5: // bus
                schedulesRouteSelectionIntent = new Intent(getActivity(), SchedulesRouteSelectionActionBarActivity.class);
                schedulesRouteSelectionIntent.putExtra(getString(R.string.actionbar_titletext_key), Constants.VALUE_BUS);
                schedulesRouteSelectionIntent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), Constants.ACTION_BAR_IMAGE_SUFFIX_BUS);
                schedulesRouteSelectionIntent.putExtra(getString(R.string.schedules_routeselect_travelType),
                        Constants.VALUE_TRAVEL_TYPE_BUS);

                startActivity(schedulesRouteSelectionIntent);

                break;

            default:
                Log.d(TAG, "should not get here");

                break;
        }
    }
}

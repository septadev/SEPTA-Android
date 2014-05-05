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
import org.septa.android.app.adapters.Schedules_ListFragment_ArrayAdapter;

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

        switch(position) {
            case 0:
                Log.d(TAG, "position 0");

                break;

            case 1:
                Log.d(TAG, "position 1");

                break;

            case 2:
                Log.d(TAG, "position 2");

                break;

            case 3:
                Log.d(TAG, "position 3");

                break;

            case 4:
                Log.d(TAG, "position 4");

                break;

            case 5:
                Log.d(TAG, "position 5");

                break;

            default:
                Log.d(TAG, "should not get here");

                break;
        }
    }
}

/*
 * ConnectListFragment.java
 * Last modified on 02-02-2014 18:09-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.septa.android.app.R;
import org.septa.android.app.adapters.ConnectListFragmentItemsArrayAdapter;
import org.septa.android.app.dialogs.CustomerServiceDialDialogFragment;
import org.septa.android.app.utilities.SocialPageLaunch;

public class ConnectListFragment extends ListFragment {
    private static final String TAG = ConnectListFragment.class.getName();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set the divider to null in order to allow the gradient to work
        getListView().setDivider(null);
        getListView().setPadding(0,5,0,5);
        getListView().setDividerHeight(5);

        // programmatically set the background to the main background
        getListView().setBackgroundResource(R.drawable.main_background);

        String[] values = getResources().getStringArray(R.array.connectfragment_listview_items);
        ArrayAdapter<String> adapter = new ConnectListFragmentItemsArrayAdapter(getActivity(), values);

//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
//                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // do something with the data
        Log.d("me", "list item was listed, in position " + position);

        switch(position) {
            case 0:
                Log.d(TAG, "launch the fare activity");

                break;

            case 1:
                Log.d(TAG, "launch the dial function");

                CustomerServiceDialDialogFragment customerServiceDialDialog = new CustomerServiceDialDialogFragment();
                customerServiceDialDialog.show(getFragmentManager(), getResources().getString(R.string.connect_customerservice_dialog_fragment_tag));

                break;

            case 2:
                Log.d(TAG, "check for facebook or URI to facebook");
                SocialPageLaunch.facebook(getActivity());

                break;

            case 3:
                Log.d(TAG, "check for twitter or URI to twitter");
                SocialPageLaunch.twitter(getActivity());

                break;

            case 4:
                Log.d(TAG, "launch the comments activity");

                break;

            case 5:
                Log.d(TAG, "launch the leave feedback activity");

                break;

            default:
                Log.d(TAG, "should not get here");

                break;
        }
    }
}

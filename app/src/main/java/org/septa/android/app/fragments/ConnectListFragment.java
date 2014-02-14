/*
 * ConnectListFragment.java
 * Last modified on 02-02-2014 18:09-0500 by brianhmayo
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
import org.septa.android.app.activities.AppFeedbackFormActivity;
import org.septa.android.app.activities.CommentsFormActionBarActivity;
import org.septa.android.app.activities.FareInformationActionBarActivity;
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

        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String connectFragmentListviewItem;
        String actionbar_titletext;

        switch(position) {
            case 0:
                Log.d(TAG, "launch the fare activity");
                connectFragmentListviewItem = getResources().getStringArray(R.array.connectfragment_listview_items)[position];
                actionbar_titletext = getString(R.string.titlebar_text_separator).concat(" ").concat(connectFragmentListviewItem);

                Intent fareInformationIntent = new Intent(getActivity(), FareInformationActionBarActivity.class);
                fareInformationIntent.putExtra(getString(R.string.actionbar_titletext_key), actionbar_titletext);
                fareInformationIntent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), "fares");

                startActivity(fareInformationIntent);

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
                connectFragmentListviewItem = getResources().getStringArray(R.array.connectfragment_listview_items)[position];
                actionbar_titletext = getString(R.string.titlebar_text_separator).concat(" ").concat(connectFragmentListviewItem);

                Intent commentsFormIntent = new Intent(getActivity(), CommentsFormActionBarActivity.class);
                commentsFormIntent.putExtra(getString(R.string.actionbar_titletext_key), actionbar_titletext);
                commentsFormIntent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), "comments");

                startActivity(commentsFormIntent);

                break;

            case 5:
                Log.d(TAG, "launch the leave feedback activity");
                Intent appFeedbackFormIntent = new Intent(getActivity(), AppFeedbackFormActivity.class);
                appFeedbackFormIntent.putExtra(getString(R.string.actionbar_titletext_key), "| App Feedback");
                startActivity(appFeedbackFormIntent);

                break;

            default:
                Log.d(TAG, "should not get here");

                break;
        }
    }
}

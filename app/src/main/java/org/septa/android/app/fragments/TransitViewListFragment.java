/*
 * TransitViewListFragment.java
 * Last modified on 04-12-2014 18:15-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.septa.android.app.R;
import org.septa.android.app.adapters.Settings_ListViewItem_ArrayAdapter;
import org.septa.android.app.models.adapterhelpers.IconTextPendingIntentModel;

public class TransitViewListFragment extends ListFragment {
    private static final String TAG = TransitViewListFragment.class.getName();

    public static Handler mainActivityHandler;
    IconTextPendingIntentModel[] _values;
    ArrayAdapter<IconTextPendingIntentModel> _adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(TAG, "on activity created");

        // set the divider to null in order to allow the gradient to work
        getListView().setDivider(null);
        getListView().setPadding(0,5,0,5);
        getListView().setDividerHeight(5);

        // programmatically set the background to the main background
//        getListView().setBackgroundResource(R.drawable.main_background);

        int transitViewListviewItemCount = getResources().getStringArray(R.array.settings_listview_items_texts).length;
//        int settingsListViewItemCount = getResources().getStringArray(R.array.settings_listview_items_texts).length;
        IconTextPendingIntentModel[] values = new IconTextPendingIntentModel[transitViewListviewItemCount];
        for (int i = 0; i < transitViewListviewItemCount; i++) {
            String text = getResources().getStringArray(R.array.settings_listview_items_texts)[i];
            boolean enabled = true;

            String icon_ImageBase = getResources().getString(R.string.settings_icon_imageBase);
            String icon_ImageSuffix = getResources().getStringArray(R.array.settings_listview_items_iconSuffixes)[i];
            String url_toLoad = getResources().getStringArray(R.array.settings_listview_items_urls)[i];

            IconTextPendingIntentModel iconTextPendingIntentModel = new IconTextPendingIntentModel(text,
                    icon_ImageBase, icon_ImageSuffix, url_toLoad, enabled);

            values[i] = iconTextPendingIntentModel;
        }

        this._values = values;

        _adapter = new Settings_ListViewItem_ArrayAdapter(getActivity(), values);

        setListAdapter(_adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // now that we have returned from the global settings activity,
        //  notify a data set change in case the user changed the setting
        _adapter.notifyDataSetChanged();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "on view created in the list fragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "on create view in the list fragment");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        FragmentTransaction fragmentTransaction;

        switch(position) {
            case 0:
                Log.d(TAG, "transition the user to the system setting to update");
                startActivityForResult(new Intent(Settings.ACTION_DATE_SETTINGS), 2332);

                break;

            case 1:
                Log.d(TAG, "transition the user to the update activity");

                break;

            default:
                Log.d(TAG, "launch default");

                break;
        }
    }
}
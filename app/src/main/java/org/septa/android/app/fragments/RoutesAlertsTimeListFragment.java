/*
 * RoutesAlertsTimeListFragment.java
 * Last modified on 04-30-2014 09:12-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.septa.android.app.R;
import org.septa.android.app.adapters.FareInformation_ListViewItem_ArrayAdapter;
import org.septa.android.app.models.adapterhelpers.TextImageModel;

public class RoutesAlertsTimeListFragment extends ListFragment {
    public static final String TAG = RoutesAlertsTimeListFragment.class.getName();

    public RoutesAlertsTimeListFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        int fareInformationCount = getResources().getStringArray(R.array.connect_fares_listview_item_texts).length;
        TextImageModel[] values = new TextImageModel[(fareInformationCount+1)];
        for (int i = 0; i < fareInformationCount; i++) {
            String fareInformation_item_text = getResources().getStringArray(R.array.connect_fares_listview_item_texts)[i];
            String fareInformation_item_imageBase = getResources().getString(R.string.fareinformation_listViewItems_imagebase);
            String fareInformation_item_imageSuffix = getResources().getStringArray(R.array.connect_fares_listview_item_image_suffixes)[i];

            TextImageModel textImageModel = new TextImageModel(fareInformation_item_text,
                    fareInformation_item_imageBase,
                    fareInformation_item_imageSuffix);

            values[i] = textImageModel;
        }

        values[(values.length-1)] = new TextImageModel(getString(R.string.connect_fareinformation_getmoredetails_buttontext), null, null);

        ArrayAdapter<TextImageModel> adapter = new FareInformation_ListViewItem_ArrayAdapter(inflater.getContext(), values);

        setListAdapter(adapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        Log.d(TAG, "detected a list fragment item being clicked");
    }
}
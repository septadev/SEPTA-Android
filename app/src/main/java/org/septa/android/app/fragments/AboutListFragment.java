/*
 * AboutListFragment.java
 * Last modified on 02-10-2014 11:01-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.septa.android.app.BuildConfig;
import org.septa.android.app.R;
import org.septa.android.app.adapters.About_Attributions_ListViewItem_ArrayAdapter;
import org.septa.android.app.models.adapterhelpers.IconTextPendingIntentModel;

public class AboutListFragment  extends ListFragment {
    private static final String TAG = AboutListFragment.class.getName();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set the divider to null in order to allow the gradient to work
        getListView().setDivider(null);
        getListView().setPadding(0,5,0,5);
        getListView().setDividerHeight(5);

        // programmatically set the background to the main background
//        getListView().setBackgroundResource(R.drawable.main_background);

        int aboutListViewItemCount = getResources().getStringArray(R.array.about_listview_items_texts).length;
        IconTextPendingIntentModel[] values = new IconTextPendingIntentModel[aboutListViewItemCount];
        for (int i = 0; i < aboutListViewItemCount; i++) {
            String text = getResources().getStringArray(R.array.about_listview_items_texts)[i];

            // TODO: clean this up to not hard code the equation for Version
            if (text.equals("Version")) {
                text = text.concat(":  " + BuildConfig.VERSIONNAME);
            }

            String icon_ImageBase = getResources().getString(R.string.about_icon_imageBase);
            String icon_ImageSuffix = getResources().getStringArray(R.array.about_listview_items_iconSuffixes)[i];

            IconTextPendingIntentModel iconTextPendingIntentModel = new IconTextPendingIntentModel(text,
                    icon_ImageBase, icon_ImageSuffix, null);

            values[i] = iconTextPendingIntentModel;
        }

        ArrayAdapter<IconTextPendingIntentModel> adapter = new About_Attributions_ListViewItem_ArrayAdapter(getActivity(), values);

        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        FragmentTransaction fragmentTransaction;

        switch(position) {
            case 0:
                Log.d(TAG, "launch 0");

                fragmentTransaction = getFragmentManager().beginTransaction();

                fragmentTransaction.replace(R.id.fragment_container, new AboutAttributionsListFragment(), "about_attribution_fragment");
                fragmentTransaction.addToBackStack("about_fragment_backstack");

                fragmentTransaction.commit();

                break;

            case 1:
                Log.d(TAG, "launch 1");

                fragmentTransaction = getFragmentManager().beginTransaction();

                fragmentTransaction.replace(R.id.fragment_container, new AboutSourceCodeWebView(), "about_sourcecode_fragment");
                fragmentTransaction.addToBackStack("about_fragment_backstack");

                fragmentTransaction.commit();

                break;

            case 2:
                Log.d(TAG, "launch 2");

                break;

            default:
                Log.d(TAG, "launch default");

                break;
        }
    }
}

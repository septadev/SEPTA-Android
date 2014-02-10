/*
 * FareInformationFragment.java
 * Last modified on 02-08-2014 08:20-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.fragments;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.septa.android.app.R;
import org.septa.android.app.adapters.FareInformation_ListViewItem_ArrayAdapter;
import org.septa.android.app.models.adapterhelpers.TextImageModel;
import org.septa.android.app.utilities.WebViewFragment;

public class FareInformationFragment extends ListFragment {
    public static final String TAG = FareInformationFragment.class.getName();

    public FareInformationFragment() {

    }

    /**
     * Called to instantiate the view. Creates and returns the WebView.
     */
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
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.d(TAG, "detected a listfragment item being clicked");
        if (v instanceof Button) {
            Log.d(TAG, "it is an instance of Button, must be get more details.");
            // we clicked the button, swap the fragments
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            WebViewFragment getMoreDetailsWebViewFragment = new WebViewFragment(getString(R.string.connect_fareinformation_getmoredetails_url));
            ft.replace(R.id.container, getMoreDetailsWebViewFragment, "getMoreDetailsWebViewFragment");
            ft.commit();
        }
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

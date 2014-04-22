/*
 * TransitViewListFragment.java
 * Last modified on 04-12-2014 18:15-0400 by brianhmayo
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
import org.septa.android.app.adapters.TrainView_ListViewItem_ArrayAdapter;
import org.septa.android.app.models.servicemodels.TrainViewModel;

import java.util.ArrayList;
import java.util.List;

public class TransitViewRouteViewListFragment extends ListFragment {
    public static final String TAG = TrainViewListFragment.class.getName();

    private List<TrainViewModel> trainViewModels;

    public TransitViewRouteViewListFragment() {
        // instanciate an empty array list for the TrainViewModels
        trainViewModels = new ArrayList<TrainViewModel>(0);
    }

    public void setTrainViewModels(List<TrainViewModel>trainViewModels) {
        this.trainViewModels = trainViewModels;

        ArrayAdapter<TrainViewModel> adapter = new TrainView_ListViewItem_ArrayAdapter(getActivity(), trainViewModels);
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

        ArrayAdapter<TrainViewModel> adapter = new TrainView_ListViewItem_ArrayAdapter(inflater.getContext(), trainViewModels);
        setListAdapter(adapter);

        Log.d(TAG, "about to call super from the onCreateView in TrainViewListFragment");

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.d(TAG, "detected a listfragment item being clicked");
//        if (v instanceof Button) {
//            Log.d(TAG, "it is an instance of Button, must be get more details.");
//            // we clicked the button, swap the fragments
//            FragmentManager fm = getFragmentManager();
//            FragmentTransaction ft = fm.beginTransaction();
//
//            WebViewFragment getMoreDetailsWebViewFragment = WebViewFragment.newInstance(getString(R.string.connect_fareinformation_getmoredetails_url));
//            ft.replace(R.id.container, getMoreDetailsWebViewFragment, "getMoreDetailsWebViewFragment");
//            ft.commit();
//        }
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
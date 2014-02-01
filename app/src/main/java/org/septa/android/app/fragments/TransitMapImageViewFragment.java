/*
 * TransitMapImageViewFragment.java
 * Last modified on 02-01-2014 08:00-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.septa.android.app.R;
import org.septa.android.app.TouchImageView;

public class TransitMapImageViewFragment extends Fragment {
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        //setContentView(R.layout.activity_main);
//
//        TouchImageView img = new TouchImageView(getActivity());
//        img.setImageResource(R.drawable.system_map);
//        img.setMaxZoom(4f);
//        setContentView(img);
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        mDrawerListView = (ListView) inflater.inflate(
//                R.layout.fragment_navigation_drawer, container, false);
//        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                selectItem(position);
//            }
//        });
//
//        mDrawerListView.setAdapter(new NavigationDrawer_ListViewItem_ArrayAdapter(getActionBar().getThemedContext(),
//                getResources().getStringArray(R.array.nav_main_items)));
//
//        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        TouchImageView img = new TouchImageView(getActivity());
        img.setImageResource(R.drawable.system_map);
        img.setMaxZoom(4f);

        return img;
//        return mDrawerListView;
    }
}

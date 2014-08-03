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
import org.septa.android.app.SubsamplingScaleImageView;


public class TransitMapImageViewFragment extends Fragment {
    private static final String TAG = TransitMapImageViewFragment.class.getName();

    private SubsamplingScaleImageView imageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View theView = inflater.inflate(R.layout.fragment_transitmap, container, false);

            imageView = (SubsamplingScaleImageView) theView.findViewById(R.id.imageView);
            imageView.setImageAsset("system-map.png");

        return theView;
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
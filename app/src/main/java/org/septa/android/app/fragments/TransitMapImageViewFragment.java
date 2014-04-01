/*
 * TransitMapImageViewFragment.java
 * Last modified on 02-01-2014 08:00-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.fragments;

import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.septa.android.app.R;
import org.septa.android.app.SubsamplingScaleImageView;

import java.io.IOException;

public class TransitMapImageViewFragment extends Fragment {
    private static final String TAG = TransitMapImageViewFragment.class.getName();

    private static final String STATE_SCALE = "state-scale";
    private static final String STATE_CENTER_X = "state-center-x";
    private static final String STATE_CENTER_Y = "state-center-y";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View theView = inflater.inflate(R.layout.fragment_transitmap, container, false);

        try {
            SubsamplingScaleImageView imageView = (SubsamplingScaleImageView)theView.findViewById(R.id.imageView);
            imageView.setImageAsset("system-map.png");
        } catch (IOException e) {
            Log.e(TransitMapImageViewFragment.class.getSimpleName(), "Could not load asset", e);
        }

        return theView;
    }
}

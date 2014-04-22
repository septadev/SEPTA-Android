/*
 * TransitViewActionBarActivity.java
 * Last modified on 04-21-2014 22:10-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.septa.android.app.R;
import org.septa.android.app.fragments.TransitViewListFragment;

public class TransitViewActionBarActivity extends BaseAnalyticsActionBarActivity {
    public static final String TAG = TransitViewActionBarActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String titleText = getIntent().getStringExtra(getString(R.string.actionbar_titletext_key));

        setContentView(R.layout.transitview);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setIcon(R.drawable.ic_actionbar_tips);
        getSupportActionBar().setTitle(titleText);

        if (findViewById(R.id.transitview_fragment_container) != null) {
            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            TransitViewListFragment transitViewListFragment = new TransitViewListFragment();

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.transitview_fragment_container, transitViewListFragment, "transitview_fragment").commit();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActityResult with resultCode of " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
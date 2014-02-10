/*
 * AboutActionBarActivity.java
 * Last modified on 02-09-2014 16:39-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import org.septa.android.app.R;
import org.septa.android.app.fragments.AboutListFragment;

public class AboutActionBarActivity extends BaseAnalyticsActionBarActivity {
    public static final String TAG = AboutActionBarActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String titleText = getIntent().getStringExtra(getString(R.string.actionbar_titletext_key));

        setContentView(R.layout.about);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setIcon(R.drawable.ic_actionbar_tips);
        getSupportActionBar().setTitle(titleText);

        if (findViewById(R.id.about_fragment_container) != null) {
            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            AboutListFragment aboutListFragment = new AboutListFragment();

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.about_fragment_container, aboutListFragment, "about_fragment").commit();
        }
   }
}

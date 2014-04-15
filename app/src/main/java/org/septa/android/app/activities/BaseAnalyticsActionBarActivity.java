/*
 * BaseAnalyticsActionBarActivity.java
 * Last modified on 01-29-2014 13:20-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Window;

public class BaseAnalyticsActionBarActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // enables the activity indicator in the action bar
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // placeholder for analytics code
    }

    @Override
    protected void onStop() {
        super.onStop();

        // placeholder for analytics code
    }
}

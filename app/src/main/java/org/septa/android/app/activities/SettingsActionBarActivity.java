/*
 * SettingsActionBarActivity.java
 * Last modified on 02-04-2014 14:36-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.os.Bundle;

public class SettingsActionBarActivity extends BaseAnalyticsActionBarActivity {
    public static final String TAG = SettingsActionBarActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}

/**
 * Created by acampbell on 11/3/14.
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities.schedules;

import android.os.Bundle;

import org.septa.android.app.activities.BaseAnalyticsActionBarActivity;

public class ServiceAdvisoryActivity extends BaseAnalyticsActionBarActivity {
    private static String TAG = ServiceAdvisoryActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

        }
    }
}

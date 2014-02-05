/*
 * PlaceholderActionBarActivity.java
 * Last modified on 02-04-2014 13:49-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.septa.android.app.R;

public class PlaceholderActionBarActivity extends BaseAnalyticsActionBarActivity {
    public static final String TAG = PlaceholderActionBarActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String titleText = getIntent().getStringExtra("titleText");
        String iconText = getIntent().getStringExtra("iconText");

        String resourceName = "ic_actionbar_".concat(iconText);

        Log.d(TAG, "resource name is " + resourceName);

        int id = getResources().getIdentifier(resourceName, "drawable", getPackageName());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(titleText);
        getSupportActionBar().setIcon(id);

        setContentView(R.layout.placeholderactionbaractivity);
        TextView textView = (TextView)findViewById(R.id.placeholderactionbaractivityTextView);
        textView.setText(titleText);
    }
}

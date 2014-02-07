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

        String actionBarTitleText = getIntent().getStringExtra(getString(R.string.actionbar_titletext_key));
        String iconImageNameSuffix = getIntent().getStringExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key));

        String resourceName = getString(R.string.actionbar_iconimage_imagename_base).concat(iconImageNameSuffix);

        int id = getResources().getIdentifier(resourceName, "drawable", getPackageName());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(actionBarTitleText);
        getSupportActionBar().setIcon(id);

        setContentView(R.layout.placeholderactionbaractivity);
        TextView textView = (TextView)findViewById(R.id.placeholderactionbaractivityTextView);
        textView.setText(actionBarTitleText);
    }
}

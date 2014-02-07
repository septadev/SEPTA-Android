/*
 * FareInformationGetMoreDetailsActionBarActivity.java
 * Last modified on 02-07-2014 18:22-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.os.Bundle;
import android.webkit.WebView;

import org.septa.android.app.R;

public class FareInformationGetMoreDetailsActionBarActivity extends BaseAnalyticsActionBarActivity {
    public static final String TAG = FareInformationGetMoreDetailsActionBarActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String titleText = getIntent().getStringExtra(getString(R.string.actionbar_titletext_key));

        setContentView(R.layout.getmoredetails);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(titleText);

        WebView getMoreDetailsWebView = (WebView)findViewById(R.id.getmoredetails_webview);
        getMoreDetailsWebView.getSettings().setJavaScriptEnabled(true);

        getMoreDetailsWebView.loadUrl(getString(R.string.connect_fareinformation_getmoredetails_url));
    }
}

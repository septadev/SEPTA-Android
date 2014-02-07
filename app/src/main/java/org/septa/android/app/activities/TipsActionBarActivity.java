/*
 * TipsActionBarActivity.java
 * Last modified on 02-05-2014 15:19-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.os.Bundle;
import android.webkit.WebView;

import org.septa.android.app.R;

public class TipsActionBarActivity extends BaseAnalyticsActionBarActivity {
    public static final String TAG = TipsActionBarActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String titleText = getIntent().getStringExtra(getString(R.string.actionbar_titletext_key));

        setContentView(R.layout.tips);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_actionbar_tips);
        getSupportActionBar().setTitle(titleText);

        WebView tipsWebView = (WebView)findViewById(R.id.tips_webview);
        tipsWebView.getSettings().setJavaScriptEnabled(true);

        tipsWebView.loadUrl(getString(R.string.realtime_tips_webview_url));
    }
}

/*
 * AboutSourceCodeWebView.java
 * Last modified on 02-10-2014 12:59-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.fragments;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.septa.android.app.activities.MainTabbarActivity;
import org.septa.android.app.utilities.WebViewFragment;

public class AboutWebView extends WebViewFragment {
    public static final String TAG = AboutWebView.class.getName();

    private String urlToLoad;

    public AboutWebView(String urlToLoad) {

        this.urlToLoad = urlToLoad;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getWebView().getSettings().setJavaScriptEnabled(true);
        getWebView().getSettings().setBuiltInZoomControls(true);
        getWebView().getSettings().setLoadWithOverviewMode(true);
        getWebView().getSettings().setUseWideViewPort(true);

        getWebView().setWebViewClient(new SwAWebClient());

        Log.d(TAG, "about to load the WebView with this string |"+this.urlToLoad+"|");
        getWebView().loadUrl(this.urlToLoad);
    }

    private class SwAWebClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            return false;
        }
    }
}

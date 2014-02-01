/*
 * TransitMapWebViewFragment.java
 * Last modified on 02-01-2014 12:13-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.support.v4.app.Fragment;

import org.septa.android.app.R;

public class TransitMapWebViewFragment extends Fragment {

    private String currentURL;

    public void init(String url) {
        currentURL = url;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.transitmap_webview_fragment, container, false);

        WebView webView = (WebView) view.findViewById(R.id.webPage);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        webView.setWebViewClient(new SwAWebClient());

        webView.loadDataWithBaseURL("file:///android_asset/",
                "<img src=\"file:///android_res/drawable/system_map.jpg\">",
                "text/html",
                "utf-8",
                null);

        return view;
    }

    private class SwAWebClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }
    }
}

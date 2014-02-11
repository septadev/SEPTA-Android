/*
 * WebViewFragment.java
 * Last modified on 02-08-2014 07:48-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.utilities;

/**
 * Copyright (c) 2013, Thomas Charrière
 * http://www.codepanda.ch/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.FrameLayout;

import org.septa.android.app.fragments.AboutWebView;

/**
 * A fragment that displays a WebView.
 * <p>
 * The WebView is automatically paused or resumed when the Fragment is paused or resumed.
 */
public class WebViewFragment extends Fragment {
    private WebView mWebView;
    private FrameLayout mLayout;
    private boolean mIsWebViewAvailable;
    private String urlToLoad;

    public static WebViewFragment newInstance(String urlToLoad) {
        WebViewFragment webViewFragment = new WebViewFragment();
        Bundle args = new Bundle();
        args.putString("urlToLoad", urlToLoad);
        webViewFragment.setArguments(args);

        return webViewFragment;
    }

    /**
     * Called to instantiate the view. Creates and returns the WebView.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mWebView != null) {
            mWebView.destroy();
        }
        mLayout = new FrameLayout(getActivity());
        mWebView = new WebView(getActivity());
        mIsWebViewAvailable = true;

        mWebView.getSettings().setJavaScriptEnabled(true);

        /** check if we have already set the Url to load, if yes, load it */
        if (urlToLoad != null && !urlToLoad.trim().equals("")) {
            mWebView.loadUrl(urlToLoad);
        }

        mLayout.addView(mWebView, LayoutParams.MATCH_PARENT);

        return mLayout;
    }

    /**
     * Called when the fragment is visible to the user and actively running. Resumes the WebView.
     */
    @Override
    public void onPause() {

        super.onPause();
    }

    /**
     * Called when the fragment is no longer resumed. Pauses the WebView.
     */
    @Override
    public void onResume() {

        super.onResume();
    }

    /**
     * Called when the WebView has been detached from the fragment.
     * The WebView is no longer available after this time.
     */
    @Override
    public void onDestroyView() {
        mIsWebViewAvailable = false;
        super.onDestroyView();
    }

    /**
     * Called when the fragment is no longer in use. Destroys the internal state of the WebView.
     */
    @Override
    public void onDestroy() {
        if (mWebView != null) {
            mLayout.removeAllViews();
            mWebView.removeAllViews();
            mWebView.destroy();
            mWebView = null;
            mLayout = null;
        }
        super.onDestroy();
    }

    /**
     * Gets the WebView.
     */
    public WebView getWebView() {

        return mIsWebViewAvailable ? mWebView : null;
    }

    public void setUrlToLoad(String urlToLoad) {

        this.urlToLoad = urlToLoad;
        mWebView.loadUrl(urlToLoad);
    }
}

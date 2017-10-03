package org.septa.android.app.webview;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import org.septa.android.app.R;

/**
 * Created by jkampf on 10/3/17.
 */

public class WebViewFragment extends Fragment {
    WebView webView;

    String url;
    View progressView;
    ProgressBar progressBar;


    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.webview_fragment_main, container, false);

        webView = (WebView) rootView.findViewById(R.id.web_contents);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);

        progressView = rootView.findViewById(R.id.progress_view);
        progressView.setVisibility(View.VISIBLE);
        progressBar = (ProgressBar) progressView.findViewById(R.id.progress_bar);


        webView.setWebViewClient(new WebViewClient() {

            //If you will not use this method url links are opeen in new brower not in webview
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                webView.loadUrl(url);
                return true;
            }

            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressView.setVisibility(View.VISIBLE);

            }

            public void onPageFinished(WebView view, String url) {
                progressView.setVisibility(View.GONE);
            }

        });

        webView.loadUrl(url);


        return rootView;
    }

    public static Fragment getInstance(String url){
        WebViewFragment fragement = new WebViewFragment();
        fragement.url = url;
        return fragement;
    }

}

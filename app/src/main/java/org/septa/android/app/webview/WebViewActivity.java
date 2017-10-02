package org.septa.android.app.webview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import org.septa.android.app.Constants;
import org.septa.android.app.R;

import java.net.URI;

/**
 * Created by jkampf on 9/13/17.
 */

public class WebViewActivity extends AppCompatActivity {

    private static final String TAG = WebViewActivity.class.getSimpleName();

    WebView webView;

    String url;
    String title;
    View progressView;
    ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_main);
        setTitle("Web View");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        webView = (WebView) findViewById(R.id.web_contents);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);

        progressView = findViewById(R.id.progress_view);
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

        Intent intent = getIntent();

        if (intent.getExtras().containsKey(Constants.TARGET_URL)) {
            url = intent.getExtras().getString(Constants.TARGET_URL);
            webView.loadUrl(url);
        }

        if (intent.getExtras().containsKey(Constants.TITLE)) {
            title = intent.getExtras().getString(Constants.TITLE);
            setTitle(title);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}

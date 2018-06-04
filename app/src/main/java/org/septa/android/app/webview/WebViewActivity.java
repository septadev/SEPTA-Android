package org.septa.android.app.webview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.septa.android.app.Constants;
import org.septa.android.app.R;

/**
 * Created by jkampf on 9/13/17.
 */

public class WebViewActivity extends AppCompatActivity {

    private static final String TAG = WebViewActivity.class.getSimpleName();

    WebView webView;

    String url;
    String title;
    View progressView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        setTitle("Web View");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        webView = (WebView) findViewById(R.id.web_contents);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setGeolocationEnabled(true);

        progressView = findViewById(R.id.progress_view);

        webView.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            private final String googleDocs = "https://docs.google.com/viewer?url=";

            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressView.setVisibility(View.VISIBLE);

            }

            public void onPageFinished(WebView view, String url) {
                progressView.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.toUpperCase().endsWith(".PDF")) {
                    String pdfUrl = googleDocs + url;
                    view.loadUrl(pdfUrl);
                } else {
                    view.loadUrl(url);
                }
                return true;
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

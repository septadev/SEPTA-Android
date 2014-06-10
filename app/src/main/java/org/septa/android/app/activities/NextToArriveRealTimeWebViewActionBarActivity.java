package org.septa.android.app.activities;

import android.os.Bundle;
import android.webkit.WebView;

import org.septa.android.app.R;

/**
 * Created by bmayo on 6/10/14.
 */
public class NextToArriveRealTimeWebViewActionBarActivity extends BaseAnalyticsActionBarActivity {
    public static final String TAG = NextToArriveRealTimeWebViewActionBarActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.nexttoarrive_realtime_webview);

        WebView webView = (WebView)findViewById(R.id.webView);
        webView.loadUrl(getResources().getString(R.string.nexttoarrive_realtime_webview_url));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_actionbar_nexttoarrive);
        getSupportActionBar().setTitle("Real Time");
    }
}
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


    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        restoreArgs();

        View rootView = inflater.inflate(R.layout.webview_fragment_main, container, false);

        webView = (WebView) rootView.findViewById(R.id.web_contents);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);

        progressView = rootView.findViewById(R.id.progress_view);

        webView.setWebViewClient(new WebViewClient() {

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

    public static Fragment getInstance(String url) {
        WebViewFragment fragement = new WebViewFragment();
        Bundle args = new Bundle();

        args.putString("url", url);
        fragement.setArguments(args);
        return fragement;
    }

    private void restoreArgs() {
        url = getArguments().getString("url");
    }

}

package org.septa.android.app.connect;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.webview.WebViewActivity;

/**
 * Created by jkampf on 9/29/17.
 */

public class ConnectFragement extends Fragment {

    private static final String TAG = ConnectFragement.class.getSimpleName();

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.connect_fragment_main, container, false);

        setAppIntent(rootView, R.id.facebook_arrow, getResources().getString(R.string.facebook_url), getResources().getString(R.string.facebook_app_url));
        setAppIntent(rootView, R.id.twitter_arrow, getResources().getString(R.string.twitter_url), getResources().getString(R.string.twitter_app_url));

        setHttpIntent(rootView, R.id.chat_arrow, getResources().getString(R.string.chat_url), getResources().getString(R.string.chat_title));
        setHttpIntent(rootView, R.id.comment_arrow, getResources().getString(R.string.comment_url), getResources().getString(R.string.comment_title));

        return rootView;
    }

    private void setAppIntent(View rootView, int viewId, final String url, final String appUrl) {
        View twitterLink = rootView.findViewById(viewId);
        twitterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                if (activity != null) {
                    Uri app = Uri.parse(appUrl);
                    Intent intent = new Intent(Intent.ACTION_VIEW, app);
                    if (intent.resolveActivity(getActivity().getPackageManager()) == null) {
                        Uri webpage = Uri.parse(url);
                        intent = new Intent(Intent.ACTION_VIEW, webpage);
                        if (intent.resolveActivity(getActivity().getPackageManager()) == null) {
                            Log.e(TAG, "Unable to resolve app and website Intent URLs:" + appUrl + " and: " + url);
                            return;
                        }
                    }

                    startActivity(intent);
                }
            }
        });
    }


    private void setHttpIntent(View rootView, int viewId, final String url, final String title) {
        View twitterLink = rootView.findViewById(viewId);
        twitterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                if (activity != null) {
                    Intent intent = new Intent(activity, WebViewActivity.class);
                    intent.putExtra(Constants.TARGET_URL, url);
                    intent.putExtra(Constants.TITLE, title);
                    startActivity(intent);
                }
            }
        });
    }
}
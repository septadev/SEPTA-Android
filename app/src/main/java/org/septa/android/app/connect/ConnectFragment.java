package org.septa.android.app.connect;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.rating.RatingUtil;
import org.septa.android.app.support.AnalyticsManager;
import org.septa.android.app.webview.WebViewActivity;

public class ConnectFragment extends Fragment {

    private static final String TAG = ConnectFragment.class.getSimpleName();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_connect, container, false);

        // user can rate the app
        View rateTheApp = rootView.findViewById(R.id.rate_the_app);
        rateTheApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // send user to playstore to rate app
                RatingUtil.rateAppInPlayStore(getContext());
            }
        });

        setAppIntent(rootView, R.id.facebook_arrow, getResources().getString(R.string.facebook_url), getResources().getString(R.string.facebook_app_url));
        setAppIntent(rootView, R.id.twitter_arrow, getResources().getString(R.string.twitter_url), getResources().getString(R.string.twitter_app_url));

        setHttpIntent(rootView, R.id.chat_arrow, getResources().getString(R.string.chat_url), getResources().getString(R.string.chat_title));
        setHttpIntent(rootView, R.id.comment_arrow, getResources().getString(R.string.comment_url), getResources().getString(R.string.comment_title));

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", getActivity().getTitle().toString());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            String title = savedInstanceState.getString("title");
            if (title != null && getActivity() != null) {
                getActivity().setTitle(title);
            }
        }
    }

    private void setAppIntent(View rootView, final int viewId, final String url, final String appUrl) {
        View link = rootView.findViewById(viewId);
        link.setOnClickListener(new View.OnClickListener() {
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

                    // analytics
                    if (viewId == R.id.facebook_arrow) {
                        AnalyticsManager.logContentViewEvent(TAG, AnalyticsManager.CUSTOM_EVENT_FACEBOOK, AnalyticsManager.CUSTOM_EVENT_ID_CONNECT, null);
                    } else if (viewId == R.id.twitter_arrow) {
                        AnalyticsManager.logContentViewEvent(TAG, AnalyticsManager.CUSTOM_EVENT_TWITTER, AnalyticsManager.CUSTOM_EVENT_ID_CONNECT, null);
                    } else {
                        Log.e(TAG, String.format("Could not track event analytics for url: %s", url));
                    }

                    startActivity(intent);
                }
            }
        });
    }

    private void setHttpIntent(View rootView, final int viewId, final String url, final String title) {
        View link = rootView.findViewById(viewId);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                if (activity != null) {
                    Intent intent = new Intent(activity, WebViewActivity.class);
                    intent.putExtra(Constants.TARGET_URL, url);
                    intent.putExtra(Constants.TITLE, title);

                    // analytics
                    if (viewId == R.id.chat_arrow) {
                        AnalyticsManager.logContentViewEvent(TAG, AnalyticsManager.CUSTOM_EVENT_LIVE_CHAT, AnalyticsManager.CUSTOM_EVENT_ID_CONNECT, null);
                    } else if (viewId == R.id.comment_arrow) {
                        AnalyticsManager.logContentViewEvent(TAG, AnalyticsManager.CUSTOM_EVENT_SEND_COMMENT, AnalyticsManager.CUSTOM_EVENT_ID_CONNECT, null);
                    } else {
                        Log.e(TAG, String.format("Could not track event analytics for url: %s", url));
                    }

                    startActivity(intent);
                }
            }
        });
    }

}

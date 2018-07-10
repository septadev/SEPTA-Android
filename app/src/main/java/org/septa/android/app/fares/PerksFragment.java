package org.septa.android.app.fares;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.support.AnalyticsManager;
import org.septa.android.app.webview.WebViewActivity;

public class PerksFragment extends Fragment {

    private static final String TAG = PerksFragment.class.getSimpleName(), TOOLBAR_TITLE = "TOOLBAR_TITLE";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_perks, container, false);

        setHttpIntent(rootView, R.id.perks_button, getResources().getString(R.string.about_perks_url), getResources().getString(R.string.about_perks_title));

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TOOLBAR_TITLE, getActivity().getTitle().toString());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            String title = savedInstanceState.getString(TOOLBAR_TITLE);
            if (title != null && getActivity() != null) {
                getActivity().setTitle(title);
            }
        }
    }

    private void setHttpIntent(View rootView, int viewId, final String url, final String title) {
        View link = rootView.findViewById(viewId);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                if (activity != null) {
                    Intent intent = new Intent(activity, WebViewActivity.class);
                    intent.putExtra(Constants.TARGET_URL, url);
                    intent.putExtra(Constants.TITLE, title);

                    AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_PERKS_MORE, AnalyticsManager.CUSTOM_EVENT_ID_EXTERNAL_LINK, null);
                    startActivity(intent);
                }
            }
        });
    }

}
package org.septa.android.app.fares;

import android.app.Activity;
import android.content.Intent;
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
import org.septa.android.app.support.AnalyticsManager;
import org.septa.android.app.view.TextView;
import org.septa.android.app.webview.WebViewActivity;

import java.text.MessageFormat;

public class FaresFragment extends Fragment {

    private static final String TAG = FaresFragment.class.getSimpleName(), TOOLBAR_TITLE = "TOOLBAR_TITLE";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_fares, container, false);

        setHttpIntent(rootView, R.id.septa_key_link, getResources().getString(R.string.septa_key_url), getResources().getString(R.string.septa_key_title));
        setHttpIntent(rootView, R.id.more_about_fares_button, getResources().getString(R.string.about_fares_url), getResources().getString(R.string.about_fares_title));

        formatAndSetText(rootView, R.id.cash_text, R.string.cash_text, new Object[]{getResources().getString(R.string.cash_price), getResources().getString(R.string.quick_trip_price)});
        formatAndSetText(rootView, R.id.weekly_text, R.string.weekly_text, new Object[]{getResources().getString(R.string.transpass_weekly_price), getResources().getString(R.string.transpass_monthly_price)});
        formatAndSetText(rootView, R.id.cross_text, R.string.cross_text, new Object[]{getResources().getString(R.string.cross_county_weekly_price), getResources().getString(R.string.cross_county_monthly_price)});
        formatAndSetText(rootView, R.id.one_day_con_text, R.string.one_day_con_text, new Object[]{getResources().getString(R.string.one_day_pass_price), getResources().getString(R.string.one_day_quantity)});
        formatAndSetText(rootView, R.id.one_day_ind_text, R.string.one_day_ind_text, new Object[]{getResources().getString(R.string.one_day_ind_individual_price), getResources().getString(R.string.one_day_ind_family_price)});

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

    private void formatAndSetText(View rootView, int targetViewId, int sourceString, Object[] objects) {
        MessageFormat format = new MessageFormat(getResources().getString(sourceString));
        String content = format.format(objects).toString();
        ((TextView) rootView.findViewById(targetViewId)).setHtml(content);
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
                    if (viewId == R.id.septa_key_link) {
                        AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_KEY_MORE, AnalyticsManager.CUSTOM_EVENT_ID_EXTERNAL_LINK, null);
                    } else if (viewId == R.id.more_about_fares_button) {
                        AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_FARES_MORE, AnalyticsManager.CUSTOM_EVENT_ID_EXTERNAL_LINK, null);
                    } else {
                        Log.e(TAG, String.format("Could not track event analytics for url: %s", url));
                    }

                    startActivity(intent);
                }
            }
        });
    }

}

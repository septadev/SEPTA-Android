package org.septa.android.app.fares;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.view.TextView;
import org.septa.android.app.webview.WebViewActivity;

import java.text.MessageFormat;

public class FaresFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_fares, container, false);

        setHttpIntent(rootView, R.id.septa_key_link, getResources().getString(R.string.septa_key_url), getResources().getString(R.string.septa_key_title));
        setHttpIntent(rootView, R.id.more_about_fares_button, getResources().getString(R.string.about_fares_url), getResources().getString(R.string.about_fares_title));
        setHttpIntent(rootView, R.id.perks_button, getResources().getString(R.string.about_perks_url), getResources().getString(R.string.about_perks_title));

        formatAndSetText(rootView, R.id.cash_text, R.string.cash_text, new Object[]{getResources().getString(R.string.cash_price), getResources().getString(R.string.quick_trip_price)});
        formatAndSetText(rootView, R.id.token_text, R.string.token_text, new Object[]{getResources().getString(R.string.token_each_price), getResources().getString(R.string.token_transfer_price)});
        formatAndSetText(rootView, R.id.weekly_text, R.string.weekly_text, new Object[]{getResources().getString(R.string.transpass_weekly_price), getResources().getString(R.string.transpass_monthly_price)});
        formatAndSetText(rootView, R.id.cross_text, R.string.cross_text, new Object[]{getResources().getString(R.string.cross_county_weekly_price), getResources().getString(R.string.cross_county_mothly_price)});
        formatAndSetText(rootView, R.id.one_day_con_text, R.string.one_day_con_text, new Object[]{getResources().getString(R.string.one_day_pass_price), getResources().getString(R.string.one_day_quantity)});
        formatAndSetText(rootView, R.id.one_day_ind_text, R.string.one_day_ind_text, new Object[]{getResources().getString(R.string.one_day_ind_individual_price), getResources().getString(R.string.one_day_ind_family_price)});

        return rootView;
    }

    private void formatAndSetText(View rootView, int targetViewId, int sourceString, Object[] objects) {
        MessageFormat format = new MessageFormat(getResources().getString(sourceString));
        String content = format.format(objects).toString();
        ((TextView) rootView.findViewById(targetViewId)).setHtml(content);
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
            if (title != null && getActivity() != null)
                getActivity().setTitle(title);
        }
    }
}

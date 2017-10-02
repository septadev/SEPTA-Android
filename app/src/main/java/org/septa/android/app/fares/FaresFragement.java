package org.septa.android.app.fares;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import org.septa.android.app.R;
import org.septa.android.app.view.TextView;

import java.text.MessageFormat;

/**
 * Created by jkampf on 9/29/17.
 */

public class FaresFragement extends Fragment {

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fares_fragement_main, container, false);

        setHttpIntent(rootView, R.id.septa_key_link, getResources().getString(R.string.septa_key_url));
        setHttpIntent(rootView, R.id.more_about_fares_button, getResources().getString(R.string.about_fares_url));
        setHttpIntent(rootView, R.id.pass_perks_button, getResources().getString(R.string.about_pass_perks_url));

        formatAndSetText(rootView, R.id.cash_text, R.string.cash_text, new Object[]{getResources().getString(R.string.cash_price), getResources().getString(R.string.quick_trip_price)});
        formatAndSetText(rootView, R.id.token_text, R.string.token_text, new Object[]{getResources().getString(R.string.token_each_price), getResources().getString(R.string.token_transfer_price)});
        formatAndSetText(rootView, R.id.weekly_text, R.string.weekly_text, new Object[]{getResources().getString(R.string.transpass_weekly_price), getResources().getString(R.string.transpass_monthly_price)});
        formatAndSetText(rootView, R.id.cross_text, R.string.cross_text, new Object[]{getResources().getString(R.string.cross_county_weekly_price), getResources().getString(R.string.cross_county_mothly_price)});
        formatAndSetText(rootView, R.id.one_day_con_text, R.string.one_day_con_text, new Object[]{getResources().getString(R.string.one_day_pass_price), getResources().getString(R.string.one_day_quantity)});
        formatAndSetText(rootView, R.id.one_day_ind_text, R.string.one_day_ind_text, new Object[]{getResources().getString(R.string.one_day_ind_individual_price), getResources().getString(R.string.one_day_ind_family_price)});

        final View field = rootView.findViewById(R.id.pass_perks_logo_fill);
        final View logo = rootView.findViewById(R.id.pass_perks_logo);
        final View logoLayout = rootView.findViewById(R.id.pass_perks_logo_layout);

        logoLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (logoLayout.getWidth() != 0) {
                    ViewGroup.LayoutParams param = field.getLayoutParams();
                    param.width = logoLayout.getWidth() - logo.getWidth();
                    param.height = logoLayout.getHeight();
                    field.setLayoutParams(param);
                }
            }
        });

        return rootView;
    }

    private void formatAndSetText(View rootView, int targetViewId, int sourceString, Object[] objects) {
        MessageFormat format = new MessageFormat(getResources().getString(sourceString));
        String content = format.format(objects).toString();
        ((TextView) rootView.findViewById(targetViewId)).setHtml(content);
    }


    private void setHttpIntent(View rootView, int viewId, final String url) {
        View twitterLink = rootView.findViewById(viewId);
        twitterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                if (activity != null) {
                    Uri webpage = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            }
        });
    }
}

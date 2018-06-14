package org.septa.android.app.about;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.septa.android.app.BuildConfig;
import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.database.DatabaseManager;
import org.septa.android.app.view.TextView;
import org.septa.android.app.webview.WebViewActivity;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class AboutFragment extends Fragment {
    boolean attribExpanded = false;
    TextView attribTitle;
    LinearLayout attribListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View fragmentView = inflater.inflate(R.layout.fragment_about, null);

        attribListView = (LinearLayout) fragmentView.findViewById(R.id.attrib_list);
        for (String s : getResources().getStringArray(R.array.about_attributions_listview_items_texts)) {
            TextView attribLine = (TextView) inflater.inflate(R.layout.item_about_attribute, null);
            attribLine.setHtml(s);
            attribListView.addView(attribLine);

        }

        attribTitle = (TextView) fragmentView.findViewById(R.id.attrib_title);
        attribTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!attribExpanded) {
                    Drawable[] drawables = attribTitle.getCompoundDrawables();
                    attribTitle.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1],
                            ContextCompat.getDrawable(getContext(), R.drawable.alert_toggle_open), drawables[3]);
                    attribListView.setVisibility(View.VISIBLE);
                    attribExpanded = true;
                    attribTitle.setText("Hide Attributions");
                } else {
                    Drawable[] drawables = attribTitle.getCompoundDrawables();
                    attribTitle.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1],
                            ContextCompat.getDrawable(getContext(), R.drawable.alert_toggle_closed), drawables[3]);
                    attribListView.setVisibility(View.GONE);
                    attribTitle.setText("View Attributions");
                    attribExpanded = false;
                }
            }
        });

        StringBuilder versionInfoBuilder = new StringBuilder("App Version: ");
        versionInfoBuilder.append(BuildConfig.VERSIONNAME).append("<br>");

        SimpleDateFormat formatter = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        formatter.setTimeZone(TimeZone.getTimeZone("gmt"));


        if (getActivity() == null)
            return fragmentView;

        try {
            ApplicationInfo ai = getActivity().getPackageManager().getApplicationInfo(getActivity().getPackageName(), 0);

            long time = new File(ai.sourceDir).lastModified();
            String s = formatter.format(new java.util.Date(time));
            versionInfoBuilder.append("Last App Update: ").append(s).append(" GMT").append("<br>");
        } catch (Exception e) {
        }

        versionInfoBuilder.append("Database Version: ").append(DatabaseManager.getDatabase(getContext()).getVersion()).append("<br>");

        try {
            File dbFile = new File(DatabaseManager.getDatabase().getPath());
            String s = formatter.format(new java.util.Date(dbFile.lastModified()));
            versionInfoBuilder.append("Last Schedule Update: ").append(s).append(" GMT");
        } catch (Exception e) {
        }


        ((TextView) fragmentView.findViewById(R.id.build_info)).setHtml(versionInfoBuilder.toString());

        setHttpIntent(fragmentView, R.id.feedback_button, getResources().getString(R.string.comment_url), getResources().getString(R.string.comment_title));
        return fragmentView;

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

package org.septa.android.app.about;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.septa.android.app.BuildConfig;
import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.database.DatabaseManager;
import org.septa.android.app.rating.SharedPreferencesRatingUtil;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.support.AnalyticsManager;
import org.septa.android.app.systemstatus.SharedPreferencesAlertUtil;
import org.septa.android.app.view.TextView;
import org.septa.android.app.webview.WebViewActivity;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class AboutFragment extends Fragment {

    private static final String TAG = AboutFragment.class.getSimpleName();
    private Activity activity;

    private boolean attribExpanded = false;
    private TextView attribTitle;
    private LinearLayout attribListView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View fragmentView = inflater.inflate(R.layout.fragment_about, null);

        activity = getActivity();
        if (activity == null) {
            return fragmentView;
        }

        String deviceId = SeptaServiceFactory.getNotificationsService().getDeviceId(activity);
        ((TextView) fragmentView.findViewById(R.id.device_id)).setText(getString(R.string.device_id, deviceId));

        attribListView = fragmentView.findViewById(R.id.attrib_list);
        for (String s : getResources().getStringArray(R.array.about_attributions_listview_items_texts)) {
            TextView attribLine = (TextView) inflater.inflate(R.layout.item_about_attribute, null);
            attribLine.setHtml(s);
            attribListView.addView(attribLine);
        }

        attribTitle = fragmentView.findViewById(R.id.attrib_title);
        attribTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!attribExpanded) {
                    Drawable[] drawables = attribTitle.getCompoundDrawables();
                    attribTitle.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1],
                            ContextCompat.getDrawable(activity, R.drawable.alert_toggle_open), drawables[3]);
                    attribListView.setVisibility(View.VISIBLE);
                    attribExpanded = true;
                    attribTitle.setText("Hide Attributions");
                } else {
                    Drawable[] drawables = attribTitle.getCompoundDrawables();
                    attribTitle.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1],
                            ContextCompat.getDrawable(activity, R.drawable.alert_toggle_closed), drawables[3]);
                    attribListView.setVisibility(View.GONE);
                    attribTitle.setText("View Attributions");
                    attribExpanded = false;
                }
            }
        });

        // reset the number of app uses / pop up alert snoozing
        if (BuildConfig.IS_NONPROD_BUILD) {
            fragmentView.findViewById(R.id.septa_logo).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferencesRatingUtil.setAppRated(activity, false);
                    SharedPreferencesRatingUtil.setNumberOfUses(activity, 0);
                    SharedPreferencesAlertUtil.setHiddenMobileAlertTimestamp(activity, "");
                    SharedPreferencesAlertUtil.setHiddenGlobalAlertTimestamp(activity, "");
                    Toast.makeText(activity, "Resetting  App Rating and Hidden Pop-Up Alerts", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // version number
        StringBuilder versionInfoBuilder = new StringBuilder("App Version: ");
        versionInfoBuilder.append(BuildConfig.VERSIONNAME);

        // add note if built as beta
        if (BuildConfig.IS_NONPROD_BUILD) {
            versionInfoBuilder.append(" (BETA)");
        }
        versionInfoBuilder.append("<br>");

        SimpleDateFormat formatter = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        formatter.setTimeZone(TimeZone.getTimeZone("gmt"));

        try {
            ApplicationInfo ai = activity.getPackageManager().getApplicationInfo(activity.getPackageName(), 0);

            long time = new File(ai.sourceDir).lastModified();
            String s = formatter.format(new java.util.Date(time));
            versionInfoBuilder.append("Last App Update: ").append(s).append(" GMT").append("<br>");
        } catch (Exception e) {
        }

        versionInfoBuilder.append("Database Version: ").append(DatabaseManager.getDatabase(activity).getVersion()).append("<br>");

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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", activity.getTitle().toString());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            String title = savedInstanceState.getString("title");
            if (title != null && activity != null) {
                activity.setTitle(title);
            }
        }
    }

    private void setHttpIntent(View rootView, int viewId, final String url, final String title) {
        View link = rootView.findViewById(viewId);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity != null) {
                    Intent intent = new Intent(activity, WebViewActivity.class);
                    intent.putExtra(Constants.TARGET_URL, url);
                    intent.putExtra(Constants.TITLE, title);

                    AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_APP_FEEDBACK, AnalyticsManager.CUSTOM_EVENT_ID_EXTERNAL_LINK, null);
                    startActivity(intent);
                }
            }
        });
    }

}
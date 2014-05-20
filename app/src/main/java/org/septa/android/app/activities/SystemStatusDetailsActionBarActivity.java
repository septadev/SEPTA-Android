/*
 * SystemStatusDetailsActionBarActivity.java
 * Last modified on 05-19-2014 12:25-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import org.septa.android.app.R;
import org.septa.android.app.models.SystemStatusDetailType;
import org.septa.android.app.models.servicemodels.RouteAlertDataModel;
import org.septa.android.app.services.apiproxies.RouteAlertServiceProxy;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import roboguice.util.Ln;

public class SystemStatusDetailsActionBarActivity extends BaseAnalyticsActionBarActivity {
    public static final String TAG = SystemStatusDetailsActionBarActivity.class.getName();

    private SystemStatusDetailType selectedTab = SystemStatusDetailType.ADVISORY;

    private boolean advisoryTabEnabled = false;
    private boolean alertsTabEnabled = false;
    private boolean detourTabEnabled = false;

    private String routeId = null;
    private ArrayList<RouteAlertDataModel>routeAlertModelList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String actionBarTitleText = getIntent().getStringExtra(getString(R.string.actionbar_titletext_key));
        String iconImageNameSuffix = getIntent().getStringExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key));
        routeId = getIntent().getStringExtra(getString(R.string.systemstatus_details_route_id));

        advisoryTabEnabled = getIntent().getBooleanExtra(getString(R.string.systemstatus_details_tabenabled_advisory), false);
        alertsTabEnabled = getIntent().getBooleanExtra(getString(R.string.systemstatus_details_tabenabled_alerts), false);
        detourTabEnabled = getIntent().getBooleanExtra(getString(R.string.systemstatus_details_tabenabled_detour), false);

        String resourceName = getString(R.string.actionbar_iconimage_imagename_base).concat(iconImageNameSuffix);

        int id = getResources().getIdentifier(resourceName, "drawable", getPackageName());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(actionBarTitleText);
        getSupportActionBar().setIcon(id);

        setContentView(R.layout.realtime_systemstatus_details);

        RelativeLayout advisoryLayoutView = (RelativeLayout)findViewById(R.id.realtime_systemstatus_details_tab_advisory_view);
        RelativeLayout alertsLayoutView = (RelativeLayout)findViewById(R.id.realtime_systemstatus_details_tab_alert_view);
        RelativeLayout detourLayoutView = (RelativeLayout)findViewById(R.id.realtime_systemstatus_details_tab_detour_view);

        if (detourTabEnabled) {
            detourLayoutView.setVisibility(View.VISIBLE);
            selectedTab = SystemStatusDetailType.DETOUR;
        } else {
            detourLayoutView.setVisibility(View.GONE);
        }

        if (alertsTabEnabled) {
            alertsLayoutView.setVisibility(View.VISIBLE);
            selectedTab = SystemStatusDetailType.ALERTS;
        } else {
            alertsLayoutView.setVisibility(View.GONE);
        }

        if (advisoryTabEnabled) {
            advisoryLayoutView.setVisibility(View.VISIBLE);
            selectedTab = SystemStatusDetailType.ADVISORY;
        } else {
            advisoryLayoutView.setVisibility(View.GONE);
        }

        WebView webView = (WebView)findViewById(R.id.realtime_systemstatus_details_webview);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                Ln.d("got the url as "+url);

                return false;
            }
        });

        webView.loadData("<html><head><title></title></head><body>loading...</body></html>", "text/html", null);

        fetchRouteAlerts();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        switch (selectedTab) {
            case ADVISORY: {
                selectedAdvisoryTab();
                break;
            }
            case ALERTS: {
                selectedAlertsTab();
                break;
            }
            case DETOUR: {
                selectedDetourTab();
                break;
            }
            default: {
                Ln.d("this should never be reached");
            }
        }
    }

    public void tabSelected(View view) {
        switch (view.getId()) {
            case R.id.realtime_systemstatus_details_tab_advisory_view: {
                selectedTab = SystemStatusDetailType.ADVISORY;

                selectedAdvisoryTab();

                break;
            }
            case R.id.realtime_systemstatus_details_tab_alert_view: {
                selectedTab = SystemStatusDetailType.ALERTS;

                selectedAlertsTab();

                break;
            }
            case R.id.realtime_systemstatus_details_tab_detour_view: {
                selectedTab = SystemStatusDetailType.DETOUR;

                selectedDetourTab();

                break;
            }
            default: {
                Ln.d("not sure how we feel into this default for this switch");
            }
        }

//        fetchRouteAlerts();
    }

    private void selectedAdvisoryTab() {
        // get the color from the looking array given the ordinal position of the route type
        String barColor = this.getResources().getStringArray(R.array.systemstatus_details_separatorbar_colors)[0];

        RelativeLayout advisoryTabView = (RelativeLayout)findViewById(R.id.realtime_systemstatus_details_tab_advisory_view);
        RelativeLayout detourTabView = (RelativeLayout)findViewById(R.id.realtime_systemstatus_details_tab_detour_view);
        RelativeLayout alertsTabView = (RelativeLayout)findViewById(R.id.realtime_systemstatus_details_tab_alert_view);

        View separatorView = (View)findViewById(R.id.realtime_systemstatus_details_separator_view);
        separatorView.setBackgroundColor(Color.parseColor(barColor));

        GradientDrawable advisoryTabViewShapeDrawable = (GradientDrawable)advisoryTabView.getBackground();
        GradientDrawable detourTabViewShapeDrawable = (GradientDrawable)detourTabView.getBackground();
        GradientDrawable alertsTabViewShapeDrawable = (GradientDrawable)alertsTabView.getBackground();

        advisoryTabViewShapeDrawable.setColor(Color.parseColor("#FFAAAAAA"));
        detourTabViewShapeDrawable.setColor(Color.WHITE);
        alertsTabViewShapeDrawable.setColor(Color.WHITE);

        renderRouteAlertsToWebView();
    }

    private void selectedDetourTab() {
        // get the color from the looking array given the ordinal position of the route type
        String barColor = this.getResources().getStringArray(R.array.systemstatus_details_separatorbar_colors)[1];

        RelativeLayout advisoryTabView = (RelativeLayout)findViewById(R.id.realtime_systemstatus_details_tab_advisory_view);
        RelativeLayout detourTabView = (RelativeLayout)findViewById(R.id.realtime_systemstatus_details_tab_detour_view);
        RelativeLayout alertsTabView = (RelativeLayout)findViewById(R.id.realtime_systemstatus_details_tab_alert_view);

        View separatorView = (View)findViewById(R.id.realtime_systemstatus_details_separator_view);
        separatorView.setBackgroundColor(Color.parseColor(barColor));

        GradientDrawable advisoryTabViewShapeDrawable = (GradientDrawable)advisoryTabView.getBackground();
        GradientDrawable detourTabViewShapeDrawable = (GradientDrawable)detourTabView.getBackground();
        GradientDrawable alertsTabViewShapeDrawable = (GradientDrawable)alertsTabView.getBackground();

        advisoryTabViewShapeDrawable.setColor(Color.WHITE);
        detourTabViewShapeDrawable.setColor(Color.parseColor("#FFAAAAAA"));
        alertsTabViewShapeDrawable.setColor(Color.WHITE);

        renderRouteAlertsToWebView();
    }

    private void selectedAlertsTab() {
        // get the color from the looking array given the ordinal position of the route type
        String barColor = this.getResources().getStringArray(R.array.systemstatus_details_separatorbar_colors)[2];

        RelativeLayout advisoryTabView = (RelativeLayout)findViewById(R.id.realtime_systemstatus_details_tab_advisory_view);
        RelativeLayout detourTabView = (RelativeLayout)findViewById(R.id.realtime_systemstatus_details_tab_detour_view);
        RelativeLayout alertsTabView = (RelativeLayout)findViewById(R.id.realtime_systemstatus_details_tab_alert_view);

        View separatorView = (View)findViewById(R.id.realtime_systemstatus_details_separator_view);
        separatorView.setBackgroundColor(Color.parseColor(barColor));

        GradientDrawable advisoryTabViewShapeDrawable = (GradientDrawable)advisoryTabView.getBackground();
        GradientDrawable detourTabViewShapeDrawable = (GradientDrawable)detourTabView.getBackground();
        GradientDrawable alertsTabViewShapeDrawable = (GradientDrawable)alertsTabView.getBackground();

        advisoryTabViewShapeDrawable.setColor(Color.WHITE);
        detourTabViewShapeDrawable.setColor(Color.WHITE);
        alertsTabViewShapeDrawable.setColor(Color.parseColor("#FFAAAAAA"));

        renderRouteAlertsToWebView();
    }

    private void renderRouteAlertsToWebView() {
        String htmlToDisplay = "<html><body>";
        if (routeAlertModelList != null) {
            for (RouteAlertDataModel routeAlert : routeAlertModelList) {
                switch (selectedTab) {
                    case ADVISORY:
                    case ALERTS: {

                        htmlToDisplay += routeAlert.getAdvisoryMessage();
                        break;
                    }
                    case DETOUR: {
                        htmlToDisplay += routeAlert.getDetourDetailsAsHTML();
                        break;
                    }
                    default: {
                        Ln.d("should never be able to get here");
                    }
                }
            }
        }

        htmlToDisplay += "</body></html>";

        WebView webView = (WebView)findViewById(R.id.realtime_systemstatus_details_webview);
        webView.loadData(htmlToDisplay, "text/html", null);
    }

    private void fetchRouteAlerts() {
        Callback callback = new Callback() {
            @Override
            public void success(Object o, Response response) {
                setProgressBarIndeterminateVisibility(Boolean.FALSE);

                routeAlertModelList = (ArrayList<RouteAlertDataModel>)o;
                Ln.d("callback called for alerts with count of "+routeAlertModelList.size());

                renderRouteAlertsToWebView();
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                setProgressBarIndeterminateVisibility(Boolean.FALSE);

                try {
                    Log.d(TAG, "A failure in the call to train view service with body |" + retrofitError.getResponse().getBody().in() + "|");
                } catch (Exception ex) {
                    // TODO: clean this up
                    Log.d(TAG, "blah... what is going on?");
                }
            }
        };

        RouteAlertServiceProxy routeAlertServiceProxy = new RouteAlertServiceProxy();
        setProgressBarIndeterminateVisibility(Boolean.TRUE);
        routeAlertServiceProxy.getRouteAlertData(routeId, callback);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}


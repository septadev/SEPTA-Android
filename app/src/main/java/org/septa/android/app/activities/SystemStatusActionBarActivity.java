/*
 * SystemStatusActionBarActivity.java
 * Last modified on 05-16-2014 15:41-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.internal.is;

import org.septa.android.app.R;
import org.septa.android.app.adapters.SystemStatus_ListViewItem_ArrayAdapter;
import org.septa.android.app.models.ObjectFactory;
import org.septa.android.app.models.servicemodels.AlertModel;
import org.septa.android.app.models.servicemodels.ElevatorOutagesMetaModel;
import org.septa.android.app.models.servicemodels.ElevatorOutagesModel;
import org.septa.android.app.services.apiproxies.AlertsServiceProxy;

import java.util.ArrayList;
import java.util.Collections;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SystemStatusActionBarActivity extends BaseAnalyticsActionBarActivity implements AdapterView.OnItemClickListener{
    public static final String TAG = SystemStatusActionBarActivity.class.getName();

    private boolean inFilterMode = false;

    private final String[] tabLabels = new String[] {"BUS", "TROLLEY", "REGIONAL RAIL", "MFL, BSL, NHSL"};
    private int selectedTab = 0;

    private ArrayList<AlertModel>alertModelList = new ArrayList<AlertModel>();
    private ElevatorOutagesModel elevatorOutages = ElevatorOutagesModel.EmptyElevatorOutagesModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String resourceName = getString(R.string.actionbar_iconimage_imagename_base).concat("systemstatus");

        int id = getResources().getIdentifier(resourceName, "drawable", getPackageName());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("System Status");
        getSupportActionBar().setIcon(id);

        setContentView(R.layout.realtime_systemstatus);

        selectedTab = ObjectFactory.getInstance().getSharedPreferencesManager(this).getSystemStatusSelectedTab();
        inFilterMode = ObjectFactory.getInstance().getSharedPreferencesManager(this).getSystemStatusFilterEnabled();

        // set the empty view in case we don't have any data
        LinearLayout emptyView = (LinearLayout)findViewById(R.id.empty);
        ListView listView = (ListView)findViewById(R.id.realtime_systemstatus_listview);
        listView.setOnItemClickListener(this);
        listView.setEmptyView(emptyView);

        TextView loadingTextView = (TextView)findViewById(R.id.realtime_systemstatus_emptylist_textview);
        ProgressBar loadingProgressBar = (ProgressBar)findViewById(R.id.realtime_systemstatus_emptylist_progressbar);
        loadingTextView.setText("no data to display.");
        loadingProgressBar.setVisibility(View.GONE);

        fetchAlerts();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        switch (selectedTab) {
            case 0: {
                selectedBusTab();
                break;
            }
            case 1: {
                selectedTrolleyTab();
                break;
            }
            case 2: {
                selectedRegionalRailTab();
                break;
            }
            case 3: {
                selectedMFLBSLNHSLTab();
                break;
            }
            default: {
                Log.d("f", "this should never be reached");
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AlertModel alert = (AlertModel)parent.getItemAtPosition(position);

        String displayRouteName = alert.getRouteName();
        if (alert.isBus()) {
            displayRouteName = "Route " + displayRouteName;
        }
        if (alert.isTrolley()) {
            displayRouteName = "Trolley " + displayRouteName;
        }

        Intent systemStatusDetailsIntent;
        systemStatusDetailsIntent = new Intent(this, SystemStatusDetailsActionBarActivity.class);
        systemStatusDetailsIntent.putExtra(getString(R.string.actionbar_titletext_key), displayRouteName);
        systemStatusDetailsIntent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), "systemstatus");

        // if the suspend is enabled, no other tabs will exist
        if (alert.hasSuspendedFlag()) {
            systemStatusDetailsIntent.putExtra("system_status_details_tabenabled_suspend", true);
        } else {
            if (alert.hasAdvisoryFlag()) {
                systemStatusDetailsIntent.putExtra(getString(R.string.systemstatus_details_tabenabled_advisory), true);
            }
            if (alert.hasAlertFlag()) {
                systemStatusDetailsIntent.putExtra(getString(R.string.systemstatus_details_tabenabled_alerts), true);
            }
            if (alert.hasDetourFlag()) {
                systemStatusDetailsIntent.putExtra(getString(R.string.systemstatus_details_tabenabled_detour), true);
            }
        }

        Log.d(TAG, "about to put route id as "+alert.getRouteId());
        systemStatusDetailsIntent.putExtra(getString(R.string.systemstatus_details_route_id), alert.getRouteId());

        startActivity(systemStatusDetailsIntent);
    }

    public void tabSelected(View view) {
        // first, clear out the current data and invalidate the listview to reload
        ListView listView = (ListView)findViewById(R.id.realtime_systemstatus_listview);
        SystemStatus_ListViewItem_ArrayAdapter systemStatusListViewArrayAdapter = new SystemStatus_ListViewItem_ArrayAdapter(SystemStatusActionBarActivity.this, new ArrayList<AlertModel>());
        listView.setAdapter(systemStatusListViewArrayAdapter);
        listView.invalidate();

        switch (view.getId()) {
            case R.id.realtime_systemstatus_tab_bus_view: {
                selectedTab = 0;

                selectedBusTab();

                break;
            }
            case R.id.realtime_systemstatus_tab_trolley_view: {
                selectedTab = 1;

                selectedTrolleyTab();

                break;
            }
            case R.id.realtime_systemstatus_tab_regionalrail_view: {
                selectedTab = 2;

                selectedRegionalRailTab();

                break;
            }
            case R.id.realtime_systemstatus_tab_mflbslnhsl_view: {
                selectedTab = 3;

                selectedMFLBSLNHSLTab();

                break;
            }
            default: {
                Log.d("f", "not sure how we feel into this default for this switch");
            }
        }

        LinearLayout tabbarLayoutView = (LinearLayout)findViewById(R.id.realtime_systemstatus_tabbar_linearlayout);
        tabbarLayoutView.setBackgroundColor(Color.BLACK);

        fetchAlerts();
    }

    private void selectedBusTab() {
        // get the color from the looking array given the ordinal position of the route type
        String busColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[5];
        String trolleyColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[3];
        String regionalRailColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[0];
        String mflColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[1];
        String bslColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[2];
        String nhslColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[4];

        ImageView busImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_bus_imageview);
        ImageView trolleyImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_trolley_imageview);
        ImageView regionalRailImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_regionalrail_rrl_imageview);
        ImageView mflImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_mfl_imageview);
        ImageView bslImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_bsl_imageview);
        ImageView nhslImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_nhsl_imageview);

        View mflbslnhslVerticalLine1 = (View)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_verticalline1);
        View mflbslnhslVerticalLine2 = (View)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_verticalline2);

        LinearLayout busTabView = (LinearLayout)findViewById(R.id.realtime_systemstatus_tab_bus_view);
        LinearLayout trolleyTabView = (LinearLayout)findViewById(R.id.realtime_systemstatus_tab_trolley_view);
        LinearLayout regionalRailTabView = (LinearLayout)findViewById(R.id.realtime_systemstatus_tab_regionalrail_view);
        RelativeLayout mflBSLNHSLTabView = (RelativeLayout)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_view);

        TextView tabSelectedTextView = (TextView)findViewById(R.id.realtime_systemstatus_tabselection_textview);

        GradientDrawable busImageViewShapeDrawable = (GradientDrawable)busImageView.getBackground();
        GradientDrawable trolleyImageViewShapeDrawable = (GradientDrawable)trolleyImageView.getBackground();
        GradientDrawable regionalrailImageViewShapeDrawable = (GradientDrawable) regionalRailImageView.getBackground();
        GradientDrawable mflImageViewShapeDrawable = (GradientDrawable)mflImageView.getBackground();
        GradientDrawable bslImageViewShapeDrawable = (GradientDrawable)bslImageView.getBackground();
        GradientDrawable nhslImageViewShapeDrawable = (GradientDrawable)nhslImageView.getBackground();

        busImageViewShapeDrawable.setColor(Color.WHITE);
        trolleyImageViewShapeDrawable.setColor(Color.parseColor(trolleyColor));
        regionalrailImageViewShapeDrawable.setColor(Color.parseColor(regionalRailColor));
        mflImageViewShapeDrawable.setColor(Color.parseColor(mflColor));
        bslImageViewShapeDrawable.setColor(Color.parseColor(bslColor));
        nhslImageViewShapeDrawable.setColor(Color.parseColor(nhslColor));

        busImageView.setImageResource(R.drawable.ic_systemstatus_bus_black);
        trolleyImageView.setImageResource(R.drawable.ic_systemstatus_trolley_white);

        regionalRailImageView.setImageResource(R.drawable.ic_systemstatus_rrl_white);

        mflImageView.setImageResource(R.drawable.ic_systemstatus_mfl_white);
        bslImageView.setImageResource(R.drawable.ic_systemstatus_bsl_white);
        nhslImageView.setImageResource(R.drawable.ic_systemstatus_nhsl_white);

        mflbslnhslVerticalLine1.setVisibility(View.INVISIBLE);
        mflbslnhslVerticalLine2.setVisibility(View.INVISIBLE);

        tabSelectedTextView.setText(tabLabels[0]);
    }

    private void selectedTrolleyTab() {
        // get the color from the looking array given the ordinal position of the route type
        String busColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[5];
        String trolleyColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[3];
        String regionalRailColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[0];
        String mflColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[1];
        String bslColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[2];
        String nhslColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[4];

        ImageView busImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_bus_imageview);
        ImageView trolleyImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_trolley_imageview);
        ImageView regionalRailImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_regionalrail_rrl_imageview);
        ImageView mflImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_mfl_imageview);
        ImageView bslImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_bsl_imageview);
        ImageView nhslImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_nhsl_imageview);

        View mflbslnhslVerticalLine1 = (View)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_verticalline1);
        View mflbslnhslVerticalLine2 = (View)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_verticalline2);

        TextView tabSelectedTextView = (TextView)findViewById(R.id.realtime_systemstatus_tabselection_textview);

        GradientDrawable busImageViewShapeDrawable = (GradientDrawable)busImageView.getBackground();
        GradientDrawable trolleyImageViewShapeDrawable = (GradientDrawable)trolleyImageView.getBackground();
        GradientDrawable regionalrailImageViewShapeDrawable = (GradientDrawable) regionalRailImageView.getBackground();
        GradientDrawable mflImageViewShapeDrawable = (GradientDrawable)mflImageView.getBackground();
        GradientDrawable bslImageViewShapeDrawable = (GradientDrawable)bslImageView.getBackground();
        GradientDrawable nhslImageViewShapeDrawable = (GradientDrawable)nhslImageView.getBackground();

        busImageViewShapeDrawable.setColor(Color.parseColor(busColor));
        trolleyImageViewShapeDrawable.setColor(Color.WHITE);
        regionalrailImageViewShapeDrawable.setColor(Color.parseColor(regionalRailColor));
        mflImageViewShapeDrawable.setColor(Color.parseColor(mflColor));
        bslImageViewShapeDrawable.setColor(Color.parseColor(bslColor));
        nhslImageViewShapeDrawable.setColor(Color.parseColor(nhslColor));

        busImageView.setImageResource(R.drawable.ic_systemstatus_bus_white);
        trolleyImageView.setImageResource(R.drawable.ic_systemstatus_trolley_green);

        regionalRailImageView.setImageResource(R.drawable.ic_systemstatus_rrl_white);

        mflImageView.setImageResource(R.drawable.ic_systemstatus_mfl_white);
        bslImageView.setImageResource(R.drawable.ic_systemstatus_bsl_white);
        nhslImageView.setImageResource(R.drawable.ic_systemstatus_nhsl_white);

        mflbslnhslVerticalLine1.setVisibility(View.INVISIBLE);
        mflbslnhslVerticalLine2.setVisibility(View.INVISIBLE);

        tabSelectedTextView.setText(tabLabels[1]);
    }

    private void selectedRegionalRailTab() {
        // get the color from the looking array given the ordinal position of the route type
        String busColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[5];
        String trolleyColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[3];
        String regionalRailColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[0];
        String mflColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[1];
        String bslColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[2];
        String nhslColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[4];

        ImageView busImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_bus_imageview);
        ImageView trolleyImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_trolley_imageview);
        ImageView regionalRailImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_regionalrail_rrl_imageview);
        ImageView mflImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_mfl_imageview);
        ImageView bslImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_bsl_imageview);
        ImageView nhslImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_nhsl_imageview);

        View mflbslnhslVerticalLine1 = (View)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_verticalline1);
        View mflbslnhslVerticalLine2 = (View)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_verticalline2);

        TextView tabSelectedTextView = (TextView)findViewById(R.id.realtime_systemstatus_tabselection_textview);

        GradientDrawable busImageViewShapeDrawable = (GradientDrawable)busImageView.getBackground();
        GradientDrawable trolleyImageViewShapeDrawable = (GradientDrawable)trolleyImageView.getBackground();
        GradientDrawable regionalrailImageViewShapeDrawable = (GradientDrawable) regionalRailImageView.getBackground();
        GradientDrawable mflImageViewShapeDrawable = (GradientDrawable)mflImageView.getBackground();
        GradientDrawable bslImageViewShapeDrawable = (GradientDrawable)bslImageView.getBackground();
        GradientDrawable nhslImageViewShapeDrawable = (GradientDrawable)nhslImageView.getBackground();

        busImageViewShapeDrawable.setColor(Color.parseColor(busColor));
        trolleyImageViewShapeDrawable.setColor(Color.parseColor(trolleyColor));
        regionalrailImageViewShapeDrawable.setColor(Color.WHITE);
        mflImageViewShapeDrawable.setColor(Color.parseColor(mflColor));
        bslImageViewShapeDrawable.setColor(Color.parseColor(bslColor));
        nhslImageViewShapeDrawable.setColor(Color.parseColor(nhslColor));

        busImageView.setImageResource(R.drawable.ic_systemstatus_bus_white);
        trolleyImageView.setImageResource(R.drawable.ic_systemstatus_trolley_white);

        regionalRailImageView.setImageResource(R.drawable.ic_systemstatus_rrl_blue);

        mflImageView.setImageResource(R.drawable.ic_systemstatus_mfl_white);
        bslImageView.setImageResource(R.drawable.ic_systemstatus_bsl_white);
        nhslImageView.setImageResource(R.drawable.ic_systemstatus_nhsl_white);

        mflbslnhslVerticalLine1.setVisibility(View.INVISIBLE);
        mflbslnhslVerticalLine2.setVisibility(View.INVISIBLE);

        tabSelectedTextView.setText(tabLabels[2]);
    }

    private void selectedMFLBSLNHSLTab() {
        // get the color from the looking array given the ordinal position of the route type
        String busColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[5];
        String trolleyColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[3];
        String regionalRailColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[0];
        String mflColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[1];
        String bslColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[2];
        String nhslColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[4];

        ImageView busImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_bus_imageview);
        ImageView trolleyImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_trolley_imageview);
        ImageView regionalRailImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_regionalrail_rrl_imageview);
        ImageView mflImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_mfl_imageview);
        ImageView bslImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_bsl_imageview);
        ImageView nhslImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_nhsl_imageview);

        View mflbslnhslVerticalLine1 = (View)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_verticalline1);
        View mflbslnhslVerticalLine2 = (View)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_verticalline2);

        TextView tabSelectedTextView = (TextView)findViewById(R.id.realtime_systemstatus_tabselection_textview);

        GradientDrawable busImageViewShapeDrawable = (GradientDrawable)busImageView.getBackground();
        GradientDrawable trolleyImageViewShapeDrawable = (GradientDrawable)trolleyImageView.getBackground();
        GradientDrawable regionalrailImageViewShapeDrawable = (GradientDrawable) regionalRailImageView.getBackground();
        GradientDrawable mflImageViewShapeDrawable = (GradientDrawable)mflImageView.getBackground();
        GradientDrawable bslImageViewShapeDrawable = (GradientDrawable)bslImageView.getBackground();
        GradientDrawable nhslImageViewShapeDrawable = (GradientDrawable)nhslImageView.getBackground();

        busImageViewShapeDrawable.setColor(Color.parseColor(busColor));
        trolleyImageViewShapeDrawable.setColor(Color.parseColor(trolleyColor));
        regionalrailImageViewShapeDrawable.setColor(Color.parseColor(regionalRailColor));
        mflImageViewShapeDrawable.setColor(Color.WHITE);
        bslImageViewShapeDrawable.setColor(Color.WHITE);
        nhslImageViewShapeDrawable.setColor(Color.WHITE);

        busImageView.setImageResource(R.drawable.ic_systemstatus_bus_white);
        trolleyImageView.setImageResource(R.drawable.ic_systemstatus_trolley_white);

        regionalRailImageView.setImageResource(R.drawable.ic_systemstatus_rrl_white);

        mflImageView.setImageResource(R.drawable.ic_systemstatus_mfl_blue);
        bslImageView.setImageResource(R.drawable.ic_systemstatus_bsl_orange);
        nhslImageView.setImageResource(R.drawable.ic_systemstatus_nhsl_purple);

        mflbslnhslVerticalLine1.setVisibility(View.VISIBLE);
        mflbslnhslVerticalLine2.setVisibility(View.VISIBLE);

        tabSelectedTextView.setText(tabLabels[3]);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "creating the menu in find nearest location actionbar activity");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.realtime_systemstatus_action_bar, menu);

        menu.findItem(R.id.actionmenu_systemstatusactionbar_filter_none).setVisible(!inFilterMode);
        menu.findItem(R.id.actionmenu_systemstatusactionbar_filter_removeemptyrows).setVisible(inFilterMode);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionmenu_systemstatusactionbar_filter_none:
                inFilterMode = true;

                reloadListView();

                ActivityCompat.invalidateOptionsMenu(this);

                return true;
            case R.id.actionmenu_systemstatusactionbar_filter_removeemptyrows:
                inFilterMode = false;

                reloadListView();

                ActivityCompat.invalidateOptionsMenu(this);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void reloadListView() {
        ArrayList<AlertModel>selectedAlertList = new ArrayList<AlertModel>();

        switch (selectedTab) {
            case 0: {
                for (AlertModel alert : alertModelList) {
                    if ((alert.isGeneral() || alert.isBus()) && (!inFilterMode || alert.hasFlag())) {
                        selectedAlertList.add(alert);
                    }
                }

                break;
            }
            case 1: {
                for (AlertModel alert : alertModelList) {
                    if ((alert.isGeneral() || alert.isTrolley()) && (!inFilterMode || alert.hasFlag())) {
                        selectedAlertList.add(alert);
                    }
                }

                break;

            }
            case 2: {
                for (AlertModel alert : alertModelList) {
                    if ((alert.isGeneral() || alert.isRegionalRail()) && (!inFilterMode || alert.hasFlag())) {
                        selectedAlertList.add(alert);
                    }
                }

                break;
            }
            case 3: {
                for (AlertModel alert : alertModelList) {
                    if (((alert.isGeneral() || alert.isMFL())  && (!inFilterMode || alert.hasFlag())) ||
                        ((alert.isGeneral() || alert.isBSL())  && (!inFilterMode || alert.hasFlag())) ||
                        ((alert.isGeneral() || alert.isNHSL()) && (!inFilterMode || alert.hasFlag()))) {
                            selectedAlertList.add(alert);
                    }
                }

                break;
            }
            default: {
                Log.d("f", "should never get here");
            }
        }

        ListView listView = (ListView)findViewById(R.id.realtime_systemstatus_listview);
        Collections.sort(selectedAlertList);
        SystemStatus_ListViewItem_ArrayAdapter systemStatusListViewArrayAdapter = new SystemStatus_ListViewItem_ArrayAdapter(SystemStatusActionBarActivity.this, selectedAlertList);
        listView.setAdapter(systemStatusListViewArrayAdapter);
        listView.invalidate();
    }

    private void fetchAlerts() {
        TextView loadingTextView = (TextView)findViewById(R.id.realtime_systemstatus_emptylist_textview);
        ProgressBar loadingProgressBar = (ProgressBar)findViewById(R.id.realtime_systemstatus_emptylist_progressbar);
        loadingTextView.setText("loading data...");
        loadingProgressBar.setVisibility(View.VISIBLE);

        Callback callback = new Callback() {
            @Override
            public void success(Object o, Response response) {
                setProgressBarIndeterminateVisibility(Boolean.FALSE);

                TextView loadingTextView = (TextView)findViewById(R.id.realtime_systemstatus_emptylist_textview);
                ProgressBar loadingProgressBar = (ProgressBar)findViewById(R.id.realtime_systemstatus_emptylist_progressbar);
                loadingTextView.setText("");
                loadingProgressBar.setVisibility(View.GONE);

                alertModelList = (ArrayList<AlertModel>)o;

                reloadListView();
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                setProgressBarIndeterminateVisibility(Boolean.FALSE);

                TextView loadingTextView = (TextView)findViewById(R.id.realtime_systemstatus_emptylist_textview);
                ProgressBar loadingProgressBar = (ProgressBar)findViewById(R.id.realtime_systemstatus_emptylist_progressbar);
                loadingTextView.setText("an error has occurred.");
                loadingProgressBar.setVisibility(View.GONE);

                try {
                    Log.d(TAG, "A failure in the call to train view service with body |" + retrofitError.getResponse().getBody().in() + "|");
                } catch (Exception ex) {
                    // TODO: clean this up
                    Log.d(TAG, "blah... what is going on?");
                }
            }
        };

        AlertsServiceProxy alertsServiceProxy = new AlertsServiceProxy();
        setProgressBarIndeterminateVisibility(Boolean.TRUE);
        alertsServiceProxy.getAlerts(callback);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ObjectFactory.getInstance().getSharedPreferencesManager(this).setSystemStatusSelectedTab(selectedTab);
        ObjectFactory.getInstance().getSharedPreferencesManager(this).setSystemStatusFilterEnabled(inFilterMode);
    }
}


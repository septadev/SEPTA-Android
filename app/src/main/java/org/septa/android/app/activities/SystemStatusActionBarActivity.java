/*
 * SystemStatusActionBarActivity.java
 * Last modified on 05-16-2014 15:41-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.septa.android.app.R;

import roboguice.util.Ln;

public class SystemStatusActionBarActivity extends BaseAnalyticsActionBarActivity {
    public static final String TAG = SystemStatusActionBarActivity.class.getName();

    private boolean inChangeRadiusMode = false;

    private final String[] tabLabels = new String[] {"TRANSIT", "REGIONAL RAIL", "MFL, BSL, NHSL"};
    private int selectedTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String actionBarTitleText = getIntent().getStringExtra(getString(R.string.actionbar_titletext_key));
        String iconImageNameSuffix = getIntent().getStringExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key));

        String resourceName = getString(R.string.actionbar_iconimage_imagename_base).concat(iconImageNameSuffix);

        int id = getResources().getIdentifier(resourceName, "drawable", getPackageName());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(actionBarTitleText);
        getSupportActionBar().setIcon(id);

        setContentView(R.layout.realtime_systemstatus);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        switch (selectedTab) {
            case 0: {
                selectedBusTrolleyTab();
                break;
            }
            case 1: {
                selectedRegionalRailTab();
                break;
            }
            case 2: {
                selectedMFLBSLNHSLTab();
                break;
            }
            default: {
                Ln.d("this should never be reached");
            }
        }
    }

    public void tabSelected(View view) {
        switch (view.getId()) {
            case R.id.realtime_systemstatus_tab_bustrolley_view: {
                selectedTab = 0;

                selectedBusTrolleyTab();

                break;
            }
            case R.id.realtime_systemstatus_tab_regionalrail_view: {
                selectedTab = 1;

                selectedRegionalRailTab();

                break;
            }
            case R.id.realtime_systemstatus_tab_mflbslnhsl_view: {
                selectedTab = 2;

                selectedMFLBSLNHSLTab();

                break;
            }
            default: {
                Ln.d("not sure how we feel into this default for this switch");
            }
        }
    }

    private void selectedBusTrolleyTab() {
        // get the color from the looking array given the ordinal position of the route type
        String busColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[5];
        String trolleyColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[3];
        String regionalRailColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[0];
        String mflColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[1];
        String bslColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[2];
        String nhslColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[4];

        ImageView busImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_bustrolley_bus_imageview);
        ImageView trolleyImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_bustrolley_trolley_imageview);
        ImageView regionalRailImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_regionalrail_rrl_imageview);
        ImageView mflImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_mfl_imageview);
        ImageView bslImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_bsl_imageview);
        ImageView nhslImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_nhsl_imageview);

        View busTrolleyVerticalLine = (View)findViewById(R.id.realtime_systemstatus_tab_bustrolley_verticalline);
        View mflbslnhslVerticalLine1 = (View)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_verticalline1);
        View mflbslnhslVerticalLine2 = (View)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_verticalline2);

        TextView tabSelectedTextView = (TextView)findViewById(R.id.realtime_systemstatus_tabselection_textview);

        GradientDrawable busImageViewShapeDrawable = (GradientDrawable)busImageView.getBackground();
        GradientDrawable trolleyImageViewShapeDrawable = (GradientDrawable)trolleyImageView.getBackground();
        GradientDrawable regionalrailImageViewShapeDrawable = (GradientDrawable) regionalRailImageView.getBackground();
        GradientDrawable mflImageViewShapeDrawable = (GradientDrawable)mflImageView.getBackground();
        GradientDrawable bslImageViewShapeDrawable = (GradientDrawable)bslImageView.getBackground();
        GradientDrawable nhslImageViewShapeDrawable = (GradientDrawable)nhslImageView.getBackground();

        busImageViewShapeDrawable.setColor(Color.WHITE);
        trolleyImageViewShapeDrawable.setColor(Color.WHITE);
        regionalrailImageViewShapeDrawable.setColor(Color.parseColor(regionalRailColor));
        mflImageViewShapeDrawable.setColor(Color.parseColor(mflColor));
        bslImageViewShapeDrawable.setColor(Color.parseColor(bslColor));
        nhslImageViewShapeDrawable.setColor(Color.parseColor(nhslColor));

        busImageView.setImageResource(R.drawable.ic_systemstatus_bus_black);
        trolleyImageView.setImageResource(R.drawable.ic_systemstatus_trolley_green);

        regionalRailImageView.setImageResource(R.drawable.ic_systemstatus_rrl_white);

        mflImageView.setImageResource(R.drawable.ic_systemstatus_mfl_white);
        bslImageView.setImageResource(R.drawable.ic_systemstatus_bsl_white);
        nhslImageView.setImageResource(R.drawable.ic_systemstatus_nhsl_white);

        busTrolleyVerticalLine.setVisibility(View.VISIBLE);
        mflbslnhslVerticalLine1.setVisibility(View.INVISIBLE);
        mflbslnhslVerticalLine1.setVisibility(View.INVISIBLE);

        tabSelectedTextView.setText(tabLabels[0]);
    }

    private void selectedRegionalRailTab() {
        // get the color from the looking array given the ordinal position of the route type
        String busColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[5];
        String trolleyColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[3];
        String regionalRailColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[0];
        String mflColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[1];
        String bslColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[2];
        String nhslColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[4];

        ImageView busImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_bustrolley_bus_imageview);
        ImageView trolleyImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_bustrolley_trolley_imageview);
        ImageView regionalRailImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_regionalrail_rrl_imageview);
        ImageView mflImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_mfl_imageview);
        ImageView bslImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_bsl_imageview);
        ImageView nhslImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_nhsl_imageview);

        View busTrolleyVerticalLine = (View)findViewById(R.id.realtime_systemstatus_tab_bustrolley_verticalline);
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

        busTrolleyVerticalLine.setVisibility(View.INVISIBLE);
        mflbslnhslVerticalLine1.setVisibility(View.INVISIBLE);
        mflbslnhslVerticalLine1.setVisibility(View.INVISIBLE);

        tabSelectedTextView.setText(tabLabels[1]);
    }

    private void selectedMFLBSLNHSLTab() {
        // get the color from the looking array given the ordinal position of the route type
        String busColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[5];
        String trolleyColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[3];
        String regionalRailColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[0];
        String mflColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[1];
        String bslColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[2];
        String nhslColor = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[4];

        ImageView busImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_bustrolley_bus_imageview);
        ImageView trolleyImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_bustrolley_trolley_imageview);
        ImageView regionalRailImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_regionalrail_rrl_imageview);
        ImageView mflImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_mfl_imageview);
        ImageView bslImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_bsl_imageview);
        ImageView nhslImageView = (ImageView)findViewById(R.id.realtime_systemstatus_tab_mflbslnhsl_nhsl_imageview);

        View busTrolleyVerticalLine = (View)findViewById(R.id.realtime_systemstatus_tab_bustrolley_verticalline);
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

        busTrolleyVerticalLine.setVisibility(View.INVISIBLE);
        mflbslnhslVerticalLine1.setVisibility(View.VISIBLE);
        mflbslnhslVerticalLine2.setVisibility(View.VISIBLE);

        tabSelectedTextView.setText(tabLabels[2]);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "creating the menu in find nearest location actionbar activity");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.find_nearest_location_action_bar, menu);

        menu.findItem(R.id.actionmenu_findnearestlocationactionbar_changeradius).setVisible(!inChangeRadiusMode);
        menu.findItem(R.id.actionmenu_findnearestlocationactionbar_changeradius_done).setVisible(inChangeRadiusMode);

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
            case R.id.actionmenu_findnearestlocationactionbar_changeradius:
                inChangeRadiusMode = true;
                ActivityCompat.invalidateOptionsMenu(this);

                return true;
            case R.id.actionmenu_findnearestlocationactionbar_changeradius_done:
                inChangeRadiusMode = false;
                ActivityCompat.invalidateOptionsMenu(this);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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


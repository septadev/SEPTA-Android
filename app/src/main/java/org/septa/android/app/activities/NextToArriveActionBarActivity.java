/*
 * NextToArriveActionBarActivity.java
 * Last modified on 05-22-2014 11:46-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;

import org.septa.android.app.R;
import org.septa.android.app.activities.schedules.SchedulesItineraryActionBarActivity;
import org.septa.android.app.adapters.NextToArrive_ListViewItem_ArrayAdapter;
import org.septa.android.app.models.SchedulesRouteModel;

import roboguice.util.Ln;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class NextToArriveActionBarActivity  extends BaseAnalyticsActionBarActivity implements
        AdapterView.OnItemClickListener, StickyListHeadersListView.OnHeaderClickListener,
        StickyListHeadersListView.OnStickyHeaderOffsetChangedListener,
        StickyListHeadersListView.OnStickyHeaderChangedListener, View.OnTouchListener {
    public static final String TAG = NextToArriveActionBarActivity.class.getName();

    private NextToArrive_ListViewItem_ArrayAdapter mAdapter;

    private StickyListHeadersListView stickyList;

    private String iconImageNameSuffix;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nexttoarrive);

        String actionBarTitleText = getIntent().getStringExtra(getString(R.string.actionbar_titletext_key));
        iconImageNameSuffix = getIntent().getStringExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key));
        String resourceName = getString(R.string.actionbar_iconimage_imagename_base).concat(iconImageNameSuffix);

        Ln.d("resource name is to be " + resourceName);

        int id = getResources().getIdentifier(resourceName, "drawable", getPackageName());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("|" + actionBarTitleText);
        getSupportActionBar().setIcon(id);

        mAdapter = new NextToArrive_ListViewItem_ArrayAdapter(this);

        stickyList = (StickyListHeadersListView) findViewById(R.id.list);
        stickyList.setOnItemClickListener(this);
        stickyList.setOnHeaderClickListener(this);
        stickyList.setOnStickyHeaderChangedListener(this);
        stickyList.setOnStickyHeaderOffsetChangedListener(this);
        stickyList.setEmptyView(findViewById(R.id.empty));
        stickyList.setDrawingListUnderStickyHeader(true);
        stickyList.setAreHeadersSticky(true);
        stickyList.setAdapter(mAdapter);
        stickyList.setOnTouchListener(this);

        stickyList.setFastScrollAlwaysVisible(true);
        stickyList.setFastScrollEnabled(true);

//        stickyList.setDivider(null);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "creating the menu in find nearest location actionbar activity");
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.realtime_systemstatus_action_bar, menu);
//
//        menu.findItem(R.id.actionmenu_systemstatusactionbar_filter_none).setVisible(!inFilterMode);
//        menu.findItem(R.id.actionmenu_systemstatusactionbar_filter_removeemptyrows).setVisible(inFilterMode);

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

                ActivityCompat.invalidateOptionsMenu(this);

                return true;
            case R.id.actionmenu_systemstatusactionbar_filter_removeemptyrows:

                ActivityCompat.invalidateOptionsMenu(this);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.setOnTouchListener(null);
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
        String routeShortName = "";
        Ln.d("onItemClick occurred at position "+position+" with id "+id+" and route short name of "+routeShortName);

        if (!mAdapter.isFavorite(position) && !mAdapter.isRecentlyViewed(position)) {
            SchedulesRouteModel rtm = (SchedulesRouteModel)mAdapter.getItem(position);

            routeShortName = rtm.getRouteShortName();
        }

        Intent schedulesItineraryIntent = null;

        schedulesItineraryIntent = new Intent(this, SchedulesItineraryActionBarActivity.class);
//        schedulesItineraryIntent.putExtra(getString(R.string.actionbar_titletext_key), routesModel.get(position).getRouteId());
        schedulesItineraryIntent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), iconImageNameSuffix);
//        schedulesItineraryIntent.putExtra(getString(R.string.schedules_itinerary_travelType),
//                travelType.name());
        schedulesItineraryIntent.putExtra(getString(R.string.schedules_itinerary_routeShortName), routeShortName);

        startActivity(schedulesItineraryIntent);
    }

    @Override
    public void onHeaderClick(StickyListHeadersListView l, View header, int itemPosition, long headerId, boolean currentlySticky) {

    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onStickyHeaderOffsetChanged(StickyListHeadersListView l, View header, int offset) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            header.setAlpha(1 - (offset / (float) header.getMeasuredHeight()));
        }
    }

    @Override
    public void onStickyHeaderChanged(StickyListHeadersListView l, View header,
                                      int itemPosition, long headerId) {

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

    }
}
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.septa.android.app.R;
import org.septa.android.app.adapters.NextToArrive_ListViewItem_ArrayAdapter;
import org.septa.android.app.models.SchedulesRouteModel;
import org.septa.android.app.models.TripDataModel;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class NextToArriveActionBarActivity  extends BaseAnalyticsActionBarActivity implements
        AdapterView.OnItemClickListener, StickyListHeadersListView.OnHeaderClickListener,
        StickyListHeadersListView.OnStickyHeaderOffsetChangedListener,
        StickyListHeadersListView.OnStickyHeaderChangedListener, View.OnTouchListener {
    public static final String TAG = NextToArriveActionBarActivity.class.getName();

    private NextToArrive_ListViewItem_ArrayAdapter mAdapter;
    private StickyListHeadersListView stickyList;

    private TripDataModel tripDataModel = new TripDataModel();

    private boolean menuRevealed = false;
    private boolean inProcessOfStartDestinationFlow = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nexttoarrive);

        String actionBarTitleText = getString(R.string.nexttoarrive_activity_titlebar_text);
        String resourceName = getString(R.string.actionbar_iconimage_imagename_base).concat("nexttoarrive");

        int id = getResources().getIdentifier(resourceName, "drawable", getPackageName());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(actionBarTitleText);
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

        stickyList.setFastScrollEnabled(true);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    public void startEndSelectionSelected(View view) {
        this.inProcessOfStartDestinationFlow = true;

        Intent stopSelectionIntent = null;

        stopSelectionIntent = new Intent(this, NextToArriveStopSelectionActionBarActivity.class);
        stopSelectionIntent.putExtra(getString(R.string.nexttoarrive_stopselection_startordestination), "Start");
        startActivityForResult(stopSelectionIntent, getResources().getInteger(R.integer.nexttoarrive_stopselection_activityforresult_request_id));

        Log.d(TAG, "start destination selection selected");
    }

    public void selectStartSelected(View view) {
        Intent stopSelectionIntent = null;

        stopSelectionIntent = new Intent(this, NextToArriveStopSelectionActionBarActivity.class);
        stopSelectionIntent.putExtra(getString(R.string.nexttoarrive_stopselection_startordestination), "Start");
        startActivityForResult(stopSelectionIntent, getResources().getInteger(R.integer.nexttoarrive_stopselection_activityforresult_request_id));

        Log.d(TAG, "select start selected");
    }

    public void selectDestinationSelected(View view) {
        Intent stopSelectionIntent = null;

        stopSelectionIntent = new Intent(this, NextToArriveStopSelectionActionBarActivity.class);
        stopSelectionIntent.putExtra(getString(R.string.nexttoarrive_stopselection_startordestination), "Destination");
        startActivityForResult(stopSelectionIntent, getResources().getInteger(R.integer.nexttoarrive_stopselection_activityforresult_request_id));

        Log.d(TAG, "select destination selected");
    }

    public void reverseStartEndSelected(View view) {
        Log.d(TAG, "reverse start end selected");

        tripDataModel.reverseStartAndDestinationStops();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == getResources().getInteger(R.integer.nexttoarrive_stopselection_activityforresult_request_id)) {
            if (resultCode == RESULT_OK) {
                String stopName = data.getStringExtra("stop_name");
                String stopId = data.getStringExtra("stop_id");
                String selectionMode = data.getStringExtra("selection_mode");

                if (selectionMode.equals("destination")) {
                    tripDataModel.setDestinationStopId(stopId);
                    tripDataModel.setDestinationStopName(stopName);

                    if (inProcessOfStartDestinationFlow) {
                        inProcessOfStartDestinationFlow = false;
                    }
                } else {
                    tripDataModel.setStartStopId(stopId);
                    tripDataModel.setStartStopName(stopName);

                    if (inProcessOfStartDestinationFlow) {
                        Intent stopSelectionIntent = null;

                        stopSelectionIntent = new Intent(this, NextToArriveStopSelectionActionBarActivity.class);
                        stopSelectionIntent.putExtra(getString(R.string.nexttoarrive_stopselection_startordestination), "Destination");
                        startActivityForResult(stopSelectionIntent, getResources().getInteger(R.integer.nexttoarrive_stopselection_activityforresult_request_id));
                    }
                }
            }
            if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "the result is canceled");
                //Write your code if there's no result
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "creating the menu in find nearest location actionbar activity");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nexttoarrive_action_bar, menu);

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
            case R.id.actionmenu_transitview_reveallistview:

                if (menuRevealed) {
                    menuRevealed = false;

                    hideListView();
                } else {
                    menuRevealed = true;

                    revealListView();
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void revealListView() {
        final FrameLayout rl1 = (FrameLayout) findViewById(R.id.trainview_map_fragment_view);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_right_to_left);

        anim.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {
                final View shadowView = (View) findViewById(R.id.trainview_map_fragmet_view_shadow);
                shadowView.setVisibility(View.VISIBLE);
                shadowView.bringToFront();
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0) {
                LinearLayout ll2 = (LinearLayout) findViewById(R.id.back_frame);
                ll2.bringToFront();
            }
        });

        anim.setInterpolator((new AccelerateDecelerateInterpolator()));
        anim.setFillAfter(true);
        rl1.startAnimation(anim);
    }

    private void hideListView() {
        final FrameLayout rl1 = (FrameLayout) findViewById(R.id.trainview_map_fragment_view);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_left_to_right);
        rl1.bringToFront();

        anim.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0) {
                View shadowView = (View) findViewById(R.id.trainview_map_fragmet_view_shadow);
                shadowView.setVisibility(View.INVISIBLE);
                LinearLayout mapView = (LinearLayout) findViewById(R.id.trainview_map_fragment_innerview);
                mapView.bringToFront();
            }
        });

        anim.setInterpolator((new AccelerateDecelerateInterpolator()));
        anim.setFillAfter(true);
        rl1.startAnimation(anim);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.setOnTouchListener(null);
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
        String routeShortName = "";
        Log.d("f", "onItemClick occurred at position "+position+" with id "+id);

        if (!mAdapter.isFavorite(position) && !mAdapter.isRecentlyViewed(position)) {
            SchedulesRouteModel rtm = (SchedulesRouteModel)mAdapter.getItem(position);

            routeShortName = rtm.getRouteShortName();
        }

//        Intent schedulesItineraryIntent = null;
//
//        schedulesItineraryIntent = new Intent(this, SchedulesItineraryActionBarActivity.class);
//        schedulesItineraryIntent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), iconImageNameSuffix);
//        schedulesItineraryIntent.putExtra(getString(R.string.schedules_itinerary_routeShortName), routeShortName);
//
//        startActivity(schedulesItineraryIntent);
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
}
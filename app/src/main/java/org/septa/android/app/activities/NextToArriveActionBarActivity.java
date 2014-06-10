/*
 * NextToArriveActionBarActivity.java
 * Last modified on 05-22-2014 11:46-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import org.septa.android.app.R;
import org.septa.android.app.adapters.NextToArrive_ListViewItem_ArrayAdapter;
import org.septa.android.app.adapters.NextToArrive_MenuDialog_ListViewItem_ArrayAdapter;
import org.septa.android.app.managers.NextToArriveFavoritesAndRecentlyViewedStore;
import org.septa.android.app.models.NextToArriveRecentlyViewedModel;
import org.septa.android.app.models.NextToArriveStoredTripModel;
import org.septa.android.app.models.TripDataModel;
import org.septa.android.app.models.adapterhelpers.TextSubTextImageModel;
import org.septa.android.app.models.servicemodels.NextToArriveModel;
import org.septa.android.app.services.apiproxies.NextToArriveServiceProxy;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class NextToArriveActionBarActivity  extends BaseAnalyticsActionBarActivity implements
        AdapterView.OnItemClickListener, StickyListHeadersListView.OnHeaderClickListener,
        StickyListHeadersListView.OnStickyHeaderOffsetChangedListener,
        StickyListHeadersListView.OnStickyHeaderChangedListener, View.OnTouchListener, View.OnClickListener {
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
        NextToArrive_ListViewItem_ArrayAdapter nextToArriveListViewItemArrayAdapter = (NextToArrive_ListViewItem_ArrayAdapter)stickyList.getAdapter();
        nextToArriveListViewItemArrayAdapter.setStartStopName(tripDataModel.getStartStopName());
        nextToArriveListViewItemArrayAdapter.setDestinationStopName(tripDataModel.getDestinationStopName());

        checkTripStartAndDestinationForNextToArriveDataRequest();
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

                NextToArrive_ListViewItem_ArrayAdapter nextToArriveListViewItemArrayAdapter = (NextToArrive_ListViewItem_ArrayAdapter)stickyList.getAdapter();
                nextToArriveListViewItemArrayAdapter.setStartStopName(tripDataModel.getStartStopName());
                nextToArriveListViewItemArrayAdapter.setDestinationStopName(tripDataModel.getDestinationStopName());

                checkTripStartAndDestinationForNextToArriveDataRequest();
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

        FrameLayout menuDialog = (FrameLayout)findViewById(R.id.nexttoarrive_menudialog_mainlayout);
        menuDialog.setBackgroundResource(R.drawable.nexttoarrive_menudialog_bottomcorners);
        GradientDrawable drawable = (GradientDrawable) menuDialog.getBackground();
        drawable.setColor(0xFF353534);

        String iconPrefix = getResources().getString(R.string.nexttoarrive_menu_icon_imageBase);
        String[] texts = getResources().getStringArray(R.array.nexttoarrive_menu_listview_items_texts);
        String[] subTexts = getResources().getStringArray(R.array.nexttoarrive_menu_listview_items_subtexts);
        String[] iconSuffix = getResources().getStringArray(R.array.nexttoarrive_menu_listview_items_iconsuffixes);

        TextSubTextImageModel[] listMenuItems = new TextSubTextImageModel[texts.length];
        for (int i=0; i<texts.length; i++) {
            TextSubTextImageModel textSubTextImageModel = new TextSubTextImageModel(texts[i], subTexts[i], iconPrefix, iconSuffix[i]);
            listMenuItems[i] = textSubTextImageModel;
        }

        ListView menuListView = (ListView)findViewById(R.id.nexttoarrive_menudialog_fragmentlistview);
        menuListView.setAdapter(new NextToArrive_MenuDialog_ListViewItem_ArrayAdapter(this, listMenuItems));

//        menuListView.setDivider(null);
//        menuListView.setPadding(0, 5, 0, 0);
        menuListView.setDividerHeight(5);
        menuListView.setDividerHeight(5);

        menuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
                Log.d(TAG, "click in the menu dialog at position "+position);

                switch (position) {
                    case 0: {       // refresh

                        break;
                    }
                    case 1: {       // favorite

                        break;
                    }
                    case 2: {       // fare
                        startActivity(new Intent(NextToArriveActionBarActivity.this,
                                      FareInformationActionBarActivity.class));
                        break;
                    }
                    case 3: {       // real time

                        break;
                    }
                }

            }
        });

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
            case R.id.actionmenu_nexttoarrive_revealactions:

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
        FrameLayout menuDialog = (FrameLayout)findViewById(R.id.nexttoarrive_menudialog_mainlayout);
        menuDialog.clearAnimation();

//        menuDialog.setBackgroundResource(R.drawable.nexttoarrive_menudialog_bottomcorners);
//        GradientDrawable drawable = (GradientDrawable) menuDialog.getBackground();
//        drawable.setColor(0xBB000000);

        menuDialog.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.nexttoarrive_menudialog_scale_in);
        animation.setDuration(500);

        menuDialog.startAnimation(animation);
    }

    private void hideListView() {
        Log.d(TAG, "hide list view");
        FrameLayout menuDialog = (FrameLayout)findViewById(R.id.nexttoarrive_menudialog_mainlayout);
        menuDialog.clearAnimation();

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.nexttoarrive_menudialog_scale_out);
        animation.setDuration(500);
        menuDialog.startAnimation(animation);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.setOnTouchListener(null);
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
        NextToArriveStoredTripModel storedTripModel = mAdapter.getSelectedFavoriteOrRecentlyViewed(position);
        tripDataModel.setStartStopId(storedTripModel.getStartStopId());
        tripDataModel.setStartStopName(storedTripModel.getStartStopName());
        tripDataModel.setDestinationStopId(storedTripModel.getDestintationStopId());
        tripDataModel.setDestinationStopName(storedTripModel.getDestinationStopName());

        mAdapter.setStartStopName(tripDataModel.getStartStopName());
        mAdapter.setDestinationStopName(tripDataModel.getDestinationStopName());

        checkTripStartAndDestinationForNextToArriveDataRequest();
    }

    @Override
    public void onHeaderClick(StickyListHeadersListView l, View header, int itemPosition, long headerId, boolean currentlySticky) {
        Log.d(TAG, "sticky header click");

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

    private void checkTripStartAndDestinationForNextToArriveDataRequest() {
        // check if we have both the start and destination stops, if yes, fetch the data.
        if ((tripDataModel.getStartStopName() != null) && tripDataModel.getDestinationStopName() != null) {

            NextToArriveFavoritesAndRecentlyViewedStore store = new NextToArriveFavoritesAndRecentlyViewedStore(this);
            NextToArriveRecentlyViewedModel nextToArriveRecentlyViewedModel = new NextToArriveRecentlyViewedModel();
            nextToArriveRecentlyViewedModel.setStartStopId(tripDataModel.getStartStopId());
            nextToArriveRecentlyViewedModel.setStartStopName(tripDataModel.getStartStopName());
            nextToArriveRecentlyViewedModel.setDestintationStopId(tripDataModel.getDestinationStopId());
            nextToArriveRecentlyViewedModel.setDestinationStopName(tripDataModel.getDestinationStopName());

            store.addRecentlyViewed(nextToArriveRecentlyViewedModel);

            fetchNextToArrive();
        }
    }

    private void fetchNextToArrive() {
        Callback callback = new Callback() {
            @Override
            public void success(Object o, Response response) {
                Log.d(TAG, "successfully ended fetch next to arrive service call with " + ((ArrayList<NextToArriveModel>) o).size());
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                mAdapter.setNextToArriveTrainList((ArrayList<NextToArriveModel>)o);
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

        NextToArriveServiceProxy nextToArriveServiceProxy = new NextToArriveServiceProxy();
//        mAdapter.clearNextToArriveTrainList();
        setProgressBarIndeterminateVisibility(Boolean.TRUE);
        // TODO: make the number of results a string value in xml.
        nextToArriveServiceProxy.getNextToArrive(tripDataModel.getStartStopName(),tripDataModel.getDestinationStopName(),"50", callback);
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "detected a click on this view "+v.toString());
    }
}
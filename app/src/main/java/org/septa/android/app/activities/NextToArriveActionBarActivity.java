/*
 * NextToArriveActionBarActivity.java
 * Last modified on 05-22-2014 11:46-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import org.septa.android.app.models.NextToArriveFavoriteModel;
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

public class NextToArriveActionBarActivity extends BaseAnalyticsActionBarActivity implements
        StickyListHeadersListView.OnHeaderClickListener,
        StickyListHeadersListView.OnStickyHeaderOffsetChangedListener,
        StickyListHeadersListView.OnStickyHeaderChangedListener,
        AdapterView.OnItemLongClickListener {

    public static final String TAG = NextToArriveActionBarActivity.class.getName();
    static final String TRIP_MODEL = "tripModel";

    private NextToArrive_ListViewItem_ArrayAdapter mAdapter;
    private StickyListHeadersListView stickyList;
    private ListView menuDialogListView;

    private TripDataModel tripDataModel = new TripDataModel();

    private boolean menuRevealed = false;
    private boolean inProcessOfStartDestinationFlow = false;

    private CountDownTimer scheduleRefreshCountDownTimer;

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
        stickyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NextToArriveStoredTripModel storedTripModel = mAdapter.getSelectedFavoriteOrRecentlyViewed(position);

                // if the tap took place on a next to arrive data row, just return.
                if (storedTripModel == null) {
                    return;
                }
                tripDataModel.setStartStopId(storedTripModel.getStartStopId());
                tripDataModel.setStartStopName(storedTripModel.getStartStopName());
                tripDataModel.setDestinationStopId(storedTripModel.getDestintationStopId());
                tripDataModel.setDestinationStopName(storedTripModel.getDestinationStopName());

                mAdapter.setStartStopName(tripDataModel.getStartStopName());
                mAdapter.setDestinationStopName(tripDataModel.getDestinationStopName());

                checkTripStartAndDestinationForNextToArriveDataRequest();
            }
        });
        stickyList.setOnItemLongClickListener(this);
        stickyList.setOnHeaderClickListener(this);
        stickyList.setOnStickyHeaderChangedListener(this);
        stickyList.setOnStickyHeaderOffsetChangedListener(this);
        stickyList.setDrawingListUnderStickyHeader(true);
        stickyList.setAreHeadersSticky(true);
        stickyList.setAdapter(mAdapter);

        stickyList.setFastScrollEnabled(true);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if(savedInstanceState != null){
            tripDataModel = savedInstanceState.getParcelable(TRIP_MODEL);
            if (tripDataModel != null) {
                mAdapter.setStartStopName(tripDataModel.getStartStopName());
                mAdapter.setDestinationStopName(tripDataModel.getDestinationStopName());
            }
        }


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(TRIP_MODEL, tripDataModel);
        super.onSaveInstanceState(outState);
    }

    public void startEndSelectionSelected(View view) {
        this.inProcessOfStartDestinationFlow = true;

        tripDataModel.clear();

        Intent stopSelectionIntent = null;

        stopSelectionIntent = new Intent(this, NextToArriveStopSelectionActionBarActivity.class);
        stopSelectionIntent.putExtra(getString(R.string.regionalrail_stopselection_startordestination), "Start");
        startActivityForResult(stopSelectionIntent, getResources().getInteger(R.integer.nexttoarrive_stopselection_activityforresult_request_id));
    }

    public void selectStartSelected(View view) {
        Intent stopSelectionIntent = null;

        stopSelectionIntent = new Intent(this, NextToArriveStopSelectionActionBarActivity.class);
        stopSelectionIntent.putExtra(getString(R.string.regionalrail_stopselection_startordestination), "Start");
        startActivityForResult(stopSelectionIntent, getResources().getInteger(R.integer.nexttoarrive_stopselection_activityforresult_request_id));
    }

    public void selectDestinationSelected(View view) {
        Intent stopSelectionIntent = null;

        stopSelectionIntent = new Intent(this, NextToArriveStopSelectionActionBarActivity.class);
        stopSelectionIntent.putExtra(getString(R.string.regionalrail_stopselection_startordestination), "Destination");
        startActivityForResult(stopSelectionIntent, getResources().getInteger(R.integer.nexttoarrive_stopselection_activityforresult_request_id));
    }

    public void reverseStartEndSelected(View view) {
        tripDataModel.reverseStartAndDestinationStops();
        NextToArrive_ListViewItem_ArrayAdapter nextToArriveListViewItemArrayAdapter = (NextToArrive_ListViewItem_ArrayAdapter) stickyList.getAdapter();
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
                        stopSelectionIntent.putExtra(getString(R.string.regionalrail_stopselection_startordestination), "Destination");
                        startActivityForResult(stopSelectionIntent, getResources().getInteger(R.integer.nexttoarrive_stopselection_activityforresult_request_id));
                    }
                }

                NextToArrive_ListViewItem_ArrayAdapter nextToArriveListViewItemArrayAdapter = (NextToArrive_ListViewItem_ArrayAdapter) stickyList.getAdapter();
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nexttoarrive_action_bar, menu);

        FrameLayout menuDialog = (FrameLayout) findViewById(R.id.nexttoarrive_menudialog_mainlayout);
        menuDialog.setBackgroundResource(R.drawable.nexttoarrive_menudialog_bottomcorners);
        GradientDrawable drawable = (GradientDrawable) menuDialog.getBackground();
        drawable.setColor(0xFF353534);

        String iconPrefix = getResources().getString(R.string.nexttoarrive_menu_icon_imageBase);
        String[] texts = getResources().getStringArray(R.array.nexttoarrive_menu_listview_items_texts);
        String[] subTexts = getResources().getStringArray(R.array.nexttoarrive_menu_listview_items_subtexts);
        String[] iconSuffix = getResources().getStringArray(R.array.nexttoarrive_menu_listview_items_iconsuffixes);

        TextSubTextImageModel[] listMenuItems = new TextSubTextImageModel[texts.length];
        for (int i = 0; i < texts.length; i++) {
            TextSubTextImageModel textSubTextImageModel = new TextSubTextImageModel(texts[i], subTexts[i], iconPrefix, iconSuffix[i]);
            listMenuItems[i] = textSubTextImageModel;
        }

        ListView menuListView = (ListView) findViewById(R.id.nexttoarrive_menudialog_listview);
        this.menuDialogListView = menuListView;
        menuListView.setAdapter(new NextToArrive_MenuDialog_ListViewItem_ArrayAdapter(this, listMenuItems));

        menuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 0: {       // refresh
                        checkTripStartAndDestinationForNextToArriveDataRequest();
                        hideListView();
                        break;
                    }
                    case 1: {       // favorite
                        addOrRemoveRouteFromFavorites();
                        mAdapter.reloadFavoriteAndRecentlyViewedLists();
                        mAdapter.notifyDataSetChanged();
                        hideListView();
                        break;
                    }
                    case 2: {       // fare
                        startActivity(new Intent(NextToArriveActionBarActivity.this,
                                FareInformationActionBarActivity.class));
                        hideListView();
                        break;
                    }
                    case 3: {       // real time
                        startActivity(new Intent(NextToArriveActionBarActivity.this,
                                NextToArriveRealTimeWebViewActionBarActivity.class));
                        hideListView();
                        break;
                    }
                }
            }
        });

        checkTripStartAndDestinationForNextToArriveDataRequest();
        return true;
    }

    private void addOrRemoveRouteFromFavorites() {
        NextToArriveFavoritesAndRecentlyViewedStore store = new NextToArriveFavoritesAndRecentlyViewedStore(this);

        NextToArriveFavoriteModel nextToArriveFavoriteModel = new NextToArriveFavoriteModel();
        nextToArriveFavoriteModel.setStartStopId(tripDataModel.getStartStopId());
        nextToArriveFavoriteModel.setStartStopName(tripDataModel.getStartStopName());
        nextToArriveFavoriteModel.setDestintationStopId(tripDataModel.getDestinationStopId());
        nextToArriveFavoriteModel.setDestinationStopName(tripDataModel.getDestinationStopName());

        // check if the selected route is already a favorite, then we allow the option of removing this
        // route from the favorites list.
        if (store.isFavorite(nextToArriveFavoriteModel)) {
            store.removeFavorite(nextToArriveFavoriteModel);
            ((NextToArrive_MenuDialog_ListViewItem_ArrayAdapter) menuDialogListView.getAdapter()).disableRemovedSavedFavorite();
        } else {
            store.addFavorite(nextToArriveFavoriteModel);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionmenu_nexttoarrive_revealactions:

                if (menuRevealed) {

                    hideListView();
                } else {

                    revealListView();
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void revealListView() {
        menuRevealed = true;

        FrameLayout menuDialog = (FrameLayout) findViewById(R.id.nexttoarrive_menudialog_mainlayout);
        ListView listView = (ListView) findViewById(R.id.nexttoarrive_menudialog_listview);

        menuDialog.clearAnimation();

        listView.setVisibility(View.VISIBLE);
        menuDialog.setVisibility(View.VISIBLE);

        Animation mainLayoutAnimation = AnimationUtils.loadAnimation(this, R.anim.nexttoarrive_menudialog_scale_in);
        mainLayoutAnimation.setDuration(500);

        menuDialog.startAnimation(mainLayoutAnimation);
        listView.setVisibility(View.VISIBLE);
    }

    private void hideListView() {
        FrameLayout menuDialog = (FrameLayout) findViewById(R.id.nexttoarrive_menudialog_mainlayout);
        menuDialog.clearAnimation();

        Animation mainLayOutAnimation = AnimationUtils.loadAnimation(this, R.anim.nexttoarrive_menudialog_scale_out);
        mainLayOutAnimation.setDuration(500);

        mainLayOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                ListView listView = (ListView) findViewById(R.id.nexttoarrive_menudialog_listview);
                listView.setVisibility(View.GONE);
                menuRevealed = false;
            }
        });

        menuDialog.startAnimation(mainLayOutAnimation);
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

    private void checkTripStartAndDestinationForNextToArriveDataRequest() {
        // check if we have both the start and destination stops, if yes, fetch the data.
        if ((tripDataModel.getStartStopName() != null) && tripDataModel.getDestinationStopName() != null) {
            ((NextToArrive_MenuDialog_ListViewItem_ArrayAdapter) menuDialogListView.getAdapter()).enableRefresh();
            ((NextToArrive_MenuDialog_ListViewItem_ArrayAdapter) menuDialogListView.getAdapter()).enableSaveAsFavorite();

            NextToArriveFavoritesAndRecentlyViewedStore store = new NextToArriveFavoritesAndRecentlyViewedStore(this);

            NextToArriveRecentlyViewedModel nextToArriveRecentlyViewedModel = new NextToArriveRecentlyViewedModel();

            nextToArriveRecentlyViewedModel.setStartStopId(tripDataModel.getStartStopId());
            nextToArriveRecentlyViewedModel.setStartStopName(tripDataModel.getStartStopName());
            nextToArriveRecentlyViewedModel.setDestintationStopId(tripDataModel.getDestinationStopId());
            nextToArriveRecentlyViewedModel.setDestinationStopName(tripDataModel.getDestinationStopName());

            NextToArriveFavoriteModel nextToArriveFavoriteModel = new NextToArriveFavoriteModel();
            nextToArriveFavoriteModel.setStartStopId(tripDataModel.getStartStopId());
            nextToArriveFavoriteModel.setStartStopName(tripDataModel.getStartStopName());
            nextToArriveFavoriteModel.setDestintationStopId(tripDataModel.getDestinationStopId());
            nextToArriveFavoriteModel.setDestinationStopName(tripDataModel.getDestinationStopName());

            // check if the selected route is already a favorite, then we allow the option of removing this
            // route from the favorites list.
            if (store.isFavorite(nextToArriveFavoriteModel)) {
                ((NextToArrive_MenuDialog_ListViewItem_ArrayAdapter) menuDialogListView.getAdapter()).enableRemoveSavedFavorite();
            }

            // check if this route is already stored as a favorite; if not store as a recent
            if (!store.isFavorite(nextToArriveRecentlyViewedModel)) {
                store.addRecentlyViewed(nextToArriveRecentlyViewedModel);
            }

            fetchNextToArrive();

            if (scheduleRefreshCountDownTimer != null) {
                scheduleRefreshCountDownTimer.cancel();
                scheduleRefreshCountDownTimer.start();
            } else {
                scheduleRefreshCountDownTimer = createScheduleRefreshCountDownTimer();
                scheduleRefreshCountDownTimer.start();
            }
        } else if (menuDialogListView != null && menuDialogListView.getAdapter() != null){
            ((NextToArrive_MenuDialog_ListViewItem_ArrayAdapter) menuDialogListView.getAdapter()).disableRefresh();
            ((NextToArrive_MenuDialog_ListViewItem_ArrayAdapter) menuDialogListView.getAdapter()).disableSaveAsFavorite();
            ((NextToArrive_MenuDialog_ListViewItem_ArrayAdapter) menuDialogListView.getAdapter()).disableRemovedSavedFavorite();
        }
    }

    private void fetchNextToArrive() {
        Callback callback = new Callback() {
            @Override
            public void success(Object o, Response response) {
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                mAdapter.setNextToArriveTrainList((ArrayList<NextToArriveModel>) o);
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

        String startStopName = tripDataModel.getStartStopName();
        String destinationStopName = tripDataModel.getDestinationStopName();

        String[] displayStopNamesList = getResources().getStringArray(R.array.stopname_translation_display_name);
        String[] gtfsStopNamesList = getResources().getStringArray(R.array.stopname_translation_gtfs_names);
        for (int i = 0; i < displayStopNamesList.length; i++) {
            if (displayStopNamesList[i].equals(startStopName)) {
                startStopName = gtfsStopNamesList[i];
            }
            if (displayStopNamesList[i].equals(destinationStopName)) {
                destinationStopName = gtfsStopNamesList[i];
            }
        }

        nextToArriveServiceProxy.getNextToArrive(startStopName, destinationStopName, "50", callback);
    }

    @Override
    protected void onPause() {
        super.onPause();

        scheduleRefreshCountDownTimer.cancel();
    }

    @Override
    protected void onStart() {
        super.onStart();

        scheduleRefreshCountDownTimer = createScheduleRefreshCountDownTimer();
        if ((tripDataModel.getStartStopName() != null) && tripDataModel.getDestinationStopName() != null) {
            scheduleRefreshCountDownTimer.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        scheduleRefreshCountDownTimer = null;
    }

    private int adjustedPosition(int position) {
        return --position;
    }

    private CountDownTimer createScheduleRefreshCountDownTimer() {
        return new CountDownTimer(20000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                if(menuDialogListView != null && menuDialogListView.getAdapter() != null)
                    ((NextToArrive_MenuDialog_ListViewItem_ArrayAdapter) menuDialogListView.getAdapter()).setNextRefreshInSecondsValue(millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {

                checkTripStartAndDestinationForNextToArriveDataRequest();
            }
        };
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        Log.d(TAG, "on item long click detected");
        if (mAdapter.isFavorite(adjustedPosition(position)) || mAdapter.isRecentlyViewed(adjustedPosition(position))) {
            String alertDialogTitle = mAdapter.isFavorite(adjustedPosition(position)) ? "Delete Favorite" : "Delete Recently Viewed";
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(alertDialogTitle);
            alert.setMessage("Are you sure you want to delete?");

            alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final NextToArriveStoredTripModel nextToArriveStoredTripModel = (NextToArriveStoredTripModel) mAdapter.getItem(adjustedPosition(position));

                    NextToArriveActionBarActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mAdapter.isFavorite(adjustedPosition(position))) {
                                removeFavorite(nextToArriveStoredTripModel);
                            }
                            if (mAdapter.isRecentlyViewed(adjustedPosition(position))) {
                                removeRecentlyViewed(nextToArriveStoredTripModel);
                            }

                            mAdapter.reloadFavoriteAndRecentlyViewedLists();
                            mAdapter.notifyDataSetChanged();
                        }
                    });

                    //do your work here
                    dialog.dismiss();

                }
            });
            alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                }
            });

            alert.show();
            return true;
        }

        return false;
    }

    public boolean removeFavorite(NextToArriveStoredTripModel nextToArriveStoredTripModel) {
        NextToArriveFavoritesAndRecentlyViewedStore store = new NextToArriveFavoritesAndRecentlyViewedStore(this);

        // check if the selected route is already a favorite, then we allow the option of removing this
        // route from the favorites list.
        if (store.isFavorite(nextToArriveStoredTripModel)) {
            Log.d("tt", "detected this is a favorite, remove ");
            store.removeFavorite(nextToArriveStoredTripModel);

            return true;
        }

        return false;
    }

    public boolean removeRecentlyViewed(NextToArriveStoredTripModel nextToArriveStoredTripModel) {
        NextToArriveFavoritesAndRecentlyViewedStore store = new NextToArriveFavoritesAndRecentlyViewedStore(this);

        // check if the selected route is already a favorite, then we allow the option of removing this
        // route from the favorites list.
        if (store.isRecentlyViewed(nextToArriveStoredTripModel)) {
            Log.d("tt", "detected this is a recently viewed, remove ");
            store.removeRecentlyViewed(nextToArriveStoredTripModel);

            return true;
        }

        return false;
    }
}

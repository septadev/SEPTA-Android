/*
 * SchedulesItineraryActionBarActivity.java
 * Last modified on 05-12-2014 09:45-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities.schedules;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.septa.android.app.R;
import org.septa.android.app.activities.BaseAnalyticsActionBarActivity;
import org.septa.android.app.activities.NextToArriveStopSelectionActionBarActivity;
import org.septa.android.app.activities.SchedulesRRStopSelectionActionBarActivity;
import org.septa.android.app.adapters.schedules.SchedulesItinerary_ListViewItem_ArrayAdapter;
import org.septa.android.app.databases.SEPTADatabase;
import org.septa.android.app.managers.SchedulesFavoritesAndRecentlyViewedStore;
import org.septa.android.app.models.RouteTypes;
import org.septa.android.app.models.SchedulesFavoriteModel;
import org.septa.android.app.models.SchedulesRecentlyViewedModel;
import org.septa.android.app.models.SchedulesRouteModel;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static org.septa.android.app.models.RouteTypes.RAIL;
import static org.septa.android.app.models.RouteTypes.valueOf;

public class SchedulesItineraryActionBarActivity  extends BaseAnalyticsActionBarActivity implements
        AdapterView.OnItemClickListener, StickyListHeadersListView.OnHeaderClickListener,
        StickyListHeadersListView.OnStickyHeaderOffsetChangedListener,
        StickyListHeadersListView.OnStickyHeaderChangedListener, View.OnTouchListener {
    public static final String TAG = SchedulesItineraryActionBarActivity.class.getName();

    private SchedulesItinerary_ListViewItem_ArrayAdapter mAdapter;
    private boolean fadeHeader = true;

    private RouteTypes travelType;

    private StickyListHeadersListView stickyList;

    private ArrayList<SchedulesRouteModel> routesModel;

    private String iconImageNameSuffix;

    private final String[] tabLabels = new String[] {"NOW", "WEEKDAY", "SATURDAY", "SUNDAY"};
    private int selectedTab = 0;

    private boolean inProcessOfStartDestinationFlow;

    private SchedulesRouteModel schedulesRouteModel = new SchedulesRouteModel();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedules_itinerary);

        String actionBarTitleText = getIntent().getStringExtra(getString(R.string.actionbar_titletext_key));

        iconImageNameSuffix = getIntent().getStringExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key));
        String resourceName = getString(R.string.actionbar_iconimage_imagename_base).concat(iconImageNameSuffix);

        String schedulesRouteModelJSONString = getIntent().getStringExtra(getString(R.string.schedules_itinerary_schedulesRouteModel));
        Gson gson = new Gson();
        schedulesRouteModel = gson.fromJson(schedulesRouteModelJSONString, new TypeToken<SchedulesRouteModel>(){}.getType());

        Log.d(TAG, "onCreate and have the route Code as "+schedulesRouteModel.getRouteCode());

        int id = getResources().getIdentifier(resourceName, "drawable", getPackageName());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("| " + actionBarTitleText);
        getSupportActionBar().setIcon(id);

        String stringTravelType = getIntent().getStringExtra(getString(R.string.schedules_itinerary_travelType));
        if (stringTravelType != null) {
            travelType = valueOf(getIntent().getStringExtra(getString(R.string.schedules_itinerary_travelType)));
        } else {
            Log.d("f", "travelType is null...");
        }

        this.inProcessOfStartDestinationFlow = false;

        mAdapter = new SchedulesItinerary_ListViewItem_ArrayAdapter(this, travelType);

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

//        stickyList.setFastScrollAlwaysVisible(true);
        stickyList.setFastScrollEnabled(true);

        stickyList.setDivider(null);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        routesModel = new ArrayList<SchedulesRouteModel>();
        RoutesLoader routesLoader = new RoutesLoader(routesModel);

        //TODO: this is casuing a crash since travelType is coming in null at times.  this needs work.
//        routesLoader.execute(travelType);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        View routeDirectionView = (View)findViewById(R.id.schedules_itinerary_routedirection_view);

        // get the color from the looking array given the ordinal position of the route type
        String color = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[travelType.ordinal()];
        routeDirectionView.setBackgroundColor(Color.parseColor(color));

        Button tabNowButton = (Button)findViewById(R.id.schedules_itinerary_tab_now_button);
        Button tabWeekdayButton = (Button)findViewById(R.id.schedules_itinerary_tab_weekday_button);
        Button tabSatButton = (Button)findViewById(R.id.schedules_itinerary_tab_sat_button);
        Button tabSunButton = (Button)findViewById(R.id.schedules_itinerary_tab_sun_button);

        GradientDrawable nowTabButtonShapeDrawable = (GradientDrawable)tabNowButton.getBackground();
        GradientDrawable weekdayTabButtonShapeDrawable = (GradientDrawable)tabWeekdayButton.getBackground();
        GradientDrawable satTabButtonShapeDrawable = (GradientDrawable)tabSatButton.getBackground();
        GradientDrawable sunTabButtonShapeDrawable = (GradientDrawable)tabSunButton.getBackground();

        nowTabButtonShapeDrawable.setColor(Color.parseColor(color));
        tabNowButton.setTextColor(Color.WHITE);
        weekdayTabButtonShapeDrawable.setColor(Color.LTGRAY);
        tabWeekdayButton.setTextColor(Color.BLACK);
        satTabButtonShapeDrawable.setColor(Color.LTGRAY);
        tabSatButton.setTextColor(Color.BLACK);
        sunTabButtonShapeDrawable.setColor(Color.LTGRAY);
        tabSunButton.setTextColor(Color.BLACK);
    }

    public void startEndSelectionSelected(View view) {
        this.inProcessOfStartDestinationFlow = true;

//        tripDataModel.clear();

        Intent stopSelectionIntent = null;

        stopSelectionIntent = new Intent(this, SchedulesRRStopSelectionActionBarActivity.class);
        stopSelectionIntent.putExtra(getString(R.string.regionalrail_stopselection_startordestination), "Start");
        startActivityForResult(stopSelectionIntent, getResources().getInteger(R.integer.nexttoarrive_stopselection_activityforresult_request_id));
    }

    public void selectStartSelected(View view) {
        Intent stopSelectionIntent = null;

        stopSelectionIntent = new Intent(this, SchedulesRRStopSelectionActionBarActivity.class);
        stopSelectionIntent.putExtra(getString(R.string.regionalrail_stopselection_startordestination), "Start");
        startActivityForResult(stopSelectionIntent, getResources().getInteger(R.integer.nexttoarrive_stopselection_activityforresult_request_id));
    }

    public void selectDestinationSelected(View view) {
        Intent stopSelectionIntent = null;

        stopSelectionIntent = new Intent(this, SchedulesRRStopSelectionActionBarActivity.class);
        stopSelectionIntent.putExtra(getString(R.string.regionalrail_stopselection_startordestination), "Destination");
        startActivityForResult(stopSelectionIntent, getResources().getInteger(R.integer.nexttoarrive_stopselection_activityforresult_request_id));
    }

    public void reverseStartEndSelected(View view) {
        schedulesRouteModel.reverseStartAndDestinationStops();
        SchedulesItinerary_ListViewItem_ArrayAdapter schedulesListViewItemArrayAdapter = (SchedulesItinerary_ListViewItem_ArrayAdapter)stickyList.getAdapter();
        schedulesListViewItemArrayAdapter.setRouteStartName(schedulesRouteModel.getRouteStartName());
        schedulesListViewItemArrayAdapter.setRouteEndName(schedulesRouteModel.getRouteEndName());

        checkTripStartAndDestinationForNextToArriveDataRequest();
    }

    private void checkTripStartAndDestinationForNextToArriveDataRequest() {
        // check if we have both the start and destination stops, if yes, fetch the data.
        if ((schedulesRouteModel.getRouteStartName() != null) &&
                !schedulesRouteModel.getRouteStartName().isEmpty() &&
                (schedulesRouteModel.getRouteEndName() != null) &&
                !schedulesRouteModel.getRouteEndName().isEmpty()) {
//            ((NextToArrive_MenuDialog_ListViewItem_ArrayAdapter)menuDialogListView.getAdapter()).enableRefresh();
//            ((NextToArrive_MenuDialog_ListViewItem_ArrayAdapter)menuDialogListView.getAdapter()).enableSaveAsFavorite();

            Log.d(TAG, "get back both start and end");

            SchedulesFavoritesAndRecentlyViewedStore store = new SchedulesFavoritesAndRecentlyViewedStore(this);

            SchedulesRecentlyViewedModel schedulesRecentlyViewedModel = new SchedulesRecentlyViewedModel();
            schedulesRecentlyViewedModel.setRouteCode(schedulesRouteModel.getRouteCode());
            schedulesRecentlyViewedModel.setRouteStartName(schedulesRouteModel.getRouteStartName());
            schedulesRecentlyViewedModel.setRouteStartStopId(schedulesRouteModel.getRouteStartStopId());
                    Log.d(TAG, "start stop id is "+schedulesRouteModel.getRouteStartStopId());
            schedulesRecentlyViewedModel.setRouteEndName(schedulesRouteModel.getRouteEndName());
            schedulesRecentlyViewedModel.setRouteEndStopId(schedulesRouteModel.getRouteEndStopId());

            SchedulesFavoriteModel schedulesFavoriteModel = new SchedulesFavoriteModel();
            schedulesFavoriteModel.setRouteCode(schedulesRouteModel.getRouteCode());
            schedulesFavoriteModel.setRouteStartStopId(schedulesRouteModel.getRouteStartStopId());
            schedulesFavoriteModel.setRouteStartName(schedulesRouteModel.getRouteStartName());
            schedulesFavoriteModel.setRouteEndStopId(schedulesRouteModel.getRouteEndStopId());
            schedulesFavoriteModel.setRouteEndName(schedulesRouteModel.getRouteEndName());

            // check if the selected route is already a favorite, then we allow the option of removing this
            // route from the favorites list.
            if (store.isFavorite(schedulesFavoriteModel)) {
                Log.d(TAG, "this is already a favorite");
//                ((NextToArrive_MenuDialog_ListViewItem_ArrayAdapter)menuDialogListView.getAdapter()).enableRemoveSavedFavorite();
            }

            // check if this route is already stored as a favorite; if not store as a recent
            if (!store.isFavorite(schedulesRecentlyViewedModel)) {
                Log.d(TAG, "storing the recently viewed");
                store.addRecentlyViewed(schedulesRecentlyViewedModel);
            }

//            fetchNextToArrive();
//
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == getResources().getInteger(R.integer.nexttoarrive_stopselection_activityforresult_request_id)) {
            if (resultCode == RESULT_OK) {
                String stopName = data.getStringExtra("stop_name");
                String stopId = data.getStringExtra("stop_id");
                String selectionMode = data.getStringExtra("selection_mode");

                if (selectionMode.equals("destination")) {
                    schedulesRouteModel.setRouteEndStopId(stopId);
                    schedulesRouteModel.setRouteEndName(stopName);

                    if (inProcessOfStartDestinationFlow) {
                        inProcessOfStartDestinationFlow = false;
                    }
                } else {
                    if (schedulesRouteModel == null) {
                        Log.d(TAG, "schedulesRouteModel is null here, bad");
                    } else {
                        Log.d(TAG, "schedulesroutemodel is not null here, good");
                    }
                    schedulesRouteModel.setRouteStartStopId(stopId);
                    schedulesRouteModel.setRouteStartName(stopName);

                    if (inProcessOfStartDestinationFlow) {
                        Intent stopSelectionIntent = null;

                        stopSelectionIntent = new Intent(this, NextToArriveStopSelectionActionBarActivity.class);
                        stopSelectionIntent.putExtra(getString(R.string.regionalrail_stopselection_startordestination), "Destination");
                        startActivityForResult(stopSelectionIntent, getResources().getInteger(R.integer.nexttoarrive_stopselection_activityforresult_request_id));
                    }
                }

                SchedulesItinerary_ListViewItem_ArrayAdapter schedulesListViewItemArrayAdapter = (SchedulesItinerary_ListViewItem_ArrayAdapter)stickyList.getAdapter();
                schedulesListViewItemArrayAdapter.setRouteStartName(schedulesRouteModel.getRouteStartName());
                schedulesListViewItemArrayAdapter.setRouteEndName(schedulesRouteModel.getRouteEndName());

                checkTripStartAndDestinationForNextToArriveDataRequest();
            }
            if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "the result is canceled");
                //Write your code if there's no result
            }
        }
    }

    public void tabSelected(View view) {
        switch (view.getId()) {
            case R.id.schedules_itinerary_tab_now_button: {
                selectedTab = 0;
                mAdapter.setHeaderViewText(tabLabels[selectedTab]);
                selectedNowTab();

                break;
            }
            case R.id.schedules_itinerary_tab_weekday_button: {
                selectedTab = 1;
                mAdapter.setHeaderViewText(tabLabels[selectedTab]);
                selectedWeekdayTab();

                break;
            }
            case R.id.schedules_itinerary_tab_sat_button: {
                selectedTab = 2;
                mAdapter.setHeaderViewText(tabLabels[selectedTab]);
                selectedSatTab();

                break;
            }
            case R.id.schedules_itinerary_tab_sun_button: {
                selectedTab = 3;
                mAdapter.setHeaderViewText(tabLabels[selectedTab]);
                selectedSunTab();

                break;
            }
            default: {
                Log.d("f", "not sure how we feel into this default for this switch");
            }
        }
    }

    private void selectedNowTab() {
        // get the color from the looking array given the ordinal position of the route type
        String color = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[travelType.ordinal()];

        Button tabNowButton = (Button)findViewById(R.id.schedules_itinerary_tab_now_button);
        Button tabWeekdayButton = (Button)findViewById(R.id.schedules_itinerary_tab_weekday_button);
        Button tabSatButton = (Button)findViewById(R.id.schedules_itinerary_tab_sat_button);
        Button tabSunButton = (Button)findViewById(R.id.schedules_itinerary_tab_sun_button);

        GradientDrawable nowTabButtonShapeDrawable = (GradientDrawable)tabNowButton.getBackground();
        GradientDrawable weekdayTabButtonShapeDrawable = (GradientDrawable)tabWeekdayButton.getBackground();
        GradientDrawable satTabButtonShapeDrawable = (GradientDrawable)tabSatButton.getBackground();
        GradientDrawable sunTabButtonShapeDrawable = (GradientDrawable)tabSunButton.getBackground();

        nowTabButtonShapeDrawable.setColor(Color.parseColor(color));
        tabNowButton.setTextColor(Color.WHITE);
        weekdayTabButtonShapeDrawable.setColor(Color.LTGRAY);
        tabWeekdayButton.setTextColor(Color.BLACK);
        satTabButtonShapeDrawable.setColor(Color.LTGRAY);
        tabSatButton.setTextColor(Color.BLACK);
        sunTabButtonShapeDrawable.setColor(Color.LTGRAY);
        tabSunButton.setTextColor(Color.BLACK);
    }

    private void selectedWeekdayTab() {
        // get the color from the looking array given the ordinal position of the route type
        String color = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[travelType.ordinal()];

        Button tabNowButton = (Button)findViewById(R.id.schedules_itinerary_tab_now_button);
        Button tabWeekdayButton = (Button)findViewById(R.id.schedules_itinerary_tab_weekday_button);
        Button tabSatButton = (Button)findViewById(R.id.schedules_itinerary_tab_sat_button);
        Button tabSunButton = (Button)findViewById(R.id.schedules_itinerary_tab_sun_button);

        GradientDrawable nowTabButtonShapeDrawable = (GradientDrawable)tabNowButton.getBackground();
        GradientDrawable weekdayTabButtonShapeDrawable = (GradientDrawable)tabWeekdayButton.getBackground();
        GradientDrawable satTabButtonShapeDrawable = (GradientDrawable)tabSatButton.getBackground();
        GradientDrawable sunTabButtonShapeDrawable = (GradientDrawable)tabSunButton.getBackground();

        nowTabButtonShapeDrawable.setColor(Color.LTGRAY);
        tabNowButton.setTextColor(Color.BLACK);
        weekdayTabButtonShapeDrawable.setColor(Color.parseColor(color));
        tabWeekdayButton.setTextColor(Color.WHITE);
        satTabButtonShapeDrawable.setColor(Color.LTGRAY);
        tabSatButton.setTextColor(Color.BLACK);
        sunTabButtonShapeDrawable.setColor(Color.LTGRAY);
        tabSunButton.setTextColor(Color.BLACK);
    }

    private void selectedSatTab() {
        // get the color from the looking array given the ordinal position of the route type
        String color = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[travelType.ordinal()];

        Button tabNowButton = (Button)findViewById(R.id.schedules_itinerary_tab_now_button);
        Button tabWeekdayButton = (Button)findViewById(R.id.schedules_itinerary_tab_weekday_button);
        Button tabSatButton = (Button)findViewById(R.id.schedules_itinerary_tab_sat_button);
        Button tabSunButton = (Button)findViewById(R.id.schedules_itinerary_tab_sun_button);

        GradientDrawable nowTabButtonShapeDrawable = (GradientDrawable)tabNowButton.getBackground();
        GradientDrawable weekdayTabButtonShapeDrawable = (GradientDrawable)tabWeekdayButton.getBackground();
        GradientDrawable satTabButtonShapeDrawable = (GradientDrawable)tabSatButton.getBackground();
        GradientDrawable sunTabButtonShapeDrawable = (GradientDrawable)tabSunButton.getBackground();

        nowTabButtonShapeDrawable.setColor(Color.LTGRAY);
        tabNowButton.setTextColor(Color.BLACK);
        weekdayTabButtonShapeDrawable.setColor(Color.LTGRAY);
        tabWeekdayButton.setTextColor(Color.BLACK);
        satTabButtonShapeDrawable.setColor(Color.parseColor(color));
        tabSatButton.setTextColor(Color.WHITE);
        sunTabButtonShapeDrawable.setColor(Color.LTGRAY);
        tabSunButton.setTextColor(Color.BLACK);
    }

    private void selectedSunTab() {
        // get the color from the looking array given the ordinal position of the route type
        String color = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[travelType.ordinal()];

        Button tabNowButton = (Button)findViewById(R.id.schedules_itinerary_tab_now_button);
        Button tabWeekdayButton = (Button)findViewById(R.id.schedules_itinerary_tab_weekday_button);
        Button tabSatButton = (Button)findViewById(R.id.schedules_itinerary_tab_sat_button);
        Button tabSunButton = (Button)findViewById(R.id.schedules_itinerary_tab_sun_button);

        GradientDrawable nowTabButtonShapeDrawable = (GradientDrawable)tabNowButton.getBackground();
        GradientDrawable weekdayTabButtonShapeDrawable = (GradientDrawable)tabWeekdayButton.getBackground();
        GradientDrawable satTabButtonShapeDrawable = (GradientDrawable)tabSatButton.getBackground();
        GradientDrawable sunTabButtonShapeDrawable = (GradientDrawable)tabSunButton.getBackground();

        nowTabButtonShapeDrawable.setColor(Color.LTGRAY);
        tabNowButton.setTextColor(Color.BLACK);
        weekdayTabButtonShapeDrawable.setColor(Color.LTGRAY);
        tabWeekdayButton.setTextColor(Color.BLACK);
        satTabButtonShapeDrawable.setColor(Color.LTGRAY);
        tabSatButton.setTextColor(Color.BLACK);
        sunTabButtonShapeDrawable.setColor(Color.parseColor(color));
        tabSunButton.setTextColor(Color.WHITE);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
    }

    @Override
    public void onHeaderClick(StickyListHeadersListView l, View header,
                              int itemPosition, long headerId, boolean currentlySticky) {

    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onStickyHeaderOffsetChanged(StickyListHeadersListView l, View header, int offset) {
        if (fadeHeader && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            header.setAlpha(1 - (offset / (float) header.getMeasuredHeight()));
        }
    }

    @Override
    public void onStickyHeaderChanged(StickyListHeadersListView l, View header,
                                      int itemPosition, long headerId) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.setOnTouchListener(null);
        return false;
    }

    /* --
        RoutesLoader - asynctask to load the routes

     */
    private class RoutesLoader extends AsyncTask<RouteTypes, Integer, Boolean> {
        ArrayList<SchedulesRouteModel> routesModelList = null;

        public RoutesLoader(ArrayList<SchedulesRouteModel> routesModelList) {

            this.routesModelList = routesModelList;
        }

        private void loadRoutes(RouteTypes routeType) {
            SEPTADatabase septaDatabase = new SEPTADatabase(SchedulesItineraryActionBarActivity.this);
            SQLiteDatabase database = septaDatabase.getReadableDatabase();

            String queryString = null;
            switch (routeType) {
                case RAIL: {
                    queryString = "SELECT route_short_name, route_id, route_type, route_long_name FROM routes_rail WHERE route_type=2 ORDER BY route_short_name ASC";
                    break;
                }
                case BUS: {
                    queryString = "SELECT route_short_name, route_id, route_type, route_long_name FROM routes_bus WHERE route_type=3 AND route_short_name NOT IN (\"MF\",\"BSO\") ORDER BY route_short_name ASC";
                    break;
                }
                case BSL: {
                    queryString = "SELECT route_short_name, route_id, route_type, route_long_name FROM routes_bus WHERE route_short_name LIKE \"BS_\" ORDER BY route_short_name ASC";
                    break;
                }
                case MFL: {
                    queryString = "SELECT route_short_name, route_id, route_type, route_long_name FROM routes_bus WHERE route_short_name LIKE \"MF_\" ORDER BY route_short_name";
                    break;
                }
                case NHSL: {
                    queryString = "SELECT route_short_name, route_id, route_type, route_long_name FROM routes_bus WHERE route_short_name=\"NHSL\" ORDER BY route_short_name";
                    break;
                }
                case TROLLEY: {
                    queryString = "SELECT route_short_name, route_id, route_type, route_long_name FROM routes_bus WHERE route_type=0 AND route_short_name != \"NHSL\" ORDER BY route_short_name ASC";
                    break;
                }
            }

            Cursor cursor = database.rawQuery(queryString, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        SchedulesRouteModel routeModel = null;
                        if (routeType == RAIL) {
                            routeModel = new SchedulesRouteModel(
                                    cursor.getInt(2),
                                    cursor.getString(1),
                                    cursor.getString(0),
                                    cursor.getString(3),
                                    "",
                                    "",
                                    "",
                                    "");
                        } else {
                            // for bus
                            routeModel = new SchedulesRouteModel(
                                    cursor.getInt(2),
                                    cursor.getString(0),
                                    cursor.getString(0),
                                    cursor.getString(3),
                                    "",
                                    "",
                                    "",
                                    "");
                        }
//                        routesModelList.add(routeModel);
                    } while (cursor.moveToNext());
                }

                cursor.close();
            } else {
                Log.d("f", "cursor is null");
            }

            database.close();
        }

        @Override
        protected Boolean doInBackground(RouteTypes... params) {
            RouteTypes routeType = params[0];

            Log.d("f", "about to call the loadRoutes...");
            loadRoutes(routeType);
            Log.d("f", "called the loadRoutes.");

            return false;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);

            Log.d("f", "calling onPostExecute...");
            mAdapter.setSchedulesRouteModel(routesModelList);
            mAdapter.notifyDataSetChanged();
            Log.d("f", "done with the onPostExecute call.");
        }
    }
}
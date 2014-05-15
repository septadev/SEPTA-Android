/*
 * ItinerarySelectionActionBarActivity.java
 * Last modified on 05-13-2014 10:31-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities.schedules;

import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;

import org.septa.android.app.R;
import org.septa.android.app.activities.BaseAnalyticsActionBarActivity;
import org.septa.android.app.adapters.schedules.ItinerarySelection_ListViewItem_ArrayAdapter;
import org.septa.android.app.databases.SEPTADatabase;
import org.septa.android.app.models.RouteTypes;
import org.septa.android.app.models.SchedulesRouteModel;
import org.septa.android.app.models.TripDataModel;

import java.util.ArrayList;

import roboguice.util.Ln;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static org.septa.android.app.models.RouteTypes.RAIL;
import static org.septa.android.app.models.RouteTypes.valueOf;

public class ItinerarySelectionActionBarActivity extends BaseAnalyticsActionBarActivity implements
        AdapterView.OnItemClickListener, StickyListHeadersListView.OnHeaderClickListener,
        StickyListHeadersListView.OnStickyHeaderOffsetChangedListener,
        StickyListHeadersListView.OnStickyHeaderChangedListener, View.OnTouchListener {

    private ItinerarySelection_ListViewItem_ArrayAdapter mAdapter;
    private boolean fadeHeader = true;

    private RouteTypes travelType;
    private String routeShortName;

    private StickyListHeadersListView stickyList;

    private ArrayList<SchedulesRouteModel> routesModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.itineraryselection);

        String actionBarTitleText = getIntent().getStringExtra(getString(R.string.actionbar_titletext_key));
        String iconImageNameSuffix = getIntent().getStringExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key));
        String resourceName = getString(R.string.actionbar_iconimage_imagename_base).concat(iconImageNameSuffix);

        Ln.d("resource name is to be " + resourceName);

        int id = getResources().getIdentifier(resourceName, "drawable", getPackageName());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("|" + actionBarTitleText);
        getSupportActionBar().setIcon(id);

        travelType = valueOf(getIntent().getStringExtra(getString(R.string.schedules_itinerary_travelType)));
        routeShortName = getIntent().getStringExtra(getString(R.string.schedules_itinerary_routeShortName));

        Ln.d("got the travel type as "+travelType.name());

        mAdapter = new ItinerarySelection_ListViewItem_ArrayAdapter(this, travelType);

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

        DirectionHeaderLoader directionHeaderLoader = new DirectionHeaderLoader(routeShortName);
        directionHeaderLoader.execute(travelType);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

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

    private class StopsLoader extends AsyncTask<RouteTypes, Integer, Boolean> {
        String routeShortName;
        ArrayList<TripDataModel> tripDataModelsListDirection0 = new ArrayList<TripDataModel>();
        ArrayList<TripDataModel> tripDataModelsListDirection1 = new ArrayList<TripDataModel>();

        public StopsLoader(String routeShortName) {

            this.routeShortName = routeShortName;
        }

        private void loadTrips(RouteTypes routeType) {
            SEPTADatabase septaDatabase = new SEPTADatabase(ItinerarySelectionActionBarActivity.this);
            SQLiteDatabase database = septaDatabase.getReadableDatabase();

            String queryString = null;
            switch (routeType) {
                case RAIL: {
                    Ln.d("type is rail, loading the trips");
                    queryString = "SELECT stop_name, stop_id, null as direction_id, wheelchair_boarding, null as stop_sequence FROM stops_rail ORDER BY stop_name";

                    break;
                }
                case BUS: {
                    Ln.d("type is bus, loading the trips");
                    queryString = "SELECT stop_name, stop_id, direction_id, wheelchair_boarding, stop_sequence FROM stopNameLookUpTable NATURAL JOIN stops_bus s WHERE route_short_name=\"%%route_short_name%%\" ORDER BY stop_name";
                    queryString = queryString.replace("%%route_short_name%%", routeShortName);

                    break;
                }
                case TROLLEY: {
                    Ln.d("type is trolley, loading the trips");
                    queryString = "SELECT stop_name, stop_id, direction_id, wheelchair_boarding, stop_sequence FROM stopNameLookUpTable NATURAL JOIN stops_bus s WHERE route_short_name=\"%%route_short_name%%\" ORDER BY stop_name";
                    queryString = queryString.replace("%%route_short_name%%", routeShortName);

                    break;
                }
                case BSL: {
                    Ln.d("type is bsl, loading the trips");
                    queryString = "SELECT s.stop_name, st.stop_id, t.direction_id, s.wheelchair_boarding, stop_sequence FROM trips_BSL t JOIN stop_times_BSL st ON t.trip_id=st.trip_id NATURAL JOIN stops_bus s GROUP BY st.stop_id ORDER BY s.stop_name;";

                     break;
                }
                case MFL: {
                    Ln.d("type is mfl, loading the trips");
                    queryString = "SELECT stop_name, stop_id, direction_id, wheelchair_boarding, stop_sequence FROM stopNameLookUpTable NATURAL JOIN stops_bus WHERE route_short_name=\"%%route_short_name\" ORDER BY stop_name";
                    queryString = queryString.replace("%%route_short_name%%", routeShortName);
                    break;
                }
                case NHSL: {
                    Ln.d("type is nhsl, loading the trips");
                    queryString = "SELECT s.stop_name, st.stop_id, t.direction_id, s.wheelchair_boarding, stop_sequence FROM trips_NHSL t JOIN stop_times_NHSL st ON trips_NHSL.trip_id=st.trip_id NATURAL JOIN stops_bus s GROUP BY st.stop_id ORDER BY s.stop_name;";

                    break;
                }
            }

            Cursor cursor = database.rawQuery(queryString, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        TripDataModel tripDataModel = new TripDataModel();

                        tripDataModel.setStartStopName(cursor.getString(0));      // s.stop_name
                        Integer stopId = Integer.getInteger(cursor.getString(1)); // st.stop_id
                        tripDataModel.setStartStopId(stopId);                     // s.wheelchair_boarding
                        if (cursor.getInt(3) == 1) {                              // stop_sequence
                            tripDataModel.setWheelBoardingFeature(true);
                        } else {
                            tripDataModel.setWheelBoardingFeature(false);
                        }
                        tripDataModel.setStartStopSequence(cursor.getInt(4));

                        if (routeType != RAIL) {
                            if (cursor.getInt(2) == 0) {
                                Ln.d("found a dir 0, add this tripdatamodel to 0");
                                tripDataModelsListDirection0.add(tripDataModel);
                            } else {
                                Ln.d("found a dir 1, add this tripdatamodel to 1");
                                tripDataModelsListDirection1.add(tripDataModel);
                            }
                        } else {
                            tripDataModelsListDirection0.add(tripDataModel);
                        }
                    } while (cursor.moveToNext());
                }

                cursor.close();
            } else {
                Ln.d("cursor is null");
            }

            database.close();
        }

        @Override
        protected Boolean doInBackground(RouteTypes... params) {
            RouteTypes routeType = params[0];

            Ln.d("about to call the loadTrips...");
            loadTrips(routeType);
            Ln.d("called the loadTrips.");

            return false;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);

            Ln.d("calling onPostExecute...");
            mAdapter.setTripDataForDirection0(tripDataModelsListDirection0);
            mAdapter.setTripDataForDirection1(tripDataModelsListDirection1);
            mAdapter.notifyDataSetChanged();
            Ln.d("done with the onPostExecute call.");
        }
    }

    private class DirectionHeaderLoader extends AsyncTask<RouteTypes, Integer, Boolean> {
        String routeShortName;
        RouteTypes routeType;
        String[] directionHeaderLabels = new String[]{"Dir0","Dir1"};

        public DirectionHeaderLoader(String routeShortName) {

            this.routeShortName = routeShortName;
        }

        private void loadDirectionHeaders(RouteTypes routeType) {
            Ln.d("starting loadDirectionHeaders...");
            this.routeType = routeType;
            SEPTADatabase septaDatabase = new SEPTADatabase(ItinerarySelectionActionBarActivity.this);
            SQLiteDatabase database = septaDatabase.getReadableDatabase();

            String queryString = null;
            switch (routeType) {
                case RAIL: {
                    break;
                }
                case BUS:
                case BSL:
                case MFL:
                case NHSL:
                case TROLLEY: {
                    Ln.d("in trolley, setting querystring");
                    queryString = "SELECT dircode, Route, DirectionDescription FROM bus_stop_directions WHERE Route=\"%%route_short_name%%\" ORDER BY dircode";
                    queryString = queryString.replace("%%route_short_name%%", routeShortName);

                    break;
                }
            }

            Cursor cursor = null;

            if (queryString != null) {
                cursor = database.rawQuery(queryString, null);
            }

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    Ln.d("cursor is not null and moving to first.");
                    do {
                        if (cursor.getInt(0) == 0) {
                            Ln.d("direction is 0 will set text to be "+cursor.getString(2));
                            directionHeaderLabels[0] = cursor.getString(2);
                        } else {
                            Ln.d("direction is 1 will set text to be "+cursor.getString(2));
                            directionHeaderLabels[1] = cursor.getString(2);
                        }
                    } while (cursor.moveToNext());
                }

                cursor.close();
            } else {
                Ln.d("cursor is null");
            }

            database.close();
        }

        @Override
        protected Boolean doInBackground(RouteTypes... params) {
            RouteTypes routeType = params[0];

            Ln.d("about to call the loadDirectionHeaders...");
            loadDirectionHeaders(routeType);
            Ln.d("called the loadDirectionHeaders.");

            return false;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);

            Ln.d("calling onPostExecute...");
            mAdapter.setDirectionHeadingLabels(directionHeaderLabels);
//            mAdapter.notifyDataSetChanged();

            StopsLoader stopsLoader = new StopsLoader(routeShortName);
            stopsLoader.execute(routeType);
            Ln.d("done with the onPostExecute call.");
        }
    }
}
/*
 * ItinerarySelectionActionBarActivity.java
 * Last modified on 05-13-2014 10:31-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities.schedules;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;

import org.septa.android.app.R;
import org.septa.android.app.activities.BaseAnalyticsActionBarActivity;
import org.septa.android.app.adapters.schedules.ItinerarySelection_ListViewItem_ArrayAdapter;
import org.septa.android.app.databases.SEPTADatabase;
import org.septa.android.app.models.RouteTypes;
import org.septa.android.app.models.SchedulesRouteModel;
import org.septa.android.app.models.StopModel;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static org.septa.android.app.models.RouteTypes.NHSL;
import static org.septa.android.app.models.RouteTypes.RAIL;
import static org.septa.android.app.models.RouteTypes.valueOf;

public class SchedulesStopsSelectionActionBarActivity extends BaseAnalyticsActionBarActivity implements
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

        int id = getResources().getIdentifier(resourceName, "drawable", getPackageName());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("| " + actionBarTitleText);
        getSupportActionBar().setIcon(id);

        // get the start or destination string from the extra intent string if it exists
        final String schedulesItineraryStopSelectionStartOrDestinationString = getIntent().getStringExtra(getString(R.string.schedules_stopselection_startordestination));
        if (schedulesItineraryStopSelectionStartOrDestinationString != null) {
            getSupportActionBar().setTitle("| Select "+schedulesItineraryStopSelectionStartOrDestinationString);
        } else {
            getSupportActionBar().setTitle("| Select Start");
        }

        travelType = valueOf(getIntent().getStringExtra(getString(R.string.schedules_itinerary_travelType)));
        routeShortName = getIntent().getStringExtra(getString(R.string.schedules_itinerary_routeShortName));

        Log.d("f", "got the travel type as "+travelType.name());

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

        stickyList.setFastScrollEnabled(true);

        stickyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                StopModel stop = (StopModel)mAdapter.getItem(position);

                Gson gson = new Gson();
                String stopModelJSONString = gson.toJson(stop);

                Intent returnIntent = new Intent();
                returnIntent.putExtra("stop_name", stop.getStopName());
                returnIntent.putExtra("stop_id", stop.getStopId());
                returnIntent.putExtra("selection_mode", schedulesItineraryStopSelectionStartOrDestinationString);
                SchedulesStopsSelectionActionBarActivity.this.setResult(SchedulesStopsSelectionActionBarActivity.this.RESULT_OK, returnIntent);
                SchedulesStopsSelectionActionBarActivity.this.finish();
            }
        });

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
        ArrayList<StopModel> stopModelListDirection0 = new ArrayList<StopModel>();
        ArrayList<StopModel> stopModelListDirection1 = new ArrayList<StopModel>();

        public StopsLoader(String routeShortName) {

            this.routeShortName = routeShortName;
        }

        private void loadTrips(RouteTypes routeType) {
            SEPTADatabase septaDatabase = new SEPTADatabase(SchedulesStopsSelectionActionBarActivity.this);
            SQLiteDatabase database = septaDatabase.getReadableDatabase();

            String queryString = null;
            switch (routeType) {
                case RAIL: {
                    Log.d("f", "type is rail, loading the trips");
                    queryString = "SELECT stop_id, stop_name, null as direction_id, wheelchair_boarding, null as stop_sequence FROM stops_rail ORDER BY stop_name";

                    break;
                }
                case BUS: {
                    Log.d("f", "type is bus, loading the trips");
                    queryString = "SELECT stop_id, stop_name, direction_id, wheelchair_boarding, stop_sequence FROM stopNameLookUpTable NATURAL JOIN stops_bus s WHERE route_short_name=\"%%route_short_name%%\" ORDER BY stop_name";
                    queryString = queryString.replace("%%route_short_name%%", routeShortName);

                    break;
                }
                case TROLLEY: {
                    Log.d("f", "type is trolley, loading the trips");
                    queryString = "SELECT stop_id, stop_name, direction_id, wheelchair_boarding, stop_sequence FROM stopNameLookUpTable NATURAL JOIN stops_bus s WHERE route_short_name=\"%%route_short_name%%\" ORDER BY stop_name";
                    queryString = queryString.replace("%%route_short_name%%", routeShortName);

                    break;
                }
                case BSL: {
                    Log.d("f", "type is bsl, loading the trips");
                    queryString = "SELECT st.stop_id, s.stop_name, t.direction_id, s.wheelchair_boarding, stop_sequence FROM trips_BSL t JOIN stop_times_BSL st ON t.trip_id=st.trip_id NATURAL JOIN stops_bus s GROUP BY st.stop_id ORDER BY s.stop_name;";

                     break;
                }
                case MFL: {
                    Log.d("f", "type is mfl, loading the trips");
                    queryString = "SELECT stop_id, stop_name, direction_id, wheelchair_boarding, stop_sequence FROM stopNameLookUpTable NATURAL JOIN stops_bus WHERE route_short_name=\"%%route_short_name\" ORDER BY stop_name";
                    queryString = queryString.replace("%%route_short_name%%", routeShortName);
                    break;
                }
                case NHSL: {
                    Log.d("f", "type is nhsl, loading the trips");
                    queryString = "SELECT st.stop_id, s.stop_name, t.direction_id, s.wheelchair_boarding, stop_sequence FROM trips_NHSL t JOIN stop_times_NHSL st ON t.trip_id=st.trip_id NATURAL JOIN stops_bus s GROUP BY st.stop_id ORDER BY s.stop_name;";

                    break;
                }
            }

            Cursor cursor = database.rawQuery(queryString, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        StopModel stopModel = new StopModel(cursor.getString(0), cursor.getString(1), cursor.getInt(4), (cursor.getInt(3) == 1) ? true : false);

                        if (routeType != RAIL) {
                            if (cursor.getInt(2) == 0) {
                                stopModelListDirection0.add(stopModel);
                            } else {
                                 stopModelListDirection1.add(stopModel);
                            }
                        } else {
                            stopModelListDirection0.add(stopModel);
                        }

                        if (routeType == NHSL) {

                            stopModelListDirection0.add(stopModel);
                        }

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

            loadTrips(routeType);

            return false;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);

            mAdapter.setTripDataForDirection0(stopModelListDirection0);
            mAdapter.setTripDataForDirection1(stopModelListDirection1);
            mAdapter.notifyDataSetChanged();
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
            this.routeType = routeType;
            SEPTADatabase septaDatabase = new SEPTADatabase(SchedulesStopsSelectionActionBarActivity.this);
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
                    Log.d("f", "setting querystring with route short name as "+routeShortName);
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
                    do {
                        if (cursor.getInt(0) == 0) {
                            Log.d("f", "direction is 0 will set text to be "+cursor.getString(2));
                            directionHeaderLabels[0] = cursor.getString(2);
                        } else {
                            Log.d("f", "direction is 1 will set text to be "+cursor.getString(2));
                            directionHeaderLabels[1] = cursor.getString(2);
                        }
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

            loadDirectionHeaders(routeType);

            return false;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);

            mAdapter.setDirectionHeadingLabels(directionHeaderLabels);
//            mAdapter.notifyDataSetChanged();

            StopsLoader stopsLoader = new StopsLoader(routeShortName);
            stopsLoader.execute(routeType);
        }
    }
}
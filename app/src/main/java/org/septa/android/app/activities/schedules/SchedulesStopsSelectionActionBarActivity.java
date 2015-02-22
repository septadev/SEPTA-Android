/*
 * ItinerarySelectionActionBarActivity.java
 * Last modified on 05-13-2014 10:31-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities.schedules;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import org.septa.android.app.R;
import org.septa.android.app.activities.BaseAnalyticsActionBarActivity;
import org.septa.android.app.activities.GeocoderActivity;
import org.septa.android.app.adapters.schedules.ItinerarySelection_ListViewItem_ArrayAdapter;
import org.septa.android.app.databases.SEPTADatabase;
import org.septa.android.app.models.RouteTypes;
import org.septa.android.app.models.SortOrder;
import org.septa.android.app.models.StopModel;
import org.septa.android.app.utilities.Constants;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static org.septa.android.app.models.RouteTypes.valueOf;

public class SchedulesStopsSelectionActionBarActivity extends BaseAnalyticsActionBarActivity implements
        AdapterView.OnItemClickListener, StickyListHeadersListView.OnHeaderClickListener,
        StickyListHeadersListView.OnStickyHeaderOffsetChangedListener,
        StickyListHeadersListView.OnStickyHeaderChangedListener, View.OnTouchListener,
        View.OnClickListener, LocationListener {

    private static final String TAG = SchedulesStopsSelectionActionBarActivity.class.getName();

    private static final int REQUEST_CODE_GECODER = 1000;

    private ItinerarySelection_ListViewItem_ArrayAdapter mAdapter;
    private boolean fadeHeader = true;

    private RouteTypes travelType;
    private String routeShortName;

    private StickyListHeadersListView stickyList;

    private LocationManager locationManager;
    private Menu optionsMenu;
    private SortOrder sortOrder = SortOrder.DEFAULT;
    private Location returnedLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.itineraryselection);

        String actionBarTitleText = getIntent().getStringExtra(getString(R.string.actionbar_titletext_key));
        String iconImageNameSuffix = getIntent().getStringExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key));
        String resourceName = getString(R.string.actionbar_iconimage_imagename_base).concat(iconImageNameSuffix);
        if(getIntent().hasExtra(getString(R.string.schedules_stopselection_sort_order))) {
            sortOrder = (SortOrder) getIntent().getSerializableExtra(getString(R.string.schedules_stopselection_sort_order));
        }

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

        mAdapter = new ItinerarySelection_ListViewItem_ArrayAdapter(this, travelType, routeShortName);

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

                Intent returnIntent = new Intent();
                returnIntent.putExtra("direction_id", stop.getDirectionId());
                returnIntent.putExtra("stop_name", stop.getStopName());
                returnIntent.putExtra("stop_id", stop.getStopId());
                returnIntent.putExtra("selection_mode", schedulesItineraryStopSelectionStartOrDestinationString);
                if(sortOrder != SortOrder.DEFAULT) {
                    returnIntent.putExtra(getString(R.string.schedules_stopselection_sort_order), sortOrder);
                }
                SchedulesStopsSelectionActionBarActivity.this.setResult(Activity.RESULT_OK, returnIntent);
                SchedulesStopsSelectionActionBarActivity.this.finish();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        DirectionHeaderLoader directionHeaderLoader = new DirectionHeaderLoader(routeShortName);
        directionHeaderLoader.execute(travelType);

        View headerView = findViewById(R.id.headerview_include);
        headerView.findViewById(R.id.headerview_textview_current_location).setOnClickListener(this);
        headerView.findViewById(R.id.headerview_textview_enter_address).setOnClickListener(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(REQUEST_CODE_GECODER == requestCode && resultCode == Activity.RESULT_OK) {
            Location addressLocation = data.getParcelableExtra(Constants.KEY_LOCATION);
            returnedLocation = addressLocation;
            sortByLocations(addressLocation);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sort, menu);
        optionsMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort_123:
                sortByName();
                break;
            case R.id.menu_sort_abc:
                sortBySequence();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void sortByName() {
        mAdapter.sortByName();
        sortOrder = SortOrder.NAME;
        updateSortOptions();
    }

    private void sortBySequence() {
        mAdapter.sortByStopSequence();
        sortOrder = SortOrder.SEQUENCE;
        updateSortOptions();
    }

    public void updateSortOptions() {
        MenuItem sortSequence = optionsMenu.findItem(R.id.menu_sort_123);
        MenuItem sortName = optionsMenu.findItem(R.id.menu_sort_abc);
        if(sortName != null && sortSequence != null) {
            switch (sortOrder) {
                case SEQUENCE:
                    sortName.setVisible(false);
                    sortSequence.setVisible(true);
                    onPrepareOptionsMenu(optionsMenu);
                    break;
                default:
                    sortName.setVisible(true);
                    sortSequence.setVisible(false);
                    onPrepareOptionsMenu(optionsMenu);
                    break;
            }
        }
    }

    public void removeSortOptions() {
        MenuItem sortStop = optionsMenu.findItem(R.id.menu_sort_123);
        MenuItem sortName = optionsMenu.findItem(R.id.menu_sort_abc);
        if(sortName != null && sortStop != null) {
            sortName.setVisible(false);
            sortStop.setVisible(false);
            onPrepareOptionsMenu(optionsMenu);
        }
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.headerview_textview_current_location:
                getUserLocation();
                break;
            case R.id.headerview_textview_enter_address:
                Intent intent = new Intent(this, GeocoderActivity.class);
                startActivityForResult(intent, REQUEST_CODE_GECODER);
                break;
        }
    }

    private void getUserLocation() {
        Location userLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        locationManager.removeUpdates(this);
        if(userLocation == null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, getString(R.string.error_location_disabled),
                    Toast.LENGTH_SHORT).show();
        }  else if(userLocation != null) {
            Log.i(TAG, "Using cached location: " + String.valueOf(userLocation));
            sortByLocations(userLocation);
        }  else {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this,
                    Looper.myLooper());
        }
    }

    private void sortByLocations(Location userLocation) {
        if(userLocation != null && mAdapter != null) {
            sortOrder = SortOrder.LOCATION;
            returnedLocation = userLocation;
            mAdapter.sortByLocation(userLocation);
            removeSortOptions();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Location: " + location);
        sortByLocations(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.i(TAG, "Provider: " + s + " Status: " + i);
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.i(TAG, "Enabled: " + s);
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.i(TAG, "Disabled: " + s);
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

            // special rule if the routeShortName is MFO or BSO, then the type is actually a BUS and not MFL or BSL.
            if (routeShortName.equals("MFO") || routeShortName.equals("BSO")) {
                routeType = RouteTypes.BUS;
            }

            String queryString = null;
            switch (routeType) {
                case RAIL: {
                    Log.d("f", "type is rail, loading the trips");
                    queryString = "SELECT stop_id, stop_name, null as direction_id, wheelchair_boarding, null as stop_sequence, stop_lat, stop_lon  FROM stops_rail ORDER BY stop_name";

                    break;
                }
                case BUS: {
                    Log.d("f", "type is bus, loading the trips");
                    queryString = "SELECT stop_id, stop_name, direction_id, wheelchair_boarding, stop_sequence, s.stop_lat, s.stop_lon  FROM stopNameLookUpTable NATURAL JOIN stops_bus s WHERE route_short_name=\"%%route_short_name%%\" ORDER BY stop_name";
                    queryString = queryString.replace("%%route_short_name%%", routeShortName);

                    break;
                }
                case TROLLEY: {
                    Log.d("f", "type is trolley, loading the trips");
                    queryString = "SELECT stop_id, stop_name, direction_id, wheelchair_boarding, stop_sequence, stop_lat, stop_lon  FROM stopNameLookUpTable NATURAL JOIN stops_bus s WHERE route_short_name=\"%%route_short_name%%\" ORDER BY stop_name";
                    queryString = queryString.replace("%%route_short_name%%", routeShortName);

                    break;
                }
                case BSL: {
                    Log.d("f", "type is bsl, loading the trips");
                    queryString = "SELECT stop_times_BSL.stop_id, stops_bus.stop_name, direction_id, stops_bus.wheelchair_boarding, stop_sequence, stop_lat, stop_lon  FROM trips_BSL JOIN stop_times_BSL ON trips_BSL.trip_id=stop_times_BSL.trip_id NATURAL JOIN stops_bus GROUP BY stop_times_BSL.stop_id ORDER BY stops_bus.stop_name;";

                     break;
                }
                case MFL: {
                    Log.d("f", "type is mfl, loading the trips with shortname as "+routeShortName);
                    queryString = "SELECT stop_times_MFL.stop_id, stops_bus.stop_name, direction_id, stops_bus.wheelchair_boarding, stop_sequence, stop_lat, stop_lon  FROM trips_MFL JOIN stop_times_MFL ON trips_MFL.trip_id=stop_times_MFL.trip_id NATURAL JOIN stops_bus GROUP BY stop_times_MFL.stop_id ORDER BY stops_bus.stop_name;";

                    break;
                }
                case NHSL: {
                    Log.d("f", "type is nhsl, loading the trips");
                    queryString = "SELECT stop_times_NHSL.stop_id, stops_bus.stop_name, direction_id, stops_bus.wheelchair_boarding, stop_sequence, stop_lat, stop_lon FROM trips_NHSL JOIN stop_times_NHSL ON trips_NHSL.trip_id=stop_times_NHSL.trip_id NATURAL JOIN stops_bus GROUP BY stop_times_NHSL.stop_id ORDER BY stops_bus.stop_name;";

                    break;
                }
            }

            Cursor cursor = database.rawQuery(queryString, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        StopModel stopModel = new StopModel(cursor.getString(0), cursor.getString(1),
                                cursor.getInt(4), (cursor.getInt(3) == 1) ? true : false,
                                cursor.getString(5), cursor.getString(6));

                        switch (routeType) {
                            // in the case of RAIL, the direction is always 0
                            case RAIL: {
                                stopModelListDirection0.add(stopModel);

                                break;
                            }
                            // in the case of BUS and TROLLEY, the direction will be read from the selected row
                            case BUS:
                            case TROLLEY: {
                                if (cursor.getInt(2) == 0) {
                                    stopModelListDirection0.add(stopModel);
                                } else {
                                    stopModelListDirection1.add(stopModel);
                                }

                                break;
                            }
                            // in the case of BSL, MFL, and NHSL, stops will be shown for both directions
                            case BSL:
                            case MFL:
                            case NHSL: {
                                stopModelListDirection0.add(stopModel);
                                stopModelListDirection1.add(stopModel);

                                break;
                            }
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            } else {
                Log.d("ii", "cursor is null");
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
            stickyList.setFastScrollEnabled(false);
            mAdapter.setTripDataForDirection0(stopModelListDirection0);
            mAdapter.setTripDataForDirection1(stopModelListDirection1);
            stickyList.setFastScrollEnabled(true);
            switch (sortOrder) {
                case SEQUENCE:
                    sortBySequence();
                    break;
                case LOCATION:
                    sortByLocations(returnedLocation);
                    break;
                default:
                    break;
            }
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
                case RAIL:
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
                Log.d("z", "cursor is null");
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

            StopsLoader stopsLoader = new StopsLoader(routeShortName);
            stopsLoader.execute(routeType);
        }
    }
}
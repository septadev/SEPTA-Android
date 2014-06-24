/*
 * SchedulesTransportTypeActionBarActivity.java
 * Last modified on 05-05-2014 16:47-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities.schedules;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;

import com.google.gson.Gson;

import org.septa.android.app.R;
import org.septa.android.app.activities.BaseAnalyticsActionBarActivity;
import org.septa.android.app.adapters.schedules.SchedulesRouteSelection_ListViewItem_ArrayAdapter;
import org.septa.android.app.databases.SEPTADatabase;
import org.septa.android.app.models.RouteTypes;
import org.septa.android.app.models.SchedulesRouteModel;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static org.septa.android.app.models.RouteTypes.*;

public class SchedulesRouteSelectionActionBarActivity extends BaseAnalyticsActionBarActivity implements
        AdapterView.OnItemClickListener, StickyListHeadersListView.OnHeaderClickListener,
        StickyListHeadersListView.OnStickyHeaderOffsetChangedListener,
        StickyListHeadersListView.OnStickyHeaderChangedListener, View.OnTouchListener {
    public static final String TAG = SchedulesRouteSelectionActionBarActivity.class.getName();
    private SchedulesRouteSelection_ListViewItem_ArrayAdapter mAdapter;
    private boolean fadeHeader = true;

    private RouteTypes travelType;
    private String iconImageNameSuffix;

    private StickyListHeadersListView stickyList;

    private ArrayList<SchedulesRouteModel>routesModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedules_routeselection);

        String actionBarTitleText = getIntent().getStringExtra(getString(R.string.actionbar_titletext_key));
        iconImageNameSuffix = getIntent().getStringExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key));
        String resourceName = getString(R.string.actionbar_iconimage_imagename_base).concat(iconImageNameSuffix);

        int id = getResources().getIdentifier(resourceName, "drawable", getPackageName());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("| " + actionBarTitleText);
        getSupportActionBar().setIcon(id);

        travelType = valueOf(getIntent().getStringExtra(getString(R.string.schedules_routeselect_travelType)));

        mAdapter = new SchedulesRouteSelection_ListViewItem_ArrayAdapter(this, travelType);

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

        stickyList.setFastScrollAlwaysVisible(false);
        stickyList.setFastScrollEnabled(true);

        stickyList.setDivider(null);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        routesModel = new ArrayList<SchedulesRouteModel>();
        RoutesLoader routesLoader = new RoutesLoader(routesModel);
        routesLoader.execute(travelType);
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
        String routeShortName = "";

        SchedulesRouteModel route = (SchedulesRouteModel)mAdapter.getItem(position);
        Log.d("f", "onItemClick occurred at position "+position+" with id "+id+" and route short name of "+route.getRouteShortName());

        if (!mAdapter.isFavorite(position) && !mAdapter.isRecentlyViewed(position)) {


            routeShortName = route.getRouteShortName();
        }

        Log.d("tt", "in scheduelsrouteselectionactionbaractivity, the route type as name is "+route.getRouteType().name());

        Intent schedulesItineraryIntent = null;

        schedulesItineraryIntent = new Intent(this, SchedulesItineraryActionBarActivity.class);
        schedulesItineraryIntent.putExtra(getString(R.string.actionbar_titletext_key), route.getRouteId());
        schedulesItineraryIntent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), iconImageNameSuffix);
        schedulesItineraryIntent.putExtra(getString(R.string.schedules_itinerary_travelType),
                travelType.name());
        schedulesItineraryIntent.putExtra(getString(R.string.schedules_itinerary_routeShortName), routeShortName);

        Gson gson = new Gson();
        String schedulesRouteModelJSONString = gson.toJson(route);
        schedulesItineraryIntent.putExtra(getString(R.string.schedules_itinerary_schedulesRouteModel), schedulesRouteModelJSONString);

        startActivity(schedulesItineraryIntent);
    }

    @Override
    public void onHeaderClick(StickyListHeadersListView l, View header, int itemPosition, long headerId, boolean currentlySticky) {

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

    private class RoutesLoader extends AsyncTask<RouteTypes, Integer, Boolean> {
        ArrayList<SchedulesRouteModel> routesModelList = null;

        public RoutesLoader(ArrayList<SchedulesRouteModel> routesModelList) {

            this.routesModelList = routesModelList;
        }

        private void loadRoutes(RouteTypes routeType) {
            SEPTADatabase septaDatabase = new SEPTADatabase(SchedulesRouteSelectionActionBarActivity.this);
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
                                    cursor.getInt(2),   // route_type
                                    cursor.getString(1),    // route_id
                                    cursor.getString(0),    // route_short_name
                                    cursor.getString(3),    // route_long_name
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
                        routesModelList.add(routeModel);
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

            loadRoutes(routeType);
            return false;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);

            mAdapter.setSchedulesRouteModel(routesModelList);
            mAdapter.notifyDataSetChanged();
        }
    }
}
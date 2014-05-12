/*
 * SchedulesItineraryActionBarActivity.java
 * Last modified on 05-12-2014 09:45-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import org.septa.android.app.R;
import org.septa.android.app.adapters.SchedulesItinerary_ListViewItem_ArrayAdapter;
import org.septa.android.app.databases.SEPTADatabase;
import org.septa.android.app.models.RouteTypes;
import org.septa.android.app.models.SchedulesRouteModel;

import java.util.ArrayList;

import roboguice.util.Ln;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static org.septa.android.app.models.RouteTypes.RAIL;
import static org.septa.android.app.models.RouteTypes.valueOf;

public class SchedulesItineraryActionBarActivity  extends BaseAnalyticsActionBarActivity implements
        AdapterView.OnItemClickListener, StickyListHeadersListView.OnHeaderClickListener,
        StickyListHeadersListView.OnStickyHeaderOffsetChangedListener,
        StickyListHeadersListView.OnStickyHeaderChangedListener, View.OnTouchListener {

    private SchedulesItinerary_ListViewItem_ArrayAdapter mAdapter;
    private boolean fadeHeader = true;

    private RouteTypes travelType;

    private StickyListHeadersListView stickyList;

    private ArrayList<SchedulesRouteModel> routesModel;

    private final String[] tabLabels = new String[] {"REMAINING TRIPS TO TODAY", "WEEKDAY", "SATURDAY", "SUNDAY"};
    private int selectedTab = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedules_itinerary);

        String actionBarTitleText = getIntent().getStringExtra(getString(R.string.actionbar_titletext_key));
        String iconImageNameSuffix = getIntent().getStringExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key));
        String resourceName = getString(R.string.actionbar_iconimage_imagename_base).concat(iconImageNameSuffix);

        Ln.d("resource name is to be " + resourceName);

        int id = getResources().getIdentifier(resourceName, "drawable", getPackageName());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("|" + actionBarTitleText);
        getSupportActionBar().setIcon(id);

        travelType = valueOf(getIntent().getStringExtra(getString(R.string.schedules_itinerary_travelType)));

        Ln.d("got the travel type as "+travelType.name());

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

        stickyList.setFastScrollAlwaysVisible(true);
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
                Ln.d("not sure how we feel into this default for this switch");
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
                                    0,
                                    0);
                        } else {
                            // for bus
                            routeModel = new SchedulesRouteModel(
                                    cursor.getInt(2),
                                    cursor.getString(0),
                                    cursor.getString(0),
                                    cursor.getString(3),
                                    "",
                                    "",
                                    0,
                                    0);
                        }
                        routesModelList.add(routeModel);
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

            Ln.d("about to call the loadRoutes...");
            loadRoutes(routeType);
            Ln.d("called the loadRoutes.");

            return false;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);

            Ln.d("calling onPostExecute...");
            mAdapter.setSchedulesRouteModel(routesModelList);
            mAdapter.notifyDataSetChanged();
            Ln.d("done with the onPostExecute call.");
        }
    }
}
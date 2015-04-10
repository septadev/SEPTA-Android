/*
 * SchedulesTransportTypeActionBarActivity.java
 * Last modified on 05-05-2014 16:47-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities.schedules;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.gson.Gson;

import org.septa.android.app.R;
import org.septa.android.app.activities.BaseAnalyticsActionBarActivity;
import org.septa.android.app.adapters.schedules.SchedulesRouteSelection_ListViewItem_ArrayAdapter;
import org.septa.android.app.databases.SEPTADatabase;
import org.septa.android.app.managers.SchedulesFavoritesAndRecentlyViewedStore;
import org.septa.android.app.models.ObjectFactory;
import org.septa.android.app.models.RouteTypes;
import org.septa.android.app.models.SchedulesFavoriteModel;
import org.septa.android.app.models.SchedulesRecentlyViewedModel;
import org.septa.android.app.models.SchedulesRouteModel;
import org.septa.android.app.models.servicemodels.AlertModel;
import org.septa.android.app.services.apiproxies.AlertsServiceProxy;
import org.septa.android.app.utilities.Constants;
import org.septa.android.app.views.StatusView;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static org.septa.android.app.models.RouteTypes.RAIL;
import static org.septa.android.app.models.RouteTypes.valueOf;

public class SchedulesRouteSelectionActionBarActivity extends BaseAnalyticsActionBarActivity implements
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener,
        StickyListHeadersListView.OnHeaderClickListener,
        StickyListHeadersListView.OnStickyHeaderOffsetChangedListener,
        StickyListHeadersListView.OnStickyHeaderChangedListener,
        View.OnTouchListener {

    public static final String TAG = SchedulesRouteSelectionActionBarActivity.class.getName();

    public static final String VALUE_ALERT_RESPONSE_EMPTY = "Empty";

    private SchedulesRouteSelection_ListViewItem_ArrayAdapter mAdapter;
    private boolean fadeHeader = true;

    private StatusView statusView;
    private RouteTypes travelType;
    private String iconImageNameSuffix;

    private StickyListHeadersListView stickyList;

    private ArrayList<SchedulesRouteModel>routesModel;

    private TextView mAlertHeader;
    private TextView mAlertMessage;

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

        statusView = (StatusView)findViewById(R.id.empty);
        statusView.setLoading(true);

        stickyList = (StickyListHeadersListView) findViewById(R.id.list);
        stickyList.setOnItemClickListener(this);
        stickyList.setOnItemLongClickListener(this);
        stickyList.setOnHeaderClickListener(this);
        stickyList.setOnStickyHeaderChangedListener(this);
        stickyList.setOnStickyHeaderOffsetChangedListener(this);
        stickyList.setEmptyView(statusView);
        stickyList.setDrawingListUnderStickyHeader(true);
        stickyList.setAreHeadersSticky(true);
        stickyList.setAdapter(mAdapter);
        stickyList.setOnTouchListener(this);

        stickyList.setFastScrollAlwaysVisible(false);
        stickyList.setFastScrollEnabled(true);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mAlertHeader = (TextView) findViewById(R.id.schedules_routesselection_alert_header);
        mAlertMessage = (TextView) findViewById(R.id.schedules_routesselection_alert_message);

        routesModel = new ArrayList<SchedulesRouteModel>();
        RoutesLoader routesLoader = new RoutesLoader(routesModel);
        routesLoader.execute(travelType);

        if (actionBarTitleText.equals(Constants.VALUE_REGIONAL_RAIL_LINE)) {
            fetchAlerts();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAdapter.reloadFavoriteAndRecentlyViewedLists();
        mAdapter.notifyDataSetChanged();
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
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        if (mAdapter.isFavorite(position) || mAdapter.isRecentlyViewed(position)) {
            String alertDialogTitle = mAdapter.isFavorite(position) ? "Delete Favorite" : "Delete Recently Viewed";
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(alertDialogTitle);
            alert.setMessage("Are you sure you want to delete?");

            alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final SchedulesRouteModel schedulesRouteModel = (SchedulesRouteModel)mAdapter.getItem(position);

                    SchedulesRouteSelectionActionBarActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mAdapter.isFavorite(position)) { removeFavorite(schedulesRouteModel); }
                            if (mAdapter.isRecentlyViewed(position)) { removeRecentlyViewed(schedulesRouteModel); }

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

    public boolean removeFavorite(SchedulesRouteModel schedulesRouteModel) {
        SchedulesFavoritesAndRecentlyViewedStore store = ObjectFactory.getInstance().getSchedulesFavoritesAndRecentlyViewedStore(SchedulesRouteSelectionActionBarActivity.this);

        SchedulesFavoriteModel schedulesFavoriteModel = new SchedulesFavoriteModel();
        schedulesFavoriteModel.setRouteId(schedulesRouteModel.getRouteId());
        schedulesFavoriteModel.setRouteStartStopId(schedulesRouteModel.getRouteStartStopId());
        schedulesFavoriteModel.setRouteStartName(schedulesRouteModel.getRouteStartName());
        schedulesFavoriteModel.setRouteEndStopId(schedulesRouteModel.getRouteEndStopId());
        schedulesFavoriteModel.setRouteEndName(schedulesRouteModel.getRouteEndName());
        schedulesFavoriteModel.setRouteShortName(schedulesRouteModel.getRouteShortName());

        // check if the selected route is already a favorite, then we allow the option of removing this
        // route from the favorites list.
        if (store.isFavorite(travelType.name(), schedulesFavoriteModel)) {
            Log.d("tt", "detected this is a favorite, remove ");
            store.removeFavorite(travelType.name(), schedulesFavoriteModel);

            return true;
        }

        return false;
    }

    public boolean removeRecentlyViewed(SchedulesRouteModel schedulesRouteModel) {
        SchedulesFavoritesAndRecentlyViewedStore store = ObjectFactory.getInstance().getSchedulesFavoritesAndRecentlyViewedStore(SchedulesRouteSelectionActionBarActivity.this);

        SchedulesRecentlyViewedModel schedulesRecentlyViewedModel = new SchedulesRecentlyViewedModel();
        schedulesRecentlyViewedModel.setRouteId(schedulesRouteModel.getRouteId());
        schedulesRecentlyViewedModel.setRouteStartStopId(schedulesRouteModel.getRouteStartStopId());
        schedulesRecentlyViewedModel.setRouteStartName(schedulesRouteModel.getRouteStartName());
        schedulesRecentlyViewedModel.setRouteEndStopId(schedulesRouteModel.getRouteEndStopId());
        schedulesRecentlyViewedModel.setRouteEndName(schedulesRouteModel.getRouteEndName());
        schedulesRecentlyViewedModel.setRouteShortName(schedulesRouteModel.getRouteShortName());

        // check if the selected route is already a favorite, then we allow the option of removing this
        // route from the favorites list.
        if (store.isRecentlyViewed(travelType.name(), schedulesRecentlyViewedModel)) {
            Log.d("tt", "detected this is a recently viewed, remove ");
            store.removeRecentlyViewed(travelType.name(), schedulesRecentlyViewedModel);

            return true;
        }

        return false;
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

    private void fetchAlerts() {
        Callback callback = new Callback() {
            @Override
            public void success(Object o, Response response) {
                ArrayList<AlertModel> alertModelList = (ArrayList<AlertModel>) o;
                for (int i = 0; i < alertModelList.size(); i++) {
                    AlertModel alertModel = alertModelList.get(i);
                    if (alertModel != null) {
                        if (alertModel.isGeneral()) {
                            String generalAlert = alertModel.getCurrentMessage();
                            if (!TextUtils.isEmpty(generalAlert) && !generalAlert.equals(VALUE_ALERT_RESPONSE_EMPTY)) {
                                StringBuilder message = new StringBuilder();
                                message.append("<b>").append(getString(R.string.schedules_alerts_general_message_prefix)).append("</b> ").append(generalAlert);
                                mAlertMessage.setText(Html.fromHtml(message.toString()));
                                mAlertHeader.setVisibility(View.VISIBLE);
                                mAlertMessage.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                try {
                    Log.d(TAG, "A failure in the call to train view service with body |" + retrofitError.getResponse().getBody().in() + "|");
                } catch (Exception ex) {
                    Log.d(TAG, ex.getMessage());
                }
            }
        };

        AlertsServiceProxy alertsServiceProxy = new AlertsServiceProxy();
        alertsServiceProxy.getAlerts(callback);
    }
}
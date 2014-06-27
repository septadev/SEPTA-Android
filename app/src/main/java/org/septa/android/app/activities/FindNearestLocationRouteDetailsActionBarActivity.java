package org.septa.android.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.septa.android.app.R;

import org.septa.android.app.models.LocationModel;
import org.septa.android.app.models.servicemodels.BusScheduleModel;
import org.septa.android.app.models.servicemodels.BusSchedulesModel;
import org.septa.android.app.services.apiproxies.BusSchedulesServiceProxy;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class FindNearestLocationRouteDetailsActionBarActivity extends BaseAnalyticsActionBarActivity implements View.OnTouchListener,AdapterView.OnItemClickListener {
    public static final String TAG = FindNearestLocationRouteDetailsActionBarActivity.class.getName();

    public LocationModel locationModel = null;

    private StickyListHeadersListView stickyListHeadersListView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find Locations");
        getSupportActionBar().setIcon(R.drawable.ic_actionbar_findnearestlocation);

        String locationModelJSONString = getIntent().getStringExtra(getString(R.string.findNearestLocation_locationRouteModel));
        Gson gson = new Gson();
        locationModel = gson.fromJson(locationModelJSONString, new TypeToken<LocationModel>(){}.getType());

        Log.d(TAG, "in the findnearestlocationroutedetails actionbar activity with location model name as "+locationModel.getLocationName());

        setContentView(R.layout.findnearestlocation_routedetails);

        stickyListHeadersListView = (StickyListHeadersListView) findViewById(R.id.findnearestlocations_routedetails_listview);
        stickyListHeadersListView.setOnItemClickListener(this);
        stickyListHeadersListView.setEmptyView(findViewById(R.id.empty));
        stickyListHeadersListView.setDrawingListUnderStickyHeader(true);
        stickyListHeadersListView.setAreHeadersSticky(true);
//        stickyListHeadersListView.setAdapter(mAdapter);
        stickyListHeadersListView.setOnTouchListener(this);

        stickyListHeadersListView.setFastScrollEnabled(true);

        fetchBusSchedules();

        // set the empty view in case we don't have any data
//        LinearLayout emptyView = (LinearLayout)findViewById(R.id.empty);
//        StickyListHeadersListView listView = (StickyListHeadersListView)findViewById(R.id.findnearestlocations_routedetails_listview);
//        listView.setOnItemClickListener(this);
//        listView.setEmptyView(emptyView);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {

        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "detected an item click at position " + position);

//        Intent systemStatusDetailsIntent;
//        systemStatusDetailsIntent = new Intent(this, SystemStatusDetailsActionBarActivity.class);
//        systemStatusDetailsIntent.putExtra(getString(R.string.actionbar_titletext_key), displayRouteName);
//        systemStatusDetailsIntent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), "systemstatus");

//        systemStatusDetailsIntent.putExtra(getString(R.string.systemstatus_details_route_id), alert.getRouteId());
//        startActivity(systemStatusDetailsIntent);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    private void fetchBusSchedules() {
        Callback callback = new Callback() {
            @Override
            public void success(Object o, Response response) {
                Log.d(TAG, "fetch bus schedules success");

                if (o!=null) {
                    Log.d(TAG, "the object is not null");

                    BusSchedulesModel busSchedulesModel = (BusSchedulesModel)o;
                    Log.d(TAG, "number of entries is "+busSchedulesModel.getBusScheduleList().size());

                    for (BusScheduleModel busScheduleModel: busSchedulesModel.getBusScheduleList()) {
                        locationModel.getRoutes().get(0).addTimeDayPair(busScheduleModel.getDate(), busScheduleModel.getDay());
                    }
                }
//                setProgressBarIndeterminateVisibility(Boolean.FALSE);
//                mAdapter.setNextToArriveTrainList((ArrayList<NextToArriveModel>)o);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
//                setProgressBarIndeterminateVisibility(Boolean.FALSE);

                try {
                    Log.d(TAG, "A failure in the call to fetch bus schdules with body |" + retrofitError.getResponse().getBody().in() + "|");
                } catch (Exception ex) {
                    // TODO: clean this up
                    Log.d(TAG, "blah... what is going on?");
                }
            }
        };

        Log.d(TAG, "about to call bus schedules service");
        BusSchedulesServiceProxy busSchedulesServiceProxy = new BusSchedulesServiceProxy();
        busSchedulesServiceProxy.getBusSchedules(locationModel.getLocationId(), locationModel.getRoutes().get(0).getRouteShortName(), null, 5, callback);
        Log.d(TAG, "called bus schedules service");


//        setProgressBarIndeterminateVisibility(Boolean.TRUE);
    }
}
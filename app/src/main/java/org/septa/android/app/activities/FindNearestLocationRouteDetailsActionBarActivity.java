package org.septa.android.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.septa.android.app.R;
import org.septa.android.app.adapters.FindNearestLocation_RouteDetails_ListViewItem_ArrayAdapter;
import org.septa.android.app.managers.AlertManager;
import org.septa.android.app.models.LocationBasedRouteModel;
import org.septa.android.app.models.LocationModel;
import org.septa.android.app.models.servicemodels.AlertModel;
import org.septa.android.app.models.servicemodels.BusScheduleModel;
import org.septa.android.app.models.servicemodels.BusSchedulesModel;
import org.septa.android.app.services.apiproxies.BusSchedulesServiceProxy;
import org.septa.android.app.views.StatusView;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class FindNearestLocationRouteDetailsActionBarActivity extends BaseAnalyticsActionBarActivity implements AdapterView.OnItemClickListener, AlertManager.IAlertListener {

    public static final String TAG = FindNearestLocationRouteDetailsActionBarActivity.class.getName();

    public LocationModel locationModel;
    private FindNearestLocation_RouteDetails_ListViewItem_ArrayAdapter mAdapter;
    private StickyListHeadersListView stickyListHeadersListView;
    private StatusView statusView;

    //number of requests in process. Used to show/hide loading view
    private int numRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find Locations");
        getSupportActionBar().setIcon(R.drawable.ic_actionbar_findnearestlocation);

        String locationModelJSONString = getIntent().getStringExtra(getString(R.string.findNearestLocation_locationRouteModel));
        Gson gson = new Gson();
        locationModel = gson.fromJson(locationModelJSONString, new TypeToken<LocationModel>() {
        }.getType());

        setContentView(R.layout.findnearestlocation_routedetails);

        stickyListHeadersListView = (StickyListHeadersListView) findViewById(R.id.findnearestlocations_routedetails_listview);
        stickyListHeadersListView.setOnItemClickListener(this);
        stickyListHeadersListView.setDrawingListUnderStickyHeader(true);
        stickyListHeadersListView.setAreHeadersSticky(true);

        stickyListHeadersListView.setFastScrollEnabled(true);
        statusView = (StatusView)findViewById(R.id.empty);

        stickyListHeadersListView.setEmptyView(statusView);
        mAdapter = new FindNearestLocation_RouteDetails_ListViewItem_ArrayAdapter(this);
        stickyListHeadersListView.setAdapter(mAdapter);
        fetchBusSchedules();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AlertManager.getInstance().addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AlertManager.getInstance().removeListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LocationBasedRouteModel route = (LocationBasedRouteModel)mAdapter.getItem(position);
        AlertModel alert = mAdapter.getAlert(position);

        Intent systemStatusDetailsIntent;
        systemStatusDetailsIntent = new Intent(this, SystemStatusDetailsActionBarActivity.class);
        //@TODO this will likely load from cache but will need to be updated once all sqllite access is made async
        systemStatusDetailsIntent.putExtra(getString(R.string.actionbar_titletext_key), route.getRouteShortNameWithDirection() + " - " + mAdapter.getHeaderValue(route.getRouteShortName(), route.getDirectionCode()));
        systemStatusDetailsIntent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), "systemstatus");
        systemStatusDetailsIntent.putExtra(getString(R.string.systemstatus_details_route_id), alert.getRouteId());
        systemStatusDetailsIntent.putExtra(getString(R.string.systemstatus_details_tabenabled_advisory), alert.hasAdvisoryFlag());
        systemStatusDetailsIntent.putExtra(getString(R.string.systemstatus_details_tabenabled_alerts), alert.hasAlertFlag());
        systemStatusDetailsIntent.putExtra(getString(R.string.systemstatus_details_tabenabled_detour), alert.hasDetourFlag());

        startActivity(systemStatusDetailsIntent);

    }

    @Override
    public void alertsDidUpdate() {
        if(mAdapter != null)
            mAdapter.alertsDidChange();
    }

    private void fetchBusSchedules() {

        Log.d(TAG, "about to call bus schedules service");
        BusSchedulesServiceProxy busSchedulesServiceProxy = new BusSchedulesServiceProxy();

        for(LocationBasedRouteModel route : locationModel.getRoutes()){
            String direction = null;
            //hack because services are broken and return bad json. splitting into two route directions returns clean data.. for now.
            RouteFetchCallback callback = new RouteFetchCallback(route);
            if(route.getRouteShortName().equals("MFL") || route.getRouteShortName().equals("BSL") ){
                RouteFetchCallback callbackInbound = new RouteFetchCallback(LocationBasedRouteModel.routeModelForDirection(route, "I"));
                busSchedulesServiceProxy.getBusSchedules(locationModel.getLocationId(), route.getRouteShortName(), "i", 5, callbackInbound);

                RouteFetchCallback callbackOutbound = new RouteFetchCallback(LocationBasedRouteModel.routeModelForDirection(route, "O"));
                busSchedulesServiceProxy.getBusSchedules(locationModel.getLocationId(), route.getRouteShortName(), "o", 5, callbackOutbound);

                numRequests += 2;
            } else {
                busSchedulesServiceProxy.getBusSchedules(locationModel.getLocationId(), route.getRouteShortName(), null, 5, callback);
                numRequests ++;
            }


        }

        Log.d(TAG, "called bus schedules service");
    }

    private void updateLoadingView(){
        statusView.setLoading(numRequests > 0);
    }

    private class RouteFetchCallback implements Callback{
        LocationBasedRouteModel route;
        public RouteFetchCallback(LocationBasedRouteModel route){
            this.route = route;
        }

        @Override
        public void success(Object o, Response response) {
            Log.d(TAG, "fetch bus schedules success");

            if (o != null) {
                Log.d(TAG, "the object is not null");

                BusSchedulesModel busSchedulesModel = (BusSchedulesModel) o;
                Log.d(TAG, "number of entries is " + busSchedulesModel.getBusScheduleList().size());

                for (BusScheduleModel busScheduleModel : busSchedulesModel.getBusScheduleList()) {
                    route.addTimeDayPair(busScheduleModel.getDate(), busScheduleModel.getDay());

                }
                mAdapter.addRoute(route);
            }
            requestComplete();
        }

        @Override
        public void failure(RetrofitError retrofitError) {

            try {
                Log.d(TAG, "A failure in the call to fetch bus schdules with body |" + retrofitError.getResponse().getBody().in() + "|");
            } catch (Exception ex) {
                // TODO: clean this up
                Log.d(TAG, "blah... what is going on?");
            }
            requestComplete();
        }

        private void requestComplete(){
            numRequests--;
            updateLoadingView();
        }

    }
}
package org.septa.android.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.septa.android.app.R;

import org.septa.android.app.models.LocationModel;
import org.septa.android.app.models.servicemodels.AlertModel;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class FindNearestLocationRouteDetailsActionBarActivity extends BaseAnalyticsActionBarActivity implements AdapterView.OnItemClickListener{
    public static final String TAG = FindNearestLocationRouteDetailsActionBarActivity.class.getName();

    public LocationModel locationModel = null;

    private ArrayList<AlertModel> alertModelList = new ArrayList<AlertModel>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String resourceName = getString(R.string.actionbar_iconimage_imagename_base).concat("findlocations");

        int id = getResources().getIdentifier(resourceName, "drawable", getPackageName());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find Locations");
        getSupportActionBar().setIcon(id);

        String locationModelJSONString = getIntent().getStringExtra(getString(R.string.findNearestLocation_locationRouteModel));
        Gson gson = new Gson();
        locationModel = gson.fromJson(locationModelJSONString, new TypeToken<LocationModel>(){}.getType());

        Log.d(TAG, "in the findnearestlocationroutedetails actionbar activity with location model name as "+locationModel.getLocationName());

        setContentView(R.layout.findnearestlocation_routedetails);

        // set the empty view in case we don't have any data
        LinearLayout emptyView = (LinearLayout)findViewById(R.id.empty);
        StickyListHeadersListView listView = (StickyListHeadersListView)findViewById(R.id.findnearestlocations_routedetails_listview);
        listView.setOnItemClickListener(this);
        listView.setEmptyView(emptyView);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {

        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AlertModel alert = (AlertModel)parent.getItemAtPosition(position);

        String displayRouteName = alert.getRouteName();
        if (alert.isBus()) {
            displayRouteName = "Route " + displayRouteName;
        }
        if (alert.isTrolley()) {
            displayRouteName = "Trolley " + displayRouteName;
        }

        Intent systemStatusDetailsIntent;
        systemStatusDetailsIntent = new Intent(this, SystemStatusDetailsActionBarActivity.class);
        systemStatusDetailsIntent.putExtra(getString(R.string.actionbar_titletext_key), displayRouteName);
        systemStatusDetailsIntent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), "systemstatus");

        // if the suspend is enabled, no other tabs will exist
        if (alert.hasSuspendedFlag()) {
            systemStatusDetailsIntent.putExtra("system_status_details_tabenabled_suspend", true);
        } else {
            if (alert.hasAdvisoryFlag()) {
                systemStatusDetailsIntent.putExtra(getString(R.string.systemstatus_details_tabenabled_advisory), true);
            }
            if (alert.hasAlertFlag()) {
                systemStatusDetailsIntent.putExtra(getString(R.string.systemstatus_details_tabenabled_alerts), true);
            }
            if (alert.hasDetourFlag()) {
                systemStatusDetailsIntent.putExtra(getString(R.string.systemstatus_details_tabenabled_detour), true);
            }
        }

        Log.d(TAG, "about to put route id as "+alert.getRouteId());
        systemStatusDetailsIntent.putExtra(getString(R.string.systemstatus_details_route_id), alert.getRouteId());

        startActivity(systemStatusDetailsIntent);
    }
}
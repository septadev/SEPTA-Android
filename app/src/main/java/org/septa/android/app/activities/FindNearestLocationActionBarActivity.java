/*
 * FindNearestLocationActionBarActivity.java
 * Last modified on 03-25-2014 14:52-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.content.DialogInterface;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.septa.android.app.R;
import org.septa.android.app.dialogs.FindNearestLocationEditRadiusDialog;
import org.septa.android.app.fragments.FindNearestLocationsListFragment;
import org.septa.android.app.fragments.FindNearestLocationsListFragment.OnRetryLocationSearchListener;
import org.septa.android.app.managers.SharedPreferencesManager;
import org.septa.android.app.models.LocationModel;
import org.septa.android.app.services.apiproxies.LocationServiceProxy;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class FindNearestLocationActionBarActivity extends BaseAnalyticsActionBarActivity implements
        LocationListener,
        OnRetryLocationSearchListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    public static final String TAG = FindNearestLocationActionBarActivity.class.getName();

    //State Keys
    public static final String STATE_CURRENT_LOCATION = "currentLocation";

    public static final int UPDATE_INTERVAL_IN_SECONDS = 120;
    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 60;
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    private final int RQS_GooglePlayServices = 1;

    private LocationClient mLocationClient;
    private boolean inChangeRadiusMode = false;
    private float defaultZoom;
    private float maxDistanceFromCityCenter; //if new locations exceed maximum we return to city center instead
    private float maxDistancePerStep; //max distance to be traveled between list reloads.


    private GoogleMap mMap;
    private FindNearestLocationEditRadiusDialog findNearestLocationEditRadiusDialog;
    private int locationServiceCalls = 0;
    private float mapSearchRadius;
    private FindNearestLocationsListFragment mListFragment;
    private List<LocationModel> mLocationList;

    private Location defaultLocation;
    private Location currentLocation;
    private Location lastListedLocation; //the last location from which we loaded the list. This presents unnecessary UI flicker.

    private float getMapSearchRadius() {
        return mapSearchRadius;
    }

    private void setMapSearchRadius(float mapSearchRadius) {
        this.mapSearchRadius = mapSearchRadius;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String titleText = getIntent().getStringExtra(getString(R.string.actionbar_titletext_key));

        setContentView(R.layout.findnearestlocation);

        mListFragment = (FindNearestLocationsListFragment) getSupportFragmentManager().
                findFragmentById(R.id.nearestLocationListFragment);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_actionbar_findnearestlocation);
        getSupportActionBar().setTitle(titleText);

        mapSearchRadius = SharedPreferencesManager.getInstance().getNearestLocationMapSearchRadius();

        mMap = ((SupportMapFragment) getSupportFragmentManager().
                findFragmentById(R.id.nearestLocationMapFragment)).
                getMap();

        mLocationList = new ArrayList<LocationModel>();

        // set the initial center point of the map on Center City, Philadelphia with a default zoom
        double defaultLatitude = Double.parseDouble(getResources().getString(R.string.generalmap_default_location_latitude));
        double defaultLongitude = Double.parseDouble(getResources().getString(R.string.generalmap_default_location_longitude));
        maxDistanceFromCityCenter = Float.parseFloat(getResources().getString(R.string.generalmap_max_distance_from_center));
        maxDistancePerStep = Float.parseFloat(getResources().getString(R.string.generalmap_max_distance_per_step));
        defaultZoom = Float.parseFloat(getResources().getString(R.string.findnearestlocation_map_zoom_level_float));

        defaultLocation = new Location("default");
        defaultLocation.setLatitude(defaultLatitude);
        defaultLocation.setLongitude(defaultLongitude);

        mMap.setMyLocationEnabled(true);
        moveMap(defaultLocation, false);

        mLocationClient = new LocationClient(this, this, this);
        if(savedInstanceState != null){
            currentLocation = (Location)savedInstanceState.get(STATE_CURRENT_LOCATION);
        }

        if(currentLocation != null){
            moveMapAndLoadList(currentLocation, false);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "creating the menu in find nearest location actionbar activity");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.find_nearest_location_action_bar, menu);

        menu.findItem(R.id.actionmenu_findnearestlocationactionbar_changeradius).setVisible(!inChangeRadiusMode);
        menu.findItem(R.id.actionmenu_findnearestlocationactionbar_changeradius_done).setVisible(inChangeRadiusMode);

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mLocationClient != null)
            mLocationClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mLocationClient != null)
            mLocationClient.disconnect();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionmenu_findnearestlocationactionbar_changeradius:
                inChangeRadiusMode = true;
                ActivityCompat.invalidateOptionsMenu(this);

                findNearestLocationEditRadiusDialog = new FindNearestLocationEditRadiusDialog(this, SharedPreferencesManager.getInstance().getNearestLocationMapSearchRadius());

                findNearestLocationEditRadiusDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        inChangeRadiusMode = false;
                        ActivityCompat.invalidateOptionsMenu(FindNearestLocationActionBarActivity.this);

                        float radius = findNearestLocationEditRadiusDialog.getMapSearchRadius();
                        if (getMapSearchRadius() != radius) {
                            setMapSearchRadius(radius);
                            SharedPreferencesManager.getInstance().setNearestLocationMapSearchRadius(radius);
                            loadMapAndListView(currentLocation, radius);
                        }
                    }
                });

                findNearestLocationEditRadiusDialog.show();

                return true;
            case R.id.actionmenu_findnearestlocationactionbar_changeradius_done:
                findNearestLocationEditRadiusDialog.dismiss();

                inChangeRadiusMode = false;
                ActivityCompat.invalidateOptionsMenu(this);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_CURRENT_LOCATION, currentLocation);
        outState.putParcelable(STATE_CURRENT_LOCATION, currentLocation);

    }

    private void loadMapAndListView(Location newLocation, float mapSearchRadius) {

        double longitude = newLocation.getLongitude();
        double latitude =  newLocation.getLatitude();

        if (mMap != null) {
            mMap.clear();
        }
        lastListedLocation = newLocation;
        mLocationList.clear();
        mListFragment.clearLocationLists();

        locationServiceCalls += 3;
        LocationServiceProxy busStopsLocationServiceProxy = new LocationServiceProxy();
        busStopsLocationServiceProxy.getLocation(longitude, latitude, mapSearchRadius, "bus_stops", new RouteFetchCallback());

        LocationServiceProxy railStopsLocationServiceProxy = new LocationServiceProxy();
        railStopsLocationServiceProxy.getLocation(longitude, latitude, mapSearchRadius, "rail_stations", new RouteFetchCallback());

        LocationServiceProxy trolleyStopsLocationServiceProxy = new LocationServiceProxy();
        trolleyStopsLocationServiceProxy.getLocation(longitude, latitude, mapSearchRadius, "trolley_stops", new RouteFetchCallback());
    }

    private void moveMapAndLoadList(Location location, boolean animated){
        moveMap(location, animated);
        loadMapAndListView(location, SharedPreferencesManager.getInstance().getNearestLocationMapSearchRadius());
    }

    private void moveMap(Location location, boolean animate){
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), defaultZoom);
        if(animate){
            mMap.animateCamera(cameraUpdate);
        } else{
            mMap.moveCamera(cameraUpdate);
        }

    }

    @Override
    public void onLocationChanged(Location newLocation) {
        float distanceFromCenter  = defaultLocation.distanceTo(newLocation);

        if (newLocation.getAccuracy()< getResources().getInteger(R.integer.findnearestlocation_map_accuracy_limit_in_meters) && distanceFromCenter < maxDistanceFromCityCenter) {
            this.currentLocation = newLocation;

        } else {
            //too far away. no need for more updates.
            mLocationClient.disconnect();
            this.currentLocation = defaultLocation;
        }

        /*
         * We do not want to refresh the map on every location change but we do
         * want to keep track of our newest location. Once the user travels past the
         * max step we reload the list. Otherwise we just move the map.
         */
        if(lastListedLocation == null || lastListedLocation.distanceTo(currentLocation) > maxDistancePerStep ){
            moveMapAndLoadList(currentLocation, true);
        } else {
            moveMap(currentLocation, true);
        }

    }

    /*
     * Called by LocationModel Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {

        // Display the connection status
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    /*
     * Called by LocationModel Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        Log.d(TAG, "location services disconnected.");
    }

    /*
     * Called by LocationModel Services if the attempt to
     * LocationModel Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        9000);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            mLocationClient.disconnect();
            currentLocation = defaultLocation;
            moveMapAndLoadList(currentLocation, false);
        }
    }


    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        if (resultCode != ConnectionResult.SUCCESS){

            GooglePlayServicesUtil.getErrorDialog(resultCode, this, RQS_GooglePlayServices);
        }
    }

    private void updateDisplay(){
        mListFragment.setLocationList(mLocationList);

        for (LocationModel location : mLocationList) {
            // check to make sure that mMap is not null
            if (mMap != null) {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(location.getLocationLatitude(), location.getLocationLongitude()))
                        .title(location.getLocationName())
                        .snippet("Route: "));
            }
        }
    }

    public void onRetryLocationSearch() {
        updateDisplay();
    }

    private class RouteFetchCallback implements Callback{
        @Override
        public void success(Object o, Response response) {

            mLocationList.addAll((ArrayList<LocationModel>) o);
            if (--locationServiceCalls < 1) {
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                updateDisplay();
            }
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            if (--locationServiceCalls < 1) {
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                updateDisplay();
            }
        }
    }
}

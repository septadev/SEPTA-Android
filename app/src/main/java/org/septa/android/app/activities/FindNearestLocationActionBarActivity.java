/*
 * FindNearestLocationActionBarActivity.java
 * Last modified on 03-25-2014 14:52-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import com.google.android.gms.common.GooglePlayServicesClient;

import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.septa.android.app.R;
import org.septa.android.app.dialogs.FindNearestLocationEditRadiusDialog;
import org.septa.android.app.fragments.FindNearestLocationsListFragment;
import org.septa.android.app.models.LocationModel;
import org.septa.android.app.models.ObjectFactory;
import org.septa.android.app.services.apiproxies.LocationServiceProxy;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class FindNearestLocationActionBarActivity extends BaseAnalyticsActionBarActivity implements
        LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {
    public static final String TAG = FindNearestLocationActionBarActivity.class.getName();

    private boolean inChangeRadiusMode = false;

    private GoogleMap mMap;

    final int RQS_GooglePlayServices = 1;

    LocationClient mLocationClient;

    private static final int MILLISECONDS_PER_SECOND = 1000;

    public static final int UPDATE_INTERVAL_IN_SECONDS = 10;

    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    private static final int FASTEST_INTERVAL_IN_SECONDS = 10;
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    private FindNearestLocationEditRadiusDialog findNearestLocationEditRadiusDialog;

    private int locationServiceCalls = 0;

    private Location existingLocation = null;

    private float mapSearchRadius;

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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_actionbar_findnearestlocation);
        getSupportActionBar().setTitle(titleText);

        mapSearchRadius = ObjectFactory.getInstance().getSharedPreferencesManager(this).getNearestLocationMapSearchRadius();

        mMap = ((SupportMapFragment) getSupportFragmentManager().
                findFragmentById(R.id.nearestLocationMapFragment)).
                getMap();

        // set the initial center point of the map on Center City, Philadelphia with a default zoom
        double defaultLatitute = Double.parseDouble(getResources().getString(R.string.generalmap_default_location_latitude));
        double defaultLongitude = Double.parseDouble(getResources().getString(R.string.generalmap_default_location_longitude));
        float defaultZoomLevel = Float.parseFloat(getResources().getString(R.string.generalmap_default_zoomlevel));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(defaultLatitute, defaultLongitude), defaultZoomLevel));
//        loadMapAndListView(defaultLatitute, defaultLongitude, mapSearchRadius);

        mMap.setMyLocationEnabled(true);

        mLocationClient = new LocationClient(this, this, this);
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

                findNearestLocationEditRadiusDialog = new FindNearestLocationEditRadiusDialog(this, ObjectFactory.getInstance().getSharedPreferencesManager(this).getNearestLocationMapSearchRadius());

                findNearestLocationEditRadiusDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        inChangeRadiusMode = false;
                        ActivityCompat.invalidateOptionsMenu(FindNearestLocationActionBarActivity.this);

                        Log.d(TAG, "performing the eval of the map search radius");
                        if (getMapSearchRadius() != findNearestLocationEditRadiusDialog.getMapSearchRadius()) {
                            Log.d(TAG, "found them to be unequal, do work");

                            setMapSearchRadius(mapSearchRadius);

                            ObjectFactory.getInstance().getSharedPreferencesManager(FindNearestLocationActionBarActivity.this).setNearestLocationMapSearchRadius(findNearestLocationEditRadiusDialog.getMapSearchRadius());
                            loadMapAndListView(existingLocation, findNearestLocationEditRadiusDialog.getMapSearchRadius());
                        } else {
                            Log.d(TAG, "radius are equal, nothing");
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

    private void loadMapAndListView(Location newLocation, float mapSearchRadius) {
        if (newLocation != null) {
            Log.d(TAG, "newLocation's latitude "+newLocation.getLatitude());
        } else {
            Log.d(TAG, "newLocation is null, wrong.");
        }

        Log.d(TAG, "mapSearchRadius is "+mapSearchRadius);

        loadMapAndListView(newLocation.getLatitude(), newLocation.getLongitude(), mapSearchRadius);
    }

    private void loadMapAndListView(Double latitude, Double longitude, float mapSearchRadius) {


        if (mMap != null) {

            mMap.clear();
        }

        FindNearestLocationsListFragment findNearestLocationsListFragment = (FindNearestLocationsListFragment) getSupportFragmentManager().
                findFragmentById(R.id.nearestLocationListFragment);
        findNearestLocationsListFragment.clearLocationLists();

        Callback busStopsCallback = new Callback() {
            @Override
            public void success(Object o, Response response) {
                if (--locationServiceCalls < 1) {
                    setProgressBarIndeterminateVisibility(Boolean.FALSE);
                }
                ActivityCompat.invalidateOptionsMenu(FindNearestLocationActionBarActivity.this);

                FindNearestLocationsListFragment listFragment = (FindNearestLocationsListFragment) getSupportFragmentManager().findFragmentById(R.id.nearestLocationListFragment);
                listFragment.setLocationList((ArrayList<LocationModel>) o, "bus");

                for (LocationModel location : (ArrayList<LocationModel>) o) {
                    // check to make sure that mMap is not null
                    if (mMap != null) {
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(location.getLocationLatitude(), location.getLocationLongitude()))
                                .title(location.getLocationName())
                                .snippet("Route: "));
                    }
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                if (--locationServiceCalls < 1) {
                    setProgressBarIndeterminateVisibility(Boolean.FALSE);
                }
                ActivityCompat.invalidateOptionsMenu(FindNearestLocationActionBarActivity.this);

                try {
                    Log.d(TAG, "A failure in the call to location service with body |" + retrofitError.getResponse().getBody().in() + "|");
                } catch (Exception ex) {
                    // TODO: clean this up
                    Log.d(TAG, "blah... what is going on?");
                }
            }
        };

        Callback railStopsCallBack = new Callback() {
            @Override
            public void success(Object o, Response response) {
                if (--locationServiceCalls < 1) {
                    setProgressBarIndeterminateVisibility(Boolean.FALSE);
                }
                ActivityCompat.invalidateOptionsMenu(FindNearestLocationActionBarActivity.this);

                FindNearestLocationsListFragment listFragment = (FindNearestLocationsListFragment) getSupportFragmentManager().findFragmentById(R.id.nearestLocationListFragment);
                listFragment.setLocationList((ArrayList<LocationModel>) o, "rail");

                for (LocationModel location : (ArrayList<LocationModel>) o) {
                    // check to make sure that mMap is not null
                    if (mMap != null) {
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(location.getLocationLatitude(), location.getLocationLongitude()))
                                .title(location.getLocationName())
                                .snippet("Route: "));
                    }
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                if (--locationServiceCalls < 1) {
                    setProgressBarIndeterminateVisibility(Boolean.FALSE);
                }
                ActivityCompat.invalidateOptionsMenu(FindNearestLocationActionBarActivity.this);

                try {
                    Log.d(TAG, "A failure in the call to location service with body |" + retrofitError.getResponse().getBody().in() + "|");
                } catch (Exception ex) {
                    // TODO: clean this up
                    Log.d(TAG, "blah... what is going on?");
                }
            }
        };

        Callback trolleyStopsCallBack = new Callback() {
            @Override
            public void success(Object o, Response response) {
                if (--locationServiceCalls < 1) {
                    setProgressBarIndeterminateVisibility(Boolean.FALSE);
                }
                ActivityCompat.invalidateOptionsMenu(FindNearestLocationActionBarActivity.this);

                FindNearestLocationsListFragment listFragment = (FindNearestLocationsListFragment) getSupportFragmentManager().findFragmentById(R.id.nearestLocationListFragment);
                listFragment.setLocationList((ArrayList<LocationModel>) o, "trolley");

                for (LocationModel location : (ArrayList<LocationModel>) o) {
                    // check to make sure that mMap is not null
                    if (mMap != null) {
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(location.getLocationLatitude(), location.getLocationLongitude()))
                                .title(location.getLocationName())
                                .snippet("Route: "));
                    }
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                if (--locationServiceCalls < 1) {
                    setProgressBarIndeterminateVisibility(Boolean.FALSE);
                }
                ActivityCompat.invalidateOptionsMenu(FindNearestLocationActionBarActivity.this);

                try {
                    Log.d(TAG, "A failure in the call to location service with body |" + retrofitError.getResponse().getBody().in() + "|");
                } catch (Exception ex) {
                    // TODO: clean this up
                    Log.d(TAG, "blah... what is going on?");
                }
            }
        };

        LocationServiceProxy busStopsLocationServiceProxy = new LocationServiceProxy();
        setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
        locationServiceCalls++;
        busStopsLocationServiceProxy.getLocation(longitude, latitude, mapSearchRadius, "bus_stops", busStopsCallback);

        LocationServiceProxy railStopsLocationServiceProxy = new LocationServiceProxy();
        setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
        locationServiceCalls++;
        railStopsLocationServiceProxy.getLocation(longitude, latitude, mapSearchRadius, "rail_stations", railStopsCallBack);

        LocationServiceProxy trolleyStopsLocationServiceProxy = new LocationServiceProxy();
        setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
        locationServiceCalls++;
        trolleyStopsLocationServiceProxy.getLocation(longitude, latitude, mapSearchRadius, "trolley_stops", trolleyStopsCallBack);
    }

    @Override
    public void onLocationChanged(Location newLocation) {
        Log.d(TAG, "location changed with accuracy of "+newLocation.getAccuracy());
        // TODO: find a different way to tell if we should make our network calls, with a timer.
        // TODO: find a better way to shut off the updates and resume when it makes sense
        if (newLocation.getAccuracy()< getResources().getInteger(R.integer.findnearestlocation_map_accuracy_limit_in_meters)) {
            this.existingLocation = newLocation;

            mLocationClient.disconnect();

            LatLng currentLocation = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, Float.parseFloat(getString(R.string.findnearestlocation_map_zoom_level_float))));

            Log.d(TAG, "onLocationChanged, newLocation's latitude "+newLocation.getLatitude());
            Log.d(TAG, "onLocationChanged, newLocation's longitude "+newLocation.getLongitude());
            loadMapAndListView(newLocation, ObjectFactory.getInstance().getSharedPreferencesManager(this).getNearestLocationMapSearchRadius());
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
//        if (connectionResult.hasResolution()) {
//            try {
                // Start an Activity that tries to resolve the error
//                connectionResult.startResolutionForResult(
//                        this,
//                        9000);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
//            } catch (IntentSender.SendIntentException e) {
//                // Log the error
//                e.printStackTrace();
//            }
//        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
//            Log.d(TAG, "location services error: " + connectionResult.getErrorCode());
//        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
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
}

/*
 * FindNearestLocationActionBarActivity.java
 * Last modified on 03-25-2014 14:52-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.app.Activity;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
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
import org.septa.android.app.services.apiproxies.LocationServiceProxy;
import org.septa.android.app.models.LocationModel;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String titleText = getIntent().getStringExtra(getString(R.string.actionbar_titletext_key));

        // enables the activity indicator in the action bar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.findnearestlocation);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_actionbar_findnearestlocation);
        getSupportActionBar().setTitle(titleText);

        mMap = ((SupportMapFragment)getSupportFragmentManager().
                                        findFragmentById(R.id.nearestLocationMapFragment)).
                                        getMap();

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
                Log.d(TAG, "heard the change radius action item pressed");

                inChangeRadiusMode = true;
                ActivityCompat.invalidateOptionsMenu(this);

                return true;
            case R.id.actionmenu_findnearestlocationactionbar_changeradius_done:
                Log.d(TAG, "heard the done change radius action item pressed");

                inChangeRadiusMode = false;
                ActivityCompat.invalidateOptionsMenu(this);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onLocationChanged(Location newLocation) {
        // TODO: find a different way to tell if we should make our network calls, with a timer.
        // TODO: find a better way to shut off the updates and resume when it makes sense
        if (newLocation.getAccuracy()< 16.0) {
            mLocationClient.disconnect();
            LatLng currentLocation = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, Float.parseFloat(getString(R.string.findnearestlocation_map_zoom_level_float))));

            Callback callback = new Callback() {
                @Override
                public void success(Object o, Response response) {
                    Log.d(TAG, "successfully ended location service call with " + ((ArrayList<LocationModel>) o).size());
                    setProgressBarIndeterminateVisibility(Boolean.FALSE);
                    ActivityCompat.invalidateOptionsMenu(FindNearestLocationActionBarActivity.this);

                    for (LocationModel location: (ArrayList<LocationModel>)o) {
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
                    setProgressBarIndeterminateVisibility(Boolean.FALSE);
                    ActivityCompat.invalidateOptionsMenu(FindNearestLocationActionBarActivity.this);

                    try {
                        Log.d(TAG, "A failure in the call to location service with body |" + retrofitError.getResponse().getBody().in() + "|");
                    } catch (Exception ex) {
                        // TODO: clean this up
                        Log.d(TAG, "blah... what is going on?");
                    }
                }
            };

            Log.d(TAG, "stating location service call...");
            LocationServiceProxy locationServiceProxy = new LocationServiceProxy();
            setProgressBarIndeterminateVisibility(Boolean.TRUE);
            locationServiceProxy.getLocation(newLocation.getLongitude(), newLocation.getLatitude(), 2.5F, "bus_stops", callback);
            Log.d(TAG, "ended the call, now wait for the callback");
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
        Log.d(TAG, "location services connected");

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
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.d(TAG, "location services error: " + connectionResult.getErrorCode());
        }
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

/*
 * TransitViewActionBarActivity.java
 * Last modified on 04-11-2014 17:13-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.septa.android.app.R;
import org.septa.android.app.models.KMLModel;
import org.septa.android.app.models.LocationModel;
import org.septa.android.app.models.BusRoutesModel;
import org.septa.android.app.models.servicemodels.TrainViewModel;
import org.septa.android.app.models.servicemodels.TransitViewModel;
import org.septa.android.app.models.servicemodels.TransitViewVehicleModel;
import org.septa.android.app.services.apiproxies.TrainViewServiceProxy;
import org.septa.android.app.services.apiproxies.TransitViewServiceProxy;
import org.septa.android.app.utilities.KMLSAXXMLProcessor;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TransitViewMapAndRouteListActionBarActivity extends BaseAnalyticsActionBarActivity implements
        LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener{
    public static final String TAG = TransitViewMapAndRouteListActionBarActivity.class.getName();

    KMLModel kmlModel;

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

    private boolean listviewRevealed = false;

    private BusRoutesModel busRoutes;
    private String routeShortName;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        Log.d(TAG, "touch event heard");

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate for transit view heard");

        Intent intent = getIntent();
        String routeShortName = intent.getStringExtra("route_short_name");
        Log.d(TAG, "launched from an intent with route_short_name of "+routeShortName);

        String actionBarTitleText = getIntent().getStringExtra(getString(R.string.actionbar_titletext_key));
        String iconImageNameSuffix = getIntent().getStringExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key));
        routeShortName = getIntent().getStringExtra("route_short_name");

        String resourceName = getString(R.string.actionbar_iconimage_imagename_base).concat(iconImageNameSuffix);

        int id = getResources().getIdentifier(resourceName, "drawable", getPackageName());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(actionBarTitleText);
        getSupportActionBar().setIcon(id);

        setContentView(R.layout.transitview_mapandroutelistview);

        mMap = ((SupportMapFragment)getSupportFragmentManager().
                findFragmentById(R.id.transitview_map_fragment)).
                getMap();

        mMap.setMyLocationEnabled(true);
        mLocationClient = new LocationClient(this, this, this);

        KMLSAXXMLProcessor processor = new KMLSAXXMLProcessor(getAssets());
        processor.readKMLFile("kml/transit/"+routeShortName+".kml");

        kmlModel = processor.getKMLModel();

        // loop through the placemarks
        List<KMLModel.Document.Placemark> placemarkList = kmlModel.getDocument().getPlacemarkList();
        for (KMLModel.Document.Placemark placemark : placemarkList) {
            List<KMLModel.Document.MultiGeometry.LineString> lineStringList = placemark.getMultiGeometry().getLineStringList();
            for (KMLModel.Document.MultiGeometry.LineString lineString : lineStringList) {
                String color = "#" + kmlModel.getDocument().getColorForStyleId(placemark.getStyleUrl());
                List<LatLng> latLngCoordinateList = lineString.getLatLngCoordinates();

                PolylineOptions lineOptions = new PolylineOptions().addAll(latLngCoordinateList)
                        .color(Color.parseColor(color))
                        .width(3.0f)
                        .visible(true);
                mMap.addPolyline(lineOptions);
            }
        }
        this.fetchTransitViewDataForRoute(routeShortName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "creating the menu in find nearest location actionbar activity");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.trainsitview_action_bar, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu");

        return true;
    }

    private void revealListView() {
        final RelativeLayout ll1 = (RelativeLayout) findViewById(R.id.transitview_map_fragment_view);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_right_to_left);

        anim.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0) {
                Log.d(TAG, "brining ll2 to front");
                LinearLayout ll2 = (LinearLayout) findViewById(R.id.back_frame);
                ll2.bringToFront();
            }
        });

        anim.setInterpolator((new AccelerateDecelerateInterpolator()));
        anim.setFillAfter(true);
        ll1.startAnimation(anim);
    }

    private void hideListView() {
        Log.d(TAG, "slideMapViewOver");
        final RelativeLayout ll1 = (RelativeLayout) findViewById(R.id.transitview_map_fragment_view);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_left_to_right);
        ll1.bringToFront();

        anim.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0) {
//                RelativeLayout ll2 = (RelativeLayout) findViewById(R.id.back_frame);
//                ll1.bringToFront();
            }
        });

        anim.setInterpolator((new AccelerateDecelerateInterpolator()));
        anim.setFillAfter(true);
        ll1.startAnimation(anim);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionmenu_transitview_reveallistview:
                Log.d(TAG, "heard the reveal list view in transitview_mapandroutelistview");

                if (listviewRevealed) {
                    Log.d(TAG, "the list view is revealed, pull the map view back on top");
                    listviewRevealed = false;

                    hideListView();
                } else {
                    Log.d(TAG, "the list view is hidden, reveal it.");
                    listviewRevealed = true;

                    revealListView();
                }

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

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,
                    Float.parseFloat(getString(R.string.trainviewactionbaractivity_map_zoom_level_float))));
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

    private void fetchTransitViewDataForRoute(String routeShortName) {
        Callback callback = new Callback() {
            @Override
            public void success(Object o, Response response) {
                setProgressBarIndeterminateVisibility(Boolean.FALSE);

                List<TransitViewVehicleModel>transitViewVehicleModelList = ((TransitViewModel)o).getVehicleModelList();
                for (TransitViewVehicleModel transitViewVehicle: transitViewVehicleModelList) {
                    BitmapDescriptor transitIcon;

                    // check if the destination is blank, meaning the bus just went out of service
                    if (transitViewVehicle.getDestination().trim().equals("")) {
                        break;
                    }
                    if (transitViewVehicle.isSouthBound()) {
                        transitIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_transitview_bus_red);
                    } else {
                        transitIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_transitview_bus_blue);
                    }

//                    String title = "Train #" + trainView.getTrainNumber() + " ";
//                    if (trainView.isLate()) {
//                        title += "(" + trainView.getLate()+ " min late)";
//                    } else {
//                        title += "(on time)";
//                    }
//                    String snippet = trainView.getSource() + " to " + trainView.getDestination();

                    // check to make sure that mMap is not null
                    if (mMap != null) {
                        Log.d(TAG, "adding the marker to the map at lat of "+transitViewVehicle.getLatitude()+" and long of "+transitViewVehicle.getLongitude());
                        mMap.addMarker(new MarkerOptions()
                                .position(transitViewVehicle.getLatLng())
                                .title("Vehicle: "+transitViewVehicle.getVehicleId())
                                .icon(transitIcon)
                                .snippet("Destination: "+ transitViewVehicle.getDestination()));
                    }
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                setProgressBarIndeterminateVisibility(Boolean.FALSE);

                try {
                    Log.d(TAG, "A failure in the call to train view service with body |" + retrofitError.getResponse().getBody().in() + "|");
                } catch (Exception ex) {
                    // TODO: clean this up
                    Log.d(TAG, "blah... what is going on?");
                }
            }
        };

        TransitViewServiceProxy transitViewServiceProxy = new TransitViewServiceProxy();
        setProgressBarIndeterminateVisibility(Boolean.TRUE);
        transitViewServiceProxy.getTransitViewForRoute(routeShortName, callback);
    }
}

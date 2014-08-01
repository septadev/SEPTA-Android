/*
 * TransitViewActionBarActivity.java
 * Last modified on 04-11-2014 17:13-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.septa.android.app.R;
import org.septa.android.app.fragments.TransitViewRouteViewListFragment;
import org.septa.android.app.models.KMLModel;
import org.septa.android.app.models.RoutesModel;
import org.septa.android.app.models.servicemodels.TransitViewModel;
import org.septa.android.app.models.servicemodels.TransitViewVehicleModel;
import org.septa.android.app.services.apiproxies.TransitViewServiceProxy;
import org.septa.android.app.utilities.KMLSAXXMLProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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

    private RoutesModel busRoutes;
    private String routeShortName;

    private ArrayList<Marker> markerList = new ArrayList<Marker>();

    private Timer asyncTransitViewRefreshTimer;

    private LatLng currentLocation = null;

    private List<TransitViewVehicleModel> transitViewVehicleArrayList = new ArrayList<TransitViewVehicleModel>();

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        Log.d(TAG, "touch event heard");

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        routeShortName = intent.getStringExtra("route_short_name");

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

        if (mMap != null) {
            try {
                // set the initial center point of the map on Center City, Philadelphia with a default zoom
                double defaultLatitude = Double.parseDouble(getResources().getString(R.string.generalmap_default_location_latitude));
                double defaultLongitude = Double.parseDouble(getResources().getString(R.string.generalmap_default_location_longitude));

                currentLocation = new LatLng(defaultLatitude, defaultLongitude);
                float defaultZoomLevel = Float.parseFloat(getResources().getString(R.string.generalmap_default_zoomlevel));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(defaultLatitude, defaultLongitude), defaultZoomLevel));

                mMap.setMyLocationEnabled(true);

                mLocationClient = new LocationClient(this, this, this);

                KMLSAXXMLProcessor processor = new KMLSAXXMLProcessor(getAssets());
                processor.readKMLFile("kml/transit/" + routeShortName + ".kml");

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
            } catch (NumberFormatException e) {
                Log.e(TAG, String.format("Error parsing KML for route:%s" ,routeShortName),e);
            } catch (Resources.NotFoundException e) {
                Log.e(TAG, "Resource missing", e);
            }
        } else {
            Log.d(TAG, "map was null, map Google Play services is not installed");
        }
    }

    public void asyncTransitViewRefresh() {
        asyncTransitViewRefreshTimer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "in run for async task");
                try {

                    fetchTransitViewDataForRoute(routeShortName);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                }
            }
        };

        int refreshInterval = getResources().getInteger(R.integer.vehicle_refresh_interval_ms);
        asyncTransitViewRefreshTimer.schedule(doAsynchronousTask, 0, refreshInterval);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        final LinearLayout backFrame = (LinearLayout) findViewById(R.id.back_frame);
        backFrame.setVisibility(View.VISIBLE);

        final FrameLayout fl1 = (FrameLayout) findViewById(R.id.transitview_map_fragment_view);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_right_to_left);

        anim.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {
                final View shadowView = (View) findViewById(R.id.transitview_map_fragmet_view_shadow);
                shadowView.setVisibility(View.VISIBLE);
                shadowView.bringToFront();
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0) {
                LinearLayout ll2 = (LinearLayout) findViewById(R.id.back_frame);
                ll2.bringToFront();
            }
        });

        anim.setInterpolator((new AccelerateDecelerateInterpolator()));
        anim.setFillAfter(true);
        fl1.startAnimation(anim);
    }

    private void hideListView() {
        final FrameLayout fl1 = (FrameLayout) findViewById(R.id.transitview_map_fragment_view);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_left_to_right);
        fl1.bringToFront();

        anim.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0) {
                View shadowView = (View) findViewById(R.id.transitview_map_fragmet_view_shadow);
                shadowView.setVisibility(View.INVISIBLE);
                LinearLayout mapView = (LinearLayout) findViewById(R.id.trainview_map_fragment_innerview);
                mapView.bringToFront();

                final LinearLayout backFrame = (LinearLayout) findViewById(R.id.back_frame);
                backFrame.setVisibility(View.INVISIBLE);
            }
        });

        anim.setInterpolator((new AccelerateDecelerateInterpolator()));
        anim.setFillAfter(true);
        fl1.startAnimation(anim);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionmenu_transitview_reveallistview:
                if (listviewRevealed) {
                    listviewRevealed = false;

                    hideListView();
                } else {
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
        if (newLocation.getAccuracy()< getResources().getInteger(R.integer.transitview_map_accuracy_limit_in_meters)) {
            mLocationClient.disconnect();
            currentLocation = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,
                    Float.parseFloat(getString(R.string.trainviewactionbaractivity_map_zoom_level_float))));

            updateTransitList();
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
    protected void onPause() {
        super.onPause();

        asyncTransitViewRefreshTimer.cancel();
        asyncTransitViewRefreshTimer.purge();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        if (mLocationClient != null) {
            mLocationClient.connect();
        }
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        if (mLocationClient != null) {
            mLocationClient.disconnect();
        }

        super.onStop();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        asyncTransitViewRefresh();

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (resultCode != ConnectionResult.SUCCESS){
            GooglePlayServicesUtil.getErrorDialog(resultCode, this, RQS_GooglePlayServices);
        }
    }

    /** calculates the distance between two locations in MILES */
    private double distance(double lat1, double lng1, double lat2, double lng2) {

        double earthRadius = 3958.75; // in miles, change to 6371 for kilometers

        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double dist = earthRadius * c;

        return dist;
    }

    private double getDistanceFromCurrentLocation(LatLng compareToLocation) {
        if (currentLocation != null) {
            double currentLocationLatitude = currentLocation.latitude;
            double currentLocationLongitude = currentLocation.longitude;

            double compareToLocationLatitude = compareToLocation.latitude;
            double compareToLocationLongitude = compareToLocation.longitude;

            // lat1 and lng1 are the values of a previously stored location
            return distance(currentLocationLatitude, currentLocationLongitude, compareToLocationLatitude, compareToLocationLongitude);
        }

        return 0;
    }

    private void updateTransitList() {
        TransitViewRouteViewListFragment listFragment1 = (TransitViewRouteViewListFragment) getSupportFragmentManager().findFragmentById(R.id.transitview_list_fragment);

        for (TransitViewVehicleModel transitViewVehicle: transitViewVehicleArrayList) {
            transitViewVehicle.setDistanceFromCurrentLocation(getDistanceFromCurrentLocation(new LatLng(transitViewVehicle.getLatitude(), transitViewVehicle.getLongitude())));
        }

        Collections.sort(transitViewVehicleArrayList);
        listFragment1.setTrainViewModels(transitViewVehicleArrayList);
    }

    private void fetchTransitViewDataForRoute(String routeShortName) {
        Callback callback = new Callback() {
            @Override
            public void success(Object o, Response response) {
                setProgressBarIndeterminateVisibility(Boolean.FALSE);

                transitViewVehicleArrayList = ((TransitViewModel)o).getActiveVehicleModelList();

                TransitViewRouteViewListFragment listFragment1 = (TransitViewRouteViewListFragment) getSupportFragmentManager().findFragmentById(R.id.transitview_list_fragment);

                // test the listFragment1.  If null, we may have transitioned off this view and this fetch
                //  was long running from either a slow start or an async timer task.
                if (listFragment1 == null) {
                    return;
                }

                // clear all of the markers off the map
                for (Marker marker : markerList) {
                    marker.remove();
                }
                markerList.clear();

                for (TransitViewVehicleModel transitViewVehicle: transitViewVehicleArrayList) {
                    BitmapDescriptor transitIcon;

                    transitViewVehicle.setDistanceFromCurrentLocation(getDistanceFromCurrentLocation(new LatLng(transitViewVehicle.getLatitude(), transitViewVehicle.getLongitude())));

                    // check if the destination is blank, meaning the bus just went out of service
                    if (transitViewVehicle.getDestination().trim().equals("")) {
                        break;
                    }

                    if (transitViewVehicle.isSouthBound() || transitViewVehicle.isWestBound()) {
                        transitIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_transitview_bus_red);
                    } else {
                        transitIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_transitview_bus_blue);
                    }

                    // check to make sure that mMap is not null
                    if (mMap != null) {
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(transitViewVehicle.getLatLng())
                                .title("Vehicle: " + transitViewVehicle.getVehicleId() + " (updated: "+transitViewVehicle.getOffset()+" min)")
                                .icon(transitIcon)
                                .snippet("Destination: " + transitViewVehicle.getDestination()));
                        markerList.add(marker);
                    }
                }

                Collections.sort(transitViewVehicleArrayList);
                listFragment1.setTrainViewModels(transitViewVehicleArrayList);
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

package org.septa.android.app.nextarrive;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.data.kml.KmlLayer;

import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.favorites.DeleteFavoritesAsyncTask;
import org.septa.android.app.favorites.SaveFavoritesAsyncTask;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Favorite;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalModelResponse;
import org.septa.android.app.support.MapUtils;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by jkampf on 8/3/17.
 */

public class NextToArriveResultsActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String TAG = NextToArriveResultsActivity.class.getSimpleName();
    StopModel start;
    StopModel destination;
    TransitType transitType;
    RouteDirectionModel routeDirectionModel;
    private GoogleMap googleMap;
    ViewGroup bottomSheetLayout;
    View progressView;
    View progressViewBottom;
    View refresh;
    Favorite currentFavorite = null;
    NextToArriveTripView nextToArriveDetailsFragment;

    public static NextToArriveResultsActivity newInstance(StopModel start, StopModel end) {
        NextToArriveResultsActivity fragement = new NextToArriveResultsActivity();
        fragement.setStart(start);
        fragement.setDestination(end);

        return fragement;
    }

    public void setDestination(StopModel destination) {
        this.destination = destination;
    }

    public void setStart(StopModel start) {
        this.start = start;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.next_to_arrive);
        setContentView(R.layout.next_to_arrive_results);

        progressView = findViewById(R.id.progress_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        refresh = findViewById(R.id.refresh_button);

        bottomSheetLayout = (ViewGroup) findViewById(R.id.bottomSheetLayout);
        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        progressViewBottom = findViewById(R.id.progress_view_bottom);


        // Prevent the bottom sheet from being dragged to be opened.  Force it to use the anchor image.
        BottomSheetHandler myBottomSheetBehaviorCallBack = new BottomSheetHandler(bottomSheetBehavior);
        bottomSheetBehavior.setBottomSheetCallback(myBottomSheetBehaviorCallBack);
        View anchor = findViewById(R.id.bottom_sheet_anchor);
        anchor.setOnClickListener(myBottomSheetBehaviorCallBack);

        nextToArriveDetailsFragment = (NextToArriveTripView) findViewById(R.id.next_to_arrive_trip_details);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        destination = (StopModel) intent.getExtras().get(Constants.DESTINATAION_STATION);
        start = (StopModel) intent.getExtras().get(Constants.STARTING_STATION);
        transitType = (TransitType) intent.getExtras().get(Constants.TRANSIT_TYPE);
        routeDirectionModel = (RouteDirectionModel) intent.getExtras().get(Constants.LINE_ID);

        if (start != null && destination != null && transitType != null) {
            TextView startingStationNameText = (TextView) findViewById(R.id.starting_station_name);
            startingStationNameText.setText(start.getStopName());

            TextView destinationStationNameText = (TextView) findViewById(R.id.destination_station_name);
            destinationStationNameText.setText(destination.getStopName());

            SupportMapFragment mapFragment = SupportMapFragment.newInstance();
            mapFragment.getMapAsync(this);

            getSupportFragmentManager().beginTransaction().add(R.id.map_container, mapFragment).commit();

            nextToArriveDetailsFragment.setTransitType(transitType);
            nextToArriveDetailsFragment.setStartStopId(start.getStopId());
            nextToArriveDetailsFragment.setDestStopId(destination.getStopId());
            if (routeDirectionModel != null)
                nextToArriveDetailsFragment.setRouteId(routeDirectionModel.getRouteId());

            String favKey = Favorite.generateKey(start, destination, transitType, routeDirectionModel);
            currentFavorite = SeptaServiceFactory.getFavoritesService().getFavoriteByKey(this, favKey);
        }

    }

    public void saveAsFavorite(final MenuItem item) {
        if (!item.isEnabled())
            return;

        item.setEnabled(false);

        if (start != null && destination != null && transitType != null) {
            if (currentFavorite == null) {
                final Favorite favorite = new Favorite(start, destination, transitType, routeDirectionModel);
                SaveFavoritesAsyncTask task = new SaveFavoritesAsyncTask(this, new Runnable() {
                    @Override
                    public void run() {
                        item.setEnabled(true);
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        item.setEnabled(true);
                        item.setIcon(R.drawable.ic_favorite_made);
                        currentFavorite = favorite;
                    }
                });

                task.execute(favorite);
            } else {
                new AlertDialog.Builder(this).setCancelable(true).setTitle("Delete Favorite")
                        .setMessage("Are you sure you want to delete the Favorite '" + currentFavorite.getName() + "'?")
                        .setPositiveButton("Delete Favorite", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DeleteFavoritesAsyncTask task = new DeleteFavoritesAsyncTask(NextToArriveResultsActivity.this, new Runnable() {
                                    @Override
                                    public void run() {
                                        item.setEnabled(true);
                                    }
                                }, new Runnable() {
                                    @Override
                                    public void run() {
                                        item.setEnabled(true);
                                        item.setIcon(R.drawable.ic_favorite_available);
                                        currentFavorite = null;
                                    }
                                });

                                task.execute(currentFavorite.getKey());
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        item.setEnabled(true);
                    }
                }).create().show();
            }

        } else item.setEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.favorite_menu, menu);

        if (currentFavorite != null) {
            menu.findItem(R.id.create_favorite).setIcon(R.drawable.ic_favorite_made);
        }
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        MapStyleOptions mapStyle = MapStyleOptions.loadRawResourceStyle(this, R.raw.maps_json_styling);

        googleMap.setMapStyle(mapStyle);

        updateNextToArriveData();

        View mapContainer = findViewById(R.id.map_container);
        ViewGroup.LayoutParams layoutParams =
                bottomSheetLayout.getLayoutParams();
        layoutParams.height = mapContainer.getHeight();
        bottomSheetLayout.setLayoutParams(layoutParams);


        LatLng startingStationLatLng = new LatLng(start.getLatitude(), start.getLongitude());
        LatLng destinationStationLatLng = new LatLng(destination.getLatitude(), destination.getLongitude());
        googleMap.addMarker(new MarkerOptions().position(startingStationLatLng).title(start.getStopName()));
        googleMap.addMarker(new MarkerOptions().position(destinationStationLatLng).title(destination.getStopName()));

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(startingStationLatLng));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(startingStationLatLng);
        builder.include(destinationStationLatLng);
        LatLngBounds bounds = builder.build();
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics())));

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location Permission Granted.");
            Task<Location> locationTask = LocationServices.getFusedLocationProviderClient(this).getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                int permissionCheck = ContextCompat.checkSelfPermission(NextToArriveResultsActivity.this,
                                        Manifest.permission.ACCESS_FINE_LOCATION);
                                googleMap.setMyLocationEnabled(true);
                            } else {
                                Log.d(TAG, "location was null");
                            }
                        }
                    });
        }

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateNextToArriveData();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    private void updateNextToArriveData() {
        if (start != null && destination != null) {

            String routeId = null;
            if (routeDirectionModel != null)
                routeId = routeDirectionModel.getRouteId();
            Call<NextArrivalModelResponse> results = SeptaServiceFactory.getNextArrivalService().getNextArriaval(Integer.parseInt(start.getStopId()), Integer.parseInt(destination.getStopId()), transitType.name(), routeId);
            progressVisibility(View.VISIBLE);

            results.enqueue(new Callback<NextArrivalModelResponse>() {
                @Override
                public void onResponse(Call<NextArrivalModelResponse> call, Response<NextArrivalModelResponse> response) {
                    if (response == null || response.body() == null) {
                        Log.w(TAG, "invalid response from service.");
                    } else {

                        Log.d(TAG, response.toString());
                        Log.d(TAG, response.body().toString());


                        BitmapDescriptor vehicleBitMap = BitmapDescriptorFactory.fromResource(TransitType.valueOf(response.body().getTransType()).getMapMarkerResource());

                        googleMap.clear();

                        LatLng startingStationLatLng = new LatLng(start.getLatitude(), start.getLongitude());
                        LatLng destinationStationLatLng = new LatLng(destination.getLatitude(), destination.getLongitude());
                        googleMap.addMarker(new MarkerOptions().position(startingStationLatLng).title(start.getStopName()));
                        googleMap.addMarker(new MarkerOptions().position(destinationStationLatLng).title(destination.getStopName()));

                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                        getSupportActionBar().setDisplayShowHomeEnabled(true);

                        NextArrivalModelResponseParser parser = new NextArrivalModelResponseParser(response.body());

                        for (Map.Entry<LatLng, NextArrivalModelResponse.NextArrivalRecord> entry : parser.getLatLngMap().entrySet()) {
                            googleMap.addMarker(new MarkerOptions().position(entry.getKey()).title(entry.getValue().getOrigLineTripId()).icon(vehicleBitMap));
                        }

                        for (String routeId : parser.getRouteIdSet()) {
                            KmlLayer layer = MapUtils.getKMLByLineId(NextToArriveResultsActivity.this, googleMap, routeId, transitType);
                            if (layer != null)
                                try {
                                    layer.addLayerToMap();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (XmlPullParserException e) {
                                    e.printStackTrace();
                                }
                        }

                        nextToArriveDetailsFragment.setNextToArriveData(parser);

                        progressVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<NextArrivalModelResponse> call, Throwable t) {
                    progressVisibility(View.GONE);
                    t.printStackTrace();
                }
            });

        }

    }

    private void progressVisibility(int visibility) {
        progressView.setVisibility(visibility);
        progressViewBottom.setVisibility(visibility);
    }


    private static class BottomSheetHandler extends BottomSheetBehavior.BottomSheetCallback implements View.OnClickListener {
        BottomSheetBehavior bottomSheetBehavior;

        int targetState;

        BottomSheetHandler(BottomSheetBehavior bottomSheetBehavior) {
            this.bottomSheetBehavior = bottomSheetBehavior;
            targetState = bottomSheetBehavior.getState();
        }

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                bottomSheetBehavior.setState(targetState);
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

        }

        @Override
        public void onClick(View view) {
            Log.d(TAG, "state is:" + bottomSheetBehavior.getState());
            if (targetState == BottomSheetBehavior.STATE_EXPANDED) {
                targetState = BottomSheetBehavior.STATE_COLLAPSED;
            } else if (targetState == BottomSheetBehavior.STATE_COLLAPSED) {
                targetState = BottomSheetBehavior.STATE_EXPANDED;
            }

            bottomSheetBehavior.setState(targetState);
        }
    }
}




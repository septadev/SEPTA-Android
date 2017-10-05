package org.septa.android.app.nextarrive;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
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
import org.septa.android.app.favorites.EditFavoriteCallBack;
import org.septa.android.app.favorites.EditFavoriteDialogFragment;
import org.septa.android.app.favorites.SaveFavoritesAsyncTask;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Favorite;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalModelResponse;
import org.septa.android.app.support.Consumer;
import org.septa.android.app.support.MapUtils;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by jkampf on 8/3/17.
 */

public class NextToArriveResultsActivity extends AppCompatActivity implements OnMapReadyCallback, EditFavoriteCallBack {
    public static final String TAG = NextToArriveResultsActivity.class.getSimpleName();
    StopModel start;
    StopModel destination;
    TransitType transitType;
    RouteDirectionModel routeDirectionModel;
    private GoogleMap googleMap;
    boolean mapSized = false;
    FrameLayout mapContainerView;
    ViewGroup bottomSheetLayout;
    View rootView;
    View progressView;
    View progressViewBottom;
    View refresh;
    Favorite currentFavorite = null;
    NextToArriveTripView nextToArriveDetailsFragment;
    boolean editFavoritesFlag = false;
    private MarkerOptions startMarker;
    private MarkerOptions destMarker;
    private NextArrivalModelResponseParser parser;
    private int peekHeight = 0;
    private BottomSheetBehavior bottomSheetBehavior;

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

        rootView = findViewById(R.id.rail_next_to_arrive_results_coordinator);

        progressView = findViewById(R.id.progress_view);


        refresh = findViewById(R.id.refresh_button);

        bottomSheetLayout = (ViewGroup) findViewById(R.id.bottomSheetLayout);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        progressViewBottom = findViewById(R.id.progress_view_bottom);


        // Prevent the bottom sheet from being dragged to be opened.  Force it to use the anchor image.
        //BottomSheetHandler myBottomSheetBehaviorCallBack = new BottomSheetHandler(bottomSheetBehavior);
        //bottomSheetBehavior.setBottomSheetCallback(myBottomSheetBehaviorCallBack);
        final View anchor = bottomSheetLayout.findViewById(R.id.bottom_sheet_anchor);
        //anchor.setOnClickListener(myBottomSheetBehaviorCallBack);


        mapContainerView = (FrameLayout) findViewById(R.id.map_container);

        final TextView titleText = (TextView) bottomSheetLayout.findViewById(R.id.title_txt);
        nextToArriveDetailsFragment = (NextToArriveTripView) findViewById(R.id.next_to_arrive_trip_details);
        nextToArriveDetailsFragment.setMaxResults(null);
        nextToArriveDetailsFragment.setResults(5, TimeUnit.HOURS);

        nextToArriveDetailsFragment.setOnFirstElementHeight(new Consumer<Integer>() {
            @Override
            public void accept(Integer var1) {
                titleText.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                anchor.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                int value = anchor.getMeasuredHeight() + anchor.getPaddingTop() + anchor.getPaddingBottom() +
                        titleText.getMeasuredHeight() + titleText.getPaddingTop() +
                        titleText.getPaddingBottom() + var1 + 50;
                if (value != peekHeight) {
                    peekHeight = value;

                    if (!mapSized) {
                        mapSized = true;
                        SupportMapFragment mapFragment = SupportMapFragment.newInstance();

                        ViewGroup.LayoutParams mapContainerLayoutParams = mapContainerView.getLayoutParams();
                        mapContainerLayoutParams.height = rootView.getHeight() - value - mapContainerView.getTop();
                        mapContainerLayoutParams.width = rootView.getWidth();
                        mapContainerView.setLayoutParams(mapContainerLayoutParams);

                        mapFragment.getMapAsync(NextToArriveResultsActivity.this);
                        getSupportFragmentManager().beginTransaction().add(R.id.map_container, mapFragment).commit();

                    }

                }
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        destination = (StopModel) intent.getExtras().get(Constants.DESTINATAION_STATION);
        start = (StopModel) intent.getExtras().get(Constants.STARTING_STATION);
        transitType = (TransitType) intent.getExtras().get(Constants.TRANSIT_TYPE);
        routeDirectionModel = (RouteDirectionModel) intent.getExtras().get(Constants.ROUTE_DIRECTION_MODEL);
        editFavoritesFlag = intent.getExtras().getBoolean(Constants.EDIT_FAVORITES_FLAG, false);


        if (start != null && destination != null && transitType != null) {
            titleText.setText(transitType.getString("nta_results_title", this));
            ((TextView) findViewById(R.id.see_later_text)).setText(transitType.getString("need_to_see", this));

            final TextView startingStationNameText = (TextView) findViewById(R.id.starting_station_name);
            startingStationNameText.setText(start.getStopName());

            final TextView destinationStationNameText = (TextView) findViewById(R.id.destination_station_name);
            destinationStationNameText.setText(destination.getStopName());

            nextToArriveDetailsFragment.setTransitType(transitType);
            nextToArriveDetailsFragment.setStart(start);
            nextToArriveDetailsFragment.setDestination(destination);
            nextToArriveDetailsFragment.setRouteDirectionModel(routeDirectionModel);


            String favKey = Favorite.generateKey(start, destination, transitType, routeDirectionModel);
            currentFavorite = SeptaServiceFactory.getFavoritesService().getFavoriteByKey(this, favKey);

            findViewById(R.id.view_sched_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra(Constants.STARTING_STATION, start);
                    intent.putExtra(Constants.DESTINATAION_STATION, destination);
                    intent.putExtra(Constants.TRANSIT_TYPE, transitType);
                    if (routeDirectionModel != null)
                        intent.putExtra(Constants.ROUTE_DIRECTION_MODEL, routeDirectionModel);
                    else {
                        String directionCode = "1";
                        String directionDescription = "From Center City Philadelphia";
                        if ("Inbound".equalsIgnoreCase(parser.getResults().get(0).getOrigLineDirection())) {
                            directionCode = "0";
                            directionDescription = "To Center City Philadelphia";
                        }
                        RouteDirectionModel rdm = new RouteDirectionModel(parser.getResults().get(0).getOrigRouteId(), parser.getResults().get(0).getOrigRouteName(), parser.getResults().get(0).getOrigRouteName(), directionDescription, directionCode, null);
                        intent.putExtra(Constants.ROUTE_DIRECTION_MODEL, rdm);

                    }
                    setResult(Constants.VIEW_SCHEDULE, intent);
                    finish();
                }
            });

            updateNextToArriveData();
            findViewById(R.id.header).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    View refreshLabel = findViewById(R.id.refresh_label);
                    startingStationNameText.setRight(refreshLabel.getLeft());
                    startingStationNameText.setText(startingStationNameText.getText());
                    destinationStationNameText.setRight(refreshLabel.getLeft());
                    destinationStationNameText.setText(destinationStationNameText.getText());
                }
            });
        }

        if (currentFavorite != null && editFavoritesFlag) {
            setTitle(currentFavorite.getName());
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
                Snackbar snackbar = Snackbar
                        .make(findViewById(R.id.rail_next_to_arrive_results_coordinator), R.string.create_fav_snackbar_text, Snackbar.LENGTH_LONG);

                snackbar.show();
            } else {
                new AlertDialog.Builder(this).setCancelable(true).setTitle(R.string.delete_fav_modal_title)
                        .setMessage(R.string.delete_fav_modal_text)
                        .setPositiveButton(R.string.delete_fav_pos_button, new DialogInterface.OnClickListener() {
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
                        }).setNegativeButton(R.string.delete_fav_neg_button, new DialogInterface.OnClickListener() {
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

        if (!editFavoritesFlag) {
            getMenuInflater().inflate(R.menu.favorite_menu, menu);
            if (currentFavorite != null) {
                menu.findItem(R.id.create_favorite).setIcon(R.drawable.ic_favorite_made);
            }
        } else {
            getMenuInflater().inflate(R.menu.edit_favorites_menu, menu);
        }
        return true;
    }

    public void editFavorite(final MenuItem item) {
        Log.d(TAG, "edit Favorite.");
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        EditFavoriteDialogFragment fragment = EditFavoriteDialogFragment.getInstance(currentFavorite);

        fragment.show(ft, "Dialog");

    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        MapStyleOptions mapStyle = MapStyleOptions.loadRawResourceStyle(this, R.raw.maps_json_styling);

        googleMap.setMapStyle(mapStyle);

        final View mapContainer = findViewById(R.id.map_container);

        LatLng startingStationLatLng = new LatLng(start.getLatitude(), start.getLongitude());
        LatLng destinationStationLatLng = new LatLng(destination.getLatitude(), destination.getLongitude());
        googleMap.addMarker(new MarkerOptions().position(startingStationLatLng).title(start.getStopName()));
        googleMap.addMarker(new MarkerOptions().position(destinationStationLatLng).title(destination.getStopName()));

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(startingStationLatLng));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(startingStationLatLng);
        builder.include(destinationStationLatLng);
        final LatLngBounds bounds = builder.build();

        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                SupportMapFragment mapFragment = SupportMapFragment.newInstance();
                Display mdisp = getWindowManager().getDefaultDisplay();
                Point mdispSize = new Point();
                mdisp.getSize(mdispSize);

                ViewGroup.LayoutParams layoutParams =
                        bottomSheetLayout.getLayoutParams();
                layoutParams.height = rootView.getHeight() - mapContainerView.getTop();
                bottomSheetLayout.setLayoutParams(layoutParams);
                bottomSheetBehavior.setPeekHeight(peekHeight);
                updateMap();
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics())));
            }
        });

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
        final long timestamp = System.currentTimeMillis();
        if (start != null && destination != null) {

            String routeId = null;
            if (routeDirectionModel != null)
                routeId = routeDirectionModel.getRouteId();
            Call<NextArrivalModelResponse> results = SeptaServiceFactory.getNextArrivalService().getNextArriaval(Integer.parseInt(start.getStopId()), Integer.parseInt(destination.getStopId()), transitType.name(), routeId);
            progressVisibility(View.VISIBLE);

            results.enqueue(new Callback<NextArrivalModelResponse>() {
                @Override
                public void onResponse(Call<NextArrivalModelResponse> call, final Response<NextArrivalModelResponse> response) {
                    if (response == null || response.body() == null) {
                        Log.w(TAG, "invalid response from service.");
                    } else {

                        if (System.currentTimeMillis() - timestamp < 1000) {
                            AsyncTask<Long, Void, Void> delayTask = new AsyncTask<Long, Void, Void>() {
                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    updateView(response);
                                    progressVisibility(View.GONE);
                                }

                                @Override
                                protected Void doInBackground(Long... params) {
                                    try {
                                        Thread.sleep(params[0]);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    return null;
                                }
                            }.execute(System.currentTimeMillis() - timestamp);

                        } else {
                            updateView(response);
                            progressVisibility(View.GONE);
                        }
                    }
                }

                private void updateView(Response<NextArrivalModelResponse> response) {
                    Log.d(TAG, response.toString());
                    Log.d(TAG, response.body().toString());

                    LatLng startingStationLatLng = new LatLng(start.getLatitude(), start.getLongitude());
                    LatLng destinationStationLatLng = new LatLng(destination.getLatitude(), destination.getLongitude());
                    startMarker = new MarkerOptions().position(startingStationLatLng).title(start.getStopName());
                    destMarker = new MarkerOptions().position(destinationStationLatLng).title(destination.getStopName());

                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setDisplayShowHomeEnabled(true);
                    parser = new NextArrivalModelResponseParser(response.body());

                    nextToArriveDetailsFragment.setNextToArriveData(parser);

                    if (googleMap != null) {
                        updateMap();
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

    private void updateMap() {
        googleMap.clear();
        googleMap.addMarker(startMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        googleMap.addMarker(destMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        BitmapDescriptor vehicleBitMap = BitmapDescriptorFactory.fromResource(transitType.getMapMarkerResource());
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
    }

    private void progressVisibility(int visibility) {
        progressView.setVisibility(visibility);
        progressViewBottom.setVisibility(visibility);
    }

    @Override
    public void updateFavorite(Favorite var1) {
        currentFavorite = var1;
        setTitle(currentFavorite.getName());
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




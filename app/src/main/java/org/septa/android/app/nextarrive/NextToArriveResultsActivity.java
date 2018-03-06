package org.septa.android.app.nextarrive;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.RelativeLayout;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.data.kml.KmlLayer;

import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.database.DatabaseManager;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.favorites.DeleteFavoritesAsyncTask;
import org.septa.android.app.favorites.EditFavoriteCallBack;
import org.septa.android.app.favorites.EditFavoriteDialogFragment;
import org.septa.android.app.favorites.SaveFavoritesAsyncTask;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Favorite;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalDetails;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalModelResponse;
import org.septa.android.app.support.Consumer;
import org.septa.android.app.support.CrashlyticsManager;
import org.septa.android.app.support.Criteria;
import org.septa.android.app.support.CursorAdapterSupplier;
import org.septa.android.app.support.GeneralUtils;
import org.septa.android.app.support.MapUtils;
import org.septa.android.app.view.TextView;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by jkampf on 8/3/17.
 */

public class NextToArriveResultsActivity extends AppCompatActivity implements OnMapReadyCallback, EditFavoriteCallBack, Runnable, NextToArriveNoResultsFragment.NoResultsFragmentListener {
    public static final String TAG = NextToArriveResultsActivity.class.getSimpleName();
    public static final int REFRESH_DELAY_SECONDS = 30,
            NTA_RESULTS_FOR_NEXT_HOURS = 5;
    private static final String EDIT_FAVORITE_DIALOG_KEY = "EDIT_FAVORITE_DIALOG_KEY",
            NTA_RESULTS_TITLE = "nta_results_title",
            NEED_TO_SEE = "need_to_see";
    private static final String NEW_LINE = "<br/>";
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
    NextToArriveTripView nextToArriveDetailsView;
    boolean editFavoritesFlag = false;
    private MarkerOptions startMarker;
    private MarkerOptions destMarker;
    private NextArrivalModelResponseParser parser;
    private int peekHeight = 0;
    private BottomSheetBehavior bottomSheetBehavior;
    private Handler refreshHandler;
    SupportMapFragment mapFragment;

    Map<String, NextArrivalDetails> details = new HashMap<String, NextArrivalDetails>();

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
        refresh.setContentDescription(getString(R.string.nta_refresh));

        bottomSheetLayout = (ViewGroup) findViewById(R.id.bottomSheetLayout);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        progressViewBottom = findViewById(R.id.progress_view_bottom);


        // Prevent the bottom sheet from being dragged to be opened.  Force it to use the anchor image.
        //bottomSheetBehavior.setBottomSheetCallback(myBottomSheetBehaviorCallBack);
        final View anchor = bottomSheetLayout.findViewById(R.id.bottom_sheet_anchor);
        //anchor.setOnClickListener(myBottomSheetBehaviorCallBack);

        mapContainerView = (FrameLayout) findViewById(R.id.map_container);

        final TextView titleText = (TextView) bottomSheetLayout.findViewById(R.id.title_txt);
        nextToArriveDetailsView = (NextToArriveTripView) findViewById(R.id.next_to_arrive_trip_details);
        nextToArriveDetailsView.setMaxResults(null);
        nextToArriveDetailsView.setResults(NTA_RESULTS_FOR_NEXT_HOURS, TimeUnit.HOURS);  // if this value changes update UI message nta_empty_results

        bottomSheetLayout.setVisibility(View.GONE);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.map_container, new NextToArriveNoResultsFragment());
        ft.commit();

        nextToArriveDetailsView.setOnFirstElementHeight(new Consumer<Integer>() {
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
                        mapFragment = SupportMapFragment.newInstance();

                        ViewGroup.LayoutParams mapContainerLayoutParams = mapContainerView.getLayoutParams();
                        mapContainerLayoutParams.height = rootView.getHeight() - value - mapContainerView.getTop();
                        mapContainerLayoutParams.width = rootView.getWidth();
                        mapContainerView.setLayoutParams(mapContainerLayoutParams);

                        try {
                            getSupportFragmentManager().beginTransaction().add(R.id.map_container, mapFragment).commit();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                        mapFragment.getMapAsync(NextToArriveResultsActivity.this);

                    }

                }
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle bundle = getIntent().getExtras();

        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        } else
            restoreState(bundle);


        if (start != null && destination != null && transitType != null) {
            titleText.setText(transitType.getString(NTA_RESULTS_TITLE, this));
            ((TextView) findViewById(R.id.see_later_text)).setText(transitType.getString(NEED_TO_SEE, this));

            final TextView startingStationNameText = (TextView) findViewById(R.id.starting_station_name);
            startingStationNameText.setText(start.getStopName());

            final TextView destinationStationNameText = (TextView) findViewById(R.id.destination_station_name);
            destinationStationNameText.setText(destination.getStopName());

            nextToArriveDetailsView.setTransitType(transitType);
            nextToArriveDetailsView.setStart(start);
            nextToArriveDetailsView.setDestination(destination);
            nextToArriveDetailsView.setRouteDirectionModel(routeDirectionModel);


            String favKey = Favorite.generateKey(start, destination, transitType, routeDirectionModel);
            currentFavorite = SeptaServiceFactory.getFavoritesService().getFavoriteByKey(this, favKey);

            findViewById(R.id.view_sched_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gotoSchedulesForTarget();
                }
            });

            refreshHandler = new Handler();

            ((RelativeLayout) findViewById(R.id.header)).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
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

        refreshHandler.postDelayed(this, 30 * 1000);

        refresh.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                refreshData();
            }
        });

    }

    private void gotoSchedulesForTarget() {
        Intent intent = new Intent();
        intent.putExtra(Constants.STARTING_STATION, start);
        intent.putExtra(Constants.DESTINATION_STATION, destination);
        intent.putExtra(Constants.TRANSIT_TYPE, transitType);
        if (routeDirectionModel != null)
            intent.putExtra(Constants.ROUTE_DIRECTION_MODEL, routeDirectionModel);

        setResult(Constants.VIEW_SCHEDULE, intent);
        finish();
    }

    private void restoreState(Bundle bundle) {
        destination = (StopModel) bundle.get(Constants.DESTINATION_STATION);
        start = (StopModel) bundle.get(Constants.STARTING_STATION);
        transitType = (TransitType) bundle.get(Constants.TRANSIT_TYPE);
        routeDirectionModel = (RouteDirectionModel) bundle.get(Constants.ROUTE_DIRECTION_MODEL);
        editFavoritesFlag = bundle.getBoolean(Constants.EDIT_FAVORITES_FLAG, false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(Constants.DESTINATION_STATION, destination);
        outState.putSerializable(Constants.STARTING_STATION, start);
        outState.putSerializable(Constants.TRANSIT_TYPE, transitType);
        outState.putSerializable(Constants.ROUTE_DIRECTION_MODEL, routeDirectionModel);
        outState.putSerializable(Constants.EDIT_FAVORITES_FLAG, editFavoritesFlag);
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
                menu.findItem(R.id.create_favorite).setTitle(R.string.nta_favorite_icon_title_remove);
            } else {
                menu.findItem(R.id.create_favorite).setTitle(R.string.nta_favorite_icon_title_create);
            }
        } else {
            getMenuInflater().inflate(R.menu.edit_favorites_menu, menu);
        }
        return true;
    }

    public void editFavorite(final MenuItem item) {
        Log.d(TAG, "edit Favorite.");
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        CrashlyticsManager.log(Log.INFO, TAG, "Creating EditFavoriteDialogFragment for:" + currentFavorite.toString());
        EditFavoriteDialogFragment fragment = EditFavoriteDialogFragment.getInstance(currentFavorite);

        fragment.show(ft, EDIT_FAVORITE_DIALOG_KEY);

    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        bottomSheetLayout.setVisibility(View.VISIBLE);

        this.googleMap = googleMap;
        MapStyleOptions mapStyle = MapStyleOptions.loadRawResourceStyle(this, R.raw.maps_json_styling);

        googleMap.setMapStyle(mapStyle);

        final View mapContainer = findViewById(R.id.map_container);

        final LatLng startingStationLatLng = new LatLng(start.getLatitude(), start.getLongitude());
        final LatLng destinationStationLatLng = new LatLng(destination.getLatitude(), destination.getLongitude());
        googleMap.addMarker(new MarkerOptions().position(startingStationLatLng).title(start.getStopName()));
        googleMap.addMarker(new MarkerOptions().position(destinationStationLatLng).title(destination.getStopName()));

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(startingStationLatLng));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(startingStationLatLng);
        builder.include(destinationStationLatLng);
        final LatLngBounds bounds = builder.build();

        googleMap.setContentDescription("Map displaying selected route between " + start.getStopName() + " and " + destination.getStopName() + ".  Next to arrive details below.");

        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                Display mdisp = getWindowManager().getDefaultDisplay();
                Point mdispSize = new Point();
                mdisp.getSize(mdispSize);


                updateMap();

                try {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics())));
                    ViewGroup.LayoutParams layoutParams =
                            bottomSheetLayout.getLayoutParams();
                    layoutParams.height = rootView.getHeight() - mapContainerView.getTop();
                    bottomSheetLayout.setLayoutParams(layoutParams);
                    bottomSheetBehavior.setPeekHeight(peekHeight);
                } catch (IllegalStateException e) {
                    if (mapContainer.getViewTreeObserver().isAlive()) {
                        mapContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                mapContainer.getViewTreeObserver()
                                        .removeOnGlobalLayoutListener(this);
                                try {
                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics())));
                                } catch (Exception e1) {
                                    // Enough is enough.  Zoom the map to the starting station.
                                    googleMap.moveCamera(CameraUpdateFactory.zoomTo(13));
                                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(startingStationLatLng));
                                }
                                ViewGroup.LayoutParams layoutParams =
                                        bottomSheetLayout.getLayoutParams();
                                layoutParams.height = rootView.getHeight() - mapContainerView.getTop();
                                bottomSheetLayout.setLayoutParams(layoutParams);
                                bottomSheetBehavior.setPeekHeight(peekHeight);
                            }
                        });
                    }
                }

                googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                    @Override
                    public View getInfoWindow(Marker arg0) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        if (startMarker.getTitle().equals(marker.getTitle()) || destMarker.getTitle().equals(marker.getTitle()))
                            return null;

                        TextView title = (TextView) getLayoutInflater().inflate(R.layout.vehicle_map_details, null);
                        NextArrivalDetails detail = details.get(marker.getTitle());

                        if (transitType == TransitType.RAIL) {
                            StringBuilder builder = new StringBuilder("Train: " + marker.getTitle());
                            if (detail != null && detail.getDetails() != null) {
                                builder.append(NEW_LINE)
                                        .append("Status: ");
                                if (detail.getDetails().getNextStop() != null && detail.getDetails().getNextStop().getLate() > 0) {
                                    builder.append(GeneralUtils.getDurationAsLongString(detail.getDetails().getNextStop().getLate(), TimeUnit.MINUTES) + " late.");
                                } else {
                                    builder.append(getString(R.string.nta_on_time));
                                }
                                if (detail.getDetails().getConsist() != null && detail.getDetails().getConsist().size() > 0) {
                                    if (!(detail.getDetails().getConsist().size() == 1 && detail.getDetails().getConsist().get(0).trim().isEmpty())) {
                                        builder.append(NEW_LINE)
                                                .append("# of Train Cars: ")
                                                .append(detail.getDetails().getConsist().size());
                                    }
                                }
                            }
                            title.setHtml(builder.toString());
                        } else {
                            StringBuilder builder = new StringBuilder("Block ID: " + marker.getTitle());
                            if (detail != null && detail.getDetails() != null) {
                                builder.append(NEW_LINE)
                                        .append("Vehicle Number: ")
                                        .append(detail.getDetails().getVehicleId())
                                        .append(NEW_LINE)
                                        .append("Status: ");
                                if (detail.getDetails().getDestination() != null && detail.getDetails().getDestination().getDelay() > 0) {
                                    builder.append(GeneralUtils.getDurationAsLongString(detail.getDetails().getDestination().getDelay(), TimeUnit.MINUTES) + " late.");
                                } else {
                                    builder.append(getString(R.string.nta_on_time));
                                }

                            }
                            title.setHtml(builder.toString());
                        }
                        return title;
                    }
                });
            }
        });

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED)

        {
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

    }

    @Override
    public boolean onSupportNavigateUp() {
        try {
            onBackPressed();
        } catch (Exception e) {
            Log.w(TAG, "Exception on Backpress", e);
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void run() {
        refreshData();
    }

    private void refreshData() {
        final long timestamp = System.currentTimeMillis();
        if (start != null && destination != null) {

            String routeId = null;
            if (routeDirectionModel != null)
                routeId = routeDirectionModel.getRouteId();
            Call<NextArrivalModelResponse> results = SeptaServiceFactory.getNextArrivalService().getNextArrival(Integer.parseInt(start.getStopId()), Integer.parseInt(destination.getStopId()), transitType.name(), routeId);
            progressVisibility(View.VISIBLE);

            results.enqueue(new Callback<NextArrivalModelResponse>() {
                @Override
                public void onResponse(Call<NextArrivalModelResponse> call, final Response<NextArrivalModelResponse> response) {
                    if (response == null || response.body() == null) {
                        onFailure();
                    } else {
                        if (transitType == TransitType.RAIL && response.body().getNextArrivalRecords().size() > 0) {
                            try {
                                //Because RAIL does not need a Route to look up NTA if we want to link back to Schedules we need to figure out the Line and Direction.
                                //So grab the first Line from the results and the first train.  Then look up in the schedule DB the direction code for that train on that line.

                                String routeId = response.body().getNextArrivalRecords().get(0).getOrigRouteId();
                                String trainId = response.body().getNextArrivalRecords().get(0).getOrigLineTripId();

                                List<Criteria> criteriaList = new ArrayList<Criteria>(2);
                                criteriaList.add(new Criteria("routeId", Criteria.Operation.EQ, routeId));
                                criteriaList.add(new Criteria("trainId", Criteria.Operation.EQ, trainId));

                                CursorAdapterSupplier<String> directionIdCursorAdapterSupplier = DatabaseManager.getInstance(NextToArriveResultsActivity.this).getDirectionCodeForTrainOnRoute();
                                Cursor cursor = directionIdCursorAdapterSupplier.getCursor(NextToArriveResultsActivity.this, criteriaList);

                                // Yes we are making a DB query on the UI thread.  But it is a local DB and it is a really fast single table look up.
                                if (cursor.moveToFirst()) {
                                    String directionCode = cursor.getString(0);
                                    routeDirectionModel = new RouteDirectionModel(routeId, response.body().getNextArrivalRecords().get(0).getOrigRouteName(), response.body().getNextArrivalRecords().get(0).getOrigRouteName(), getString(R.string.empty_string), directionCode, null);
                                }
                            } catch (Exception e) {
                                // Swallow the error.  The worst that happens is the route is not selected.
                                Log.e(TAG, "problem determining the direction ID for a rail route.", e);
                            }
                        }


                        // Go through all of the results and kick off a call for Details for each vehichle that has RT data.
                        for (final NextArrivalModelResponse.NextArrivalRecord nextArrivalRecord : response.body().getNextArrivalRecords()) {
                            if (nextArrivalRecord.isOrigRealtime()) {

                                String routeId = null;
                                String dest = destination.getStopId();
                                final String vehicleId;
                                if (transitType != TransitType.RAIL) {
                                    routeId = nextArrivalRecord.getOrigRouteId();
                                    vehicleId = nextArrivalRecord.getOrigVehicleId();
                                } else {
                                    vehicleId = nextArrivalRecord.getOrigLineTripId();
                                }

                                if (nextArrivalRecord.getConnectionStationId() != null) {
                                    dest = nextArrivalRecord.getConnectionStationId().toString();
                                }

                                SeptaServiceFactory.getNextArrivalService().getNextArrivalDetails(dest, routeId, vehicleId).enqueue(new Callback<NextArrivalDetails>() {
                                    @Override
                                    public void onResponse(Call<NextArrivalDetails> call, Response<NextArrivalDetails> response) {
                                        addDetail(response, nextArrivalRecord.getOrigLineTripId());
                                    }

                                    @Override
                                    public void onFailure(Call<NextArrivalDetails> call, Throwable t) {
                                    }
                                });
                            }

                            if (nextArrivalRecord.getConnectionStationId() != null) {
                                if (nextArrivalRecord.isTermRealtime()) {
                                    String routeId = null;
                                    String dest = destination.getStopId();
                                    final String vehicleId;
                                    if (transitType != TransitType.RAIL) {
                                        routeId = nextArrivalRecord.getOrigRouteId();
                                        vehicleId = nextArrivalRecord.getTermVehicleId();
                                    } else {
                                        vehicleId = nextArrivalRecord.getTermLineTripId();
                                    }
                                    SeptaServiceFactory.getNextArrivalService().getNextArrivalDetails(dest, routeId, vehicleId).enqueue(new Callback<NextArrivalDetails>() {
                                        @Override
                                        public void onResponse(Call<NextArrivalDetails> call, Response<NextArrivalDetails> response) {
                                            addDetail(response, nextArrivalRecord.getOrigLineTripId());
                                        }

                                        @Override
                                        public void onFailure(Call<NextArrivalDetails> call, Throwable t) {
                                        }
                                    });
                                }
                            }
                        }

                        parser = new NextArrivalModelResponseParser(response.body());
                        long diff = System.currentTimeMillis() - timestamp;
                        if (diff < 1000 && diff > 0) {
                            AsyncTask<Long, Void, Void> delayTask = new AsyncTask<Long, Void, Void>() {
                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    updateView();
                                    progressVisibility(View.GONE);
                                    refreshHandler.removeCallbacks(NextToArriveResultsActivity.this);
                                    refreshHandler.postDelayed(NextToArriveResultsActivity.this, REFRESH_DELAY_SECONDS * 1000);
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
                            }.execute(diff);

                        } else {
                            updateView();
                            progressVisibility(View.GONE);
                            refreshHandler.removeCallbacks(NextToArriveResultsActivity.this);
                            refreshHandler.postDelayed(NextToArriveResultsActivity.this, REFRESH_DELAY_SECONDS * 1000);
                        }
                    }
                }

                private void updateView() {
                    LatLng startingStationLatLng = new LatLng(start.getLatitude(), start.getLongitude());
                    LatLng destinationStationLatLng = new LatLng(destination.getLatitude(), destination.getLongitude());
                    startMarker = new MarkerOptions().position(startingStationLatLng).title(start.getStopName());
                    destMarker = new MarkerOptions().position(destinationStationLatLng).title(destination.getStopName());

                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setDisplayShowHomeEnabled(true);

                    nextToArriveDetailsView.setNextToArriveData(parser);

                    if (googleMap != null) {
                        updateMap();
                    }
                }

                @Override
                public void onFailure(Call<NextArrivalModelResponse> call, Throwable t) {
                    t.printStackTrace();
                    onFailure();
                }

                private void onFailure() {
                    parser = new NextArrivalModelResponseParser();
                    //updateView();
                    progressVisibility(View.GONE);
                    refreshHandler.removeCallbacks(NextToArriveResultsActivity.this);
                    refreshHandler.postDelayed(NextToArriveResultsActivity.this, REFRESH_DELAY_SECONDS * 1000);
                    //SeptaServiceFactory.displayWebServiceError(findViewById(R.id.rail_next_to_arrive_results_coordinator), NextToArriveResultsActivity.this);
                }
            });

        }

    }

    private void addDetail(Response<NextArrivalDetails> response, String origVehicleId) {
        details.put(origVehicleId, response.body());
    }

    private void updateMap() {
        googleMap.clear();
        googleMap.addMarker(startMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        googleMap.addMarker(destMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        BitmapDescriptor vehicleBitMap = BitmapDescriptorFactory.fromResource(transitType.getMapMarkerResource());
        for (Map.Entry<LatLng, NextArrivalModelResponse.NextArrivalRecord> entry : parser.getOrigLatLngMap().entrySet()) {
            googleMap.addMarker(new MarkerOptions().position(entry.getKey()).title(entry.getValue().getOrigLineTripId()).icon(vehicleBitMap));
        }

        for (Map.Entry<LatLng, NextArrivalModelResponse.NextArrivalRecord> entry : parser.getTermLatLngMap().entrySet()) {
            googleMap.addMarker(new MarkerOptions().position(entry.getKey()).title(entry.getValue().getTermLineTripId()).icon(vehicleBitMap));
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

    @Override
    public void viewSchedulesClicked() {
        gotoSchedulesForTarget();
    }

}




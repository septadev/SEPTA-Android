package org.septa.android.app.transitview;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.data.kml.KmlLayer;

import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.database.DatabaseManager;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.favorites.DeleteFavoritesAsyncTask;
import org.septa.android.app.favorites.SaveFavoritesAsyncTask;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.TransitViewFavorite;
import org.septa.android.app.services.apiinterfaces.model.TransitViewModelResponse;
import org.septa.android.app.support.CursorAdapterSupplier;
import org.septa.android.app.support.MapUtils;
import org.septa.android.app.support.RouteModelComparator;
import org.septa.android.app.view.TextView;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.septa.android.app.transitview.TransitViewUtils.isTrolley;

public class TransitViewResultsActivity extends AppCompatActivity implements Runnable, TransitViewLinePickerFragment.TransitViewLinePickerListener, OnMapReadyCallback, TransitViewVehicleDetailsInfoWindowAdapter.TransitViewVehicleDetailsInfoWindowAdapterListener {

    private static final String TAG = TransitViewResultsActivity.class.getSimpleName();

    private RouteDirectionModel firstRoute, secondRoute, thirdRoute;
    private Set<TransitViewModelResponse.TransitViewRecord> firstRoutesResults, secondRoutesResults, thirdRoutesResults;
    private String routeIds;
    private boolean isAFavorite = false;

    // data refresh
    private Handler refreshHandler;
    private static final int REFRESH_DELAY_SECONDS = 30;
    private TransitViewModelResponseParser parser;
    private Map<String, TransitViewModelResponse.TransitViewRecord> details = new HashMap<>();
    private boolean refreshed = false; // used to distinguish refresh from a change in route selection

    // layout variables
    private TextView addLabel, firstRouteLabel, secondRouteLabel, thirdRouteLabel;
    private FrameLayout mapContainerView;
    private View progressView;
    private boolean mapSized = false;
    private SupportMapFragment mapFragment;
    private GoogleMap googleMap;
    public static final String VEHICLE_MARKER_KEY_DELIM = "_";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeActivity(savedInstanceState);

        setTitle(R.string.transit_view);
        setContentView(R.layout.activity_transitview_results);

        // initialize view
        initializeView();

        // initialize route labels
        updateRouteLabels(firstRoute, secondRoute, thirdRoute);

        // set up automatic refresh
        if (firstRoute != null) {
            refreshHandler = new Handler();
            refreshHandler.postDelayed(this, REFRESH_DELAY_SECONDS * 1000);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        invalidateOptionsMenu();
        menu.clear();

        getMenuInflater().inflate(R.menu.favorite_menu, menu);
        if (isAFavorite) {
            menu.findItem(R.id.create_favorite).setIcon(R.drawable.ic_favorite_made);
            menu.findItem(R.id.create_favorite).setTitle(R.string.nta_favorite_icon_title_remove);
        } else {
            menu.findItem(R.id.create_favorite).setTitle(R.string.nta_favorite_icon_title_create);
        }

        // TODO: should there be an "edit" feature for transitview favorites, if so update onOptionsItemSelected

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_favorite:
                Log.d(TAG, "Creating a favorite for TransitView routes: " + routeIds);
                saveAsFavorite(item);
                return true;
            case R.id.refresh_results:
                refreshed = true;
                refreshData();
                return true;
//                TODO: add logic for editing a favorite
//            case R.id.edit_favorite:
//                editFavorite(item);
//                return true;
            default:
                return super.onOptionsItemSelected(item);
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
    public void run() {
        refreshed = true;
        refreshData();
    }

    @Override
    public void selectFirstRoute(RouteDirectionModel route) {
        Log.e(TAG, "Invalid attempt to select the first route from the TransitViewResultsActivity -- going back to TransitView route picker");

        Toast.makeText(this, R.string.transitview_add_route, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void selectSecondRoute(RouteDirectionModel route) {
        // sort the routes and append a null route to the end
        List<RouteDirectionModel> selectedRoutes = new ArrayList<>();
        selectedRoutes.add(firstRoute);
        selectedRoutes.add(route);
        Collections.sort(selectedRoutes, new RouteModelComparator());
        selectedRoutes.add(null);

        updateRouteLabels(selectedRoutes.get(0), selectedRoutes.get(1), selectedRoutes.get(2));
    }

    @Override
    public void selectThirdRoute(RouteDirectionModel route) {
        // sort the routes
        List<RouteDirectionModel> selectedRoutes = new ArrayList<>();
        selectedRoutes.add(firstRoute);
        selectedRoutes.add(secondRoute);
        selectedRoutes.add(route);
        Collections.sort(selectedRoutes, new RouteModelComparator());

        updateRouteLabels(selectedRoutes.get(0), selectedRoutes.get(1), selectedRoutes.get(2));
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        MapStyleOptions mapStyle = MapStyleOptions.loadRawResourceStyle(this, R.raw.maps_json_styling);
        googleMap.setMapStyle(mapStyle);

        // hide navigation options and disable rotation
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);

        // move camera to city hall to speed up zoom process
        final LatLng cityHall = new LatLng(39.9517999, -75.1633285);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(cityHall));

        // default map zoom to show KML of all routes using builder.include()
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        // add to map of vehicle markers
        details.clear();
        for (String routeId : routeIds.split(",")) {
            for (Map.Entry<TransitViewModelResponse.TransitViewRecord, LatLng> entry : parser.getResultsForRoute(routeId).entrySet()) {
                String vehicleMarkerKey = new StringBuilder(routeId).append(VEHICLE_MARKER_KEY_DELIM).append(entry.getKey().getVehicleId()).toString();
                details.put(vehicleMarkerKey, entry.getKey());

                // map must include this vehicle when automatically moving
                builder.include(entry.getValue());
            }
        }

        final LatLngBounds bounds = builder.build();

        googleMap.setContentDescription("Map displaying TransitView routes: " + routeIds);

        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                Display mdisp = getWindowManager().getDefaultDisplay();
                Point mdispSize = new Point();
                mdisp.getSize(mdispSize);

                updateMap();

                try {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics())));
                    // TODO: calculate map height
//                    ViewGroup.LayoutParams layoutParams =
//                            bottomSheetLayout.getLayoutParams();
//                    layoutParams.height = rootView.getHeight() - mapContainerView.getTop();
//                    bottomSheetLayout.setLayoutParams(layoutParams);
//                    bottomSheetBehavior.setPeekHeight(peekHeight);
                } catch (IllegalStateException e) {
                    if (mapContainerView.getViewTreeObserver().isAlive()) {
                        mapContainerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                mapContainerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                try {
                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics())));
                                } catch (Exception e1) {
                                    Log.e(TAG, "Failed to move camera, defaulting map zoom to Philadelphia City Hall");

                                    // TODO: where should I move map to on default?
                                    googleMap.moveCamera(CameraUpdateFactory.zoomTo(13));
                                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(cityHall));
                                }
                                // TODO: calculate map height
//                                ViewGroup.LayoutParams layoutParams =
//                                        bottomSheetLayout.getLayoutParams();
//                                layoutParams.height = rootView.getHeight() - mapContainerView.getTop();
//                                bottomSheetLayout.setLayoutParams(layoutParams);
//                                bottomSheetBehavior.setPeekHeight(peekHeight);
                            }
                        });
                    }
                }

                TransitViewVehicleDetailsInfoWindowAdapter adapter = new TransitViewVehicleDetailsInfoWindowAdapter(TransitViewResultsActivity.this);
                googleMap.setInfoWindowAdapter(adapter);
            }
        });

        // hide progress view and show map
        progressView.setVisibility(View.GONE);
        mapContainerView.setVisibility(View.VISIBLE);
//        alertsView.setVisibility(View.VISIBLE); // TODO: show alerts

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location Permission Granted.");
            Task<Location> locationTask = LocationServices.getFusedLocationProviderClient(this).getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                int permissionCheck = ContextCompat.checkSelfPermission(TransitViewResultsActivity.this,
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
    public TransitViewModelResponse.TransitViewRecord getVehicleRecord(String vehicleRecordKey) {
        return details.get(vehicleRecordKey);
    }

    private void initializeActivity(@Nullable Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Bundle bundle = getIntent().getExtras();

        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        } else {
            restoreState(bundle);
        }

        checkIfAFavorite();
    }

    private void restoreState(Bundle bundle) {
        firstRoute = (RouteDirectionModel) bundle.get(TransitViewFragment.TRANSITVIEW_ROUTE_FIRST);
        secondRoute = (RouteDirectionModel) bundle.get(TransitViewFragment.TRANSITVIEW_ROUTE_SECOND);
        thirdRoute = (RouteDirectionModel) bundle.get(TransitViewFragment.TRANSITVIEW_ROUTE_THIRD);
    }

    private void initializeView() {
        mapContainerView = findViewById(R.id.map_container);
//        alertsView = findViewById(R.id.transitview_alerts); TODO: initialize alerts view
        progressView = findViewById(R.id.progress_view);
        firstRouteLabel = findViewById(R.id.first_route_delete);
        secondRouteLabel = findViewById(R.id.second_route_delete);
        thirdRouteLabel = findViewById(R.id.third_route_delete);
        addLabel = findViewById(R.id.header_add_label);

        firstRouteLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: prompt to delete

                if (secondRoute != null || thirdRoute != null) {
                    // delete first route
                    updateRouteLabels(secondRoute, thirdRoute, null);
                } else {
                    // take user back to picker screen
                    finish();
                }
            }
        });

        secondRouteLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: prompt to delete

                // delete second route
                updateRouteLabels(firstRoute, thirdRoute, null);
            }
        });

        thirdRouteLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: prompt to delete

                // delete third route
                updateRouteLabels(firstRoute, secondRoute, null);
            }
        });

        addLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get which routes have already been selected
                String[] selectedRoutes = new String[2];
                if (firstRoute != null) {
                    selectedRoutes[0] = firstRoute.getRouteId();
                }
                if (secondRoute != null) {
                    selectedRoutes[1] = secondRoute.getRouteId();
                }

                // pop-up transitview route picker dialog
                DatabaseManager dbManager = DatabaseManager.getInstance(TransitViewResultsActivity.this);
                CursorAdapterSupplier<RouteDirectionModel> busRouteCursorAdapterSupplier = dbManager.getBusNoDirectionRouteCursorAdapterSupplier(),
                        trolleyRouteCursorAdapterSupplier = dbManager.getTrolleyNoDirectionRouteCursorAdapterSupplier();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                TransitViewLinePickerFragment newFragment = TransitViewLinePickerFragment.newInstance(busRouteCursorAdapterSupplier, trolleyRouteCursorAdapterSupplier, selectedRoutes);
                newFragment.show(ft, "line_picker");
            }
        });
    }

    private void checkIfAFavorite() {
        String favoriteKey = TransitViewFavorite.generateKey(firstRoute, secondRoute, thirdRoute);
        if (SeptaServiceFactory.getFavoritesService().getFavoriteByKey(this, favoriteKey) != null) {
            isAFavorite = true;
        } else {
            isAFavorite = false;
        }
        // TODO: refresh menu toolbar
        supportInvalidateOptionsMenu();
    }

    private void updateRouteLabels(@NonNull RouteDirectionModel first, RouteDirectionModel second, RouteDirectionModel third) {
        this.firstRoute = first;
        this.secondRoute = second;
        this.thirdRoute = third;

        StringBuilder routeIdBuilder = new StringBuilder(firstRoute.getRouteId());

        firstRouteLabel.setText(firstRoute.getRouteId());

        if (secondRoute != null) {
            routeIdBuilder.append(",").append(secondRoute.getRouteId());
            secondRouteLabel.setText(secondRoute.getRouteId());
            secondRouteLabel.setVisibility(View.VISIBLE);
        } else {
            secondRouteLabel.setText(null);
            secondRouteLabel.setVisibility(View.GONE);
        }

        if (thirdRoute != null) {
            // update third route label
            routeIdBuilder.append(",").append(thirdRoute.getRouteId());
            thirdRouteLabel.setText(thirdRoute.getRouteId());
            thirdRouteLabel.setVisibility(View.VISIBLE);

            // disable add button
            disableView(addLabel);

        } else {
            thirdRouteLabel.setText(null);
            thirdRouteLabel.setVisibility(View.GONE);

            // make add button clickable
            activateView(addLabel);
        }
        routeIds = routeIdBuilder.toString();

        checkIfAFavorite();

        refreshed = false;
        refreshData();
    }

    private void disableView(View view) {
        view.setAlpha((float) .3);
        view.setClickable(false);
    }

    private void activateView(View view) {
        view.setAlpha(1);
        view.setClickable(true);
    }

    private void refreshData() {
        Log.d(TAG, "Refreshing TransitView data for " + routeIds);

        // show progress view and hide everything else
        progressView.setVisibility(View.VISIBLE);
        mapContainerView.setVisibility(View.GONE);
//        alertsView.setVisibility(View.GONE); // TODO: hide alerts

        SeptaServiceFactory.getTransitViewService().getTransitViewResults(routeIds).enqueue(new Callback<TransitViewModelResponse>() {
            @Override
            public void onResponse(Call<TransitViewModelResponse> call, @NonNull Response<TransitViewModelResponse> response) {
                if (response.body() != null) {

                    parser = new TransitViewModelResponseParser(response.body());

                    firstRoutesResults = parser.getResultsForRoute(firstRoute.getRouteId()).keySet();
                    Log.d(TAG, firstRoutesResults.toString());

                    if (secondRoute != null) {
                        secondRoutesResults = parser.getResultsForRoute(secondRoute.getRouteId()).keySet();
                        Log.d(TAG, secondRoutesResults.toString());
                    }

                    if (thirdRoute != null) {
                        thirdRoutesResults = parser.getResultsForRoute(thirdRoute.getRouteId()).keySet();
                        Log.d(TAG, thirdRoutesResults.toString());
                    }

                    prepareToDrawMap();
                } else {
                    Log.e(TAG, "Null response body when fetching TransitVIew results for: " + routeIds);
                    failure();
                }
            }

            @Override
            public void onFailure(Call<TransitViewModelResponse> call, Throwable t) {
                Log.e(TAG, "No TransitView results found for the routes: " + routeIds, t);
                failure();
            }

            private void failure() {
                progressView.setVisibility(View.GONE);
                refreshHandler.removeCallbacks(TransitViewResultsActivity.this);
                refreshHandler.postDelayed(TransitViewResultsActivity.this, REFRESH_DELAY_SECONDS * 1000);
                refreshed = false;
                showNoResultsFoundErrorMessage(); // TODO: how should this be handled
            }
        });
    }

    private void prepareToDrawMap() {
        if (!mapSized) {
            mapSized = true;
            mapFragment = SupportMapFragment.newInstance();

            // TODO: calculate map size after drawing alerts
//                    ViewGroup.LayoutParams mapContainerLayoutParams = mapContainerView.getLayoutParams();
//                    mapContainerLayoutParams.height = rootView.getHeight() - value - mapContainerView.getTop();
//                    mapContainerLayoutParams.width = rootView.getWidth();
//                    mapContainerView.setLayoutParams(mapContainerLayoutParams);

            try {
                getSupportFragmentManager().beginTransaction().add(R.id.map_container, mapFragment).commit();
            } catch (IllegalStateException e) {
                Log.e(TAG, e.toString());
            }
            mapFragment.getMapAsync(TransitViewResultsActivity.this);
        } else if (refreshed) {
            updateMap();
            refreshed = false;
        } else {
            onMapReady(googleMap);
        }
    }

    private void updateMap() {
        googleMap.clear();

        for (String routeId : routeIds.split(",")) {
            TransitType transitType = TransitType.BUS;
            int transitTypeDrawableId = R.drawable.ic_bus_blue_small;
            if (isTrolley(routeId)) {
                transitType = TransitType.TROLLEY;
                transitTypeDrawableId = R.drawable.ic_trolley_blue_small;
            }

            // redraw all vehicles
            for (Map.Entry<TransitViewModelResponse.TransitViewRecord, LatLng> entry : parser.getResultsForRoute(routeId).entrySet()) {
                // create directional icon with bus or trolley
                BitmapDescriptor vehicleBitMap = TransitViewUtils.getDirectionalIconForTransitType(this, transitTypeDrawableId, entry.getKey().getHeading());
                String vehicleMarkerKey = new StringBuilder(routeId).append(VEHICLE_MARKER_KEY_DELIM).append(entry.getKey().getVehicleId()).toString();
                googleMap.addMarker(new MarkerOptions()
                        .position(entry.getValue())
                        .title(vehicleMarkerKey)
                        .anchor((float) 0.5, (float) 0.5)
                        .icon(vehicleBitMap));
            }

            // redraw route on map
            KmlLayer layer = MapUtils.getKMLByLineId(TransitViewResultsActivity.this, googleMap, routeId, transitType);
            if (layer != null) {
                try {
                    layer.addLayerToMap();
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                } catch (XmlPullParserException e) {
                    Log.e(TAG, e.toString());
                }
            }
        }

        // hide error message in case connection regained
        progressView.setVisibility(View.GONE);
        mapContainerView.setVisibility(View.VISIBLE);
//        alertsView.setVisibility(View.VISIBLE); // TODO: show alerts
    }

    private void showNoResultsFoundErrorMessage() {
        // show error message and hide
        Log.e(TAG, "No TransitView results found");
    }

    private void saveAsFavorite(final MenuItem item) {
        if (!item.isEnabled()) {
            return;
        }

        item.setEnabled(false);

        // favorite a transitview selection
        if (firstRoute != null) {
            if (isAFavorite) {
                // prompt to delete favorite
                new AlertDialog.Builder(this).setCancelable(true).setTitle(R.string.delete_fav_modal_title)
                        .setMessage(R.string.delete_fav_modal_text)
                        .setPositiveButton(R.string.delete_fav_pos_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DeleteFavoritesAsyncTask task = new DeleteFavoritesAsyncTask(TransitViewResultsActivity.this, new Runnable() {
                                    @Override
                                    public void run() {
                                        item.setEnabled(true);
                                    }
                                }, new Runnable() {
                                    @Override
                                    public void run() {
                                        item.setEnabled(true);
                                        item.setIcon(R.drawable.ic_favorite_available);
                                        isAFavorite = false;
                                    }
                                });

                                task.execute(TransitViewFavorite.generateKey(firstRoute, secondRoute, thirdRoute));
                            }
                        }).setNegativeButton(R.string.delete_fav_neg_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        item.setEnabled(true);
                    }
                }).create().show();
            } else {
                // save new favorite
                final TransitViewFavorite favorite = new TransitViewFavorite(firstRoute, secondRoute, thirdRoute);
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
                        isAFavorite = true;
                    }
                });

                task.execute(favorite);

                Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_transitview_results), R.string.create_transitview_fav_snackbar, Snackbar.LENGTH_LONG);
                snackbar.show();
            }

        } else {
            item.setEnabled(true);
        }
    }

}

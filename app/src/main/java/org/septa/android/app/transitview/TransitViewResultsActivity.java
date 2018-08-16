package org.septa.android.app.transitview;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.google.maps.android.data.kml.KmlLayer;

import org.septa.android.app.BaseActivity;
import org.septa.android.app.R;
import org.septa.android.app.database.DatabaseManager;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.favorites.DeleteFavoritesAsyncTask;
import org.septa.android.app.favorites.edit.RenameFavoriteDialogFragment;
import org.septa.android.app.favorites.edit.RenameFavoriteListener;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Alerts;
import org.septa.android.app.services.apiinterfaces.model.Favorite;
import org.septa.android.app.services.apiinterfaces.model.TransitViewFavorite;
import org.septa.android.app.services.apiinterfaces.model.TransitViewModelResponse;
import org.septa.android.app.support.AnalyticsManager;
import org.septa.android.app.support.CrashlyticsManager;
import org.septa.android.app.support.CursorAdapterSupplier;
import org.septa.android.app.support.MapUtils;
import org.septa.android.app.support.RouteModelComparator;
import org.septa.android.app.systemstatus.SystemStatusState;
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

import static org.septa.android.app.favorites.edit.RenameFavoriteDialogFragment.EDIT_FAVORITE_DIALOG_KEY;
import static org.septa.android.app.transitview.TransitViewUtils.isTrolley;

public class TransitViewResultsActivity extends BaseActivity implements Runnable, TransitViewLinePickerFragment.TransitViewLinePickerListener, OnMapReadyCallback, TransitViewVehicleDetailsInfoWindowAdapter.TransitViewVehicleDetailsInfoWindowAdapterListener, RenameFavoriteListener, TransitViewRouteCard.TransitViewRouteCardListener {

    private static final String TAG = TransitViewResultsActivity.class.getSimpleName();

    private RouteDirectionModel firstRoute, secondRoute, thirdRoute;
    private String routeIds;
    private String activeRouteId;
    private boolean isAFavorite = false;
    private TransitViewFavorite currentFavorite = null;

    // data refresh
    private Handler refreshHandler;
    private static final int REFRESH_DELAY_SECONDS = 30;
    private TransitViewModelResponseParser parser;
    private Map<String, TransitViewModelResponse.TransitViewRecord> vehicleDetailsMap = new HashMap<>();
    private boolean refreshed = false; // used to distinguish refresh from a change in route selection

    // layout variables
    private LinearLayout routeCardContainer;
    private TransitViewRouteCard firstRouteCard, secondRouteCard = null, thirdRouteCard = null;
    private ImageView addLabel;
    private FrameLayout mapContainerView;
    private View progressView;
    private boolean firstRun = false;
    private SupportMapFragment mapFragment;
    private TextView noResultsMsg;
    private GoogleMap googleMap;
    public static final String VEHICLE_MARKER_KEY_DELIM = "_";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeActivity(savedInstanceState);

        setContentView(R.layout.activity_transitview_results);

        // initialize view
        initializeView();

        // initialize route labels
        updateRouteCards(firstRoute, secondRoute, thirdRoute);

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

        if (isAFavorite) {
            getMenuInflater().inflate(R.menu.edit_favorites_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.favorite_menu, menu);
        }

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
            case R.id.edit_favorite:
                editFavorite(item);
                return true;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int unmaskedRequestCode = requestCode & 0x0000ffff;

        // TODO: put push notifications back in
//        if (unmaskedRequestCode == Constants.SYSTEM_STATUS_REQUEST) {
//            if (resultCode == Constants.VIEW_NOTIFICATION_MANAGEMENT) {
//                goToNotificationsManagement();
//            }
//        }
    }

    @Override
    public void run() {
        refreshed = true;
        refreshData();
    }

    @Override
    public void removeRoute(int routeToRemove) {
        switch (routeToRemove) {
            case 3:
                // prompt to remove route
                new AlertDialog.Builder(this).setCancelable(true)
                        .setTitle(getString(R.string.transitview_remove_route_title, thirdRoute.getRouteId()))
                        .setMessage(R.string.transitview_remove_route_text)
                        .setPositiveButton(R.string.transitview_remove_route_pos_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                activeRouteId = firstRoute.getRouteId();

                                refreshed = false;

                                // delete third route
                                updateRouteCards(firstRoute, secondRoute, null);
                            }
                        })
                        .setNegativeButton(R.string.transitview_remove_route_neg_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).create().show();
                break;
            case 2:
                // prompt to remove route
                new AlertDialog.Builder(this).setCancelable(true)
                        .setTitle(getString(R.string.transitview_remove_route_title, secondRoute.getRouteId()))
                        .setMessage(R.string.transitview_remove_route_text)
                        .setPositiveButton(R.string.transitview_remove_route_pos_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                activeRouteId = firstRoute.getRouteId();

                                refreshed = false;

                                // delete second route
                                updateRouteCards(firstRoute, thirdRoute, null);
                            }
                        })
                        .setNegativeButton(R.string.transitview_remove_route_neg_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).create().show();
                break;
            case 1:
            default:
                // prompt to remove route
                new AlertDialog.Builder(this).setCancelable(true)
                        .setTitle(getString(R.string.transitview_remove_route_title, firstRoute.getRouteId()))
                        .setMessage(R.string.transitview_remove_route_text)
                        .setPositiveButton(R.string.transitview_remove_route_pos_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (secondRoute != null) {
                                    activeRouteId = secondRoute.getRouteId();

                                    refreshed = false;

                                    // delete first route
                                    updateRouteCards(secondRoute, thirdRoute, null);
                                } else {
                                    // take user back to picker screen
                                    finish();
                                }
                            }
                        })
                        .setNegativeButton(R.string.transitview_remove_route_neg_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).create().show();
                break;
        }
    }

    @Override
    public void addFirstRoute(RouteDirectionModel route) {
        Log.e(TAG, "Invalid attempt to select the first route from the TransitViewResultsActivity -- going back to TransitView route picker");

        Toast.makeText(this, R.string.transitview_add_route, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void addSecondRoute(RouteDirectionModel route) {
        // sort the routes and append a null route to the end
        List<RouteDirectionModel> selectedRoutes = new ArrayList<>();
        selectedRoutes.add(firstRoute);
        selectedRoutes.add(route);
        Collections.sort(selectedRoutes, new RouteModelComparator());
        selectedRoutes.add(null);

        activeRouteId = route.getRouteId();
        refreshed = false;
        updateRouteCards(selectedRoutes.get(0), selectedRoutes.get(1), selectedRoutes.get(2));
    }

    @Override
    public void addThirdRoute(RouteDirectionModel route) {
        // sort the routes
        List<RouteDirectionModel> selectedRoutes = new ArrayList<>();
        selectedRoutes.add(firstRoute);
        selectedRoutes.add(secondRoute);
        selectedRoutes.add(route);
        Collections.sort(selectedRoutes, new RouteModelComparator());

        activeRouteId = route.getRouteId();
        refreshed = false;
        updateRouteCards(selectedRoutes.get(0), selectedRoutes.get(1), selectedRoutes.get(2));
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG, "OnMapReady");

        this.googleMap = googleMap;
        MapStyleOptions mapStyle = MapStyleOptions.loadRawResourceStyle(this, R.raw.maps_json_styling);
        googleMap.setMapStyle(mapStyle);

        // hide navigation options and disable rotation
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);

        // move camera to city hall to speed up zoom process
        if (!firstRun) {
            final LatLng cityHall = new LatLng(39.9517999, -75.1633285);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(cityHall));
            googleMap.moveCamera(CameraUpdateFactory.zoomTo(13));
            firstRun = true;
        }

        // default map zoom to show KML of all routes using builder.include()
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        // map must include vehicles on active route when automatically moving camera
        Map<TransitViewModelResponse.TransitViewRecord, LatLng> results = parser.getResultsForRoute(activeRouteId);
        if (results != null) {
            for (Map.Entry<TransitViewModelResponse.TransitViewRecord, LatLng> entry : results.entrySet()) {
                builder.include(entry.getValue());
            }
        } else {
            CrashlyticsManager.log(Log.ERROR, TAG, "Could not get results for active route: " + activeRouteId);
            Log.e(TAG, "Could not get results for active route: " + activeRouteId + ". Moving camera to default location over Philly");
            final LatLng northPhilly = new LatLng(39.979822, -75.157954);
            final LatLng southPhilly = new LatLng(39.925151, -75.170484);
            builder.include(northPhilly);
            builder.include(southPhilly);
        }
        final LatLngBounds bounds = builder.build();

        googleMap.setContentDescription("Map displaying TransitView routes: " + routeIds);
        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                Log.d(TAG, "OnMapLoaded");

                Display mdisp = getWindowManager().getDefaultDisplay();
                Point mdispSize = new Point();
                mdisp.getSize(mdispSize);

                updateMap();

                try {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics())));
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

                                    final LatLng cityHall = new LatLng(39.9517999, -75.1633285);

                                    googleMap.moveCamera(CameraUpdateFactory.zoomTo(13));
                                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(cityHall));
                                }
                            }
                        });
                    }
                }

                // custom vehicle vehicleDetailsMap info window
                TransitViewVehicleDetailsInfoWindowAdapter adapter = new TransitViewVehicleDetailsInfoWindowAdapter(TransitViewResultsActivity.this);
                googleMap.setInfoWindowAdapter(adapter);
            }
        });

        // hide progress view and show map
        noResultsMsg.setVisibility(View.GONE);
        progressView.setVisibility(View.GONE);
        mapContainerView.setVisibility(View.VISIBLE);

    }

    @Override
    public TransitViewModelResponse.TransitViewRecord getVehicleRecord(String vehicleRecordKey) {
        return vehicleDetailsMap.get(vehicleRecordKey);
    }

    @Override
    public void updateFavorite(Favorite favorite) {
        if (favorite instanceof TransitViewFavorite) {
            currentFavorite = (TransitViewFavorite) favorite;
            isAFavorite = true;
            renameFavorite(currentFavorite);

            Toast.makeText(this, R.string.create_transitview_favorite, Toast.LENGTH_LONG).show();
        } else {
            Log.e(TAG, "Attempted to save invalid Favorite type");
        }
    }

    @Override
    public void renameFavorite(Favorite favorite) {
        setTitle(favorite.getName());
        invalidateOptionsMenu();
    }

    @Override
    public void favoriteCreationFailed() {
        Toast.makeText(this, R.string.create_transitview_favorite_failed, Toast.LENGTH_LONG).show();
    }

    @NonNull
    @Override
    public String getActiveRouteId() {
        return activeRouteId;
    }

    @Override
    public void changeActiveRoute(String oldActiveRoute, String newActiveRoute) {
        activeRouteId = newActiveRoute;
        refreshed = true; // simply refresh data without moving camera

        updateRouteCards(firstRoute, secondRoute, thirdRoute);
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

        activeRouteId = firstRoute.getRouteId();
    }

    private void initializeView() {
        mapContainerView = findViewById(R.id.map_container);
        noResultsMsg = findViewById(R.id.no_results_msg);
        progressView = findViewById(R.id.progress_view);
        addLabel = findViewById(R.id.button_add);
        routeCardContainer = findViewById(R.id.header_routes_buttons);

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
        Favorite favoriteTemp = SeptaServiceFactory.getFavoritesService().getFavoriteByKey(this, favoriteKey);
        if (favoriteTemp != null && favoriteTemp instanceof TransitViewFavorite) {
            isAFavorite = true;
            currentFavorite = (TransitViewFavorite) favoriteTemp;
            setTitle(currentFavorite.getName());
        } else {
            isAFavorite = false;
            currentFavorite = null;
            setTitle(R.string.transit_view);
        }
        supportInvalidateOptionsMenu();
    }

    private void updateRouteCards(@NonNull RouteDirectionModel first, RouteDirectionModel second, RouteDirectionModel third) {
        routeCardContainer.removeAllViews();

        this.firstRoute = first;
        this.secondRoute = second;
        this.thirdRoute = third;

        StringBuilder routeIdBuilder = new StringBuilder(firstRoute.getRouteId());

        firstRouteCard = new TransitViewRouteCard(TransitViewResultsActivity.this, firstRoute, 1);
        firstRouteCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firstRouteCard.activateCard();
                if (secondRouteCard != null) {
                    secondRouteCard.deactivateCard();
                }
                if (thirdRouteCard != null) {
                    thirdRouteCard.deactivateCard();
                }

                activeRouteId = firstRoute.getRouteId();

                refreshed = false;
                refreshData();
            }
        });
        routeCardContainer.addView(firstRouteCard);

        if (activeRouteId.equalsIgnoreCase(firstRoute.getRouteId())) {
            firstRouteCard.activateCard();
        } else {
            firstRouteCard.deactivateCard();
        }

        if (secondRoute != null) {
            routeIdBuilder.append(",").append(secondRoute.getRouteId());

            secondRouteCard = new TransitViewRouteCard(TransitViewResultsActivity.this, secondRoute, 2);
            secondRouteCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    firstRouteCard.deactivateCard();
                    secondRouteCard.activateCard();
                    if (thirdRouteCard != null) {
                        thirdRouteCard.deactivateCard();
                    }

                    activeRouteId = secondRoute.getRouteId();

                    refreshed = false;
                    refreshData();
                }
            });
            routeCardContainer.addView(secondRouteCard);

            if (activeRouteId.equalsIgnoreCase(secondRoute.getRouteId())) {
                secondRouteCard.activateCard();
            } else {
                secondRouteCard.deactivateCard();
            }
        } else {
            secondRouteCard = null;
        }

        if (thirdRoute != null) {
            routeIdBuilder.append(",").append(thirdRoute.getRouteId());

            thirdRouteCard = new TransitViewRouteCard(TransitViewResultsActivity.this, thirdRoute, 3);
            thirdRouteCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    firstRouteCard.deactivateCard();
                    secondRouteCard.deactivateCard();
                    thirdRouteCard.activateCard();

                    activeRouteId = thirdRoute.getRouteId();

                    refreshed = false;
                    refreshData();
                }
            });
            routeCardContainer.addView(thirdRouteCard);

            if (activeRouteId.equalsIgnoreCase(thirdRoute.getRouteId())) {
                thirdRouteCard.activateCard();
            } else {
                thirdRouteCard.deactivateCard();
            }

            // disable add button
            disableView(addLabel);
        } else {
            // make add button clickable
            activateView(addLabel);
        }

        routeIds = routeIdBuilder.toString();

        checkIfAFavorite();

        refreshData();
    }

    private void disableView(View view) {
        view.setAlpha((float) .6);
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
        noResultsMsg.setVisibility(View.GONE);
        mapContainerView.setVisibility(View.GONE);

        // refresh vehicle data
        SeptaServiceFactory.getTransitViewService().getTransitViewResults(routeIds).enqueue(new Callback<TransitViewModelResponse>() {
            @Override
            public void onResponse(Call<TransitViewModelResponse> call, @NonNull Response<TransitViewModelResponse> response) {
                if (response.body() != null && response.body().getResults() != null) {

                    try {
                        parser = new TransitViewModelResponseParser(response.body());

                        Map<TransitViewModelResponse.TransitViewRecord, LatLng> results = parser.getResultsForRoute(firstRoute.getRouteId());
                        if (results != null) {
                            Set<TransitViewModelResponse.TransitViewRecord> firstRoutesResults = results.keySet();
                            Log.d(TAG, firstRoutesResults.toString());
                        } else {
                            CrashlyticsManager.log(Log.ERROR, TAG, "Could not get results from parser for first route " + firstRoute.getRouteId());
                            CrashlyticsManager.log(Log.ERROR, TAG, "TransitView Response Body: " + response.body().toString());
                            Log.e(TAG, "Could not get results from parser for first route " + firstRoute.getRouteId());
                        }

                        if (secondRoute != null) {
                            results = parser.getResultsForRoute(secondRoute.getRouteId());
                            if (results != null) {
                                Set<TransitViewModelResponse.TransitViewRecord> secondRoutesResults = results.keySet();
                                Log.d(TAG, secondRoutesResults.toString());
                            } else {
                                CrashlyticsManager.log(Log.ERROR, TAG, "Could not get results from parser for second route " + secondRoute.getRouteId());
                                CrashlyticsManager.log(Log.ERROR, TAG, "TransitView Response Body: " + response.body().toString());
                                Log.e(TAG, "Could not get results from parser for second route " + secondRoute.getRouteId());
                            }
                        }

                        if (thirdRoute != null) {
                            results = parser.getResultsForRoute(thirdRoute.getRouteId());
                            if (results != null) {
                                Set<TransitViewModelResponse.TransitViewRecord> thirdRoutesResults = results.keySet();
                                Log.d(TAG, thirdRoutesResults.toString());
                            } else {
                                CrashlyticsManager.log(Log.ERROR, TAG, "Could not get results from parser for third route " + thirdRoute.getRouteId());
                                CrashlyticsManager.log(Log.ERROR, TAG, "TransitView Response Body: " + response.body().toString());
                                Log.e(TAG, "Could not get results from parser for third route " + thirdRoute.getRouteId());
                            }
                        }

                        prepareToDrawMap();
                    } catch (Exception e) {
                        Log.e(TAG, "Failure retrieving TransitView vehicle data from server: " + routeIds);
                        failure();
                    }
                } else {
                    CrashlyticsManager.log(Log.ERROR, TAG, "Null response body when fetching TransitView results for: " + routeIds);
                    Log.e(TAG, "Null response body when fetching TransitView results for: " + routeIds);
                    failure();
                }
            }

            @Override
            public void onFailure(Call<TransitViewModelResponse> call, Throwable t) {
                Log.e(TAG, "Failed to find TransitView results found for the routes: " + routeIds, t);
                failure();
            }

            private void failure() {
                progressView.setVisibility(View.GONE);
                refreshHandler.removeCallbacks(TransitViewResultsActivity.this);
                refreshHandler.postDelayed(TransitViewResultsActivity.this, REFRESH_DELAY_SECONDS * 1000);
                refreshed = false;
                showNoResultsFoundErrorMessage();
            }
        });

        // refresh alerts
        SeptaServiceFactory.getAlertsService().getAlerts().enqueue(new Callback<Alerts>() {
            @Override
            public void onResponse(Call<Alerts> call, Response<Alerts> response) {
                SystemStatusState.update(response.body());

                // show alert changes in route cards
                firstRouteCard.refreshAlertsView();
                if (secondRouteCard != null) {
                    secondRouteCard.refreshAlertsView();
                    if (thirdRouteCard != null) {
                        thirdRouteCard.refreshAlertsView();
                    }
                }
            }

            @Override
            public void onFailure(Call<Alerts> call, Throwable t) {
                t.printStackTrace();

                Log.e(TAG, "Failed to fetch alert updates for TransitView routes: " + routeIds, t);

                // hide alert icons in route cards
                firstRouteCard.hideAlertIcons();
                if (secondRouteCard != null) {
                    secondRouteCard.hideAlertIcons();
                    if (thirdRouteCard != null) {
                        thirdRouteCard.hideAlertIcons();
                    }
                }
            }
        });
    }

    private void prepareToDrawMap() {
        if (!firstRun) {
            mapFragment = SupportMapFragment.newInstance();
            try {
                getSupportFragmentManager().beginTransaction().add(R.id.map_container, mapFragment).commit();
            } catch (IllegalStateException e) {
                Log.e(TAG, e.toString());
            }
            mapFragment.getMapAsync(TransitViewResultsActivity.this);

        } else if (refreshed) {
            // refresh map without moving camera
            updateMap();
            refreshed = false;

        } else {
            // refresh data and move camera
            onMapReady(googleMap);
        }
    }

    private void updateMap() {
        googleMap.clear();
        vehicleDetailsMap.clear();

        for (String routeId : routeIds.split(",")) {
            // draw active route and vehicles as blue
            if (routeId.equalsIgnoreCase(activeRouteId)) {
                // redraw all vehicles
                redrawAllVehiclesOnRoute(routeId);

                // redraw route on map
                KmlLayer layer = MapUtils.getKMLByLineIdWithColor(TransitViewResultsActivity.this, googleMap, routeId, R.color.transitview_route_active_kml);
                if (layer != null) {
                    try {
                        layer.addLayerToMap();
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                    } catch (XmlPullParserException e) {
                        Log.e(TAG, e.toString());
                    }
                }

            } else {
                // redraw all vehicles
                redrawAllVehiclesOnRoute(routeId);

                // redraw route on map
                KmlLayer layer = MapUtils.getKMLByLineIdWithColor(TransitViewResultsActivity.this, googleMap, routeId, R.color.transitview_route_inactive);
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
        }

        // show my location button if permission granted
        checkForLocationEnabled(googleMap);

        // hide error message in case connection regained
        progressView.setVisibility(View.GONE);
        noResultsMsg.setVisibility(View.GONE);
        mapContainerView.setVisibility(View.VISIBLE);
    }

    private void checkForLocationEnabled(final GoogleMap googleMap) {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location Permission Granted.");
            LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Go to last known location. In some rare situations this can be null.
                    if (location != null) {
                        ContextCompat.checkSelfPermission(TransitViewResultsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
                        googleMap.setMyLocationEnabled(true);
                        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                            @Override
                            public boolean onMyLocationButtonClick() {
                                ContextCompat.checkSelfPermission(TransitViewResultsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

                                // move camera to user
                                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                                if (locationManager != null) {
                                    android.location.Criteria criteria = new android.location.Criteria();
                                    Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
                                    if (location != null) {
                                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
                                    }
                                }
                                return false;
                            }
                        });
                        googleMap.setOnMyLocationClickListener(new GoogleMap.OnMyLocationClickListener() {
                            @Override
                            public void onMyLocationClick(@NonNull Location location) {
                                // move camera to user
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
                            }
                        });
                    } else {
                        Log.d(TAG, "Location was null");
                        googleMap.setMyLocationEnabled(false);
                    }
                }
            });
        }

    }

    private void redrawAllVehiclesOnRoute(String routeId) {
        boolean isTrolley = isTrolley(TransitViewResultsActivity.this, routeId);
        boolean isActiveRoute = activeRouteId.equalsIgnoreCase(routeId);

        Map<TransitViewModelResponse.TransitViewRecord, LatLng> results = parser.getResultsForRoute(routeId);

        if (results != null) {
            for (Map.Entry<TransitViewModelResponse.TransitViewRecord, LatLng> entry : results.entrySet()) {

                // add to map of vehicle marker details
                String vehicleMarkerKey = new StringBuilder(routeId).append(VEHICLE_MARKER_KEY_DELIM).append(entry.getKey().getVehicleId()).toString();
                vehicleDetailsMap.put(vehicleMarkerKey, entry.getKey());

                // create directional icon with bus or trolley
                BitmapDescriptor vehicleBitMap = TransitViewUtils.getDirectionalIconForTransitType(this, isTrolley, isActiveRoute, entry.getKey().getHeading());
                googleMap.addMarker(new MarkerOptions()
                        .position(entry.getValue())
                        .title(vehicleMarkerKey)
                        .anchor((float) 0.5, (float) 0.5)
                        .icon(vehicleBitMap));
            }
        }
    }

    private void showNoResultsFoundErrorMessage() {
        // show error message and hide map and progress view
        mapContainerView.setVisibility(View.GONE);
        noResultsMsg.setVisibility(View.VISIBLE);
        progressView.setVisibility(View.GONE);
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

                                AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_DELETE_FAVORITE, AnalyticsManager.CUSTOM_EVENT_ID_FAVORITES_MANAGEMENT, null);

                                task.execute(TransitViewFavorite.generateKey(firstRoute, secondRoute, thirdRoute));
                            }
                        }).setNegativeButton(R.string.delete_fav_neg_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        item.setEnabled(true);
                    }
                }).create().show();
            } else {
                // prompt to save favorite
                final TransitViewFavorite favorite = new TransitViewFavorite(TransitViewResultsActivity.this, firstRoute, secondRoute, thirdRoute);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                CrashlyticsManager.log(Log.INFO, TAG, "Creating initial RenameFavoriteDialogFragment for:" + favorite.toString());
                RenameFavoriteDialogFragment fragment = RenameFavoriteDialogFragment.newInstance(true, false, favorite);
                fragment.show(ft, EDIT_FAVORITE_DIALOG_KEY);

                item.setEnabled(true);
            }
        } else {
            item.setEnabled(true);
        }
    }

    public void editFavorite(final MenuItem item) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        CrashlyticsManager.log(Log.INFO, TAG, "Creating RenameFavoriteDialogFragment for TransitView favorite: " + routeIds);
        RenameFavoriteDialogFragment fragment = RenameFavoriteDialogFragment.newInstance(true, true, currentFavorite);
        fragment.show(ft, EDIT_FAVORITE_DIALOG_KEY);
    }

    // TODO: put push notifications back in
//    private void goToNotificationsManagement() {
//        setResult(Constants.VIEW_NOTIFICATION_MANAGEMENT, new Intent());
//        finish();
//    }

}
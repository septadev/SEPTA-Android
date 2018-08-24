package org.septa.android.app.nextarrive;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.data.kml.KmlLayer;

import org.septa.android.app.ActivityClass;
import org.septa.android.app.BaseActivity;
import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.database.DatabaseManager;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.favorites.DeleteFavoritesAsyncTask;
import org.septa.android.app.favorites.edit.RenameFavoriteDialogFragment;
import org.septa.android.app.favorites.edit.RenameFavoriteListener;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Favorite;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalDetails;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalFavorite;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalModelResponse;
import org.septa.android.app.support.AnalyticsManager;
import org.septa.android.app.support.Consumer;
import org.septa.android.app.support.CrashlyticsManager;
import org.septa.android.app.support.Criteria;
import org.septa.android.app.support.CursorAdapterSupplier;
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

import static org.septa.android.app.favorites.edit.RenameFavoriteDialogFragment.EDIT_FAVORITE_DIALOG_KEY;

public class NextToArriveResultsActivity extends BaseActivity implements OnMapReadyCallback, RenameFavoriteListener, Runnable, ReverseNTAStopSelection.ReverseNTAStopSelectionListener {

    public static final String TAG = NextToArriveResultsActivity.class.getSimpleName();
    public static final int REFRESH_DELAY_SECONDS = 30,
            NTA_RESULTS_FOR_NEXT_HOURS = 5;

    private static final String NTA_RESULTS_TITLE = "nta_results_title",
            NEED_TO_SEE = "need_to_see";

    private StopModel start;
    private StopModel destination;
    private TransitType transitType;
    private RouteDirectionModel routeDirectionModel;
    private NextArrivalModelResponseParser parser;
    private boolean mapSized = false;
    private Handler refreshHandler;
    private boolean editFavoritesFlag = false;

    // layout variables
    private View containerView;
    private GoogleMap googleMap;
    private Button noResultsSchedulesButton;
    private FrameLayout mapContainerView;
    private ViewGroup bottomSheetLayout;
    private TextView titleText;
    private View anchor;
    private View rootView;
    private View reverseTrip;
    private View progressView;
    private View progressViewBottom;
    private NextArrivalFavorite currentFavorite = null;
    private NextToArriveTripView nextToArriveDetailsView;
    private MarkerOptions startMarker;
    private MarkerOptions destMarker;
    private int peekHeight = 0;
    private BottomSheetBehavior bottomSheetBehavior;
    private SupportMapFragment mapFragment;
    private FrameLayout noResultsMessage;

    private Map<String, NextArrivalDetails> details = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // need the next line to initialize BitmapDescriptorFactory
        MapsInitializer.initialize(getApplicationContext());

        setTitle(R.string.next_to_arrive);
        setContentView(R.layout.activity_next_to_arrive_results);

        containerView = findViewById(R.id.activity_next_to_arrive_results_container);

        rootView = findViewById(R.id.rail_next_to_arrive_results_coordinator);

        progressView = findViewById(R.id.progress_view);
        noResultsMessage = findViewById(R.id.nta_empty_results_msg);

        reverseTrip = findViewById(R.id.button_reverse_nta_trip);

        bottomSheetLayout = findViewById(R.id.bottomSheetLayout);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        progressViewBottom = findViewById(R.id.progress_view_bottom);

        noResultsSchedulesButton = findViewById(R.id.button_view_schedules);

        // Prevent the bottom sheet from being dragged to be opened.  Force it to use the anchor image.
        //bottomSheetBehavior.setBottomSheetCallback(myBottomSheetBehaviorCallBack);
        anchor = bottomSheetLayout.findViewById(R.id.bottom_sheet_anchor);
        //anchor.setOnClickListener(myBottomSheetBehaviorCallBack);

        mapContainerView = findViewById(R.id.map_container);

        titleText = bottomSheetLayout.findViewById(R.id.title_txt);
        nextToArriveDetailsView = findViewById(R.id.next_to_arrive_trip_details);
        nextToArriveDetailsView.setOriginClass(ActivityClass.NEXT_TO_ARRIVE);

        initializeView(savedInstanceState);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        invalidateOptionsMenu();

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

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_favorite:
                saveAsFavorite(item);
                return true;
            case R.id.refresh_results:
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
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(this);
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
        if (unmaskedRequestCode == Constants.SYSTEM_STATUS_REQUEST) {
            if (resultCode == Constants.VIEW_NOTIFICATION_MANAGEMENT) {
                goToNotificationsManagement();
            }
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        MapStyleOptions mapStyle = MapStyleOptions.loadRawResourceStyle(this, R.raw.maps_json_styling);
        googleMap.setMapStyle(mapStyle);

        final View mapContainer = findViewById(R.id.map_container);

        // hide navigation options
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        // add start and destination pins to map
        final LatLng startingStationLatLng = new LatLng(start.getLatitude(), start.getLongitude());
        final LatLng destinationStationLatLng = new LatLng(destination.getLatitude(), destination.getLongitude());
        googleMap.addMarker(new MarkerOptions().position(startingStationLatLng).title(start.getStopName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        googleMap.addMarker(new MarkerOptions().position(destinationStationLatLng).title(destination.getStopName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // set default map position
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startingStationLatLng, 13));

        // include start and destination in map camera bounds
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
                                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startingStationLatLng, 13));
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

                NTAVehicleDetailsInfoWindowAdapter adapter = new NTAVehicleDetailsInfoWindowAdapter(NextToArriveResultsActivity.this, transitType, startMarker, destMarker, details);
                googleMap.setInfoWindowAdapter(adapter);

            }
        });

        // hide error message in case connection regained
        noResultsMessage.setVisibility(View.GONE);
        mapContainerView.setVisibility(View.VISIBLE);
        bottomSheetLayout.setVisibility(View.VISIBLE);

    }

    @Override
    public void run() {
        refreshData();
    }

    @Override
    public void updateFavorite(final Favorite favorite) {
        if (favorite instanceof NextArrivalFavorite) {
            currentFavorite = (NextArrivalFavorite) favorite;
            renameFavorite(currentFavorite);

            Toast.makeText(this, R.string.create_nta_favorite, Toast.LENGTH_LONG).show();
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
        Toast.makeText(this, R.string.create_nta_favorite_failed, Toast.LENGTH_LONG).show();
    }

    @Override
    public void noReverseStopsFound() {
        Toast.makeText(this, R.string.reverse_not_found, Toast.LENGTH_LONG).show();
    }

    @Override
    public void reverseTrip(TransitType transitType, RouteDirectionModel newRouteDirectionModel, StopModel newStart, StopModel newDestination) {
        this.transitType = transitType;
        this.routeDirectionModel = newRouteDirectionModel;
        this.start = newStart;
        this.destination = newDestination;

        // create bundle for reverse trip based on results
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.DESTINATION_STATION, destination);
        bundle.putSerializable(Constants.STARTING_STATION, start);
        bundle.putSerializable(Constants.TRANSIT_TYPE, transitType);
        bundle.putSerializable(Constants.ROUTE_DIRECTION_MODEL, routeDirectionModel);

        // check if reverse trip is a favorite
        boolean isAlreadyAFavorite = false;
        NextArrivalFavorite reverseNextArrivalFavoriteTemp = new NextArrivalFavorite(start, destination, transitType, routeDirectionModel);
        if (SeptaServiceFactory.getFavoritesService().getFavoriteByKey(NextToArriveResultsActivity.this, reverseNextArrivalFavoriteTemp.getKey()) != null) {
            isAlreadyAFavorite = true;
            currentFavorite = reverseNextArrivalFavoriteTemp;
        }
        editFavoritesFlag = isAlreadyAFavorite;
        bundle.putSerializable(Constants.EDIT_FAVORITES_FLAG, editFavoritesFlag);

        // update menu dynamically
        invalidateOptionsMenu();

        // show the reverse trip results
        initializeView(bundle);
        refreshData();
    }

    private void initializeView(Bundle savedInstanceState) {
        nextToArriveDetailsView.setMaxResults(null);
        nextToArriveDetailsView.setResults(NTA_RESULTS_FOR_NEXT_HOURS, TimeUnit.HOURS);  // if this value changes update UI message nta_empty_results

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
                            Log.e(TAG, e.toString());
                        }
                        mapFragment.getMapAsync(NextToArriveResultsActivity.this);

                    }

                }
            }
        });

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

        noResultsSchedulesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoSchedulesForTarget();
            }
        });

        if (start != null && destination != null && transitType != null) {
            titleText.setText(transitType.getString(NTA_RESULTS_TITLE, this));
            ((TextView) findViewById(R.id.see_later_text)).setText(transitType.getString(NEED_TO_SEE, this));

            final TextView startingStationNameText = findViewById(R.id.starting_station_name);
            startingStationNameText.setText(start.getStopName());

            final TextView destinationStationNameText = findViewById(R.id.destination_station_name);
            destinationStationNameText.setText(destination.getStopName());

            nextToArriveDetailsView.setTransitType(transitType);
            nextToArriveDetailsView.setStart(start);
            nextToArriveDetailsView.setDestination(destination);
            nextToArriveDetailsView.setRouteDirectionModel(routeDirectionModel);

            String favKey = NextArrivalFavorite.generateKey(start, destination, transitType, routeDirectionModel);

            // currentFavorite can be null if the current selection is not a favorite
            currentFavorite = (NextArrivalFavorite) SeptaServiceFactory.getFavoritesService().getFavoriteByKey(this, favKey);

            findViewById(R.id.view_sched_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AnalyticsManager.logContentViewEvent(TAG, AnalyticsManager.CONTENT_VIEW_EVENT_SCHEDULE_FROM_NTA, AnalyticsManager.CONTENT_ID_SCHEDULE, null);
                    gotoSchedulesForTarget();
                }
            });

            refreshHandler = new Handler();

            (findViewById(R.id.route_cards)).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    startingStationNameText.setRight(reverseTrip.getLeft());
                    startingStationNameText.setText(startingStationNameText.getText());
                    destinationStationNameText.setRight(reverseTrip.getLeft());
                    destinationStationNameText.setText(destinationStationNameText.getText());
                }
            });
        }

        // change title if the selection is an existing favorite
        if (currentFavorite != null && editFavoritesFlag) {
            setTitle(currentFavorite.getName());
        } else {
            setTitle(R.string.next_to_arrive);
        }

        // reverse trip button clickable
        reverseTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onReverseTripButtonClicked();
            }
        });

        refreshHandler.postDelayed(this, 30 * 1000);
    }

    public void setStart(StopModel start) {
        this.start = start;
    }

    public void setDestination(StopModel destination) {
        this.destination = destination;
    }

    private void restoreState(Bundle bundle) {
        destination = (StopModel) bundle.get(Constants.DESTINATION_STATION);
        start = (StopModel) bundle.get(Constants.STARTING_STATION);
        transitType = (TransitType) bundle.get(Constants.TRANSIT_TYPE);
        routeDirectionModel = (RouteDirectionModel) bundle.get(Constants.ROUTE_DIRECTION_MODEL);
        editFavoritesFlag = bundle.getBoolean(Constants.EDIT_FAVORITES_FLAG, false);
    }

    private void refreshData() {
        // hide all containers while refreshFavoritesInstance happening
        noResultsMessage.setVisibility(View.GONE);
        mapContainerView.setVisibility(View.GONE);
        bottomSheetLayout.setVisibility(View.GONE);

        final long timestamp = System.currentTimeMillis();
        if (start != null && destination != null) {

            String routeId = null;
            if (routeDirectionModel != null) {
                routeId = routeDirectionModel.getRouteId();
            }
            Call<NextArrivalModelResponse> results = SeptaServiceFactory.getNextArrivalService().getNextArrival(Integer.parseInt(start.getStopId()), Integer.parseInt(destination.getStopId()), transitType.name(), routeId);
            progressVisibility(View.VISIBLE);

            results.enqueue(new Callback<NextArrivalModelResponse>() {
                @Override
                public void onResponse(Call<NextArrivalModelResponse> call, final Response<NextArrivalModelResponse> response) {
                    if (response.body() != null && response.body().getNextArrivalRecords() != null && !response.body().getNextArrivalRecords().isEmpty()) {
                        if (transitType == TransitType.RAIL && response.body().getNextArrivalRecords().size() > 0) {
                            try {
                                //Because RAIL does not need a Route to look up NTA if we want to link back to Schedules we need to figure out the Line and Direction.
                                //So grab the first Line from the results and the first train.  Then look up in the schedule DB the direction code for that train on that line.

                                String routeId = response.body().getNextArrivalRecords().get(0).getOrigRouteId();
                                String trainId = response.body().getNextArrivalRecords().get(0).getOrigLineTripId();

                                List<Criteria> criteriaList = new ArrayList<>(2);
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


                        // Go through all of the results and kick off a call for Details for each vehicle that has RT data.
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
                                        showNoResultsFoundErrorMessage();
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
                                            showNoResultsFoundErrorMessage();
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
                                        Log.e(TAG, e.toString());
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
                    } else {
                        // empty response
                        Log.d(TAG, "Empty NTA response body");
                        onFailure();
                    }
                }

                private void updateView() {
                    LatLng startingStationLatLng = new LatLng(start.getLatitude(), start.getLongitude());
                    LatLng destinationStationLatLng = new LatLng(destination.getLatitude(), destination.getLongitude());
                    startMarker = new MarkerOptions().position(startingStationLatLng).title(start.getStopName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    destMarker = new MarkerOptions().position(destinationStationLatLng).title(destination.getStopName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                        getSupportActionBar().setDisplayShowHomeEnabled(true);
                    }

                    nextToArriveDetailsView.setNextToArriveData(NextToArriveResultsActivity.this, parser);

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
                    progressVisibility(View.GONE);
                    refreshHandler.removeCallbacks(NextToArriveResultsActivity.this);
                    refreshHandler.postDelayed(NextToArriveResultsActivity.this, REFRESH_DELAY_SECONDS * 1000);
                    showNoResultsFoundErrorMessage();
                }
            });
        }

    }

    private void progressVisibility(int visibility) {
        progressView.setVisibility(visibility);
        progressViewBottom.setVisibility(visibility);
    }

    private void showNoResultsFoundErrorMessage() {
        // show error message and hide
        mapContainerView.setVisibility(View.GONE);
        bottomSheetLayout.setVisibility(View.GONE);
        noResultsMessage.setVisibility(View.VISIBLE);
        Log.d(TAG, "No results found in NTA records");
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

        // show my location button if permission granted
        checkForLocationEnabled(googleMap);

        // hide error message in case connection regained
        noResultsMessage.setVisibility(View.GONE);
        mapContainerView.setVisibility(View.VISIBLE);
        bottomSheetLayout.setVisibility(View.VISIBLE);
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
                        ContextCompat.checkSelfPermission(NextToArriveResultsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
                        googleMap.setMyLocationEnabled(true);
                        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                            @Override
                            public boolean onMyLocationButtonClick() {
                                ContextCompat.checkSelfPermission(NextToArriveResultsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

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

    public void saveAsFavorite(final MenuItem item) {
        if (!item.isEnabled()) {
            return;
        }

        item.setEnabled(false);

        if (start != null && destination != null && transitType != null) {
            if (currentFavorite == null) {
                // prompt to save favorite
                final NextArrivalFavorite nextArrivalFavorite = new NextArrivalFavorite(start, destination, transitType, routeDirectionModel);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                CrashlyticsManager.log(Log.INFO, TAG, "Creating initial RenameFavoriteDialogFragment for:" + nextArrivalFavorite.toString());
                RenameFavoriteDialogFragment fragment = RenameFavoriteDialogFragment.newInstance(false, false, nextArrivalFavorite);
                fragment.show(ft, EDIT_FAVORITE_DIALOG_KEY);

                item.setEnabled(true);
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

                                AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_DELETE_FAVORITE, AnalyticsManager.CUSTOM_EVENT_ID_FAVORITES_MANAGEMENT, null);

                                task.execute(currentFavorite.getKey());
                            }
                        }).setNegativeButton(R.string.delete_fav_neg_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        item.setEnabled(true);
                    }
                }).create().show();
            }

        } else {
            item.setEnabled(true);
        }
    }

    public void editFavorite(final MenuItem item) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        CrashlyticsManager.log(Log.INFO, TAG, "Creating RenameFavoriteDialogFragment for:" + currentFavorite.toString());
        RenameFavoriteDialogFragment fragment = RenameFavoriteDialogFragment.newInstance(false, true, currentFavorite);
        fragment.show(ft, EDIT_FAVORITE_DIALOG_KEY);
    }

    private void onReverseTripButtonClicked() {
        // look up reverse stop IDs
        ReverseNTAStopSelection reverseTripAsyncTask = new ReverseNTAStopSelection(NextToArriveResultsActivity.this, transitType, routeDirectionModel, start, destination);
        reverseTripAsyncTask.execute();
    }

    private void gotoSchedulesForTarget() {
        Intent intent = new Intent();
        intent.putExtra(Constants.STARTING_STATION, start);
        intent.putExtra(Constants.DESTINATION_STATION, destination);
        intent.putExtra(Constants.TRANSIT_TYPE, transitType);
        if (routeDirectionModel != null) {
            intent.putExtra(Constants.ROUTE_DIRECTION_MODEL, routeDirectionModel);
        }

        setResult(Constants.VIEW_SCHEDULE, intent);
        finish();
    }

    private void goToNotificationsManagement() {
        setResult(Constants.VIEW_NOTIFICATION_MANAGEMENT, new Intent());
        finish();
    }

}
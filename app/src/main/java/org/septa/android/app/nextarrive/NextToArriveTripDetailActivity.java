package org.septa.android.app.nextarrive;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

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
import com.google.maps.android.data.kml.KmlLayer;

import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalDetails;
import org.septa.android.app.support.GeneralUtils;
import org.septa.android.app.support.MapUtils;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import android.os.Handler;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by jkampf on 8/3/17.
 */

public class NextToArriveTripDetailActivity extends AppCompatActivity implements OnMapReadyCallback, Runnable {
    public static final String TAG = NextToArriveTripDetailActivity.class.getSimpleName();
    public static final int REFRESH_DELAY_SECONDS = 30;

    StopModel start;
    StopModel destination;
    TransitType transitType;
    //RouteDirectionModel routeDirectionModel;
    //NextArrivalModelResponse.NextArrivalRecord arrivalRecord;
    String routeDescription;

    String tripId;
    GoogleMap googleMap;
    Double vehicleLat;
    Double vehicleLon;
    String appUrl;
    String webUrl;


    TextView nextStopValue;
    private TextView arrivingValue;
    private TextView lineValue;
    private TextView lineLabel;
    private TextView typeValue;
    private TextView trainsIdValue;
    private TextView numTrainsValue;
    private TextView destStationValue;
    private TextView originStationValue;
    private TextView vehicleValue;

    private String routeId;
    private String serviceRouteId = null;
    private String vehicleId;

    View progressView;
    private TextView blockidValue;

    private Handler refreshHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.nta_trip_details);

        Intent intent = getIntent();
        destination = (StopModel) intent.getExtras().get(Constants.DESTINATAION_STATION);
        start = (StopModel) intent.getExtras().get(Constants.STARTING_STATION);
        transitType = (TransitType) intent.getExtras().get(Constants.TRANSIT_TYPE);
        vehicleId = intent.getExtras().getString(Constants.VEHICLE_ID);
        tripId = intent.getExtras().getString(Constants.TRIP_ID);
        String routeName = intent.getExtras().getString(Constants.ROUTE_NAME);
        routeId = intent.getExtras().getString(Constants.ROUTE_ID);
        routeDescription = intent.getExtras().getString(Constants.ROUTE_DESCRIPTION);

        if (start != null && destination != null && transitType != null) {
            TextView tripToLocationText = ((TextView) findViewById(R.id.trip_to_location_text));
            tripToLocationText.setText(tripId + " to " + destination.getStopName());
            tripToLocationText.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, transitType.getTabInactiveImageResource()), null, null, null);

            setTitle(transitType.getString("trip_details_title", this));


            SupportMapFragment mapFragment = SupportMapFragment.newInstance();
            mapFragment.getMapAsync(this);

            getSupportFragmentManager().beginTransaction().add(R.id.map_container, mapFragment).commit();

        }

        nextStopValue = (TextView) findViewById(R.id.next_stop_value);
        arrivingValue = (TextView) findViewById(R.id.arriving_value);
        lineValue = (TextView) findViewById(R.id.line_value);
        lineLabel = (TextView) findViewById(R.id.line_label);
        originStationValue = (TextView) findViewById(R.id.origin_station_value);
        destStationValue = (TextView) findViewById(R.id.dest_station_value);
        numTrainsValue = (TextView) findViewById(R.id.num_trains_value);
        trainsIdValue = (TextView) findViewById(R.id.trains_id_value);
        typeValue = (TextView) findViewById(R.id.type_value);
        vehicleValue = (TextView) findViewById(R.id.vehicle_value);
        blockidValue = (TextView) findViewById(R.id.blockid_value);

        progressView = findViewById(R.id.progress_view);

        if (transitType != TransitType.RAIL) {
            lineLabel.setText("Route:");
            lineValue.setText(routeId + " to " + routeDescription);
            findViewById(R.id.num_train_layout).setVisibility(View.GONE);
            findViewById(R.id.type_layout).setVisibility(View.GONE);
            findViewById(R.id.next_stop_layout).setVisibility(View.GONE);
            findViewById(R.id.dest_layout).setVisibility(View.GONE);
            findViewById(R.id.origin_layout).setVisibility(View.GONE);
            findViewById(R.id.vehicle_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.blockid_layout).setVisibility(View.VISIBLE);

        } else {
            lineValue.setText(routeName);
        }

        if (transitType == TransitType.BUS || transitType == TransitType.TROLLEY) {
            serviceRouteId = routeId;
        } else {
            vehicleId = tripId;
        }


        final TextView twitterId = (TextView) findViewById(R.id.twitter_id);
        if (transitType != TransitType.RAIL) {
            twitterId.setText("@SEPTA_SOCIAL");
            webUrl = getString(R.string.twitter_url);
        } else {
            if ("med".equalsIgnoreCase(routeId)) {
                twitterId.setText("@SEPTA_ELW");
            } else
                twitterId.setText("@SEPTA_" + routeId.toUpperCase());
            webUrl = "https://twitter.com/SEPTA_" + routeId.toUpperCase();
            appUrl = getString(getResources().getIdentifier("twitter_app_url_" + routeId.toLowerCase(), "string", R.class.getPackage().getName()));
        }

        View twitterView = findViewById(R.id.twitter_view);
        twitterView.setClickable(true);
        twitterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri app = Uri.parse(appUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW, app);
                if (intent.resolveActivity(getPackageManager()) == null) {
                    Uri webpage = Uri.parse(webUrl);
                    intent = new Intent(Intent.ACTION_VIEW, webpage);
                    if (intent.resolveActivity(getPackageManager()) == null) {
                        Log.e(TAG, "Unable to resolve app and website Intent URLs:" + webUrl + " and: " + appUrl);
                        return;
                    }
                }

                startActivity(intent);
            }
        });


        vehicleValue.setText(vehicleId);
        refreshHandler = new Handler();
        run();
    }


    @Override
    public void run() {
        refresh(null);
        refreshHandler.postDelayed(this, REFRESH_DELAY_SECONDS * 1000);
    }

    public void refresh(final MenuItem item) {
        progressView.setVisibility(View.VISIBLE);
        final long timestamp = System.currentTimeMillis();
        SeptaServiceFactory.getNextArrivalService().getNextArrivalDetails(destination.getStopId(), serviceRouteId, vehicleId).enqueue(new Callback<NextArrivalDetails>() {
            @Override
            public void onResponse(Call<NextArrivalDetails> call, final Response<NextArrivalDetails> response) {

                if (System.currentTimeMillis() - timestamp < 1000) {
                    AsyncTask<Long, Void, Void> delayTask = new AsyncTask<Long, Void, Void>() {
                        @Override
                        protected void onPostExecute(Void aVoid) {
                            if (updateView(response))
                                progressView.setVisibility(View.GONE);
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
                    if (updateView(response))
                        progressView.setVisibility(View.GONE);
                }
            }

            private boolean updateView(Response<NextArrivalDetails> response) {
                NextArrivalDetails.Details details = response.body().getDetails();
                if (details != null) {
                    vehicleLat = details.getLatitiude();
                    vehicleLon = details.getLongitude();


                    if (details.getDestination() == null) {
                        arrivingValue.setBackgroundResource(R.drawable.no_rt_data_boarder);
                        arrivingValue.setText("Real time data unavailable");
                        arrivingValue.setTextColor(ContextCompat.getColor(NextToArriveTripDetailActivity.this, R.color.scheduled));
                    } else if (details.getDestination().getDelay() > 0) {
                        arrivingValue.setBackgroundResource(R.drawable.late_boarder);
                        arrivingValue.setText(GeneralUtils.getDurationAsLongString(details.getDestination().getDelay(), TimeUnit.MINUTES) + " late.");
                        arrivingValue.setTextColor(ContextCompat.getColor(NextToArriveTripDetailActivity.this, R.color.late_departing));
                    } else {
                        arrivingValue.setBackgroundResource(R.drawable.ontime_tripdetails_boarder);
                        arrivingValue.setTextColor(ContextCompat.getColor(NextToArriveTripDetailActivity.this, R.color.on_time_departing));
                        arrivingValue.setText("On Time");
                    }


                    if (transitType != TransitType.RAIL) {
                        findViewById(R.id.num_train_layout).setVisibility(View.GONE);
                        blockidValue.setText(details.getBlockId());
                    } else {
                        if (details.getDestination() != null)
                            destStationValue.setText(details.getDestination().getStation());
                        else
                            destStationValue.setText("");
                        originStationValue.setText(details.getSource());

                        typeValue.setText(details.getService());
                        if (details.getNextStop() != null)
                            nextStopValue.setText(details.getNextStop().getStation());
                        else
                            nextStopValue.setText("");

                        if (details.getConsist() != null && details.getConsist().size() > 0) {
                            if (details.getConsist().size() == 1 && "".equals(details.getConsist().get(0).trim())) {
                                numTrainsValue.setText("");
                            } else {
                                numTrainsValue.setText(details.getConsist().size() + " - ");
                                StringBuilder trainsId = new StringBuilder();
                                boolean first = true;
                                for (String trainId : details.getConsist()) {
                                    if (!first) trainsId.append(", ");
                                    first = false;
                                    trainsId.append(trainId);
                                }
                                trainsIdValue.setText(trainsId.toString());
                            }
                        } else {
                            numTrainsValue.setText("");
                        }
                    }
                    updateMap();
                    return true;
                } else {
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.trip_detail_coordinator), "Sorry, we had some issue retrieving your data.  Please try again.", Snackbar.LENGTH_LONG);
                    snackbar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            try {
                                onBackPressed();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    snackbar.show();
                    return false;
                }
            }

            @Override
            public void onFailure(Call<NextArrivalDetails> call, Throwable t) {
                Snackbar snackbar = Snackbar.make(findViewById(R.id.trip_detail_coordinator), "Sorry, we had some issue retrieving your data.  Please try again.", Snackbar.LENGTH_LONG);
                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        try {
                            onBackPressed();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                snackbar.show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        //googleMap.getUiSettings().setScrollGesturesEnabled(false);
        //googleMap.getUiSettings().setZoomControlsEnabled(false);
        //googleMap.getUiSettings().setZoomGesturesEnabled(false);
        googleMap.getUiSettings().setAllGesturesEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.setOnMarkerClickListener(
                new GoogleMap.OnMarkerClickListener() {
                    public boolean onMarkerClick(Marker marker) {
                        return true;
                    }
                });

        MapStyleOptions mapStyle = MapStyleOptions.loadRawResourceStyle(this, R.raw.maps_json_styling);

        googleMap.setMapStyle(mapStyle);

        updateMap();
    }

    private void updateMap() {
        if (googleMap == null)
            return;

        final View mapContainer = findViewById(R.id.map_container);
        googleMap.clear();

        KmlLayer layer = MapUtils.getKMLByLineId(this, googleMap, routeId, transitType);
        try {
            layer.addLayerToMap();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if (vehicleLat != null && vehicleLon != null) {
            BitmapDescriptor vehicleBitMap = BitmapDescriptorFactory.fromResource(transitType.getMapMarkerResource());
            LatLng vehicleLatLng = new LatLng(vehicleLat, vehicleLon);
            googleMap.addMarker(new MarkerOptions().position(vehicleLatLng).icon(vehicleBitMap));
            builder.include(vehicleLatLng);
        }


        LatLng startingStationLatLng = new LatLng(start.getLatitude(), start.getLongitude());
        LatLng destinationStationLatLng = new LatLng(destination.getLatitude(), destination.getLongitude());
        googleMap.addMarker(new MarkerOptions().position(startingStationLatLng).title(start.getStopName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        googleMap.addMarker(new MarkerOptions().position(destinationStationLatLng).title(destination.getStopName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(startingStationLatLng));

        builder.include(startingStationLatLng);
        builder.include(destinationStationLatLng);
        final LatLngBounds bounds = builder.build();
        try {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics())));
        } catch (IllegalStateException e) {
            if (mapContainer.getViewTreeObserver().isAlive()) {
                mapContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mapContainer.getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics())));

                    }
                });
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        run();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.refresh_menu, menu);
        Drawable drawable = menu.getItem(0).getIcon();
        DrawableCompat.setTint(drawable, ContextCompat.getColor(this, R.color.options_menu_tint));
        menu.getItem(0).setIcon(drawable);
        return super.onCreateOptionsMenu(menu);
    }


}




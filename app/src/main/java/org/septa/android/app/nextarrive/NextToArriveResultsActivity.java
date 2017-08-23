package org.septa.android.app.nextarrive;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.data.kml.KmlLayer;

import org.septa.android.app.R;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalModelResponse;
import org.septa.android.app.services.apiinterfaces.model.NextToArriveModel;
import org.septa.android.app.support.MapUtils;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by jkampf on 8/3/17.
 */

public class NextToArriveResultsActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String TAG = NextToArriveResultsActivity.class.getSimpleName();
    private StopModel start;
    private StopModel destination;
    private GoogleMap googleMap;
    ViewGroup bottomSheetLayout;
    ListView linesListView;
    ImageView refresh;
    View progressView;

    public static final String STARTING_STATION = "starting_station";
    public static final String DESTINATAION_STATION = "destination_station";

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

        setContentView(R.layout.rail_next_to_arrive_results);

        progressView = findViewById(R.id.progress_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomSheetLayout = (ViewGroup) findViewById(R.id.bottomSheetLayout);
        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);


        // Prevent the bottom sheet from being dragged to be opened.  Force it to use the anchor image.
        BottomSheetHandler myBottomSheetBehaviorCallBack = new BottomSheetHandler(bottomSheetBehavior);
        bottomSheetBehavior.setBottomSheetCallback(myBottomSheetBehaviorCallBack);
        View anchor = findViewById(R.id.bottom_sheet_anchor);
        anchor.setOnClickListener(myBottomSheetBehaviorCallBack);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        destination = (StopModel) intent.getExtras().get(DESTINATAION_STATION);
        start = (StopModel) intent.getExtras().get(STARTING_STATION);

        if (start != null && destination != null) {
            TextView startingStationNameText = (TextView) findViewById(R.id.starting_station_name);
            startingStationNameText.setText(start.getStopName());

            TextView destinationStationNameText = (TextView) findViewById(R.id.destination_station_name);
            destinationStationNameText.setText(destination.getStopName());

            SupportMapFragment mapFragment = SupportMapFragment.newInstance();
            mapFragment.getMapAsync(this);

            getSupportFragmentManager().beginTransaction().add(R.id.map_container, mapFragment).commit();

            linesListView = (ListView) findViewById(R.id.lines_list_view);
        }

        refresh = (ImageView) findViewById(R.id.refresh_image);

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        MapStyleOptions mapStyle = MapStyleOptions.loadRawResourceStyle(this, R.raw.maps_json_styling);

        googleMap.setMapStyle(mapStyle);

        updateNextToArriveData();
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateNextToArriveData();
            }
        });


        View mapContainer = findViewById(R.id.map_container);
        ViewGroup.LayoutParams layoutParams =
                bottomSheetLayout.getLayoutParams();
        layoutParams.height = mapContainer.getHeight();
        bottomSheetLayout.setLayoutParams(layoutParams);


        LatLng startingStationLatLng = new LatLng(start.getLatitude(), start.getLongitude());
        LatLng destinationStationLatLng = new LatLng(destination.getLatitude(), destination.getLongitude());

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(startingStationLatLng));

        googleMap.addMarker(new MarkerOptions().position(startingStationLatLng).title(start.getStopName()));
        googleMap.addMarker(new MarkerOptions().position(destinationStationLatLng).title(destination.getStopName()));

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

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    private void updateNextToArriveData() {
        if (start != null && destination != null) {
            Call<NextArrivalModelResponse> results = SeptaServiceFactory.getNextArrivalService().getNextArriaval(Integer.parseInt(start.getStopId()), Integer.parseInt(destination.getStopId()), "RAIL", null);
            progressView.setVisibility(View.VISIBLE);

            results.enqueue(new Callback<NextArrivalModelResponse>() {
                @Override
                public void onResponse(Call<NextArrivalModelResponse> call, Response<NextArrivalModelResponse> response) {
                    Log.d(TAG, response.toString());
                    Log.d(TAG, response.body().toString());

                    Map<String, NextToArriveLine> map = new HashMap<String, NextToArriveLine>();
                    Set<String> kmlSet = new HashSet<String>();


                    for (NextArrivalModelResponse.NextArrivalRecord data : response.body().getNextArrivalRecords()) {
                        String key;
                        if (data.getOrigRouteId() == data.getTermRouteId()) {
                            key = data.getOrigRouteId();
                        } else
                            key = data.getOrigRouteId() + "." + data.getTermRouteId();

                        if (!map.containsKey(key)) {
                            map.put(key, new NextToArriveLine(data.getOrigRouteName()));
                        }

                        map.get(key).addItem(data);

                        if (!kmlSet.contains(data.getOrigRouteId())) {
                            kmlSet.add(data.getOrigRouteId());
                            KmlLayer layer = MapUtils.getKMLByLineId(NextToArriveResultsActivity.this, googleMap, data.getOrigRouteId());
                            if (layer != null)
                                try {
                                    layer.addLayerToMap();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (XmlPullParserException e) {
                                    e.printStackTrace();
                                }
                        }

                        if (data.getConnectionStationId() != null)
                            if (!kmlSet.contains(data.getTermRouteId())) {
                                kmlSet.add(data.getTermRouteId());
                                KmlLayer layer = MapUtils.getKMLByLineId(NextToArriveResultsActivity.this, googleMap, data.getTermRouteId());
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

                    List<NextToArriveLine> nextToArriveLinesList = new ArrayList<NextToArriveLine>(map.values());
                    Collections.sort(nextToArriveLinesList, new Comparator<NextToArriveLine>() {
                        @Override
                        public int compare(NextToArriveLine x, NextToArriveLine y) {
                            if (x.getSoonestDeparture() != null)
                                return x.getSoonestDeparture().compareTo(y.getSoonestDeparture());
                            else return Integer.MAX_VALUE;
                        }
                    });

                    linesListView.setAdapter(new LinesListAdapater(NextToArriveResultsActivity.this, nextToArriveLinesList));
                    progressView.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<NextArrivalModelResponse> call, Throwable t) {
                    progressView.setVisibility(View.GONE);
                    t.printStackTrace();
                }
            });

        }

    }


    private class LinesListAdapater extends ArrayAdapter<NextToArriveLine> {

        public LinesListAdapater(@NonNull Context context, List<NextToArriveLine> list) {
            super(context, 0, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.rail_next_to_arrive_line, parent, false);
            }

            TextView lineNameText = (TextView) convertView.findViewById(R.id.line_name_text);
            lineNameText.setText(getItem(position).lineName);
            LinearLayout arrivalList = (LinearLayout) convertView.findViewById(R.id.arrival_list);
            arrivalList.removeAllViews();

            DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);

            List<NextArrivalModelResponse.NextArrivalRecord> tripList = getItem(position).getList();
            Collections.sort(
                    tripList, new Comparator<NextArrivalModelResponse.NextArrivalRecord>() {
                        @Override
                        public int compare(NextArrivalModelResponse.NextArrivalRecord x, NextArrivalModelResponse.NextArrivalRecord y) {
                            return x.getSchedDepartureTime().compareTo(y.getSchedDepartureTime());
                        }
                    });

            tripList = tripList.subList(0, (3 < tripList.size()) ? 3 : tripList.size());

            for (NextArrivalModelResponse.NextArrivalRecord unit : tripList) {
                View line = LayoutInflater.from(getContext()).inflate(R.layout.rail_next_to_arrive_unit, null, false);
                TextView arrivalTimeText = (TextView) line.findViewById(R.id.arrival_time_text);
                arrivalTimeText.setText(dateFormat.format(unit.getSchedDepartureTime()) + " - " + dateFormat.format(unit.getSchedArrivalTime()));
                arrivalList.addView(line);

                TextView tripNumberText = (TextView) line.findViewById(R.id.trip_number_text);
                tripNumberText.setText(unit.getOrigLineTripId() + " to " + unit.getOrigLastStopName());

                TextView departureTime = (TextView) line.findViewById(R.id.depature_time);
                int departsInMinutes = ((int) (unit.getSchedDepartureTime().getTime() + (unit.getOrigDelayMinutes() * 60000) - System.currentTimeMillis()) / 60000);
                departureTime.setText(String.valueOf(departsInMinutes + " Minutes"));

                TextView tardyText = (TextView) line.findViewById(R.id.tardy_text);
                if (unit.getOrigDelayMinutes() > 0) {
                    tardyText.setText(unit.getOrigDelayMinutes() + " min late.");
                    tardyText.setTextColor(Color.parseColor("#d72e11"));
                    View departingBorder = line.findViewById(R.id.departing_border);
                    departingBorder.setBackground(getResources().getDrawable(R.drawable.late_boarder));
                } else {
                    tardyText.setText("On time");
                    tardyText.setTextColor(Color.parseColor("#539e00"));
                }
            }

            return convertView;
        }
    }

    private class NextToArriveLine {
        List<NextArrivalModelResponse.NextArrivalRecord> nextToArriveModels = new ArrayList<NextArrivalModelResponse.NextArrivalRecord>();
        String lineName;
        Date soonestDeparture;

        NextToArriveLine(String lineName) {
            this.lineName = lineName;
        }

        List<NextArrivalModelResponse.NextArrivalRecord> getList() {
            return nextToArriveModels;
        }

        void addItem(NextArrivalModelResponse.NextArrivalRecord item) {
            nextToArriveModels.add(item);
            if (soonestDeparture != null) {
                if (item.getSchedDepartureTime().getTime() + (item.getOrigDelayMinutes() * 60000) > soonestDeparture.getTime())
                    return;
            }

            soonestDeparture = new Date(item.getSchedDepartureTime().getTime() + (item.getOrigDelayMinutes() * 60000));
        }

        public Date getSoonestDeparture() {
            return soonestDeparture;
        }

        @Override
        public int hashCode() {
            if (lineName == null)
                return 0;

            return lineName.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null && lineName == null)
                return true;
            if (lineName != null)
                return lineName.equals(obj);

            return false;
        }


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




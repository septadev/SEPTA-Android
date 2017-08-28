package org.septa.android.app.nextarrive;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
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
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalModelResponse;
import org.septa.android.app.support.MapUtils;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Array;
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
    private TransitType transitType;
    private RouteDirectionModel routeDirectionModel;
    private GoogleMap googleMap;
    ViewGroup bottomSheetLayout;
    ListView linesListView;
    View progressView;

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

        setContentView(R.layout.next_to_arrive_results);

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

            linesListView = (ListView) findViewById(R.id.lines_list_view);
        }

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
            progressView.setVisibility(View.VISIBLE);

            results.enqueue(new Callback<NextArrivalModelResponse>() {
                @Override
                public void onResponse(Call<NextArrivalModelResponse> call, Response<NextArrivalModelResponse> response) {
                    Log.d(TAG, response.toString());
                    Log.d(TAG, response.body().toString());

                    Map<String, NextToArriveLine> map = new HashMap<String, NextToArriveLine>();
                    Set<String> kmlSet = new HashSet<String>();

                    BitmapDescriptor vehicleBitMap = BitmapDescriptorFactory.fromResource(TransitType.valueOf(response.body().getTransType()).getMapMarkerResource());

                    googleMap.clear();

                    LatLng startingStationLatLng = new LatLng(start.getLatitude(), start.getLongitude());
                    LatLng destinationStationLatLng = new LatLng(destination.getLatitude(), destination.getLongitude());
                    googleMap.addMarker(new MarkerOptions().position(startingStationLatLng).title(start.getStopName()));
                    googleMap.addMarker(new MarkerOptions().position(destinationStationLatLng).title(destination.getStopName()));

                    int multiStopCount = 0;
                    int singleStopCount = 0;

                    for (NextArrivalModelResponse.NextArrivalRecord data : response.body().getNextArrivalRecords()) {
                        String key;
                        if (data.getOrigRouteId().equals(data.getTermRouteId())) {
                            key = data.getOrigRouteId();
                            singleStopCount++;
                        } else {
                            key = data.getOrigRouteId() + "." + data.getTermRouteId();
                            multiStopCount++;
                        }

                        if (!map.containsKey(key)) {
                            map.put(key, new NextToArriveLine(data.getOrigRouteName(), (data.getOrigRouteId() != data.getTermRouteId())));
                        }

                        map.get(key).addItem(data);

                        if (data.getVehicle_lat() != null && data.getVehicle_lon() != null) {
                            LatLng vehicleLatLng = new LatLng(data.getVehicle_lat(), data.getVehicle_lon());
                            googleMap.addMarker(new MarkerOptions().position(vehicleLatLng).title(data.getOrigLineTripId()).icon(vehicleBitMap));
                        }

                        if (!kmlSet.contains(data.getOrigRouteId())) {
                            kmlSet.add(data.getOrigRouteId());
                            KmlLayer layer = MapUtils.getKMLByLineId(NextToArriveResultsActivity.this, googleMap, data.getOrigRouteId(), transitType);
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
                                KmlLayer layer = MapUtils.getKMLByLineId(NextToArriveResultsActivity.this, googleMap, data.getTermRouteId(), transitType);
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

                    if (singleStopCount > 0 && multiStopCount > 0) {
                        //TODO Need an error message here.
                        throw new RuntimeException("We multi-stop and single stop next to arrive in the same response.  This is unexpected.");
                    }

                    if (singleStopCount > 0) {
                        List<NextToArriveLine> nextToArriveLinesList = new ArrayList<NextToArriveLine>(map.values());
                        Collections.sort(nextToArriveLinesList, new Comparator<NextToArriveLine>() {
                            @Override
                            public int compare(NextToArriveLine x, NextToArriveLine y) {
                                if (x.getSoonestDeparture() != null)
                                    return x.getSoonestDeparture().compareTo(y.getSoonestDeparture());
                                else return Integer.MAX_VALUE;
                            }
                        });


                        linesListView.setAdapter(new SingleLinesListAdapater(NextToArriveResultsActivity.this, nextToArriveLinesList));
                    }

                    if (multiStopCount > 0) {
                        Collections.sort(response.body().getNextArrivalRecords(), new Comparator<NextArrivalModelResponse.NextArrivalRecord>() {
                            @Override
                            public int compare(NextArrivalModelResponse.NextArrivalRecord x, NextArrivalModelResponse.NextArrivalRecord y) {
                                if (x == y)
                                    return 0;
                                return (int) (x.getOrigDepartureTime().getTime() + (60000 * x.getOrigDelayMinutes())
                                        - y.getOrigDepartureTime().getTime() + (60000 * y.getOrigDelayMinutes()));
                            }
                        });
                        List<NextArrivalModelResponse.NextArrivalRecord> multiStopList = response.body().getNextArrivalRecords().subList(0, (3 < response.body().getNextArrivalRecords().size()) ? 3 : response.body().getNextArrivalRecords().size());
                        linesListView.setAdapter(new MultiLinesListAdapater(NextToArriveResultsActivity.this, multiStopList));
                    }
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


    private class MultiLinesListAdapater extends ArrayAdapter<NextArrivalModelResponse.NextArrivalRecord> {

        public MultiLinesListAdapater(@NonNull Context context, List<NextArrivalModelResponse.NextArrivalRecord> list) {
            super(context, 0, list);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.next_to_arrive_unit_multistop, parent, false);
            }

            NextArrivalModelResponse.NextArrivalRecord item = getItem(position);
            DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);

            TextView origLineNameText = (TextView) convertView.findViewById(R.id.orig_line_name_text);
            origLineNameText.setText(item.getOrigRouteName());

            ((ImageView) convertView.findViewById(R.id.orig_line_marker_left)).setColorFilter(ContextCompat.getColor(getContext(), TransitType.RAIL.getLineColor(item.getOrigRouteId(), getContext())));
            ((ImageView) convertView.findViewById(R.id.orig_line_marker_right)).setColorFilter(ContextCompat.getColor(getContext(), TransitType.RAIL.getLineColor(item.getOrigRouteId(), getContext())));


            TextView origArrivalTimeText = (TextView) convertView.findViewById(R.id.orig_arrival_time_text);
            origArrivalTimeText.setText(dateFormat.format(item.getOrigDepartureTime()) + " - " + dateFormat.format(item.getOrigArrivalTime()));

            TextView origTripNumberText = (TextView) convertView.findViewById(R.id.orig_trip_number_text);
            origTripNumberText.setText(item.getOrigLineTripId() + " to " + item.getOrigLastStopName());

            TextView origDepartureTime = (TextView) convertView.findViewById(R.id.orig_depature_time);
            int origDepartsInMinutes = ((int) (item.getOrigDepartureTime().getTime() + (item.getOrigDelayMinutes() * 60000) - System.currentTimeMillis()) / 60000);
            origDepartureTime.setText(String.valueOf(origDepartsInMinutes + " Minutes"));

            TextView origTardyText = (TextView) convertView.findViewById(R.id.orig_tardy_text);
            if (item.getOrigDelayMinutes() > 0) {
                origTardyText.setText(item.getOrigDelayMinutes() + " min late.");
                origTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.delay_minutes));
                View origDepartingBorder = convertView.findViewById(R.id.orig_departing_border);
                origDepartingBorder.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.late_boarder));
                origDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.late_departing));
            } else {
                origTardyText.setText("On time");
                origTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.no_delay_minutes));
                origDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.late_departing));
            }

            TextView connectionStationText = (TextView) convertView.findViewById(R.id.connection_station_name);
            connectionStationText.setText(item.getConnectionStationName());

            TextView termLineNameText = (TextView) convertView.findViewById(R.id.term_line_name_text);
            termLineNameText.setText(item.getTermRouteName());

            ((ImageView) convertView.findViewById(R.id.term_line_marker_left)).setColorFilter(ContextCompat.getColor(getContext(), TransitType.RAIL.getLineColor(item.getTermRouteId(), getContext())));
            ((ImageView) convertView.findViewById(R.id.term_line_marker_right)).setColorFilter(ContextCompat.getColor(getContext(), TransitType.RAIL.getLineColor(item.getTermRouteId(), getContext())));

            TextView termArrivalTimeText = (TextView) convertView.findViewById(R.id.term_arrival_time_text);
            termArrivalTimeText.setText(dateFormat.format(item.getTermDepartureTime()) + " - " + dateFormat.format(item.getTermArrivalTime()));

            TextView termTripNumberText = (TextView) convertView.findViewById(R.id.term_trip_number_text);
            termTripNumberText.setText(item.getTermLineTripId() + " to " + item.getTermLastStopName());

            TextView termDepartureTime = (TextView) convertView.findViewById(R.id.term_depature_time);
            int termDepartsInMinutes = ((int) (item.getTermDepartureTime().getTime() + (item.getTermDelayMinutes() * 60000) - System.currentTimeMillis()) / 60000);
            termDepartureTime.setText(String.valueOf(termDepartsInMinutes + " Minutes"));

            TextView termTardyText = (TextView) convertView.findViewById(R.id.term_tardy_text);
            if (item.getTermDelayMinutes() > 0) {
                termTardyText.setText(item.getTermDelayMinutes() + " min late.");
                termTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.delay_minutes));
                View termDepartingBorder = convertView.findViewById(R.id.orig_departing_border);
                termDepartingBorder.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.late_boarder));
                termDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.late_departing));
            } else {
                termTardyText.setText("On time");
                termTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.no_delay_minutes));
                termDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.on_time_departing));
            }


            return convertView;

        }
    }

    private class SingleLinesListAdapater extends ArrayAdapter<NextToArriveLine> {

        public SingleLinesListAdapater(@NonNull Context context, List<NextToArriveLine> list) {
            super(context, 0, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            List<NextArrivalModelResponse.NextArrivalRecord> tripList = getItem(position).getList();
            Collections.sort(
                    tripList, new Comparator<NextArrivalModelResponse.NextArrivalRecord>() {
                        @Override
                        public int compare(NextArrivalModelResponse.NextArrivalRecord x, NextArrivalModelResponse.NextArrivalRecord y) {
                            return x.getOrigDepartureTime().compareTo(y.getOrigDepartureTime());
                        }
                    });

            tripList = tripList.subList(0, (3 < tripList.size()) ? 3 : tripList.size());


            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.next_to_arrive_line, parent, false);
            }

            TextView lineNameText = (TextView) convertView.findViewById(R.id.orig_line_name_text);
            lineNameText.setText(getItem(position).lineName);
            ((ImageView) convertView.findViewById(R.id.orig_line_marker_left)).setColorFilter(ContextCompat.getColor(getContext(), TransitType.RAIL.getLineColor(getItem(position).getList().get(0).getOrigRouteId(), getContext())));
            ((ImageView) convertView.findViewById(R.id.orig_line_marker_right)).setColorFilter(ContextCompat.getColor(getContext(), TransitType.RAIL.getLineColor(getItem(position).getList().get(0).getOrigRouteId(), getContext())));

            LinearLayout arrivalList = (LinearLayout) convertView.findViewById(R.id.arrival_list);
            arrivalList.removeAllViews();

            DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);

            for (NextArrivalModelResponse.NextArrivalRecord unit : tripList) {
                View line = LayoutInflater.from(getContext()).inflate(R.layout.next_to_arrive_unit, null, false);

                TextView origArrivalTimeText = (TextView) line.findViewById(R.id.orig_arrival_time_text);
                origArrivalTimeText.setText(dateFormat.format(unit.getOrigDepartureTime()) + " - " + dateFormat.format(unit.getOrigArrivalTime()));
                arrivalList.addView(line);

                TextView origTripNumberText = (TextView) line.findViewById(R.id.orig_trip_number_text);
                origTripNumberText.setText(unit.getOrigLineTripId() + " to " + unit.getOrigLastStopName());

                TextView origDepartureTime = (TextView) line.findViewById(R.id.orig_depature_time);
                int origDepartsInMinutes = ((int) (unit.getOrigDepartureTime().getTime() + (unit.getOrigDelayMinutes() * 60000) - System.currentTimeMillis()) / 60000);
                origDepartureTime.setText(String.valueOf(origDepartsInMinutes + " Minutes"));

                TextView origTardyText = (TextView) line.findViewById(R.id.orig_tardy_text);
                if (unit.getOrigDelayMinutes() > 0) {
                    origTardyText.setText(unit.getOrigDelayMinutes() + " min late.");
                    origTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.delay_minutes));
                    View origDepartingBorder = line.findViewById(R.id.orig_departing_border);
                    origDepartingBorder.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.late_boarder));
                    origDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.late_departing));
                } else {
                    origTardyText.setText("On time");
                    origTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.no_delay_minutes));
                    origDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.on_time_departing));
                }

            }
            return convertView;
        }
    }

    private class NextToArriveLine {
        List<NextArrivalModelResponse.NextArrivalRecord> nextToArriveModels = new ArrayList<NextArrivalModelResponse.NextArrivalRecord>();
        String lineName;
        Date soonestDeparture;
        boolean multiStop;

        NextToArriveLine(String lineName, boolean multiStop) {
            this.lineName = lineName;
            this.multiStop = multiStop;
        }

        List<NextArrivalModelResponse.NextArrivalRecord> getList() {
            return nextToArriveModels;
        }

        void addItem(NextArrivalModelResponse.NextArrivalRecord item) {
            nextToArriveModels.add(item);
            if (soonestDeparture != null) {
                if (item.getOrigDepartureTime().getTime() + (item.getOrigDelayMinutes() * 60000) > soonestDeparture.getTime())
                    return;
            }

            soonestDeparture = new Date(item.getOrigDepartureTime().getTime() + (item.getOrigDelayMinutes() * 60000));
        }

        public Date getSoonestDeparture() {
            return soonestDeparture;
        }

        public boolean isMultiStop() {
            return multiStop;
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




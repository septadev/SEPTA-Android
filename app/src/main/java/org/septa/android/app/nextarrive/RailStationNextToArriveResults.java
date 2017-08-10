package org.septa.android.app.nextarrive;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.septa.android.app.R;
import org.septa.android.app.domain.NextToArriveLine;
import org.septa.android.app.domain.StopModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jkampf on 8/3/17.
 */

public class RailStationNextToArriveResults extends AppCompatActivity implements OnMapReadyCallback {
    public static final String TAG = RailStationNextToArriveResults.class.getSimpleName();
    private StopModel start;
    private StopModel destination;
    private GoogleMap googleMap;
    ViewGroup bottomSheetLayout;

    public static final String STARTING_STATION = "starting_station";
    public static final String DESTINATAION_STATION = "destination_station";

    public static RailStationNextToArriveResults newInstance(StopModel start, StopModel end) {
        RailStationNextToArriveResults fragement = new RailStationNextToArriveResults();
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomSheetLayout = (ViewGroup) findViewById(R.id.bottomSheetLayout);


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

            ListView linesListView = (ListView) findViewById(R.id.lines_list_view);

            List<NextToArriveLine> linesList = new ArrayList<NextToArriveLine>(2);
            linesList.add(new NextToArriveLine());
            linesList.add(new NextToArriveLine());
            linesListView.setAdapter(new LinesListAdapater(this, linesList));
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        View mapContainer = findViewById(R.id.map_container);
        ViewGroup.LayoutParams layoutParams =
                bottomSheetLayout.getLayoutParams();
        layoutParams.height = mapContainer.getHeight();
        bottomSheetLayout.setLayoutParams(layoutParams);


        this.googleMap = googleMap;

        LatLng startingStationLatLng = new LatLng(start.getLatitude(), start.getLongitude());
        LatLng destinationStationLatLng = new LatLng(destination.getLatitude(), destination.getLongitude());

        //googleMap.moveCamera(CameraUpdateFactory.zoomTo(15));
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
                                int permissionCheck = ContextCompat.checkSelfPermission(RailStationNextToArriveResults.this,
                                        Manifest.permission.ACCESS_FINE_LOCATION);
                                googleMap.setMyLocationEnabled(true);
                                googleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Current Location"));

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

    private class LinesListAdapater extends ArrayAdapter<NextToArriveLine> {

        public LinesListAdapater(@NonNull Context context, List<NextToArriveLine> list) {
            super(context, 0, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.rail_next_to_arrive_line, parent, false);
            }

            LinearLayout arrivalList = (LinearLayout) convertView.findViewById(R.id.arrival_list);
            arrivalList.removeAllViews();

            for (int x : new int[]{1, 2, 3}) {
                View line = LayoutInflater.from(getContext()).inflate(R.layout.rail_next_to_arrive_unit, null, false);
                TextView arrivalTimeText = (TextView) line.findViewById(R.id.arrival_time_text);
                arrivalTimeText.setText("9:" + position + "" + x + "AM");
                arrivalList.addView(line);
            }

            return convertView;
        }
    }


}




package org.septa.android.app.nextarrive;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.septa.android.app.R;
import org.septa.android.app.domain.StopModel;

/**
 * Created by jkampf on 8/3/17.
 */

public class RailStationNextToArriveResults extends AppCompatActivity implements OnMapReadyCallback {
    public static final String TAG = RailStationNextToArriveResults.class.getSimpleName();
    private StopModel start;
    private StopModel destination;
    private GoogleMap googleMap;

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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        Intent intent = getIntent();
        destination = (StopModel) intent.getExtras().get(DESTINATAION_STATION);
        start = (StopModel) intent.getExtras().get(STARTING_STATION);

        if (start!= null && destination !=null) {
            TextView startingStationNameText = (TextView) findViewById(R.id.starting_station_name);
            startingStationNameText.setText(start.getStopName());

            TextView destinationStationNameText = (TextView) findViewById(R.id.destination_station_name);
            destinationStationNameText.setText(destination.getStopName());

            SupportMapFragment mapFragment = SupportMapFragment.newInstance();
            mapFragment.getMapAsync(this);

            getSupportFragmentManager().beginTransaction().add(R.id.map_container, mapFragment).commit();
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(39.9600888, -75.1570133)));
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(10));
        googleMap.setMyLocationEnabled(true);

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
                                LatLng latLong = new LatLng(location.getLatitude(), location.getLongitude());
                                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLong));
                                googleMap.moveCamera(CameraUpdateFactory.zoomTo(15));
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
}

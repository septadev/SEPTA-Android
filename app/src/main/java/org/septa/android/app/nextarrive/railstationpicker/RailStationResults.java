package org.septa.android.app.nextarrive.railstationpicker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.septa.android.app.R;
import org.septa.android.app.domain.StopModel;

import java.util.concurrent.Executor;

/**
 * Created by jkampf on 8/3/17.
 */

public class RailStationResults extends Fragment implements OnMapReadyCallback {
    public static final String TAG = RailStationResults.class.getSimpleName();
    private StopModel start;
    private StopModel end;
    private GoogleMap googleMap;

    FusedLocationProviderClient mFusedLocationClient;

    public static RailStationResults newInstance(StopModel start, StopModel end, FusedLocationProviderClient mFusedLocationClient) {
        RailStationResults fragement = new RailStationResults();
        fragement.setStart(start);
        fragement.setEnd(end);
        fragement.setmFusedLocationClient(mFusedLocationClient);

        return fragement;
    }

    public void setmFusedLocationClient(FusedLocationProviderClient mFusedLocationClient) {
        this.mFusedLocationClient = mFusedLocationClient;
    }

    public void setEnd(StopModel end) {
        this.end = end;
    }

    public void setStart(StopModel start) {
        this.start = start;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.rail_next_to_arrive_results, container, false);

        TextView startingStationNameText = (TextView) rootView.findViewById(R.id.starting_station_name);
        startingStationNameText.setText(start.getStopName());

        TextView destinationStationNameText = (TextView) rootView.findViewById(R.id.destination_station_name);
        destinationStationNameText.setText(end.getStopName());

        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        mapFragment.getMapAsync(this);

        getChildFragmentManager().beginTransaction().add(R.id.map_container, mapFragment).commit();

        return rootView;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(39.9600888, -75.1570133)));
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(10));

        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location Permission Granted.");
            Task<Location> locationTask = mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
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
}

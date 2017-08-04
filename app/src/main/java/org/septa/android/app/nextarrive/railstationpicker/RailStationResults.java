package org.septa.android.app.nextarrive.railstationpicker;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
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

public class RailStationResults extends Fragment {
    private StopModel start;
    private StopModel end;

    public static RailStationResults newInstance(StopModel start, StopModel end) {
        RailStationResults fragement = new RailStationResults();
        fragement.setStart(start);
        fragement.setEnd(end);

        return fragement;
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

        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        @SuppressWarnings("MissingPermission")
        Task<Location> locationTask = mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            LatLng latLong = new LatLng(location.getLatitude(), location.getLongitude());
                            CameraPosition pos = new CameraPosition.Builder().target(latLong).zoom(15).build();
                            GoogleMapOptions options = new GoogleMapOptions();
                            options.camera(pos);

                            SupportMapFragment mapFragment = SupportMapFragment.newInstance(options);

                            getChildFragmentManager().beginTransaction().add(R.id.map_container, mapFragment).commit();
                        }
                    }
                });

        return rootView;
    }
}

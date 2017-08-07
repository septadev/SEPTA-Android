package org.septa.android.app.nextarrive.railstationpicker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.septa.android.app.R;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.nextarrive.RailTabActivityHandler;
import org.septa.android.app.support.BiConsumer;
import org.septa.android.app.support.Consumer;
import org.septa.android.app.support.CursorAdapterSupplier;
import org.septa.android.app.support.LocationMathHelper;

/**
 * Created by jkampf on 8/3/17.
 */

public class RailStationQuery extends Fragment {
    private StopModel startingStation;
    private StopModel endingStation;


    private BiConsumer<StopModel, StopModel> consumer;

    private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;

    public static RailStationQuery newInstance(BiConsumer<StopModel, StopModel> consumer, CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
        RailStationQuery fragment = new RailStationQuery();
        fragment.setConsumer(consumer);
        fragment.setCursorAdapterSupplier(cursorAdapterSupplier);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.rail_next_to_arrive_search, container, false);

        final EditText startingStationEditText = (EditText) rootView.findViewById(R.id.starting_rail_station);
        final EditText endingStationEditText = (EditText) rootView.findViewById(R.id.ending_rail_station);
        final TextView closestStationText = (TextView) rootView.findViewById(R.id.closest_station);
        final Button queryButton = (Button) rootView.findViewById(R.id.view_trains_button);


        final AsyncTask<Location, Void, StopModel> task = new AsyncTask<Location, Void, StopModel>() {

            @Override
            protected StopModel doInBackground(Location... locations) {
                StopModel closestStop = null;
                Location location = locations[0];

                LatLng center = new LatLng(location.getLatitude(), location.getLongitude());

                if (location != null) {
                    LatLng p1 = LocationMathHelper.calculateDerivedPosition(center, 5, 0);
                    LatLng p2 = LocationMathHelper.calculateDerivedPosition(center, 5, 90);
                    LatLng p3 = LocationMathHelper.calculateDerivedPosition(center, 5, 180);
                    LatLng p4 = LocationMathHelper.calculateDerivedPosition(center, 5, 270);

                    String whereCaluse = "CAST(stop_lon as decimal)>" + String.valueOf(p4.longitude) +
                            " AND CAST(stop_lon as decimal)<" + String.valueOf(p2.longitude) +
                            " AND CAST(stop_lat as decimal)<" + String.valueOf(p1.latitude) +
                            " AND CAST(stop_lat as decimal)>" + String.valueOf(p3.latitude);

                    Cursor cursor = null;
                    try {
                        cursor = cursorAdapterSupplier.getCursor(getActivity(), whereCaluse);

                        double closestDistance = Double.MAX_VALUE;
                        if (cursor.moveToFirst()) {
                            StopModel stop = cursorAdapterSupplier.getCurrentItemFromCursor(cursor);
                            while (stop != null) {
                                LatLng stopPoint = new LatLng(stop.getLatitude(), stop.getLongitude());
                                double distance = LocationMathHelper.distance(center, stopPoint);
                                if (distance < closestDistance) {
                                    closestStop = stop;
                                    closestDistance = distance;
                                }
                                if (cursor.moveToNext())
                                    stop = cursorAdapterSupplier.getCurrentItemFromCursor(cursor);
                                else stop = null;
                            }
                        }
                    } finally {
                        if (cursor != null)
                            cursor.close();
                    }
                }


                return closestStop;
            }

            @Override
            protected void onPostExecute(StopModel stopModel) {
                if (stopModel != null && startingStation == null) {
                    startingStation = stopModel;
                    startingStationEditText.setText(stopModel.getStopName());
                    closestStationText.setVisibility(View.VISIBLE);
                }
            }

        };


        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED)

        {
            LocationServices.getFusedLocationProviderClient(getActivity()).getLastLocation();
            Task<Location> locationTask = LocationServices.getFusedLocationProviderClient(getActivity()).getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    task.execute(location);
                }
            });

        }
        startingStationEditText.setOnTouchListener(new RailStationQuery.StationPickerOnTouchListener(this, new Consumer<StopModel>() {
                    @Override
                    public void accept(StopModel var1) {
                        startingStation = var1;
                        startingStationEditText.setText(var1.getStopName());
                        closestStationText.setVisibility(View.INVISIBLE);
                    }
                }, cursorAdapterSupplier)
        );

        endingStationEditText.setOnTouchListener(new RailStationQuery.StationPickerOnTouchListener(this, new Consumer<StopModel>() {
                    @Override
                    public void accept(StopModel var1) {
                        endingStation = var1;
                        endingStationEditText.setText(var1.getStopName());
                    }
                }, cursorAdapterSupplier)
        );

        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (startingStation == null || endingStation == null) {
                    Toast.makeText(getActivity(), "Need to choose a start and end station.", Toast.LENGTH_SHORT).show();
                    return;
                }
                consumer.accept(startingStation, endingStation);
            }
        });


        return rootView;
    }

    public static class StationPickerOnTouchListener implements View.OnTouchListener {
        private Fragment parent;
        private Consumer<StopModel> consumer;
        private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;

        StationPickerOnTouchListener(Fragment parent, Consumer<StopModel> consumer, CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
            this.parent = parent;
            this.consumer = consumer;
            this.cursorAdapterSupplier = cursorAdapterSupplier;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int action = motionEvent.getActionMasked();
            if (action == MotionEvent.ACTION_UP) {

                FragmentTransaction ft = parent.getFragmentManager().beginTransaction();
                Fragment prev = parent.getFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                // Create and show the dialog.
                RailStationPickerFragment newFragment = RailStationPickerFragment.newInstance(consumer, cursorAdapterSupplier);
                newFragment.show(ft, "dialog");

                return true;
            }
            return false;
        }
    }

    public void setConsumer(BiConsumer<StopModel, StopModel> consumer) {
        this.consumer = consumer;
    }

    public void setCursorAdapterSupplier(CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
        this.cursorAdapterSupplier = cursorAdapterSupplier;
    }

}

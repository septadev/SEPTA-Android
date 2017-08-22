package org.septa.android.app.nextarrive;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.septa.android.app.R;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.nextarrive.locationpicker.FinderClosestStopTask;
import org.septa.android.app.nextarrive.locationpicker.LocationPickerFragment;
import org.septa.android.app.support.BaseTabActivityHandler;
import org.septa.android.app.support.Consumer;
import org.septa.android.app.support.CursorAdapterSupplier;


/**
 * Created by jkampf on 7/29/17.
 */

public class LineUnawareLocationPickerTabActivityHandler extends BaseTabActivityHandler {
    private static final String TAG = "LineUnawareLocationPickerTabActivityHandler";
    private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;

    public LineUnawareLocationPickerTabActivityHandler(String title, CursorAdapterSupplier<StopModel> cursorAdapterSupplier, int iconDrawable) {
        super(title, iconDrawable);
        this.cursorAdapterSupplier = cursorAdapterSupplier;
    }


    @Override
    public Fragment getFragment() {
        return LineUnawareLocationPickerTabActivityHandler.RailStationQuery.newInstance(cursorAdapterSupplier);
    }

    public static class RailStationQuery extends Fragment {
        private StopModel startingStation;
        private StopModel endingStation;
        private EditText startingStationEditText;
        private TextView closestStationText;
        private boolean startingStationAutoChoice = false;

        private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;

        public static RailStationQuery newInstance(CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
            RailStationQuery fragment = new RailStationQuery();
            fragment.setCursorAdapterSupplier(cursorAdapterSupplier);
            return fragment;
        }

        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.line_unaware_next_to_arrive_search, container, false);

            startingStationEditText = (EditText) rootView.findViewById(R.id.starting_rail_station);
            final EditText endingStationEditText = (EditText) rootView.findViewById(R.id.ending_rail_station);
            closestStationText = (TextView) rootView.findViewById(R.id.closest_station);
            final Button queryButton = (Button) rootView.findViewById(R.id.view_trains_button);

            if (startingStationAutoChoice)
                closestStationText.setVisibility(View.VISIBLE);
            else closestStationText.setVisibility(View.INVISIBLE);

            final AsyncTask<Location, Void, StopModel> task = new FinderClosestStopTask(getActivity(), cursorAdapterSupplier, new Consumer<StopModel>() {
                @Override
                public void accept(StopModel stopModel) {
                    if (stopModel != null && startingStation == null) {
                        startingStationAutoChoice = true;
                        setStartingStation(stopModel, View.VISIBLE);
                    }
                }
            });

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
                            startingStationAutoChoice = false;
                            setStartingStation(var1, View.INVISIBLE);
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
                    Intent intent = new Intent(getActivity(), NextToArriveResultsActivity.class);
                    intent.putExtra(NextToArriveResultsActivity.STARTING_STATION, startingStation);
                    intent.putExtra(NextToArriveResultsActivity.DESTINATAION_STATION, endingStation);

                    startActivity(intent);
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
                    LocationPickerFragment newFragment = LocationPickerFragment.newInstance(consumer, cursorAdapterSupplier);
                    newFragment.show(ft, "dialog");

                    return true;
                }
                return false;
            }
        }

        public void setCursorAdapterSupplier(CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
            this.cursorAdapterSupplier = cursorAdapterSupplier;
        }

        private void setStartingStation(StopModel start, int invisible) {
            startingStation = start;
            startingStationEditText.setText(startingStation.getStopName());
            closestStationText.setVisibility(invisible);
        }

    }
}

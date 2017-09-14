package org.septa.android.app.locationpicker;

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

import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.support.BaseTabActivityHandler;
import org.septa.android.app.support.Consumer;
import org.septa.android.app.support.CursorAdapterSupplier;


/**
 * Created by jkampf on 7/29/17.
 */

public class LineUnawareLocationPickerTabActivityHandler extends BaseTabActivityHandler {
    private static final String TAG = "LineUnawareLocationPickerTabActivityHandler";
    private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;
    private TransitType transitType;
    private Class targetClass;
    String headerStringName;
    String buttonText;

    public LineUnawareLocationPickerTabActivityHandler(String title, String headerStringName, String buttonText, TransitType transitType, CursorAdapterSupplier<StopModel> cursorAdapterSupplier, Class targetClass) {
        super(title, transitType.getTabInactiveImageResource(), transitType.getTabActiveImageResource());
        this.cursorAdapterSupplier = cursorAdapterSupplier;
        this.transitType = transitType;
        this.targetClass = targetClass;
        this.headerStringName = headerStringName;
        this.buttonText = buttonText;
    }


    @Override
    public Fragment getFragment() {
        return LineUnawareLocationPickerTabActivityHandler.RailStationQuery.newInstance(cursorAdapterSupplier, transitType, targetClass, headerStringName, buttonText);
    }

    public static class RailStationQuery extends Fragment {
        private StopModel startingStation;
        private StopModel endingStation;
        private TextView startingStationEditText;
        private TextView closestStationText;
        private boolean startingStationAutoChoice = false;
        private TransitType transitType;
        private Class targetClass;
        private String headerStringName;
        private String buttonText;

        private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;

        public static RailStationQuery newInstance(CursorAdapterSupplier<StopModel> cursorAdapterSupplier, TransitType transitType, Class targetClass, String headerStringName, String buttonText) {
            RailStationQuery fragment = new RailStationQuery();
            fragment.setCursorAdapterSupplier(cursorAdapterSupplier);
            fragment.setTransitType(transitType);
            fragment.setTargetClass(targetClass);
            fragment.setHeaderStringName(headerStringName);
            fragment.setButtonText(buttonText);
            return fragment;
        }

        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.line_unaware_next_to_arrive_search, container, false);

            if (getContext() == null){
                return rootView;
            }
            startingStationEditText = (TextView) rootView.findViewById(R.id.starting_stop);
            final TextView endingStationEditText = (TextView) rootView.findViewById(R.id.destination_stop);
            closestStationText = (TextView) rootView.findViewById(R.id.closest_stop);
            final Button queryButton = (Button) rootView.findViewById(R.id.view_buses_button);

            TextView pickerHeaderText = (TextView) rootView.findViewById(R.id.picker_header_text);
            pickerHeaderText.setText(transitType.getString(headerStringName, getContext()));

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

            queryButton.setText(buttonText);
            queryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (startingStation == null || endingStation == null) {
                        Toast.makeText(getActivity(), "Need to choose a start and end station.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(getActivity(), targetClass);
                    intent.putExtra(Constants.STARTING_STATION, startingStation);
                    intent.putExtra(Constants.DESTINATAION_STATION, endingStation);
                    intent.putExtra(Constants.TRANSIT_TYPE, transitType);

                    startActivity(intent);
                }
            });

            return rootView;
        }

        public void setTargetClass(Class targetClass) {
            this.targetClass = targetClass;
        }

        public void setHeaderStringName(String headerStringName) {
            this.headerStringName = headerStringName;
        }

        public void setButtonText(String buttonText) {
            this.buttonText = buttonText;
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

        public void setTransitType(TransitType transitType) {
            this.transitType = transitType;
        }

        private void setStartingStation(StopModel start, int invisible) {
            startingStation = start;
            startingStationEditText.setText(startingStation.getStopName());
            closestStationText.setVisibility(invisible);
        }

    }
}

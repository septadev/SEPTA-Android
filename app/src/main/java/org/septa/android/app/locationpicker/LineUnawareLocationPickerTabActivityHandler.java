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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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

public class LineUnawareLocationPickerTabActivityHandler extends BaseTabActivityHandler {
    private static final String TAG = LineUnawareLocationPickerTabActivityHandler.class.getSimpleName();
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
        return LineUnawareLocationPickerFragment.newInstance(cursorAdapterSupplier, transitType, targetClass, headerStringName, buttonText);
    }

    public static class LineUnawareLocationPickerFragment extends Fragment {
        private static final int START_MODEL_ID = 1;
        private static final int DEST_MODEL_ID = 2;
        private StopModel startingStation;
        private StopModel destinationStation;
        private TextView startingStationEditText;
        private TextView endingStationEditText;
        private TransitType transitType;
        private Class targetClass;
        private String headerStringName;
        private String buttonText;
        private Button queryButton;

        private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;

        public static LineUnawareLocationPickerFragment newInstance(CursorAdapterSupplier<StopModel> cursorAdapterSupplier, TransitType transitType, Class targetClass, String headerStringName, String buttonText) {
            LineUnawareLocationPickerFragment fragment = new LineUnawareLocationPickerFragment();
            Bundle args = new Bundle();
            args.putSerializable("cursorAdapterSupplier", cursorAdapterSupplier);
            args.putSerializable("transitType", transitType);
            args.putSerializable("targetClass", targetClass);
            args.putString("headerStringName", headerStringName);
            args.putString("buttonText", buttonText);

            fragment.setArguments(args);

            return fragment;
        }

        private void restoreArguments() {
            cursorAdapterSupplier = (CursorAdapterSupplier<StopModel>) getArguments().getSerializable("cursorAdapterSupplier");
            transitType = (TransitType) getArguments().getSerializable("transitType");
            targetClass = (Class) getArguments().getSerializable("targetClass");
            headerStringName = getArguments().getString("headerStringName");
            buttonText = getArguments().getString("buttonText");
        }

        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            restoreArguments();
            final View rootView = inflater.inflate(R.layout.line_unaware_next_to_arrive_search, container, false);

            if (getContext() == null) {
                return rootView;
            }
            startingStationEditText = (TextView) rootView.findViewById(R.id.starting_stop);
            startingStationEditText.setText(transitType.getString("start_stop_text", getContext()));

            endingStationEditText = (TextView) rootView.findViewById(R.id.destination_stop);
            endingStationEditText.setText(transitType.getString("dest_stop_text", getContext()));

            queryButton = (Button) rootView.findViewById(R.id.view_buses_button);

            TextView pickerHeaderText = (TextView) rootView.findViewById(R.id.picker_header_text);
            pickerHeaderText.setText(transitType.getString(headerStringName, getContext()));

            final AsyncTask<Location, Void, StopModel> task = new FinderClosestStopTask(getContext(), cursorAdapterSupplier, new Consumer<StopModel>() {
                @Override
                public void accept(StopModel stopModel) {
                    if (stopModel != null && startingStation == null) {
                        setStartingStation(stopModel);
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
            startingStationEditText.setOnTouchListener(new LineUnawareLocationPickerFragment.StationPickerOnTouchListener(this, START_MODEL_ID, cursorAdapterSupplier));
            endingStationEditText.setOnTouchListener(new LineUnawareLocationPickerFragment.StationPickerOnTouchListener(this, DEST_MODEL_ID, cursorAdapterSupplier));

            queryButton.setText(buttonText);
            queryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (startingStation == null || destinationStation == null) {
                        return;
                    }
                    if (getActivity() == null)
                        return;
                    Intent intent = new Intent(getActivity(), targetClass);
                    intent.putExtra(Constants.STARTING_STATION, startingStation);
                    intent.putExtra(Constants.DESTINATION_STATION, destinationStation);
                    intent.putExtra(Constants.TRANSIT_TYPE, transitType);

                    getActivity().startActivityForResult(intent, Constants.NTA_REQUEST);
                }
            });

            queryButton.setClickable(false);


            restoreState(savedInstanceState);
            return rootView;
        }

        public static class StationPickerOnTouchListener implements View.OnTouchListener {
            private Fragment parent;
            private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;
            int requestCode;

            StationPickerOnTouchListener(Fragment parent, int requestCode, CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
                this.parent = parent;
                this.requestCode = requestCode;
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
                    LocationPickerFragment newFragment = LocationPickerFragment.newInstance(cursorAdapterSupplier, false);
                    newFragment.setTargetFragment(parent, requestCode);
                    try {
                        newFragment.show(ft, "dialog");
                    } catch (Exception e) {
                        Log.w(TAG, "Problem showing dialog for location picker.", e);
                    }

                    return true;
                }
                return false;
            }
        }

        private void setDestinationStation(StopModel var1) {
            destinationStation = var1;
            endingStationEditText.setText(var1.getStopName());

            if (startingStation != null) {
                queryButton.setAlpha(1);
                queryButton.setClickable(true);
            }
        }

        private void setStartingStation(StopModel start) {
            startingStation = start;
            startingStationEditText.setText(startingStation.getStopName());

            if (destinationStation != null) {
                queryButton.setAlpha(1);
                queryButton.setClickable(true);
            }

        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            //do what ever you want here, and get the result from intent like below
            if (requestCode == START_MODEL_ID && resultCode == LocationPickerFragment.SUCCESS) {
                StopModel var1 = (StopModel) data.getSerializableExtra(LocationPickerFragment.STOP_MODEL);
                if (var1 != null) {
                    setStartingStation(var1);
                }
                return;
            }

            if (requestCode == DEST_MODEL_ID && resultCode == LocationPickerFragment.SUCCESS) {
                StopModel var1 = (StopModel) data.getSerializableExtra(LocationPickerFragment.STOP_MODEL);
                if (var1 != null) {
                    setDestinationStation(var1);
                }
                return;
            }
        }


        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putSerializable("startingStation", startingStation);
            outState.putSerializable("destinationStation", destinationStation);
        }

        private void restoreState(Bundle outState) {
            if (outState == null) {
                return;
            }

            StopModel starting = (StopModel) outState.getSerializable("startingStation");
            if (starting != null) {
                setStartingStation(starting);
            } else {
                return;
            }

            StopModel dest = (StopModel) outState.getSerializable("destinationStation");
            if (dest != null) {
                setDestinationStation(dest);
            }
        }
    }
}

package org.septa.android.app.locationpicker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.septa.android.app.R;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.services.apiinterfaces.model.PlaceAutoComplete;
import org.septa.android.app.support.BaseTabActivityHandler;
import org.septa.android.app.support.Criteria;
import org.septa.android.app.support.CursorAdapterSupplier;
import org.septa.android.app.support.LocationMathHelper;
import org.septa.android.app.support.MapUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jkampf on 7/30/17.
 */

class ByAddressTabActivityHandler extends BaseTabActivityHandler {
    final private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;
    public static final String TAG = ByAddressTabActivityHandler.class.getSimpleName();

    public ByAddressTabActivityHandler(String s, CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
        super(s);
        this.cursorAdapterSupplier = cursorAdapterSupplier;
    }

    @Override
    public Fragment getFragment() {
        return ByAddressFragment.newInstance(cursorAdapterSupplier);
    }

    public static class ByAddressFragment extends Fragment {
        CursorAdapterSupplier<StopModel> cursorAdapterSupplier;
        MyLocationClickListener myLocationClickListener;
        ListView stopsListView;
        AutoCompleteTextView addressEntry;
        View progressView;

        public static ByAddressFragment newInstance(CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
            ByAddressFragment fragment = new ByAddressFragment();
            Bundle args = new Bundle();
            args.putSerializable("cursorAdapterSupplier", cursorAdapterSupplier);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.location_picker_by_address, container, false);

            if (getActivity() == null) {
                return rootView;
            }

            restoreArgs();
            stopsListView = (ListView) rootView.findViewById(R.id.stop_list);
            progressView = rootView.findViewById(R.id.progress_view);

            final FindClosestStationTask task = new FindClosestStationTask(this);

            myLocationClickListener = new MyLocationClickListener(this);

            addressEntry = (AutoCompleteTextView) rootView.findViewById(R.id.address_text);
            int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                Task<Location> locationTask = LocationServices.getFusedLocationProviderClient(getActivity()).getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            if (getActivity() == null)
                                return;
                            PlacesAutoCompleteAdapter placesAutoCompleteAdapter = new PlacesAutoCompleteAdapter(getActivity(), R.layout.item_autocomplete_list, new LatLng(location.getLatitude(), location.getLongitude()));
                            addressEntry.setAdapter(placesAutoCompleteAdapter);
                            addressEntry.addTextChangedListener(placesAutoCompleteAdapter);
                        }
                    }
                });

                addressEntry.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        final int DRAWABLE_LEFT = 0;
                        final int DRAWABLE_TOP = 1;
                        final int DRAWABLE_RIGHT = 2;
                        final int DRAWABLE_BOTTOM = 3;

                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            if (event.getRawX() >= (addressEntry.getRight() - addressEntry.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                                myLocationClickListener.onClick(v);
                                return true;
                            }
                        }
                        return false;
                    }
                });
            } else {
                Drawable[] drawables = addressEntry.getCompoundDrawables();
                addressEntry.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], ContextCompat.getDrawable(getContext(), R.drawable.ic_gps_not_fixed_black_24_px), drawables[3]);
            }

            addressEntry.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (getActivity() == null)
                        return;
                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
                    String description = ((PlaceAutoComplete) parent.getItemAtPosition(position)).getPlaceDesc();
                    FindAddressTask findAddressTask = new FindAddressTask(ByAddressFragment.this);
                    String[] args = new String[1];
                    args[0] = description;
                    findAddressTask.execute(args);
                }
            });

            addressEntry.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (getActivity() == null)
                        return false;
                    // If the event is a key-down event on the "enter" button
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                            (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        AutoCompleteTextView editText = (AutoCompleteTextView) v;
                        editText.dismissDropDown();
                        FindAddressTask findAddressTask = new FindAddressTask(ByAddressFragment.this);
                        String[] args = new String[1];
                        args[0] = editText.getText().toString();
                        findAddressTask.execute(args);

                        return true;
                    }
                    return false;
                }
            });

            return rootView;
        }

        private void restoreArgs() {
            cursorAdapterSupplier = (CursorAdapterSupplier<StopModel>) getArguments().getSerializable("cursorAdapterSupplier");
        }
    }

    static class FindClosestStationTask extends AsyncTask<LatLng, Void, List<StopModelWithDistance>> {

        ByAddressFragment fragment;

        FindClosestStationTask(ByAddressFragment fragment) {
            this.fragment = fragment;
        }

        @Override
        protected List<StopModelWithDistance> doInBackground(LatLng... locations) {
            if (fragment.getActivity() == null)
                return new ArrayList<StopModelWithDistance>(0);
            LatLng location = locations[0];
            List<StopModelWithDistance> returnList = new ArrayList<StopModelWithDistance>();
            if (location != null) {

                List<Criteria> criteria = new LinkedList<Criteria>();
                criteria.add(new Criteria("stop_lon", Criteria.Operation.GT, LocationMathHelper.calculateDerivedPosition(location, 5, 270).longitude));
                criteria.add(new Criteria("stop_lon", Criteria.Operation.LT, LocationMathHelper.calculateDerivedPosition(location, 5, 90).longitude));
                criteria.add(new Criteria("stop_lat", Criteria.Operation.LT, LocationMathHelper.calculateDerivedPosition(location, 5, 0).latitude));
                criteria.add(new Criteria("stop_lat", Criteria.Operation.GT, LocationMathHelper.calculateDerivedPosition(location, 5, 180).latitude));

                Cursor cursor = null;
                try {
                    cursor = fragment.cursorAdapterSupplier.getCursor(fragment.getActivity(), criteria);

                    double closestDistance = Double.MAX_VALUE;
                    if (cursor.moveToFirst()) {
                        StopModel stop = fragment.cursorAdapterSupplier.getCurrentItemFromCursor(cursor);
                        while (stop != null) {
                            LatLng stopPoint = new LatLng(stop.getLatitude(), stop.getLongitude());
                            double distance = LocationMathHelper.distance(location, stopPoint);
                            returnList.add(new StopModelWithDistance(distance, stop));
                            if (cursor.moveToNext())
                                stop = fragment.cursorAdapterSupplier.getCurrentItemFromCursor(cursor);
                            else stop = null;
                        }
                    }
                } finally {
                    if (cursor != null)
                        cursor.close();
                }
            }

            Collections.sort(returnList);
            return returnList;
        }

        @Override
        protected void onPostExecute(final List<StopModelWithDistance> stopModels) {
            if (fragment.getActivity() == null)
                return;
            fragment.stopsListView.setAdapter(new StopListAdapter(fragment.getActivity(), stopModels));
            fragment.stopsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.d(TAG, this.hashCode() + "onItemSelected " + i);
                    if (fragment.getTargetFragment() != null) {
                        Intent intent = new Intent();
                        intent.putExtra(LocationPickerFragment.STOP_MODEL, stopModels.get(i).getStopModel());
                        fragment.getTargetFragment().onActivityResult(fragment.getTargetRequestCode(), LocationPickerFragment.SUCCESS, intent);
                    }
                }
            });
            fragment.myLocationClickListener.setActive(true);
            fragment.progressView.setVisibility(View.GONE);
        }

    }

    static class MyLocationClickListener implements View.OnClickListener {
        ByAddressFragment fragment;

        public void setActive(boolean active) {
            this.active = active;
        }

        boolean active = true;

        MyLocationClickListener(ByAddressFragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void onClick(View view) {
            if (!active)
                return;

            if (fragment.getActivity() == null)
                return;

            active = false;
            int permissionCheck = ContextCompat.checkSelfPermission(fragment.getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED)

            {
                fragment.progressView.setVisibility(View.VISIBLE);
                fragment.progressView.requestFocus();
                final FindClosestStationTask task = new FindClosestStationTask(fragment);

                Task<Location> locationTask = LocationServices.getFusedLocationProviderClient(fragment.getActivity()).getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) {
                            fragment.progressView.setVisibility(View.GONE);
                            return;
                        }
                        String currentAddress = MapUtils.getCurrentAddress(fragment.getActivity());
                        if (currentAddress != null) {
                            fragment.addressEntry.setText(currentAddress);
                        }
                        task.execute(new LatLng(location.getLatitude(), location.getLongitude()));
                    }
                });

            }

        }
    }

    static class StopListAdapter extends ArrayAdapter<StopModelWithDistance> {


        public StopListAdapter(@NonNull Context context, @NonNull List<StopModelWithDistance> objects) {
            super(context, 0, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_stop_by_address, null);
            }

            TextView stationName = (TextView) convertView.findViewById(R.id.station_name_text);
            TextView distance = (TextView) convertView.findViewById(R.id.station_distance_text);
            stationName.setText(this.getItem(position).getStopModel().getStopName());
            DecimalFormat numberFormat = new DecimalFormat("0.0m");
            distance.setText(numberFormat.format(this.getItem(position).getDistance()));
            return convertView;

        }

    }

    static class FindAddressTask extends AsyncTask<String, Void, Location> {
        ByAddressFragment fragment;

        @Override
        protected void onPostExecute(Location location) {
            if (location != null) {
                FindClosestStationTask task = new FindClosestStationTask(fragment);
                task.execute(new LatLng(location.getLatitude(), location.getLongitude()));
            }
        }

        public FindAddressTask(ByAddressFragment fragment) {
            this.fragment = fragment;
        }

        @Override
        protected Location doInBackground(String... strings) {
            if (fragment.getActivity() == null) {
                return null;
            }
            return MapUtils.getLocationFromAddress(fragment.getActivity(), strings[0]);
        }

    }

    static class StopModelWithDistance implements Comparable<StopModelWithDistance> {
        Double distance;
        StopModel stopModel;

        public StopModelWithDistance(Double distance, StopModel stopModel) {
            this.distance = distance;
            this.stopModel = stopModel;
        }

        public Double getDistance() {
            return distance;
        }

        public StopModel getStopModel() {
            return stopModel;
        }

        @Override
        public int compareTo(@NonNull StopModelWithDistance stopModelWithDistance) {
            return distance.compareTo(stopModelWithDistance.distance);
        }

        @Override
        public String toString() {
            return stopModel.getStopName() + " - " + distance + " miles.";
        }
    }
}



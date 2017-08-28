package org.septa.android.app.locationpicker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.septa.android.app.R;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.support.BaseTabActivityHandler;
import org.septa.android.app.support.Consumer;
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
    final private Consumer<StopModel> consumer;
    final private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;
    public static final String TAG = ByAddressTabActivityHandler.class.getSimpleName();

    public ByAddressTabActivityHandler(String s, Consumer<StopModel> consumer, CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
        super(s);
        this.consumer = consumer;
        this.cursorAdapterSupplier = cursorAdapterSupplier;
    }

    @Override
    public Fragment getFragment() {
        return ByAddressFragement.newInstance(consumer, cursorAdapterSupplier);
    }

    public static class ByAddressFragement extends Fragment {
        Consumer<StopModel> consumer;
        CursorAdapterSupplier<StopModel> cursorAdapterSupplier;
        MyLocationClickListener myLocationClickListener;
        ListView stopsListView;
        SupportPlaceAutocompleteFragment addressEntry;
        View progressView;

        public static ByAddressFragement newInstance(Consumer<StopModel> consumer, CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
            ByAddressFragement fragment = new ByAddressFragement();
            fragment.setCursorAdapterSupplier(cursorAdapterSupplier);
            fragment.setConsumer(consumer);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.location_picker_by_address, container, false);
            addressEntry = (SupportPlaceAutocompleteFragment)
                    getChildFragmentManager().findFragmentById(R.id.address_fragement);

            View myLocationButton = rootView.findViewById(R.id.my_location_button);
            stopsListView = (ListView) rootView.findViewById(R.id.stop_list);
            progressView = rootView.findViewById(R.id.progress_view);

            final FindClosestStationTask task = new FindClosestStationTask(this);

            myLocationClickListener = new MyLocationClickListener(this);
            myLocationButton.setOnClickListener(myLocationClickListener);

            addressEntry.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    progressView.setVisibility(View.VISIBLE);
                    final FindClosestStationTask task = new FindClosestStationTask(ByAddressFragement.this);
                    task.execute(place.getLatLng());
                }

                @Override
                public void onError(Status status) {

                }
            });

            return rootView;
        }

        public void setConsumer(Consumer<StopModel> target) {
            this.consumer = target;
        }

        public void setCursorAdapterSupplier(CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
            this.cursorAdapterSupplier = cursorAdapterSupplier;
        }
    }


    static class FindClosestStationTask extends AsyncTask<LatLng, Void, List<StopModelWithDistance>> {

        ByAddressFragement fragment;

        FindClosestStationTask(ByAddressFragement fragment) {
            this.fragment = fragment;
        }


        @Override
        protected List<StopModelWithDistance> doInBackground(LatLng... locations) {
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
            fragment.stopsListView.setAdapter(new StopListAdapater(fragment.getActivity(), stopModels));
            fragment.stopsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.d(TAG, this.hashCode() + "onItemSelected " + i);
                    fragment.consumer.accept(stopModels.get(i).getStopModel());
                }
            });
            fragment.myLocationClickListener.setActive(true);
            fragment.progressView.setVisibility(View.GONE);
        }

    }

    static class MyLocationClickListener implements View.OnClickListener {
        ByAddressFragement fragment;

        public void setActive(boolean active) {
            this.active = active;
        }

        boolean active = true;

        MyLocationClickListener(ByAddressFragement fragment) {
            this.fragment = fragment;
        }

        @Override
        public void onClick(View view) {
            if (!active)
                return;

            active = false;
            fragment.progressView.setVisibility(View.VISIBLE);
            int permissionCheck = ContextCompat.checkSelfPermission(fragment.getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED)

            {
                fragment.progressView.setVisibility(View.VISIBLE);
                final FindClosestStationTask task = new FindClosestStationTask(fragment);

                Task<Location> locationTask = LocationServices.getFusedLocationProviderClient(fragment.getActivity()).getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
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

    static class StopListAdapater extends ArrayAdapter<StopModelWithDistance> {


        public StopListAdapater(@NonNull Context context, @NonNull List<StopModelWithDistance> objects) {
            super(context, 0, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.stop_by_address_list_item, null);
            }

            TextView stationName = (TextView) convertView.findViewById(R.id.station_name_text);
            TextView distance = (TextView) convertView.findViewById(R.id.station_distance_text);
            stationName.setText(this.getItem(position).getStopModel().getStopName());
            DecimalFormat numberFormat = new DecimalFormat("0.0mi");
            distance.setText(numberFormat.format(this.getItem(position).getDistance()));
            return convertView;

        }


    }

    static class FindAddressTask extends AsyncTask<String, Void, Location> {
        ByAddressFragement fragment;

        @Override
        protected void onPostExecute(Location location) {
            FindClosestStationTask task = new FindClosestStationTask(fragment);
            task.execute(new LatLng(location.getLatitude(), location.getLongitude()));
        }

        public FindAddressTask(ByAddressFragement fragment) {
            this.fragment = fragment;
        }

        @Override
        protected Location doInBackground(String... strings) {
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



/**
 * Created by acampbell on 2/3/15.
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import org.septa.android.app.R;
import org.septa.android.app.utilities.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import butterknife.OnTextChanged;

public class GeocoderFragment extends Fragment {

    private static final String TAG = GeocoderFragment.class.getName();

    private List<Address> addressList;

    @InjectView(R.id.editText_search)
    EditText searchEditText;
    @InjectView(R.id.listView_geocode)
    ListView listView;
    private GeocoderTask geocoderTask;

    public static GeocoderFragment newInstance() {
        return new GeocoderFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_geocoder, container, false);
        ButterKnife.inject(this, view);

        return view;
    }

    @OnItemClick(R.id.listView_geocode)
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Address address = addressList.get(position);
        // Return location to calling activity
        if(address != null) {
            Intent data = new Intent();
            Location location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(address.getLatitude());
            location.setLongitude(address.getLongitude());
            data.putExtra(Constants.KEY_LOCATION, location);
            getActivity().setResult(Activity.RESULT_OK, data);
            getActivity().finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(geocoderTask != null) {
            geocoderTask.cancel(true);
        }
    }

    @OnTextChanged(value = R.id.editText_search, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onTextChanged(Editable text) {
        if(!TextUtils.isEmpty(text)) {
            geocoderTask = new GeocoderTask(getActivity());
            geocoderTask.execute(text.toString());
        }
    }

    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

        private Geocoder geocoder;

        public GeocoderTask(Context context) {
            geocoder = new Geocoder(context, Locale.getDefault());
        }

        @Override
        protected List<Address> doInBackground(String... strings) {
            List<Address> addresses = new ArrayList<Address>();
            try {
                addresses = geocoder.getFromLocationName(strings[0], 5, 39.87734,
                        -75.3197712, 40.1360516, -74.8994436);
            } catch (IOException e) {
                Log.e(TAG, "Error getting geocode", e);
            }

            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
           if(isCancelled()) {
               return;
           }
           List<String> addressStrings = new ArrayList<String>();
            for (Address address : addresses) {
                String line = "";
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    line += address.getAddressLine(i) + " ";
                }
                line = line.trim();
                addressStrings.add(line);
            }

            listView.setAdapter(new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_1, addressStrings));
            addressList = addresses;
        }
    }


}

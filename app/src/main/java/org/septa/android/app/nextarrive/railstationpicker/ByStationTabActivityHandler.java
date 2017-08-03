package org.septa.android.app.nextarrive.railstationpicker;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.database.DatabaseManager;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.support.BaseTabActivityHandler;
import org.septa.android.app.support.Consumer;

import java.util.List;


/**
 * Created by jkampf on 7/30/17.
 */

public class ByStationTabActivityHandler extends BaseTabActivityHandler {

    public static final String TAG = "ByStationTabActivity";
    final private Consumer<StopModel> consumer;

    public ByStationTabActivityHandler(String s, Consumer<StopModel> consumer) {
        super(s);
        this.consumer = consumer;
    }

    @Override
    public Fragment getFragment() {
        return ByStationTabActivityHandler.PlaceholderFragment.newInstance(consumer);
    }


    public static class PlaceholderFragment extends Fragment {
        ListView list;
        List<StopModel> railStops;
        StopModel currentStop;


        public void setConsumer(Consumer<StopModel> target) {
            this.consumer = target;
        }

        private Consumer<StopModel> consumer;

        private static final int URL_LOADER = 0;


        public static ByStationTabActivityHandler.PlaceholderFragment newInstance(Consumer<StopModel> consumer) {
            ByStationTabActivityHandler.PlaceholderFragment fragment = new ByStationTabActivityHandler.PlaceholderFragment();
            fragment.setConsumer(consumer);
            return fragment;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            if (currentStop != null)
                Log.d(TAG, this.hashCode() + ":Current Stop is:" + currentStop.getStopName());
            else Log.d(TAG, this.hashCode() + ":Current Stop is null");
            outState.putSerializable("Station", currentStop);

        }




        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.rail_station_picker, container, false);
            list = (ListView) rootView.findViewById(R.id.rail_station_list);
            DatabaseManager databaseManager = DatabaseManager.getInstance(getActivity());
            railStops = databaseManager.getRailStops(getActivity());
            final StationNameAdapter itemsAdapter =
                    new StationNameAdapter(getActivity(), railStops);

            list.setAdapter(itemsAdapter);

            //final EditText pickedStation = rootView.findViewById(R.id.station_name);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.d(TAG, this.hashCode() + "onItemSelected " + i + " of " + railStops.size());
                    consumer.accept(railStops.get(i));
               }
            });

            return rootView;
        }

    }

    public static class StationNameAdapter extends ArrayAdapter<StopModel> {
        public StationNameAdapter(Context context, List<StopModel> stops) {
            super(context, 0, stops);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.rail_station_picker_item, parent, false);
            }

            TextView station_name = (TextView) convertView.findViewById(R.id.station_name);
            station_name.setText(getItem(position).getStopName());
            return convertView;
        }
    }


}
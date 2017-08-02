package org.septa.android.app.nextarrive.railstationpicker;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import app.android.septa.org.septa_android.R;
import org.septa.android.app.nextarrive.org.septa.android.app.support.BaseTabActivityHandler;


/**
 * Created by jkampf on 7/30/17.
 */

public class ByStationTabActivityHandler extends BaseTabActivityHandler {

    public static final String TAG = "ByStationTabActivity";
    final private EditText target;

    public ByStationTabActivityHandler(String s, EditText target) {
        super(s);
        this.target = target;
    }

    @Override
    public Fragment getFragment() {
        return ByStationTabActivityHandler.PlaceholderFragment.newInstance(target);
    }


    public static class PlaceholderFragment extends Fragment {
        ListView list;

        public void setTarget(EditText target) {
            this.target = target;
        }

        private EditText target;

        private static final int URL_LOADER = 0;



        public static ByStationTabActivityHandler.PlaceholderFragment newInstance(EditText target) {
            ByStationTabActivityHandler.PlaceholderFragment fragment = new ByStationTabActivityHandler.PlaceholderFragment();
            fragment.setTarget(target);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.rail_station_picker, container, false);
            //getLoaderManager().initLoader(URL_LOADER, null, this);
            //adapter = new RailStopCurserAdapter(getActivity(), cursor, false);
            final StationNameAdapter itemsAdapter =
                    new StationNameAdapter(getActivity(), new String[]{"30th Street Station", "Berwyn Station",
                            "Dalyesford Station", "Devon Station", "Exton Station", "Jefferson Station", "Malvern Station",
                            "Paoli Station"
                    });

            list = (ListView) rootView.findViewById(R.id.rail_station_list);
            list.setAdapter(itemsAdapter);

            //final EditText pickedStation = rootView.findViewById(R.id.station_name);

  //          list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           //     @Override
   //             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
  //                  Log.d(TAG, "onItemSelected");
  //                  String stationName = itemsAdapter.getItem(i);
  //                  pickedStation.setText(stationName);
  //                  target.setText(stationName);
  //              }
     //       });

            return rootView;
        }

     }

    public static class StationNameAdapter extends ArrayAdapter<String> {
        public StationNameAdapter(Context context, String[] strings) {
            super(context, 0, strings);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.rail_station_picker_item, parent, false);
            }

            TextView station_name = (TextView) convertView.findViewById(R.id.station_name);
            station_name.setText(getItem(position));
            return convertView;
        }
    }


}
package org.septa.android.app.nextarrive.railstationpicker;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
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
import org.septa.android.app.support.CursorAdapterSupplier;

import java.util.List;


/**
 * Created by jkampf on 7/30/17.
 */

public class ByStationTabActivityHandler extends BaseTabActivityHandler {

    public static final String TAG = "ByStationTabActivity";
    final private Consumer<StopModel> consumer;
    final private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;

    public ByStationTabActivityHandler(String s, Consumer<StopModel> consumer, CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
        super(s);
        this.consumer = consumer;
        this.cursorAdapterSupplier = cursorAdapterSupplier;
    }

    @Override
    public Fragment getFragment() {
        ByStationTabActivityHandler.PlaceholderFragment fragment = ByStationTabActivityHandler.PlaceholderFragment.newInstance(consumer, cursorAdapterSupplier);

        return fragment;
    }


    public static class PlaceholderFragment extends Fragment {
        ListView list;
        StopModel currentStop;

        public void setConsumer(Consumer<StopModel> target) {
            this.consumer = target;
        }

        private Consumer<StopModel> consumer;
        private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;

        private static final int URL_LOADER = 0;

        public static ByStationTabActivityHandler.PlaceholderFragment newInstance(Consumer<StopModel> consumer, CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
            ByStationTabActivityHandler.PlaceholderFragment fragment = new ByStationTabActivityHandler.PlaceholderFragment();
            fragment.setConsumer(consumer);
            fragment.setCursorAdapterSupplier(cursorAdapterSupplier);
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

            //cursorAdapterSupplier = DatabaseManager.getInstance(getActivity()).getRailStopCursorAdapterSupplier();

            final StationNameAdapter itemsAdapter =
                    new StationNameAdapter(getActivity(), cursorAdapterSupplier);

            list.setAdapter(itemsAdapter);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.d(TAG, this.hashCode() + "onItemSelected " + i);
                    consumer.accept(cursorAdapterSupplier.getItemFromId(getActivity(), view.getTag()));
                }
            });

            return rootView;
        }

        public void setCursorAdapterSupplier(CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
            this.cursorAdapterSupplier = cursorAdapterSupplier;
        }
    }

    public static class StationNameAdapter extends CursorAdapter {

        private Context context;
        private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;

        public StationNameAdapter(Context context, CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
            super(context, cursorAdapterSupplier.getCursor(context), 0);
            this.context = context;
            this.cursorAdapterSupplier = cursorAdapterSupplier;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.rail_station_picker_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            StopModel stop = cursorAdapterSupplier.getCurrentItemFromCursor(cursor);
            TextView station_name = (TextView) view.findViewById(R.id.station_name);
            station_name.setText(stop.getStopName());
            view.setTag(stop.getStopId());
        }
    }


}
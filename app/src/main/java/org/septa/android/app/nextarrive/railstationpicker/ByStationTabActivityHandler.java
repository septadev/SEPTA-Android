package org.septa.android.app.nextarrive.railstationpicker;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.support.BaseTabActivityHandler;
import org.septa.android.app.support.Consumer;
import org.septa.android.app.support.CursorAdapterSupplier;

import java.util.ArrayList;
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
        private ListView list = null;
        private StopModel currentStop;
        private Consumer<StopModel> consumer;
        private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;
        View progressView;
        StationNameAdapter itemsAdapter;
        StationNameAdapter2 itemAdapater2;

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
        public void onResume() {
            Log.d(TAG, "onResume()");
            super.onResume();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.d(TAG, "onCreateView()");
            View rootView = inflater.inflate(R.layout.rail_station_picker_by_station, container, false);
            list = (ListView) rootView.findViewById(R.id.rail_station_list);
            progressView = rootView.findViewById(R.id.progress_view);

            progressView.setVisibility(View.VISIBLE);

            final ListView localList = list;

            final Handler h = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    Log.d(TAG, "handleMessage");
                    if (msg.what == 0) {
                        list.setAdapter(itemAdapater2);
                        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                Log.d(TAG, this.hashCode() + "onItemSelected " + i);
                                consumer.accept(cursorAdapterSupplier.getItemFromId(getActivity(), view.getTag()));
                            }
                        });
                        progressView.setVisibility(View.GONE);
                    } else {
                    }
                    Log.d(TAG, "handleMessage - Done");
                }
            };


            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    Log.d(TAG, "creating cursor Adapater");
                    List<StopModel> stops = new ArrayList<StopModel>();
                    Cursor c = cursorAdapterSupplier.getCursor(getActivity(), null);
                    if (c.moveToFirst()) {
                        do {
                            StopModel stop = cursorAdapterSupplier.getCurrentItemFromCursor(c);
                            stops.add(stop);
                        } while (c.moveToNext());
                    }
                    itemAdapater2 =
                            new StationNameAdapter2(getActivity(), stops);
                    Log.d(TAG, "creating cursor Adapater - Done");
                    h.sendEmptyMessage(0);
                    return null;
                }
            }.execute();

            Log.d(TAG, "onCreateView() - DONE");
            return rootView;
        }

        public void setConsumer(Consumer<StopModel> target) {
            this.consumer = target;
        }

        public void setCursorAdapterSupplier(CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
            this.cursorAdapterSupplier = cursorAdapterSupplier;
        }
    }


    public static class StationNameAdapter2 extends ArrayAdapter<StopModel> {
        public StationNameAdapter2(@NonNull Context context, @NonNull List<StopModel> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.rail_station_picker_item, parent, false);
            }

            StopModel stop = getItem(position);
            TextView station_name = (TextView) convertView.findViewById(R.id.station_name);
            station_name.setText(stop.getStopName());
            convertView.setTag(stop.getStopId());

            return convertView;
        }

    }


    public static class StationNameAdapter extends CursorAdapter {

        private Context context;
        private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;

        public StationNameAdapter(Context context, CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
            super(context, cursorAdapterSupplier.getCursor(context, null), 0);
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
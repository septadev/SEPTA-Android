package org.septa.android.app.locationpicker;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
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

public class ByStopTabActivityHandler extends BaseTabActivityHandler {

    public static final String TAG = "ByStationTabActivity";
    final private Consumer<StopModel> consumer;
    final private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;

    public ByStopTabActivityHandler(String s, Consumer<StopModel> consumer, CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
        super(s);
        this.consumer = consumer;
        this.cursorAdapterSupplier = cursorAdapterSupplier;
    }

    @Override
    public Fragment getFragment() {
        ByStopTabActivityHandler.PlaceholderFragment fragment = ByStopTabActivityHandler.PlaceholderFragment.newInstance(consumer, cursorAdapterSupplier);

        return fragment;
    }


    public static class PlaceholderFragment extends Fragment {
        private ListView list = null;
        private StopModel currentStop;
        private Consumer<StopModel> consumer;
        private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;
        View progressView;
        StationNameAdapter2 itemAdapater2;

        private static final int URL_LOADER = 0;

        public static ByStopTabActivityHandler.PlaceholderFragment newInstance(Consumer<StopModel> consumer, CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
            ByStopTabActivityHandler.PlaceholderFragment fragment = new ByStopTabActivityHandler.PlaceholderFragment();
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
            View rootView = inflater.inflate(R.layout.location_picker_by_stop, container, false);
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
                                consumer.accept(cursorAdapterSupplier.getItemFromId(getContext(), view.getTag()));
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
                    Cursor c = cursorAdapterSupplier.getCursor(getContext(), null);
                    if (c.moveToFirst()) {
                        do {
                            StopModel stop = cursorAdapterSupplier.getCurrentItemFromCursor(c);
                            stops.add(stop);
                        } while (c.moveToNext());
                    }
                    Context context = getContext();
                    if (context != null) {
                        itemAdapater2 =
                                new StationNameAdapter2(getContext(), stops);
                        Log.d(TAG, "creating cursor Adapater - Done");
                    }
                    h.sendEmptyMessage(0);
                    return null;
                }
            }.execute();

            EditText filterText = (EditText) rootView.findViewById(R.id.station_filter);

            filterText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (itemAdapater2 != null)
                        itemAdapater2.getFilter().filter(charSequence.toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });

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


    public static class StationNameAdapter2 extends ArrayAdapter<StopModel> implements Filterable {

        List<StopModel> origRoutes;
        List<StopModel> filterRoutes;

        public StationNameAdapter2(@NonNull Context context, @NonNull List<StopModel> objects) {
            super(context, R.layout.stop_picker_item, objects);
            origRoutes = objects;
            filterRoutes = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.stop_picker_item, parent, false);
            }

            StopModel stop = getItem(position);
            TextView station_name = (TextView) convertView.findViewById(R.id.station_name);
            station_name.setText(stop.getStopName());
            convertView.setTag(stop.getStopId());

            return convertView;
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint == null || "".equals(constraint.toString().trim())) {
                        filterResults.values = origRoutes;
                        filterResults.count = origRoutes.size();

                        return filterResults;
                    }

                    ArrayList<StopModel> tempList = new ArrayList<StopModel>();

                    String constraintString = constraint.toString().toLowerCase();

                    for (StopModel item : origRoutes) {
                        if (safeContains(item.getStopId(), constraintString) ||
                                safeContains(item.getStopName(), constraintString)) {
                            tempList.add(item);
                        }
                    }

                    filterResults.values = tempList;
                    filterResults.count = tempList.size();

                    return filterResults;
                }

                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(CharSequence contraint, FilterResults results) {
                    filterRoutes = (List<StopModel>) results.values;
                    if (results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
        }

        public int getCount() {
            return filterRoutes.size();
        }

        public StopModel getItem(int position) {
            return filterRoutes.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        static boolean safeContains(String target, String input) {
            if (target == null) {
                return false;
            }

            return target.toLowerCase().contains(input);
        }

    }

}
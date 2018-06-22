package org.septa.android.app.locationpicker;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
import org.septa.android.app.support.CursorAdapterSupplier;

import java.util.ArrayList;
import java.util.List;

public class ByStopTabActivityHandler extends BaseTabActivityHandler {

    public static final String TAG = ByStopTabActivityHandler.class.getSimpleName();
    final private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;
    StopPickerTabListener mListener;

    public ByStopTabActivityHandler(StopPickerTabListener listener, String s, CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
        super(s);
        this.mListener = listener;
        this.cursorAdapterSupplier = cursorAdapterSupplier;
    }

    @Override
    public Fragment getFragment() {
        ByStopTabActivityHandler.PlaceholderFragment fragment = ByStopTabActivityHandler.PlaceholderFragment.newInstance(mListener, cursorAdapterSupplier);

        return fragment;
    }

    public static class PlaceholderFragment extends Fragment {
        private ListView list = null;
        private StopModel currentStop;
        private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;
        View progressView;
        StationNameAdapter2 itemAdapter2;
        StopPickerTabListener mListener;

        private static final int URL_LOADER = 0;

        public static ByStopTabActivityHandler.PlaceholderFragment newInstance(StopPickerTabListener listener, CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
            ByStopTabActivityHandler.PlaceholderFragment fragment = new ByStopTabActivityHandler.PlaceholderFragment();

            fragment.mListener = listener;

            Bundle args = new Bundle();
            args.putSerializable("cursorAdapterSupplier", cursorAdapterSupplier);
            fragment.setArguments(args);

            return fragment;
        }

        private void restoreArgs() {
            cursorAdapterSupplier = (CursorAdapterSupplier<StopModel>) getArguments().getSerializable("cursorAdapterSupplier");
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
        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.d(TAG, "onCreateView()");
            restoreArgs();
            View rootView = inflater.inflate(R.layout.location_picker_by_stop, container, false);
            list = rootView.findViewById(R.id.rail_station_list);
            progressView = rootView.findViewById(R.id.progress_view);

            progressView.setVisibility(View.VISIBLE);

            final Handler h = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    Log.d(TAG, "handleMessage");
                    if (msg.what == 0) {
                        list.setAdapter(itemAdapter2);
                        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                Log.d(TAG, this.hashCode() + " onItemSelected " + i);

                                Intent intent = new Intent();
                                intent.putExtra(LocationPickerFragment.STOP_MODEL, cursorAdapterSupplier.getItemFromId(getContext(), view.getTag()));

                                mListener.onStopSelected(intent);
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
                    Log.d(TAG, "creating cursor Adapter");
                    List<StopModel> stops = new ArrayList<StopModel>();
                    Context context = getContext();
                    if (context == null)
                        return null;
                    Cursor c = cursorAdapterSupplier.getCursor(getContext(), null);
                    if (c.moveToFirst()) {
                        do {
                            StopModel stop = cursorAdapterSupplier.getCurrentItemFromCursor(c);
                            stops.add(stop);
                        } while (c.moveToNext());
                    }
                    itemAdapter2 =
                            new StationNameAdapter2(context, stops);
                    Log.d(TAG, "creating cursor Adapter - Done");
                    h.sendEmptyMessage(0);
                    return null;
                }
            }.execute();

            EditText filterText = rootView.findViewById(R.id.station_filter);

            filterText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (itemAdapter2 != null)
                        itemAdapter2.getFilter().filter(charSequence.toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });

            Log.d(TAG, "onCreateView() - DONE");
            return rootView;
        }
    }

    public static class StationNameAdapter2 extends ArrayAdapter<StopModel> implements Filterable {

        List<StopModel> origRoutes;
        List<StopModel> filterRoutes;

        public StationNameAdapter2(@NonNull Context context, @NonNull List<StopModel> objects) {
            super(context, R.layout.item_stop_picker, objects);
            origRoutes = objects;
            filterRoutes = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_stop_picker, parent, false);
            }

            StopModel stop = getItem(position);
            TextView station_name = convertView.findViewById(R.id.station_name);
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

                    ArrayList<StopModel> tempList = new ArrayList<>();

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
                protected void publishResults(CharSequence constraint, FilterResults results) {
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
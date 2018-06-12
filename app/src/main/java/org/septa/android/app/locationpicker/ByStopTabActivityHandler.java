package org.septa.android.app.locationpicker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import java.util.Collections;
import java.util.List;


/**
 * Created by jkampf on 7/30/17.
 */

public class ByStopTabActivityHandler extends BaseTabActivityHandler {

    public static final String TAG = ByStopTabActivityHandler.class.getSimpleName();
    final private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;

    public ByStopTabActivityHandler(String s, CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
        super(s);
        this.cursorAdapterSupplier = cursorAdapterSupplier;
    }

    @Override
    public Fragment getFragment() {
        ByStopTabActivityHandler.PlaceholderFragment fragment = ByStopTabActivityHandler.PlaceholderFragment.newInstance(cursorAdapterSupplier);

        return fragment;
    }

    public static class PlaceholderFragment extends Fragment implements LoadStopsForPicker.LoadStopsForPickerListener {
        private ListView list = null;
        StationNameAdapter2 adapter;
        private StopModel currentStop;
        private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;
        View progressView, sortAlphabeticalButton, sortInOrderButton;
        EditText filterText;

        private static final int URL_LOADER = 0;

        public static ByStopTabActivityHandler.PlaceholderFragment newInstance(CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
            ByStopTabActivityHandler.PlaceholderFragment fragment = new ByStopTabActivityHandler.PlaceholderFragment();
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
            restoreArgs();
            View rootView = inflater.inflate(R.layout.location_picker_by_stop, container, false);
            sortAlphabeticalButton = rootView.findViewById(R.id.stop_list_sort_alphabetical);
            sortInOrderButton = rootView.findViewById(R.id.stop_list_sort_in_order);
            list = (ListView) rootView.findViewById(R.id.rail_station_list);
            progressView = rootView.findViewById(R.id.progress_view);
            filterText = (EditText) rootView.findViewById(R.id.station_filter);

            progressView.setVisibility(View.VISIBLE);

            // load stops for the picker dialog
            LoadStopsForPicker loadStopsForPicker = new LoadStopsForPicker(getContext(), PlaceholderFragment.this, cursorAdapterSupplier);
            loadStopsForPicker.execute();

            // double tap listener for alphabetical button
            final GestureDetector mDetector = new GestureDetector(getContext(), new AlphabeticalSortButtonGestureListener());
            View.OnTouchListener touchListener = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    // pass the events to the gesture detector
                    // a return value of true means the detector is handling it
                    // a return value of false means the detector didn't
                    // recognize the event
                    return mDetector.onTouchEvent(motionEvent);
                }
            };
            sortAlphabeticalButton.setOnTouchListener(touchListener);

            // button to sort stops in order
            sortInOrderButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    sortStopsInOrder();
                    return false;
                }
            });

            return rootView;
        }

        @Override
        public void afterStopsLoaded(final StationNameAdapter2 adapter) {
            this.adapter = adapter;
            list.setAdapter(this.adapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.d(TAG, this.hashCode() + "onItemSelected " + i);

                    if (getTargetFragment() != null) {
                        Intent intent = new Intent();
                        intent.putExtra(LocationPickerFragment.STOP_MODEL, cursorAdapterSupplier.getItemFromId(getContext(), view.getTag()));
                        getTargetFragment().onActivityResult(getTargetRequestCode(), LocationPickerFragment.SUCCESS, intent);
                    }
                }
            });

            // set default sort based on shared preferences
            switch (LocationPickerUtils.getStopPickerSortOrder(getContext())) {
                case 2:
                    // in stop order
                    sortStopsInOrder();
                    break;
                case 1:
                    // reverse alphabetical
                    sortStopsReverseAlphabetical();
                    break;
                case 0:
                    // alphabetical
                    sortStopsAlphabetical();
                    break;
            }

            progressView.setVisibility(View.GONE);

            // add search filter to loaded stops
            filterText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (adapter != null) {
                        adapter.getFilter().filter(charSequence.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
        }

        private class AlphabeticalSortButtonGestureListener extends GestureDetector.SimpleOnGestureListener {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                // sort stops alphabetically
                sortStopsAlphabetical();
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // sort stops reverse alphabetically
                sortStopsReverseAlphabetical();
                return true;
            }
        }

        private void sortStopsAlphabetical() {
            // sort stops a to z
            Collections.sort(this.adapter.filterRoutes);
            this.adapter.notifyDataSetChanged();

            // change selected button background
            changeSortButtonBackground(sortInOrderButton, R.drawable.button_sort_stops_border);
            changeSortButtonBackground(sortAlphabeticalButton, R.drawable.button_sort_stops_selected);

            // save sort order
            LocationPickerUtils.setStopPickerSortOrder(getContext(), 0);
        }

        private void sortStopsReverseAlphabetical() {
            // sort stops z to a
            Collections.sort(this.adapter.filterRoutes, Collections.<StopModel>reverseOrder());
            this.adapter.notifyDataSetChanged();

            // TODO: change selected button background -- should this be different for reverse order?
            changeSortButtonBackground(sortInOrderButton, R.drawable.button_sort_stops_border);
            changeSortButtonBackground(sortAlphabeticalButton, R.drawable.button_sort_stops_selected);

            // save sort order
            LocationPickerUtils.setStopPickerSortOrder(getContext(), 1);
        }

        private void sortStopsInOrder() {
            // sort stops in route order
            Collections.sort(this.adapter.filterRoutes, new StopModel.StopModelSequenceComparator());
            this.adapter.notifyDataSetChanged();

            // change selected button background
            changeSortButtonBackground(sortAlphabeticalButton, R.drawable.button_sort_stops_border);
            changeSortButtonBackground(sortInOrderButton, R.drawable.button_sort_stops_selected);

            // save sort order
            LocationPickerUtils.setStopPickerSortOrder(getContext(), 2);
        }

        private void changeSortButtonBackground(View button, int backgroundResId) {
            button.setBackgroundResource(backgroundResId);
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
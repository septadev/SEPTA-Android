package org.septa.android.app.locationpicker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.support.BaseTabActivityHandler;
import org.septa.android.app.support.CursorAdapterSupplier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ByStopTabActivityHandler extends BaseTabActivityHandler {

    public static final String TAG = ByStopTabActivityHandler.class.getSimpleName();
    private final CursorAdapterSupplier<StopModel> cursorAdapterSupplier;
    private StopPickerTabListener mListener;
    private boolean isLineAware;

    public ByStopTabActivityHandler(StopPickerTabListener listener, String s, CursorAdapterSupplier<StopModel> cursorAdapterSupplier, boolean isLineAware) {
        super(s);
        this.mListener = listener;
        this.cursorAdapterSupplier = cursorAdapterSupplier;
        this.isLineAware = isLineAware;
    }

    @Override
    public Fragment getFragment() {
        ByStopTabActivityHandler.PlaceholderFragment fragment = ByStopTabActivityHandler.PlaceholderFragment.newInstance(mListener, cursorAdapterSupplier, isLineAware);
        return fragment;
    }

    public static class PlaceholderFragment extends Fragment implements LoadStopsForPicker.LoadStopsForPickerListener {
        private ListView list = null;
        StationNameAdapter2 adapter;
        private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;
        StopPickerTabListener mListener;
        View progressView, sortInOrderButton;
        ImageButton sortAlphabeticalButton;
        EditText filterText;
        private boolean isLineAware = false;
        private static final String IS_LINE_AWARE = "IS_LINE_AWARE";

        public static ByStopTabActivityHandler.PlaceholderFragment newInstance(StopPickerTabListener listener, CursorAdapterSupplier<StopModel> cursorAdapterSupplier, boolean isLineAware) {
            ByStopTabActivityHandler.PlaceholderFragment fragment = new ByStopTabActivityHandler.PlaceholderFragment();
            fragment.mListener = listener;
            Bundle args = new Bundle();
            args.putSerializable("cursorAdapterSupplier", cursorAdapterSupplier);
            args.putBoolean(IS_LINE_AWARE, isLineAware);
            fragment.setArguments(args);

            return fragment;
        }

        private void restoreArgs() {
            cursorAdapterSupplier = (CursorAdapterSupplier<StopModel>) getArguments().getSerializable("cursorAdapterSupplier");
            isLineAware = getArguments().getBoolean(IS_LINE_AWARE);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
            restoreArgs();
            View rootView = inflater.inflate(R.layout.location_picker_by_stop, container, false);
            View sortButtonsContainer = rootView.findViewById(R.id.stop_list_sort_container);
            list = (ListView) rootView.findViewById(R.id.rail_station_list);
            progressView = rootView.findViewById(R.id.progress_view);
            filterText = (EditText) rootView.findViewById(R.id.station_filter);

            progressView.setVisibility(View.VISIBLE);

            // load stops for the picker dialog
            LoadStopsForPicker loadStopsForPicker = new LoadStopsForPicker(getContext(), PlaceholderFragment.this, cursorAdapterSupplier);
            loadStopsForPicker.execute();

            if (isLineAware) {
                sortButtonsContainer.setVisibility(View.VISIBLE);

                sortAlphabeticalButton = (ImageButton) rootView.findViewById(R.id.stop_list_sort_alphabetical);
                sortInOrderButton = rootView.findViewById(R.id.stop_list_sort_in_order);

                sortAlphabeticalButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean isAlphabetical = LocationPickerUtils.getStopPickerSortOrder(getContext()) == 0;

                        if (isAlphabetical) {
                            // sort stops reverse alphabetically
                            sortStopsReverseAlphabetical();
                        } else {
                            // sort stops alphabetically
                            sortStopsAlphabetical();
                        }
                    }
                });

                // button to sort stops in order
                sortInOrderButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // sort stops in route order
                        sortStopsInOrder();
                    }
                });
            }

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

                    Intent intent = new Intent();
                    intent.putExtra(LocationPickerFragment.STOP_MODEL, cursorAdapterSupplier.getItemFromId(getContext(), view.getTag()));

                    if (getTargetFragment() != null) {
                        getTargetFragment().onActivityResult(getTargetRequestCode(), LocationPickerFragment.SUCCESS, intent);
                    } else if (mListener != null) {
                        mListener.onStopSelected(intent);
                    }
                }
            });

            if (isLineAware) {
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

        private void sortStopsAlphabetical() {
            // sort stops a to z
            Collections.sort(this.adapter.filterRoutes);
            this.adapter.notifyDataSetChanged();

            // change selected button background
            sortInOrderButton.setBackgroundResource(R.drawable.button_sort_stops_border);
            sortAlphabeticalButton.setBackgroundResource(R.drawable.button_sort_stops_selected);
            sortAlphabeticalButton.setImageResource(R.drawable.ic_sort_alpha);

            // save sort order
            LocationPickerUtils.setStopPickerSortOrder(getContext(), 0);
        }

        private void sortStopsReverseAlphabetical() {
            // sort stops z to a
            Collections.sort(this.adapter.filterRoutes, Collections.<StopModel>reverseOrder());
            this.adapter.notifyDataSetChanged();

            // change selected button background
            sortInOrderButton.setBackgroundResource(R.drawable.button_sort_stops_border);
            sortAlphabeticalButton.setBackgroundResource(R.drawable.button_sort_stops_selected);
            sortAlphabeticalButton.setImageResource(R.drawable.ic_sort_reverse_alpha);

            // save sort order
            LocationPickerUtils.setStopPickerSortOrder(getContext(), 1);
        }

        private void sortStopsInOrder() {
            // sort stops in route order
            Collections.sort(this.adapter.filterRoutes, new StopModel.StopModelSequenceComparator());
            this.adapter.notifyDataSetChanged();

            // change selected button background
            sortAlphabeticalButton.setImageResource(R.drawable.ic_sort_alpha);
            sortAlphabeticalButton.setBackgroundResource(R.drawable.button_sort_stops_border);
            sortInOrderButton.setBackgroundResource(R.drawable.button_sort_stops_selected);

            // save sort order
            LocationPickerUtils.setStopPickerSortOrder(getContext(), 2);
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
            TextView stationName = (TextView) convertView.findViewById(R.id.station_name);
            stationName.setText(stop.getStopName());
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
package org.septa.android.app.transitview;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.services.apiinterfaces.model.Alert;
import org.septa.android.app.support.CursorAdapterSupplier;
import org.septa.android.app.support.RouteModelComparator;
import org.septa.android.app.systemstatus.SystemStatusState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TransitViewLinePickerFragment extends DialogFragment {

    public static final String TAG = TransitViewLinePickerFragment.class.getSimpleName();

    public static final int SUCCESS = 0;
    public static final String ROUTE_DIRECTION_MODEL = "routeDirectionModel",
            TROLLEY_ROUTE_CURSOR_ADAPTER_SUPPLIER = "TROLLEY_ROUTE_CURSOR_ADAPTER_SUPPLIER",
            BUS_ROUTE_CURSOR_ADAPTER_SUPPLIER = "BUS_ROUTE_CURSOR_ADAPTER_SUPPLIER",
            SELECTED_ROUTES = "SELECTED_ROUTES";
    CursorAdapterSupplier<RouteDirectionModel> trolleyRouteCursorAdapterSupplier;
    CursorAdapterSupplier<RouteDirectionModel> busRouteCursorAdapterSupplier;
    String[] selectedRoutes;

    TransitViewLinePickerListener mListener;

    ListView linesList;
    TransitViewLineArrayAdapter transitViewLineArrayAdapter;
    EditText filterText;

    public static TransitViewLinePickerFragment newInstance(CursorAdapterSupplier<RouteDirectionModel> busRouteCursorAdapterSupplier, CursorAdapterSupplier<RouteDirectionModel> trolleyRouteCursorAdapterSupplier, String[] strings) {
        TransitViewLinePickerFragment fragment;
        fragment = new TransitViewLinePickerFragment();

        Bundle args = new Bundle();
        args.putSerializable(BUS_ROUTE_CURSOR_ADAPTER_SUPPLIER, busRouteCursorAdapterSupplier);
        args.putSerializable(TROLLEY_ROUTE_CURSOR_ADAPTER_SUPPLIER, trolleyRouteCursorAdapterSupplier);
        args.putStringArray(SELECTED_ROUTES, strings);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onResume() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((WindowManager.LayoutParams) params);

        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        restoreArgs();

        View rootView = inflater.inflate(R.layout.fragment_line_picker, container);

        View exitView = rootView.findViewById(R.id.exit);
        filterText = rootView.findViewById(R.id.line_filter_text);

        exitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        linesList = rootView.findViewById(R.id.line_list);
        linesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (getTargetFragment() != null) {
                    Log.d(TAG, "Passing back selected route to TransitViewFragment");

                    Intent intent = new Intent();
                    intent.putExtra(ROUTE_DIRECTION_MODEL, transitViewLineArrayAdapter.getItem(i));
                    getTargetFragment().onActivityResult(getTargetRequestCode(), SUCCESS, intent);
                } else if (mListener != null) {
                    Log.d(TAG, "Passing back selected route to TransitViewResultsActivity");

                    // one route must already be selected to add a route from TransitViewResultsActivity
                    if (selectedRoutes[1] == null) {
                        mListener.addSecondRoute(transitViewLineArrayAdapter.getItem(i));
                    } else {
                        mListener.addThirdRoute(transitViewLineArrayAdapter.getItem(i));
                    }
                } else {
                    Log.e(TAG, "Could not pass back RouteDirectionModel result");
                }
                dismiss();
            }

        });
        PopulateRouteListTask task = new PopulateRouteListTask(this, selectedRoutes);
        task.execute();

        filterText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (transitViewLineArrayAdapter != null) {
                    transitViewLineArrayAdapter.getFilter().filter(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        return rootView;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof TransitViewLinePickerListener) {
            this.mListener = (TransitViewLinePickerListener) context;
        }
    }

    private void restoreArgs() {
        busRouteCursorAdapterSupplier = (CursorAdapterSupplier<RouteDirectionModel>) getArguments().getSerializable(BUS_ROUTE_CURSOR_ADAPTER_SUPPLIER);
        trolleyRouteCursorAdapterSupplier = (CursorAdapterSupplier<RouteDirectionModel>) getArguments().getSerializable(TROLLEY_ROUTE_CURSOR_ADAPTER_SUPPLIER);
        selectedRoutes = getArguments().getStringArray(SELECTED_ROUTES);
    }

    static class TransitViewLineArrayAdapter extends ArrayAdapter<RouteDirectionModel> implements Filterable {

        private Context context;
        private List<RouteDirectionModel> origRoutes;
        private List<RouteDirectionModel> filterRoutes;
        private String[] selectedRoutes;

        TransitViewLineArrayAdapter(Context context, List<RouteDirectionModel> routes, String[] selectedRoutes) {
            super(context, 0, routes);
            this.context = context;
            this.origRoutes = routes;
            this.filterRoutes = routes;
            this.selectedRoutes = selectedRoutes;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_line_picker, null);
            }

            RouteDirectionModel route = getItem(position);
            if (route != null) {
                String routeId = route.getRouteId();

                if (routeId.equalsIgnoreCase("NHSL")) {
                    // hide NHSL from route picker, since it is stored in DB as a trolley
                    convertView = inflater.inflate(R.layout.row_null, null);
                    convertView.setVisibility(View.GONE);
                } else {
                    TextView titleText = convertView.findViewById(R.id.line_title);
                    TextView descText = convertView.findViewById(R.id.line_desc);
                    ImageView lineIcon = convertView.findViewById(R.id.route_icon);

                    if (titleText != null && descText != null && lineIcon != null) {
                        // set lineIcon based on whether the route is a bus or trolley
                        TransitType transitType = TransitType.BUS;
                        String[] trolleyRouteIds = new String[]{"10", "11", "13", "15", "34", "36", "101", "102"};
                        if (Arrays.asList(trolleyRouteIds).contains(routeId)) {
                            transitType = TransitType.TROLLEY;
                        }
                        lineIcon.setImageResource(transitType.getIconForLine(routeId, context));

                        titleText.setText(route.getRouteShortName() + " " + route.getRouteLongName());

                        if (route.getDirectionDescription() != null) {
                            descText.setText("to " + route.getDirectionDescription());
                            descText.setVisibility(View.VISIBLE);
                        } else {
                            descText.setText(R.string.empty_string);
                            descText.setVisibility(View.GONE);
                        }

                        Alert alert = SystemStatusState.getAlertForLine(transitType, route.getRouteId());
                        if (alert.isAlert()) {
                            convertView.findViewById(R.id.alert_icon).setVisibility(View.VISIBLE);
                        } else {
                            convertView.findViewById(R.id.alert_icon).setVisibility(View.GONE);
                        }
                        if (alert.isAdvisory()) {
                            convertView.findViewById(R.id.advisory_icon).setVisibility(View.VISIBLE);
                        } else {
                            convertView.findViewById(R.id.advisory_icon).setVisibility(View.GONE);
                        }
                        if (alert.isDetour()) {
                            convertView.findViewById(R.id.detour_icon).setVisibility(View.VISIBLE);
                        } else {
                            convertView.findViewById(R.id.detour_icon).setVisibility(View.GONE);
                        }
                        if (alert.isSnow()) {
                            convertView.findViewById(R.id.weather_icon).setVisibility(View.VISIBLE);
                        } else {
                            convertView.findViewById(R.id.weather_icon).setVisibility(View.GONE);
                        }

                        // disable route from picker if already selected
                        if (routeId.equalsIgnoreCase(selectedRoutes[0]) || routeId.equalsIgnoreCase(selectedRoutes[1])) {
                            disableRouteItemView(convertView);
                        } else {
                            activateRouteItemView(convertView);
                        }
                    } else {
                        Log.e(TAG, "Row layout attributes were null");

                    }
                }
            }
            return convertView;
        }


        public int getCount() {
            return filterRoutes.size();
        }

        public RouteDirectionModel getItem(int position) {
            return filterRoutes.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint == null || constraint.toString().trim().isEmpty()) {
                        filterResults.values = origRoutes;
                        filterResults.count = origRoutes.size();

                        return filterResults;
                    }

                    ArrayList<RouteDirectionModel> tempList = new ArrayList<>();

                    String constraintString = constraint.toString().toLowerCase();

                    for (RouteDirectionModel item : origRoutes) {
                        if (safeContains(item.getRouteShortName(), constraintString) ||
                                safeContains(item.getDirectionDescription(), constraintString) ||
                                safeContains(item.getRouteLongName(), constraintString)) {
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
                    filterRoutes = (List<RouteDirectionModel>) results.values;
                    notifyDataSetChanged();

                    if (results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
        }

        static boolean safeContains(String target, String input) {
            if (target == null) {
                return false;
            }

            return target.toLowerCase().contains(input);
        }

        private void disableRouteItemView(View view) {
            view.setAlpha((float) .3);
            view.setClickable(true);
        }

        private void activateRouteItemView(View view) {
            view.setAlpha(1);
            view.setClickable(false);
        }

    }

    private static class PopulateRouteListTask extends AsyncTask<Void, Void, List<RouteDirectionModel>> {
        TransitViewLinePickerFragment fragment;
        String[] selectedRoutes;

        public PopulateRouteListTask(TransitViewLinePickerFragment fragment, String[] selectedRoutes) {
            this.fragment = fragment;
            this.selectedRoutes = selectedRoutes;
        }

        @Override
        protected List<RouteDirectionModel> doInBackground(Void... voids) {
            List<RouteDirectionModel> routes = new ArrayList<>();
            if (fragment.getActivity() == null) {
                return routes;
            }

            // populate buses
            Cursor cursor = fragment.busRouteCursorAdapterSupplier.getCursor(fragment.getActivity(), null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        RouteDirectionModel route = fragment.busRouteCursorAdapterSupplier.getCurrentItemFromCursor(cursor);
                        if (route != null) {
                            routes.add(fragment.busRouteCursorAdapterSupplier.getCurrentItemFromCursor(cursor));
                        }
                    } while (cursor.moveToNext());
                }
            }

            // populate trolleys
            cursor = fragment.trolleyRouteCursorAdapterSupplier.getCursor(fragment.getActivity(), null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        RouteDirectionModel route = fragment.trolleyRouteCursorAdapterSupplier.getCurrentItemFromCursor(cursor);
                        if (route != null) {
                            routes.add(fragment.trolleyRouteCursorAdapterSupplier.getCurrentItemFromCursor(cursor));
                        }
                    } while (cursor.moveToNext());
                }
            }

            return routes;
        }

        @Override
        protected void onPostExecute(List<RouteDirectionModel> routeDirectionModels) {
            Collections.sort(routeDirectionModels, new RouteModelComparator());
            if (fragment.getContext() != null) {
                fragment.transitViewLineArrayAdapter = new TransitViewLineArrayAdapter(fragment.getContext(), routeDirectionModels, selectedRoutes);
                fragment.linesList.setAdapter(fragment.transitViewLineArrayAdapter);
                fragment.transitViewLineArrayAdapter.getFilter().filter(fragment.filterText.getText());
            }
        }
    }

    public interface TransitViewLinePickerListener {
        void addFirstRoute(RouteDirectionModel route);

        void addSecondRoute(RouteDirectionModel route);

        void addThirdRoute(RouteDirectionModel route);
    }
}

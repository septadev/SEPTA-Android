package org.septa.android.app.locationpicker;

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
import java.util.Collections;
import java.util.List;

/**
 * Created by jkampf on 8/30/17.
 */

public class LinePickerFragment extends DialogFragment {
    public static final int SUCCESS = 0;
    public static final String ROUTE_DIRECTION_MODEL = "routeDirectionModel";
    CursorAdapterSupplier<RouteDirectionModel> routeCursorAdapterSupplier;
    ListView linesList;
    LineArrayAdapter lineArrayAdapter;
    EditText filterText;
    TransitType transitType;
    private LinePickerCallBack linePickerCallBack;

    @Override
    public void onResume() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        restoreArgs();

        View rootView = inflater.inflate(R.layout.line_picker_modal, container);

        View exitView = rootView.findViewById(R.id.exit);
        filterText = (EditText) rootView.findViewById(R.id.line_filter_text);

        exitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        linesList = (ListView) rootView.findViewById(R.id.line_list);
        linesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (linePickerCallBack != null) {
                    linePickerCallBack.setRoute(lineArrayAdapter.getItem(i));
                } else if (getTargetFragment() != null) {
                    Intent intent = new Intent();
                    intent.putExtra(ROUTE_DIRECTION_MODEL, lineArrayAdapter.getItem(i));
                    getTargetFragment().onActivityResult(getTargetRequestCode(), SUCCESS, intent);
                }
                dismiss();
            }

        });
        PopulateRouteListTask task = new PopulateRouteListTask(this);
        task.execute();


        filterText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (lineArrayAdapter != null)
                    lineArrayAdapter.getFilter().filter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        return rootView;

    }

    public static LinePickerFragment newInstance(CursorAdapterSupplier<RouteDirectionModel> routeCursorAdapterSupplier, TransitType transitType) {
        LinePickerFragment fragment;
        fragment = new LinePickerFragment();

        Bundle args = new Bundle();
        args.putSerializable("transitType", transitType);
        args.putSerializable("routeCursorAdapterSupplier", routeCursorAdapterSupplier);

        fragment.setArguments(args);

        return fragment;
    }

    private void restoreArgs() {
        transitType = (TransitType) getArguments().getSerializable("transitType");
        routeCursorAdapterSupplier = (CursorAdapterSupplier<RouteDirectionModel>) getArguments().getSerializable("routeCursorAdapterSupplier");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if ((context instanceof LinePickerCallBack)) {
            linePickerCallBack = (LinePickerCallBack) context;
        }

    }

    public void setTransitType(TransitType transitType) {
        this.transitType = transitType;
    }

    static class LineArrayAdapter extends ArrayAdapter<RouteDirectionModel> implements Filterable {

        List<RouteDirectionModel> origRoutes;
        List<RouteDirectionModel> filterRoutes;
        TransitType transitType;

        public LineArrayAdapter(Context context, List<RouteDirectionModel> routes, TransitType transitType) {
            super(context, 0, routes);
            this.origRoutes = routes;
            this.filterRoutes = routes;
            this.transitType = transitType;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView,
                            @NonNull ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.line_picker_item, null);
            }

            RouteDirectionModel route = getItem(position);
            if (route != null) {
                TextView titleText = (TextView) convertView.findViewById(R.id.line_title);
                TextView descText = (TextView) convertView.findViewById(R.id.line_desc);
                ImageView lineIcon = (ImageView) convertView.findViewById(R.id.route_icon);
                lineIcon.setImageResource(transitType.getIconForLine(route.getRouteId(), getContext()));

                if (transitType == TransitType.TROLLEY || transitType == TransitType.BUS) {
                    titleText.setText(route.getRouteShortName() + " " + route.getRouteLongName());
                } else {
                    titleText.setText(route.getRouteLongName());
                }

                if (route.getDirectionDescription() != null)
                    if (transitType == TransitType.RAIL) {
                        descText.setText(route.getDirectionDescription());
                    } else {
                        descText.setText("to " + route.getDirectionDescription());
                    }
                else {
                    descText.setText(R.string.empty_string);
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

                    ArrayList<RouteDirectionModel> tempList = new ArrayList<RouteDirectionModel>();

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
                protected void publishResults(CharSequence contraint, FilterResults results) {
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

    }

    private static class PopulateRouteListTask extends AsyncTask<Void, Void, List<RouteDirectionModel>> {
        LinePickerFragment fragment;

        public PopulateRouteListTask(LinePickerFragment fragment) {
            this.fragment = fragment;
        }

        @Override
        protected List<RouteDirectionModel> doInBackground(Void... voids) {

            List<RouteDirectionModel> routes = new ArrayList<RouteDirectionModel>();
            if (fragment.getActivity() == null)
                return routes;
            Cursor cursor = fragment.routeCursorAdapterSupplier.getCursor(fragment.getActivity(), null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {

                    do {
                        RouteDirectionModel route = fragment.routeCursorAdapterSupplier.getCurrentItemFromCursor(cursor);
                        if (route != null)
                            routes.add(fragment.routeCursorAdapterSupplier.getCurrentItemFromCursor(cursor));
                    } while (cursor.moveToNext());
                }
            }

            return routes;
        }

        @Override
        protected void onPostExecute(List<RouteDirectionModel> routeDirectionModels) {
            Collections.sort(routeDirectionModels, new RouteModelComparator());
            if (fragment.getContext() != null) {
                fragment.lineArrayAdapter = new LineArrayAdapter(fragment.getContext(), routeDirectionModels, fragment.transitType);
                fragment.linesList.setAdapter(fragment.lineArrayAdapter);
                fragment.lineArrayAdapter.getFilter().filter(fragment.filterText.getText());
            }
        }
    }
}

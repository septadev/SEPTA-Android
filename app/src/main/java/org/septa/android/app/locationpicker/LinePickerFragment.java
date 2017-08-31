package org.septa.android.app.locationpicker;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
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
import org.septa.android.app.support.Consumer;
import org.septa.android.app.support.CursorAdapterSupplier;
import org.septa.android.app.support.RouteModelComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by jkampf on 8/30/17.
 */

public class LinePickerFragment extends DialogFragment {
    CursorAdapterSupplier<RouteDirectionModel> routeCursorAdapterSupplier;
    ListView linesList;
    LineArrayAdapater lineArrayAdapater;
    EditText filterText;
    Consumer<RouteDirectionModel> consumer;
    TransitType transitType;

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
                consumer.accept(lineArrayAdapater.getItem(i));
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
                if (lineArrayAdapater != null)
                    lineArrayAdapater.getFilter().filter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        return rootView;

    }

    public static LinePickerFragment newInstance(CursorAdapterSupplier<RouteDirectionModel> routeCursorAdapterSupplier, TransitType transitType, Consumer<RouteDirectionModel> consumer) {
        LinePickerFragment fragment;
        fragment = new LinePickerFragment();
        fragment.setRouteCursorAdapterSupplier(routeCursorAdapterSupplier);
        fragment.setConsumer(consumer);
        fragment.setTransitType(transitType);

        return fragment;
    }

    public void setRouteCursorAdapterSupplier(CursorAdapterSupplier<RouteDirectionModel> routeCursorAdapaterSupplier) {
        this.routeCursorAdapterSupplier = routeCursorAdapaterSupplier;
    }

    public void setConsumer(Consumer<RouteDirectionModel> consumer) {
        this.consumer = consumer;
    }

    public void setTransitType(TransitType transitType) {
        this.transitType = transitType;
    }

    static class LineArrayAdapater extends ArrayAdapter<RouteDirectionModel> implements Filterable {

        List<RouteDirectionModel> origRoutes;
        List<RouteDirectionModel> filterRoutes;
        TransitType transitType;

        public LineArrayAdapater(Context context, List<RouteDirectionModel> routes, TransitType transitType) {
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

                titleText.setText(route.getRouteShortName() + " " + route.getRouteLongName());
                descText.setText("to " + route.getDirectionDescription());
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
                    if (constraint == null || "".equals(constraint.toString().trim())) {
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
            fragment.lineArrayAdapater = new LineArrayAdapater(fragment.getContext(), routeDirectionModels, fragment.transitType);
            fragment.linesList.setAdapter(fragment.lineArrayAdapater);
            fragment.lineArrayAdapater.getFilter().filter(fragment.filterText.getText());
        }
    }
}

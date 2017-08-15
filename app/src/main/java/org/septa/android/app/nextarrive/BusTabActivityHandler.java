package org.septa.android.app.nextarrive;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.domain.RouteModel;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.support.BaseTabActivityHandler;
import org.septa.android.app.support.CursorAdapterSupplier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.Route;


/**
 * Created by jkampf on 7/29/17.
 */

public class BusTabActivityHandler extends BaseTabActivityHandler {
    CursorAdapterSupplier<RouteModel> cursorAdapterSupplier;

    public BusTabActivityHandler(String title, CursorAdapterSupplier<RouteModel> cursorAdapterSupplier, int iconDrawable) {
        super(title, iconDrawable);
        this.cursorAdapterSupplier = cursorAdapterSupplier;
    }

    @Override
    public Fragment getFragment() {
        BusTabActivityHandler.PlaceholderFragment fragment = BusTabActivityHandler.PlaceholderFragment.newInstance();
        fragment.setCursorAdapterSupplier(cursorAdapterSupplier);
        return fragment;
    }

    public static class PlaceholderFragment extends Fragment {
        CursorAdapterSupplier<RouteModel> cursorAdapterSupplier;

        public static PlaceholderFragment newInstance() {
            PlaceholderFragment fragment = new PlaceholderFragment();
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.bus_next_to_arrive, container, false);

            final Spinner routeSpinner = (Spinner) rootView.findViewById(R.id.route_spinner);

            AsyncTask<Void, Void, List<RouteModel>> task = new AsyncTask<Void, Void, List<RouteModel>>() {
                @Override
                protected List<RouteModel> doInBackground(Void... voids) {

                    List<RouteModel> routes = new ArrayList<RouteModel>();
                    Cursor cursor = cursorAdapterSupplier.getCursor(getActivity(), null);
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {

                            do {
                                routes.add(cursorAdapterSupplier.getCurrentItemFromCursor(cursor));
                            } while (cursor.moveToNext());
                        }
                    }

                    return routes;
                }

                @Override
                protected void onPostExecute(List<RouteModel> routeModels) {
                    Collections.sort(routeModels, new Comparator<RouteModel>() {
                        @Override
                        public int compare(RouteModel x, RouteModel y) {
                            StringBuilder xNumber = new StringBuilder();
                            int i = 0;
                            while (i < x.getRouteShortName().length() && Character.isDigit(x.getRouteShortName().charAt(i))) {
                                xNumber.append(x.getRouteShortName().charAt(i++));
                            }
                            double x1 = Double.MAX_VALUE;
                            if (i > 0)
                                x1 = Double.parseDouble(xNumber.toString());
                            String x2 = x.getRouteShortName().substring(i);

                            StringBuilder yNumber = new StringBuilder();
                            int j = 0;
                            while (j < y.getRouteShortName().length() && Character.isDigit(y.getRouteShortName().charAt(j))) {
                                yNumber.append(y.getRouteShortName().charAt(j++));
                            }

                            double y1 = Double.MAX_VALUE;
                            if (j > 0)
                                y1 = Double.parseDouble(yNumber.toString());
                            String y2 = y.getRouteShortName().substring(j);

                            if (x1 != y1)
                                return (int) (x1 - y1);

                            return x2.compareTo(y2);
                        }
                    });

                    routeSpinner.setAdapter(new RouteAdapter(getActivity(), routeModels));
                }
            }.execute();


            return rootView;
        }

        public void setCursorAdapterSupplier(CursorAdapterSupplier<RouteModel> cursorAdapterSupplier) {
            this.cursorAdapterSupplier = cursorAdapterSupplier;
        }
    }

    public static class RouteAdapter extends ArrayAdapter<RouteModel> {
        public RouteAdapter(@NonNull Context context, @NonNull List<RouteModel> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return getView(position, convertView, parent);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.route_spinner_element, parent, false);

            RouteModel route = getItem(position);
            TextView short_name = (TextView) convertView.findViewById(R.id.route_short_name);
            short_name.setText(route.getRouteShortName());

            TextView long_name = (TextView) convertView.findViewById(R.id.route_long_name);
            long_name.setText(route.getRouteLongName());

            return convertView;
        }

    }
}

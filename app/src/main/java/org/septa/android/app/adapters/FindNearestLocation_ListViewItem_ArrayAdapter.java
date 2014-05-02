/*
 * FindNearestLocation_ListViewItem_ArrayAdapter.java
 * Last modified on 04-01-2014 16:47-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.models.LocationBasedRouteModel;
import org.septa.android.app.models.LocationModel;
import org.septa.android.app.models.ObjectFactory;
import org.septa.android.app.models.RoutesModel;
import org.septa.android.app.utilities.PixelHelper;

import java.util.List;

public class FindNearestLocation_ListViewItem_ArrayAdapter extends ArrayAdapter<LocationModel> {
    public static final String TAG = FindNearestLocation_ListViewItem_ArrayAdapter.class.getName();

    private final Context context;
    private final List<LocationModel> values;

    private final RoutesModel busRoutesModel;

    public FindNearestLocation_ListViewItem_ArrayAdapter(Context context, List<LocationModel> values) {
        super(context, R.layout.findnearestlocations_listview_item, values);
        this.context = context;
        this.values = values;

        busRoutesModel = ObjectFactory.getInstance().getBusRoutes();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = null;

        if (values != null) {
            LocationModel location = values.get(position);

            rowView = inflater.inflate(R.layout.findnearestlocations_listview_item, parent, false);

            TextView distanceTextView = (TextView) rowView.findViewById(R.id.findnearestlocations_listView_items_distance_textView);
            TextView milesTextView = (TextView) rowView.findViewById(R.id.findnearestlocations_listView_items_milesLabel_textView);

            TextView locationNameTextView = (TextView) rowView.findViewById(R.id.findnearestlocations_listView_items_locationName_textView);

            distanceTextView.setText(String.format("%.2f%n", location.getDistance()));
            milesTextView.setText(context.getString(R.string.distance_miles));

            locationNameTextView.setText(location.getLocationName());

            LinearLayout routesViewLayout = (LinearLayout)rowView.findViewById(R.id.findnearestlocations_listView_routesView_layout);

            int routeCount = 1;

            for (LocationBasedRouteModel route : location.getRoutes()) {
                // for the view, we can fit 10 routes comfortably
                if (routeCount==10) break;

                TextView routeTextView = new TextView(context);

                int width = PixelHelper.pixelsToDensityIndependentPixels(context, 38);
                LinearLayout.LayoutParams textViewLayoutParams = new LinearLayout.LayoutParams(width,
                                                                                               ViewGroup.LayoutParams.WRAP_CONTENT);
                textViewLayoutParams.setMargins(0, 0, 5, 0); // llp.setMargins(left, top, right, bottom);
                routeTextView.setLayoutParams(textViewLayoutParams);
                routeTextView.setPadding(5, 5, 5, 5);
                routeTextView.setGravity(Gravity.CENTER);

                routeTextView.setBackgroundResource(R.drawable.findnearestlocation_roundedbutton_corners);

                RoutesModel busRoutesModel = ObjectFactory.getInstance().getBusRoutes();
                busRoutesModel.loadRoutes(context);

                GradientDrawable drawable = (GradientDrawable) routeTextView.getBackground();
                switch (route.getRouteType()) {
                    case 0: {   // trolley
                        if (route.getRouteShortName().equals("NHSL")) {
                            drawable.setColor(0xFF791D7E);
                        } else {
                            drawable.setColor(0xFF4D7900);
                        }
                        break;
                    }
                    case 1: {   // subway
                        if (route.getRouteShortName().equals("BSS")) {
                            drawable.setColor(0xFFF58426);
                        } else {
                            if (route.getRouteShortName().equals("MFL")) {
                                drawable.setColor(0xFF007DC3);
                            } else {
                                drawable.setColor(0xFF000000);
                            }
                        }
                        break;
                    }
                    case 2: {   // rail
                        drawable.setColor(0xFF45637A);

                        break;
                    }
                    case 3: {   // bus
                        if (route.getRouteShortName().equals("MFO")) {
                            drawable.setColor(0xFF007DC3);
                        } else {
                            if (route.getRouteShortName().equals("BSO")) {
                                drawable.setColor(0xFFF58426);
                            } else { // bus
                                drawable.setColor(0xFF383D42);
                            }
                        }
                        break;
                    }
                    default: {
                        Log.d(TAG, "this should not be a value option");

                        break;
                    }
                }

                String routeText = "";
                switch (route.getDirectionBinaryPower()) {
                    case 1: {
                        routeText = route.getRouteShortName() + "N";
                        break;
                    }
                    case 2: {
                        routeText = route.getRouteShortName() + "S";
                        break;
                    }
                    case 4: {
                        routeText = route.getRouteShortName() + "E";
                    }
                    case 8: {
                        routeText = route.getRouteShortName() + "W";
                        break;
                    }
                    default: {
                        routeText = route.getRouteShortName();
                        break;
                    }
                }
                routeTextView.setText(routeText);
                routeTextView.setTextColor(Color.WHITE);
                routeTextView.setTextSize(12.0f);

                routesViewLayout.addView(routeTextView);

                routeCount++;
            }
        }

        return rowView;
    }

    @Override
    public boolean areAllItemsEnabled() {

        return false;
    }

    @Override
    public boolean isEnabled(int position) {

        return false;
    }
}
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.models.LocationBasedRouteModel;
import org.septa.android.app.models.LocationModel;
import org.septa.android.app.views.RouteTextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class FindNearestLocation_ListViewItem_ArrayAdapter extends ArrayAdapter<LocationModel> {
    public static final String TAG = FindNearestLocation_ListViewItem_ArrayAdapter.class.getName();
    private static final int MAX_ROUTE_DISPLAY = 10;

    private final Context context;
    private final List<LocationModel> values;


    public FindNearestLocation_ListViewItem_ArrayAdapter(Context context, List<LocationModel> values) {
        super(context, R.layout.findnearestlocations_listview_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = convertView;

        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.findnearestlocations_listview_item, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        LocationModel location = values.get(position);

        holder.distanceTextView.setText(String.format("%.2f%n", location.getDistance()));
        holder.milesTextView.setText(context.getString(R.string.distance_miles));
        holder.locationNameTextView.setText(location.getLocationName());
        holder.routesViewLayout.removeAllViews();

        for (LocationBasedRouteModel route : location.getRoutes()) {

            if(location.getRoutes().indexOf(route) > MAX_ROUTE_DISPLAY)
                break;

            RouteTextView routeTextView = new RouteTextView(context);
            GradientDrawable drawable = (GradientDrawable) routeTextView.getBackground();

            if(route.getRouteSpecialType() == LocationBasedRouteModel.RouteSpecialType.NONE){
                switch (route.getTransportationType()){
                    case TROLLEY:
                        drawable.setColor(context.getResources().getColor(R.color.trolleyGreen));
                        break;
                    case SUBWAY:
                        drawable.setColor(Color.BLACK);
                        break;
                    case RAIL:
                        drawable.setColor(context.getResources().getColor(R.color.railGrey));
                        break;
                    case BUS:
                        drawable.setColor(context.getResources().getColor(R.color.busGrey));
                        break;
                    default:
                        Log.d(TAG, "this should not be a value option");
                        break;
                }

            } else{

                switch (route.getRouteSpecialType()){
                    case NHSL:
                        drawable.setColor(context.getResources().getColor(R.color.nshlPurple));
                        break;
                    case BSS:
                    case BSO:
                        drawable.setColor(context.getResources().getColor(R.color.bsOrange));
                        break;
                    case MFL:
                    case MFO:
                        drawable.setColor(context.getResources().getColor(R.color.mfBlue));
                        break;
                    default:
                        Log.d(TAG, "this should not be a value option");
                        break;
                }
            }

            routeTextView.setText(route.getRouteShortNameWithDirection());
            holder.routesViewLayout.addView(routeTextView);

        }

        return view;
    }

    static class ViewHolder {
        @InjectView(R.id.findnearestlocations_listView_items_distance_textView)
        TextView distanceTextView;
        @InjectView(R.id.findnearestlocations_listView_items_milesLabel_textView)
        TextView milesTextView;
        @InjectView(R.id.findnearestlocations_listView_items_locationName_textView)
        TextView locationNameTextView;
        @InjectView(R.id.findnearestlocations_listView_routesView_layout)
        LinearLayout routesViewLayout;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
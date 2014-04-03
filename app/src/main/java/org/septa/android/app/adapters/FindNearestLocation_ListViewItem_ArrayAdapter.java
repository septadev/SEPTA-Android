/*
 * FindNearestLocation_ListViewItem_ArrayAdapter.java
 * Last modified on 04-01-2014 16:47-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.activities.FareInformationGetMoreDetailsActionBarActivity;
import org.septa.android.app.models.LocationModel;

public class FindNearestLocation_ListViewItem_ArrayAdapter extends ArrayAdapter<LocationModel> {
    public static final String TAG = FindNearestLocation_ListViewItem_ArrayAdapter.class.getName();

    private final Context context;
    private final LocationModel[] values;

    public FindNearestLocation_ListViewItem_ArrayAdapter(Context context, LocationModel[] values) {
        super(context, R.layout.findnearestlocations_listview_item, values);
        this.context = context;
        this.values = values;
        Log.d(TAG, "findnearestLocation_listviewitem_arrayadapter...");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = null;

        if (values != null) {
            LocationModel location = values[position];

            rowView = inflater.inflate(R.layout.findnearestlocations_listview_item, parent, false);

            TextView distanceTextview = (TextView) rowView.findViewById(R.id.findnearestlocations_listView_items_distance_textView);
            TextView milesTextView = (TextView) rowView.findViewById(R.id.findnearestlocations_listView_items_milesLabel_textView);

            TextView locationNameTextView = (TextView) rowView.findViewById(R.id.findnearestlocations_listView_items_locationName_textView);
            TextView routesTextView = (TextView) rowView.findViewById(R.id.findnearestlocations_listView_items_routes_textView);

            distanceTextview.setText("" + location.getDistance());
            milesTextView.setText("miles");

            locationNameTextView.setText(location.getLocationName());
            routesTextView.setText("<not yet done>");

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
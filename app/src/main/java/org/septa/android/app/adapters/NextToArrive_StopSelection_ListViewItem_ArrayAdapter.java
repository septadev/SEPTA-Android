/*
 * NextToArrive_StopSelection_ListViewItem_ArrayAdapter.java
 * Last modified on 06-04-2014 16:41-0400 by brianhmayo
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
import android.widget.ImageView;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.models.StopModel;
import org.septa.android.app.models.TripDataModel;

import java.util.ArrayList;
import java.util.List;

public class NextToArrive_StopSelection_ListViewItem_ArrayAdapter extends ArrayAdapter<StopModel> {
    public static final String TAG = NextToArrive_StopSelection_ListViewItem_ArrayAdapter.class.getName();

    private final Context context;
    private List<StopModel> values;

    public NextToArrive_StopSelection_ListViewItem_ArrayAdapter(Context context, List<StopModel> tripDataModelArrayList) {
        super(context, R.layout.nexttoarrive_listview_stop_item, tripDataModelArrayList);
        this.context = context;
        this.values = tripDataModelArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.nexttoarrive_listview_stop_item, parent, false);

        ImageView icon_imageView = (ImageView) rowView.findViewById(R.id.nexttoarrive_stopselection_accessibilityicon_imageview);
        TextView text_TextView = (TextView) rowView.findViewById(R.id.nexttoarrive_stopselection_textview);

        if (values.get(position).hasWheelBoardingFeature()) {
            icon_imageView.setVisibility(View.VISIBLE);
        } else {
            icon_imageView.setVisibility(View.INVISIBLE);
        }

        text_TextView.setText(values.get(position).getStopName());

        return rowView;
    }

    public void setStopData(ArrayList<StopModel>stopsList) {

        this.values = stopsList;
    }

    @Override
    public boolean areAllItemsEnabled() {

        return true;
    }

    @Override
    public boolean isEnabled(int position) {

        return true;
    }
}
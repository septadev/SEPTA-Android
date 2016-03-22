/*
 * TransitView_RouteList_ListViewItem_ArrayAdapter.java
 * Last modified on 04-24-2014 09:35-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.models.servicemodels.TransitViewVehicleModel;

import java.util.List;

public class TransitView_RouteList_ListViewItem_ArrayAdapter  extends ArrayAdapter<TransitViewVehicleModel> {
    public static final String TAG = TransitView_RouteList_ListViewItem_ArrayAdapter.class.getName();

    private final Context context;
    private final List<TransitViewVehicleModel> values;

    public TransitView_RouteList_ListViewItem_ArrayAdapter(Context context, List<TransitViewVehicleModel> values) {
        super(context, R.layout.transitview_routelist_listview_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = null;

        TransitViewVehicleModel transitVehicleInformation = values.get(position);

        rowView = inflater.inflate(R.layout.transitview_routelist_listview_item, parent, false);

        TextView transitViewVehicleIdTextView = (TextView) rowView.findViewById(R.id.transitview_listview_item_train_number_textview);
        transitViewVehicleIdTextView.setText("" + transitVehicleInformation.getVehicleId());

        TextView transitViewDirectionLabel = (TextView) rowView.findViewById(R.id.transitview_listview_item_startlabel_textview);
        transitViewDirectionLabel.setText(context.getString(R.string.dir)+context.getString(R.string.field_separator));

        TextView transitviewDirection = (TextView) rowView.findViewById(R.id.transitview_listview_item_start_textview);
        transitviewDirection.setText(transitVehicleInformation.getDirection());
        if (transitVehicleInformation.isSouthBound() || transitVehicleInformation.isEastBound()) {
            transitviewDirection.setTextColor(context.getResources().getColor(R.color.route_direction_blue));
        } else {
            transitviewDirection.setTextColor(context.getResources().getColor(R.color.route_direction_red));
        }

        TextView trainEndLabel = (TextView) rowView.findViewById(R.id.transitview_listview_item_endlabel_textview);
        trainEndLabel.setText(context.getString(R.string.dest)+context.getString(R.string.field_separator));

        TextView trainEnd = (TextView) rowView.findViewById(R.id.transitview_listview_item_end_textview);
        trainEnd.setText(transitVehicleInformation.getDestination());

        return rowView;
    }

    @Override
    public boolean areAllItemsEnabled() {

        return true;
    }

    @Override
    public boolean isEnabled(int position) {

        return false;
    }
}
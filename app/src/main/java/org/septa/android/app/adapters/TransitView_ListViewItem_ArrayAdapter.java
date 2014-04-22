/*
 * TransitView_ListViewItem_ArrayAdapter.java
 * Last modified on 04-21-2014 21:14-0400 by brianhmayo
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
import org.septa.android.app.models.BusRouteModel;

import java.util.List;

public class TransitView_ListViewItem_ArrayAdapter extends ArrayAdapter<BusRouteModel> {
    public static final String TAG = TransitView_ListViewItem_ArrayAdapter.class.getName();

    private final Context context;
    private final List<BusRouteModel> values;

    public TransitView_ListViewItem_ArrayAdapter(Context context, List<BusRouteModel> values) {
        super(context, R.layout.transitview_listview_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = null;

        BusRouteModel busRouteModel = values.get(position);

        rowView = inflater.inflate(R.layout.transitview_listview_item, parent, false);

        ImageView transitViewBusRouteRouteTypeImageView = (ImageView) rowView.findViewById(R.id.transitview_left_bus_route_icon_ImageView);
        TextView transitViewBusRouteRouteIdTextView = (TextView) rowView.findViewById(R.id.transitview_left_bus_route_route_id_TextView);
        TextView transitViewBusRouteStatusTextView = (TextView) rowView.findViewById(R.id.transitview_right_bus_route_status_TextView);
        ImageView transitViewBusRouteStatusImageView = (ImageView) rowView.findViewById(R.id.transitview_right_bus_route_status_ImageView);


        transitViewBusRouteRouteIdTextView.setText(busRouteModel.getRouteId());
        transitViewBusRouteStatusImageView.setImageResource(R.drawable.transitview_listitem_in_service);

        if (busRouteModel.getRouteShortName().equals("NHSL")) {
            transitViewBusRouteRouteTypeImageView.setImageResource(R.drawable.transitview_listitem_nshl);
            transitViewBusRouteRouteIdTextView.setTextSize(18.0f);

            return rowView;
        } else {
           if (busRouteModel.getRouteShortName().equals("BSO")) {
               transitViewBusRouteRouteTypeImageView.setImageResource(R.drawable.transitview_listitem_bsl_owl);
               transitViewBusRouteRouteIdTextView.setText("");

               return rowView;
           } else {
               if (busRouteModel.getRouteShortName().equals("LUCYGO") || busRouteModel.getRouteShortName().equals("LUCYGR")) {
                   transitViewBusRouteRouteTypeImageView.setImageResource(R.drawable.transitview_listitem_lucy);

                   if (busRouteModel.getRouteShortName().equals("LUCYGO")) {
                       transitViewBusRouteRouteIdTextView.setText("GOLD");
                       transitViewBusRouteRouteIdTextView.setTextSize(16.0f);
                   } else {
                       transitViewBusRouteRouteIdTextView.setText("GREEN");
                       transitViewBusRouteRouteIdTextView.setTextSize(14.0f);
                   }

                   return rowView;
               } else {
                   if (busRouteModel.getRouteShortName().equals("MFO")) {
                       transitViewBusRouteRouteTypeImageView.setImageResource(R.drawable.transitview_listitem_mfl_owl);
                       transitViewBusRouteRouteIdTextView.setText("");

                       return rowView;
                   }
               }
           }
        }

        int routeType = busRouteModel.getRouteType().intValue();
        switch (routeType) {
            case 0:
                // trolley
                transitViewBusRouteRouteTypeImageView.setImageResource(R.drawable.transitview_listitem_trolley);
                break;
            case 1:
                //subway
                transitViewBusRouteRouteTypeImageView.setImageResource(R.drawable.transitview_listitem_bus);
                break;
            case 2:
                //rail
                transitViewBusRouteRouteTypeImageView.setImageResource(R.drawable.transitview_listitem_bus);
                break;
            case 3:
                //bus
                transitViewBusRouteRouteTypeImageView.setImageResource(R.drawable.transitview_listitem_bus);
                break;
            default:
                Log.d(TAG, "got here");
                break;
        }

        return rowView;
    }
}
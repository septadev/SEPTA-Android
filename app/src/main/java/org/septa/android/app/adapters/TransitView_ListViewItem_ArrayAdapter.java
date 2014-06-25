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
import org.septa.android.app.models.RouteModel;
import org.septa.android.app.utilities.PixelHelper;

import java.util.List;

public class TransitView_ListViewItem_ArrayAdapter extends ArrayAdapter<RouteModel> {
    public static final String TAG = TransitView_ListViewItem_ArrayAdapter.class.getName();

    private final Context context;
    private final List<RouteModel> values;

    public TransitView_ListViewItem_ArrayAdapter(Context context, List<RouteModel> values) {
        super(context, R.layout.transitview_listview_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = null;

        RouteModel busRouteModel = values.get(position);

        Log.d(TAG, "busroutemodel:"+busRouteModel.print());

        rowView = inflater.inflate(R.layout.transitview_listview_item, parent, false);

        ImageView transitViewBusRouteRouteTypeImageView = (ImageView) rowView.findViewById(R.id.transitview_left_bus_route_icon_ImageView);
        TextView transitViewBusRouteRouteIdTextView = (TextView) rowView.findViewById(R.id.transitview_left_bus_route_route_id_TextView);
        TextView transitViewBusRouteStatusTextView = (TextView) rowView.findViewById(R.id.transitview_right_bus_route_status_TextView);
        ImageView transitViewBusRouteStatusImageView = (ImageView) rowView.findViewById(R.id.transitview_right_bus_route_status_ImageView);

        if (values.get(position).isInService(context)) {
            Log.d(TAG, "route is in service, label as such");
            transitViewBusRouteStatusTextView.setText(context.getString(R.string.in_service));
            transitViewBusRouteStatusImageView.setImageResource(R.drawable.transitview_listitem_in_service);
        } else {
            Log.d(TAG, "route is not in service, label as such");
            transitViewBusRouteStatusTextView.setText(context.getString(R.string.not_in_service));
            transitViewBusRouteStatusImageView.setImageResource(R.drawable.transitview_listitem_out_of_service);
        }

        transitViewBusRouteRouteIdTextView.setText(busRouteModel.getRouteId());

        switch (busRouteModel.getRouteShortName().length()) {
            case 3: {
                transitViewBusRouteRouteIdTextView.setTextSize(PixelHelper.pixelsToDensityIndependentPixels(getContext(), 10));
                break;
            }
            case 4: {
                transitViewBusRouteRouteIdTextView.setTextSize(PixelHelper.pixelsToDensityIndependentPixels(getContext(), 8));
                break;
            }
        }

        if (busRouteModel.getRouteShortName().equals("NHSL")) {
            transitViewBusRouteRouteTypeImageView.setImageResource(R.drawable.transitview_listitem_nshl);

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
                       transitViewBusRouteRouteIdTextView.setTextSize(PixelHelper.pixelsToDensityIndependentPixels(getContext(), 7));
                   } else {
                       transitViewBusRouteRouteIdTextView.setText("GREEN");
                       transitViewBusRouteRouteIdTextView.setTextSize(PixelHelper.pixelsToDensityIndependentPixels(getContext(), 6));
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


    @Override
    public boolean areAllItemsEnabled() {

        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        if (values.get(position).isInService(context)) {
            return true;
        }

        return false;
    }
}
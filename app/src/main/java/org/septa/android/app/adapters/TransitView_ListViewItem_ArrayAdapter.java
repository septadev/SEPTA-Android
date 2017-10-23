/*
 * TransitView_ListViewItem_ArrayAdapter.java
 * Last modified on 04-21-2014 21:14-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.adapters;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.septa.android.app.BuildConfig;
import org.septa.android.app.R;
import org.septa.android.app.managers.AlertManager;
import org.septa.android.app.models.RouteModel;
import org.septa.android.app.models.RouteTypes;
import org.septa.android.app.models.SchedulesRouteModel;
import org.septa.android.app.models.servicemodels.AlertModel;
import org.septa.android.app.services.adaptors.AlertsAdaptor;

import java.util.List;

public class TransitView_ListViewItem_ArrayAdapter extends ArrayAdapter<RouteModel> {
    public static final String TAG = TransitView_ListViewItem_ArrayAdapter.class.getName();

    private final Context context;
    private final List<RouteModel> values;
    private AlertManager alertManager;
    private boolean alertsLoaded = false;
    private SchedulesRouteModel tempSchedulesRouteModel;


    public TransitView_ListViewItem_ArrayAdapter(Context context, List<RouteModel> values) {
        super(context, R.layout.transitview_listview_item, values);
        this.context = context;
        this.values = values;
        alertManager = AlertManager.getInstance();
        tempSchedulesRouteModel = new SchedulesRouteModel();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = convertView;
        ViewHolder viewHolder;

        RouteModel busRouteModel = values.get(position);

        if (rowView == null) {
            rowView = inflater.inflate(R.layout.transitview_listview_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.transitViewBusRouteRouteTypeImageView = (ImageView) rowView.findViewById(R.id.transitview_left_bus_route_icon_ImageView);
            viewHolder.transitViewBusRouteRouteIdTextView = (TextView) rowView.findViewById(R.id.transitview_left_bus_route_route_id_TextView);
            viewHolder.transitViewBusRouteStatusTextView = (TextView) rowView.findViewById(R.id.transitview_right_bus_route_status_TextView);
            viewHolder.transitViewBusRouteStatusImageView = (ImageView) rowView.findViewById(R.id.transitview_right_bus_route_status_ImageView);
            rowView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) rowView.getTag();
        }


        // When alerts are loaded check if route is suspended
        boolean isSuspended = false;
        if (alertsLoaded) {
            tempSchedulesRouteModel.setRouteShortName(busRouteModel.getRouteId());
            AlertModel alertModel = alertManager.getAlertForRouteShortName(
                    AlertsAdaptor.getServiceRouteName(tempSchedulesRouteModel, RouteTypes.values()[busRouteModel.getRouteType().intValue()]));
            if (alertModel != null) {
                isSuspended = alertModel.isSuspended();
            }
            if(BuildConfig.DEBUG) {
                Log.w(TAG, "Route not found in alerts: " + busRouteModel.getRouteShortName());
            }
        }

        if (values.get(position).isInService(context) && !isSuspended) {
            viewHolder.transitViewBusRouteStatusTextView.setText(context.getString(R.string.in_service));
            viewHolder.transitViewBusRouteStatusImageView.setImageResource(R.drawable.transitview_listitem_in_service);
        } else {
            viewHolder.transitViewBusRouteStatusTextView.setText(context.getString(R.string.not_in_service));
            viewHolder.transitViewBusRouteStatusImageView.setImageResource(R.drawable.transitview_listitem_out_of_service);
        }

        viewHolder.transitViewBusRouteRouteIdTextView.setText(busRouteModel.getRouteId());

        switch (busRouteModel.getRouteShortName().length()) {
            case 3: {
                viewHolder.transitViewBusRouteRouteIdTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                break;
            }
            case 4: {
                viewHolder.transitViewBusRouteRouteIdTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                break;
            }
        }

        if (busRouteModel.getRouteShortName().equals("NHSL")) {
            viewHolder.transitViewBusRouteRouteTypeImageView.setImageResource(R.drawable.transitview_listitem_nshl);

            return rowView;
        } else {
           if (busRouteModel.getRouteShortName().equals("BSO")) {
               viewHolder.transitViewBusRouteRouteTypeImageView.setImageResource(R.drawable.transitview_listitem_bsl_owl);
               viewHolder.transitViewBusRouteRouteIdTextView.setText("");

               return rowView;
           } else {
               if (busRouteModel.getRouteShortName().equals("LUCYGO") || busRouteModel.getRouteShortName().equals("LUCYGR")) {
                   viewHolder.transitViewBusRouteRouteTypeImageView.setImageResource(R.drawable.transitview_listitem_lucy);

                   if (busRouteModel.getRouteShortName().equals("LUCYGO")) {
                       viewHolder.transitViewBusRouteRouteIdTextView.setText("GOLD");
                       viewHolder.transitViewBusRouteRouteIdTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                   } else {
                       viewHolder.transitViewBusRouteRouteIdTextView.setText("GREEN");
                       viewHolder.transitViewBusRouteRouteIdTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                   }

                   return rowView;
               } else {
                   if (busRouteModel.getRouteShortName().equals("MFO")) {
                       viewHolder.transitViewBusRouteRouteTypeImageView.setImageResource(R.drawable.transitview_listitem_mfl_owl);
                       viewHolder.transitViewBusRouteRouteIdTextView.setText("");

                       return rowView;
                   } else {
                       if (busRouteModel.getRouteShortName().equals("BLVDDIR")) {
                           String str = "BLVD%nDIRECT";
                           str = String.format(str);
                           viewHolder.transitViewBusRouteRouteTypeImageView.setImageResource(R.drawable.transitview_listitem_direct_bus);
                           viewHolder.transitViewBusRouteRouteIdTextView.setText(str);
                           viewHolder.transitViewBusRouteRouteIdTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);


                           return rowView;
                       }
                   }
               }
           }
        }

        int routeType = busRouteModel.getRouteType().intValue();
        switch (routeType) {
            case 0:
                // trolley
                viewHolder.transitViewBusRouteRouteTypeImageView.setImageResource(R.drawable.transitview_listitem_trolley);
                break;
            case 1:
                //subway
                viewHolder.transitViewBusRouteRouteTypeImageView.setImageResource(R.drawable.transitview_listitem_bus);
                break;
            case 2:
                //rail
                viewHolder.transitViewBusRouteRouteTypeImageView.setImageResource(R.drawable.transitview_listitem_bus);
                break;
            case 3:
                //bus
                viewHolder.transitViewBusRouteRouteTypeImageView.setImageResource(R.drawable.transitview_listitem_bus);
                break;
            default:
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
        // even though some routes might be out of service, still allow a click and show the route sans the vehicle
        return true;
    }

    public void notifyAlertsLoaded() {
        alertsLoaded = true;
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        ImageView transitViewBusRouteRouteTypeImageView;
        TextView transitViewBusRouteRouteIdTextView;
        TextView transitViewBusRouteStatusTextView;
        ImageView transitViewBusRouteStatusImageView;
    }
}
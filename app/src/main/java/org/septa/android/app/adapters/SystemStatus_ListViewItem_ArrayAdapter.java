/*
 * SystemStatus_ListViewItem_ArrayAdapter.java
 * Last modified on 05-16-2014 21:14-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.models.servicemodels.AlertModel;

import java.util.List;

public class SystemStatus_ListViewItem_ArrayAdapter extends ArrayAdapter<AlertModel> {
    public static final String TAG = SystemStatus_ListViewItem_ArrayAdapter.class.getName();

    private final Context context;
    private final List<AlertModel> values;

    public SystemStatus_ListViewItem_ArrayAdapter(Context context, List<AlertModel> values) {
        super(context, R.layout.systemstatus_listview_route_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = null;

        AlertModel alertInformation = values.get(position);

        rowView = inflater.inflate(R.layout.systemstatus_listview_route_item, parent, false);

        ImageView routeIconImageView = (ImageView)rowView.findViewById(R.id.systemstatus_listview_route_item_route_icon);
        TextView routeTitleTextView = (TextView)rowView.findViewById(R.id.systemstatus_listview_route_item_route_text);

        ImageView advisoryImageView = (ImageView)rowView.findViewById(R.id.systemstatus_listview_route_item_advisory_icon);
        advisoryImageView.setVisibility(View.INVISIBLE);
        ImageView detourImageView = (ImageView)rowView.findViewById(R.id.systemstatus_listview_route_item_detour_icon);
        detourImageView.setVisibility(View.INVISIBLE);
        ImageView alertImageView = (ImageView)rowView.findViewById(R.id.systemstatus_listview_route_item_alert_icon);
        alertImageView.setVisibility(View.INVISIBLE);

        if (alertInformation.isGeneral()) {
            routeTitleTextView.setText("General");
        } else {
            routeTitleTextView.setText(alertInformation.getRouteName());
        }

        if (alertInformation.isBus()) routeIconImageView.setImageResource(R.drawable.ic_systemstatus_bus_black);
        else
        if (alertInformation.isRegionalRail()) routeIconImageView.setImageResource(R.drawable.ic_systemstatus_rrl_blue);
        else
        if (alertInformation.isTrolley()) routeIconImageView.setImageResource(R.drawable.ic_systemstatus_trolley_green);
        else
        if (alertInformation.isMFL()) {
            if (alertInformation.getRouteName().equals("Market Frankford Owl")) {
                routeIconImageView.setImageResource(R.drawable.ic_systemstatus_mfl_owl);
            } else {
                routeIconImageView.setImageResource(R.drawable.ic_systemstatus_mfl_blue);
            }
        }
        else
        if (alertInformation.isBSL()) {
            if (alertInformation.getRouteName().equals("Broad Street Line Owl")) {
                routeIconImageView.setImageResource(R.drawable.ic_systemstatus_bsl_owl);
            } else {
                routeIconImageView.setImageResource(R.drawable.ic_systemstatus_bsl_orange);
            }
        }
        else
        if (alertInformation.isNHSL()) routeIconImageView.setImageResource(R.drawable.ic_systemstatus_nhsl_purple);

        // TODO: replace this drawable with the correct one once obtained.
        // if this is a suspended route, replace the detour icon with the suspended one and ignore all other flags.
        if (alertInformation.isSuspended()) {
            detourImageView.setImageResource(R.drawable.ic_system_status_suspended);
            detourImageView.setVisibility(View.VISIBLE);

            return rowView;
        }

//        // TODO: replace this drawable with the correct one once obtained.
//        if (alertInformation.hasSnowFlag()) {
//            alertImageView.setImageResource(R.drawable.ic_schedules_bsl_small);
//            alertImageView.setVisibility(View.VISIBLE);
//
//            // TODO: figure out if we are suppose to cap the view here like suspended or continue
//            return rowView;
//        }

        if (alertInformation.hasFlag()) {
            if (alertInformation.hasAdvisoryFlag()) {
                advisoryImageView.setVisibility(View.VISIBLE);
            }
            if (alertInformation.hasDetourFlag()) {
                detourImageView.setVisibility(View.VISIBLE);
            }
            if (alertInformation.hasAlertFlag()) {
                alertImageView.setVisibility(View.VISIBLE);
            }
        }

        return rowView;
    }

    @Override
    public boolean areAllItemsEnabled() {

        return true;
    }

    @Override
    public boolean isEnabled(int position) {

        // in the special case of suspended, the row is not selectable.
        if (values.get(position).isSuspended()) {

            return true;
        }

        // if the alert has a flag enabled, then it is selectable
        return values.get(position).hasFlag();
    }
}

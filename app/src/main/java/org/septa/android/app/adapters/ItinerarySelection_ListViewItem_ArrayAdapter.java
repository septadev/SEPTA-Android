/*
 * ItinerarySelection_ListViewItem_ArrayAdapter.java
 * Last modified on 05-13-2014 10:33-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.models.RouteTypes;
import org.septa.android.app.models.TripDataModel;

import java.util.ArrayList;

import roboguice.util.Ln;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class ItinerarySelection_ListViewItem_ArrayAdapter extends BaseAdapter implements
        StickyListHeadersAdapter, SectionIndexer {
    public static final String TAG = ItinerarySelection_ListViewItem_ArrayAdapter.class.getName();
    private final Context mContext;
    private LayoutInflater mInflater;

//    protected ArrayList<SchedulesRouteModel> routes = new ArrayList<SchedulesRouteModel>();

    String[] resourceEndNames;

    String[] directionHeadingLabels = null;
    ArrayList<TripDataModel> tripsForDirection0 = new ArrayList<TripDataModel>();
    ArrayList<TripDataModel> tripsForDirection1 = new ArrayList<TripDataModel>();

    RouteTypes routeType = null;

    private View headerView = null;

    public ItinerarySelection_ListViewItem_ArrayAdapter(Context context, RouteTypes routeType) {
        mContext = context;
        mInflater = LayoutInflater.from(context);

        this.routeType = routeType;

        resourceEndNames = context.getResources().getStringArray(R.array.schedulesfragment_listview_bothimage_endnames);
    }

    public void setDirectionHeadingLabels(String[] directionHeadingLabels) {

        this.directionHeadingLabels = directionHeadingLabels;
    }

    public void setTripDataForDirection0(ArrayList<TripDataModel>tripsForDirection0) {

        this.tripsForDirection0 = tripsForDirection0;
    }

    public void setTripDataForDirection1(ArrayList<TripDataModel>tripsForDirection1) {

        this.tripsForDirection1 = tripsForDirection1;
    }

    protected Object[] getItems() {
        ArrayList<Object> items = new ArrayList<Object>();

        if (tripsForDirection0.size()>0) {

            items.add(new Object());
            items.add(new Object());
            items.addAll(tripsForDirection0);
        }

        if (tripsForDirection1.size()>0) {
            items.add(new Object());
            items.add(new Object());
            items.addAll(tripsForDirection1);
        }

        return items.toArray();
    }

    @Override
    public int getCount() {
        Ln.d("getCount will return "+getItems().length);
        return getItems().length;
    }

    @Override
    public Object getItem(int position) {

        // position-1 to compensate for the start end selection row
        return getItems()[(position)];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView;

        if ((getItem(position) instanceof TripDataModel)) {
            rowView = mInflater.inflate(R.layout.itineraryselection_listview_route_item, parent, false);

            ImageView handicapImageView = (ImageView) rowView.findViewById(R.id.iterinaryselection_accessibilityicon_imageview);
            if (((TripDataModel)getItem(position)).hasWheelBoardingFeature()) {
                Ln.d("the trip is wheel boarding enabled");
                handicapImageView.setVisibility(View.VISIBLE);
            } else {
                Ln.d("the trip is not wheel boarding enabled");
                handicapImageView.setVisibility(View.INVISIBLE);
            }

            TextView textView = (TextView) rowView.findViewById(R.id.iterinaryselection_accessibilityicon_textview);

            Ln.d("setting the trip row text to be "+((TripDataModel)getItem(position)).getStartStopNameTitle());
            textView.setText(((TripDataModel)getItem(position)).getStartStopNameTitle());
        } else {

            rowView = mInflater.inflate(R.layout.itineraryselection_listview_locationinput_item, parent, false);
        }

        return rowView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        View view = null;

        if (position < tripsForDirection0.size()+2) {
            view = mInflater.inflate(R.layout.schedules_routeselection_headerview, parent, false);
            TextView textView = (TextView) view.findViewById(R.id.schedules_routeselection_sectionheader_textview);

            textView.setText(directionHeadingLabels[0]);
        } else {
            view = mInflater.inflate(R.layout.schedules_routeselection_headerview, parent, false);
            TextView textView = (TextView) view.findViewById(R.id.schedules_routeselection_sectionheader_textview);

            textView.setText(directionHeadingLabels[1]);
        }

        return view;
    }

    @Override
    public long getHeaderId(int position) {
        if (position == 0) {
            return 0;
        }

        return 1;
    }

    @Override
    public int getPositionForSection(int section) {
        switch (section) {
            case 0: {

                return 0;
            }
            case 1: {

                return tripsForDirection0.size()+2;
            }
            default: {

                return 0;
            }
        }
    }

    @Override
    public int getSectionForPosition(int position) {
        if (position < tripsForDirection0.size()+2) {

            return 0;
        } else {

            return 1;
        }
    }

    @Override
    public Object[] getSections() {

        return directionHeadingLabels;
    }
}
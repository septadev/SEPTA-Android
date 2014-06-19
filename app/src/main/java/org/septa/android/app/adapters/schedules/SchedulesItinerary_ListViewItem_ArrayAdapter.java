/*
 * SchedulesItinerary_ListViewItem_ArrayAdapter.java
 * Last modified on 05-12-2014 09:48-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.adapters.schedules;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.models.RouteTypes;
import org.septa.android.app.models.TripObject;
import org.septa.android.app.utilities.CalendarDateUtilities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class SchedulesItinerary_ListViewItem_ArrayAdapter extends BaseAdapter implements
        StickyListHeadersAdapter, SectionIndexer {
    public static final String TAG = SchedulesItinerary_ListViewItem_ArrayAdapter.class.getName();
    private final Context context;
    private LayoutInflater mInflater;

    protected ArrayList<TripObject> trips = new ArrayList<TripObject>();

    String[] resourceEndNames;
    String leftImageStartName;
    String rightImageBackgroundName;

    RouteTypes routeType = null;

    private String[] sectionTitles = new String[]{ "Select Start and End (hidden)", "REMAINING TRIPS FOR TODAY"};
    private String headerViewText;
    private int selectedTab = 0;

    private View headerView = null;

    private String routeStartName;
    private String routeEndName;

    public SchedulesItinerary_ListViewItem_ArrayAdapter(Context context, RouteTypes routeType) {
        this.context = context;
        mInflater = LayoutInflater.from(context);

        this.routeType = routeType;

        resourceEndNames = context.getResources().getStringArray(R.array.schedulesfragment_listview_bothimage_endnames);
        leftImageStartName = context.getString(R.string.schedulesfragment_listview_leftimage_startname);

        headerViewText = sectionTitles[1];   // set the default primary header to be the remaining trips
    }

    public void setRouteStartName(String routeStartName) {

        this.routeStartName = routeStartName;
        notifyDataSetChanged();
    }

    public void setRouteEndName(String routeEndName) {

        this.routeEndName = routeEndName;
        notifyDataSetChanged();
    }

    public void setTripObject(ArrayList<TripObject> trips) {
        this.trips = trips;

        notifyDataSetChanged();
    }

    protected Object[] getItems() {
        ArrayList<Object> items = new ArrayList<Object>(getCount());
        items.addAll(trips);

        return items.toArray();
    }

    @Override
    public int getCount() {
        return trips.size()+1;
    }

    @Override
    public Object getItem(int position) {

        // position-1 to compensate for the start end selection row
        return getItems()[(position-1)];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setHeaderViewText(String text) {
        this.headerViewText = text;

        if (headerView == null) {

        } else {
            TextView textView = (TextView) headerView.findViewById(R.id.schedules_routeselection_sectionheader_textview);
            textView.setText(text);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView;

        if (position == 0 ) {

            rowView = mInflater.inflate(R.layout.schedules_itinerary_selectstartend_row, parent, false);
            TextView routeStartStopNameTextView = (TextView)rowView.findViewById(R.id.schedules_itinerary_selectstartend_start_textview);
            TextView routeEndStopNameTextView = (TextView)rowView.findViewById(R.id.schedules_itinerary_selectstartend_end_textview);

            if (this.routeStartName != null) {
                routeStartStopNameTextView.setText(routeStartName);
            }

            if (this.routeEndName != null) {
                routeEndStopNameTextView.setText(routeEndName);
            }

        } else {
//            String[] routeTypeLabels = context.getResources().getStringArray(R.array.schedulesfragment_listview_bothimage_endnames);
            TripObject trip = (TripObject)getItem(position);

            rowView = mInflater.inflate(R.layout.schedules_trip_listview_item, parent, false);

            TextView trainNumber = (TextView)rowView.findViewById(R.id.schedules_trip_listview_trainnumber);
            TextView timeUntilStart = (TextView)rowView.findViewById(R.id.schedules_trip_listview_timeuntilstart);
            TextView startTime = (TextView)rowView.findViewById(R.id.schedules_trip_listview_starttime);
            TextView tripTime = (TextView)rowView.findViewById(R.id.schedules_trip_listview_triptime);
            TextView endTime = (TextView)rowView.findViewById(R.id.schedules_trip_listview_endtime);

            SimpleDateFormat timeDateFormat = new SimpleDateFormat("kkmm");
            Date startDate = null;
            Date endDate = null;
            Date now = new Date();

            String nowHoursMinutes = timeDateFormat.format(now);
            try {
                startDate = new SimpleDateFormat("kkmm").parse(CalendarDateUtilities.getStringFromTime(trip.getStartTime().intValue()));
                endDate = timeDateFormat.parse(CalendarDateUtilities.getStringFromTime(trip.getEndTime().intValue()));
                now = timeDateFormat.parse(nowHoursMinutes);
            } catch (Exception ex) {
                Log.d(TAG, "a parse exception has occurred with the date");
            }

            trainNumber.setText(Integer.toString(trip.getTrainNo().intValue()));
            timeUntilStart.setText(CalendarDateUtilities.formatHoursMinutesDisplay(CalendarDateUtilities.minutesUntilStartTime(now, trip)));
            startTime.setText(CalendarDateUtilities.getLocalizedHHMMStamp(context, startDate));
            endTime.setText(CalendarDateUtilities.getLocalizedHHMMStamp(context, endDate));
            tripTime.setText(Long.toString(CalendarDateUtilities.tripTime(trip)));

            // if we are not on the "Now" tab, hide the hours until since it will not be valid.
            if (selectedTab > 0) {
                timeUntilStart.setVisibility(View.GONE);
            }
        }

        return rowView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        View view = null;

        if (position == 0) {

            view =  mInflater.inflate(R.layout.schedules_routeselection_sectionheader_hiddenview, parent, false);
        } else {
            view = mInflater.inflate(R.layout.schedules_routeselection_headerview, parent, false);
            TextView textView = (TextView) view.findViewById(R.id.schedules_routeselection_sectionheader_textview);

            textView.setText(headerViewText);
            textView.setBackgroundColor(Color.parseColor("#998A1515"));

            this.headerView = view;
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

                return 1;
            }
            default: {

                return 1;
            }
        }
    }

    @Override
    public int getSectionForPosition(int position) {
        if (position==0) {
            return 0;
        }

        return 1;
    }

    @Override
    public Object[] getSections() {

        return sectionTitles;
    }

    public int getSelectedTab() {
        return selectedTab;
    }

    public void setSelectedTab(int selectedTab) {
        this.selectedTab = selectedTab;
    }

    class HeaderViewHolder {
        TextView text;
    }

    class ViewHolder {
        TextView text;
    }
}
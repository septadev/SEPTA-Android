/*
 * SchedulesItinerary_ListViewItem_ArrayAdapter.java
 * Last modified on 05-12-2014 09:48-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.adapters.schedules;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.models.RouteTypes;
import org.septa.android.app.models.TripObject;
import org.septa.android.app.models.servicemodels.TrainViewModel;
import org.septa.android.app.utilities.CalendarDateUtilities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class SchedulesItinerary_ListViewItem_ArrayAdapter extends BaseAdapter implements
        StickyListHeadersAdapter {
    public static final String TAG = SchedulesItinerary_ListViewItem_ArrayAdapter.class.getName();
    private final Context context;
    private LayoutInflater mInflater;

    protected ArrayList<TripObject> trips = new ArrayList<TripObject>();

    String[] resourceEndNames;
    String leftImageStartName;

    RouteTypes routeType = null;

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

        String[] sectionTitles = new String[]{"Select Start and End (hidden)", "REMAINING TRIPS FOR TODAY"};
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

        }
        else {
            TripObject tripObject = (TripObject) getItem(position);

            if (tripObject.getTrainViewModel() != null && selectedTab == 0) {

                rowView = mInflater.inflate(R.layout.schedules_in_service_item, parent, false);

                TextView destination = (TextView) rowView.findViewById(R.id.destination);
                TextView nextStop = (TextView) rowView.findViewById(R.id.next_stop);
                TextView trackNumber = (TextView) rowView.findViewById(R.id.track_number);
                TextView trainNumber = (TextView) rowView.findViewById(R.id.train_number);
                TextView trainStatus = (TextView) rowView.findViewById(R.id.train_status);

                ViewGroup trackNumberLayout = (ViewGroup) rowView.findViewById(R.id.track_number_layout);

                TrainViewModel trainViewModel = tripObject.getTrainViewModel();

                // Set the destination
                String destinationString = trainViewModel.getDestination();
                if (!TextUtils.isEmpty(destinationString)) {
                    destination.setText(String.format(context.getString(R.string.destination), destinationString));
                }
                else {
                    destination.setVisibility(View.INVISIBLE);
                }

                // Set the next stop
                String nextStopString = trainViewModel.getNextStop();
                if (!TextUtils.isEmpty(nextStopString)) {
                    nextStop.setText(String.format(context.getString(R.string.next_stop), nextStopString));
                }
                else {
                    nextStop.setVisibility(View.INVISIBLE);
                }

                // Set the track number
                String trackNumberString = trainViewModel.getTrack();
                if (!TextUtils.isEmpty(trackNumberString)) {
                    trackNumber.setText(trackNumberString);
                }
                else {
                    trackNumberLayout.setVisibility(View.GONE);
                }

                // Set the train number
                String trainNumberString = trainViewModel.getTrainNumber();
                if (!TextUtils.isEmpty(trainNumberString)) {
                    trainNumber.setText(trainNumberString);
                }
                else {
                    trainNumber.setVisibility(View.INVISIBLE);
                }

                // Set the train status
                if (trainViewModel.isLate()) {
                    int trainStatusInt = trainViewModel.getLate();
                    trainStatus.setTextColor(context.getResources().getColor(R.color.text_late));
                    trainStatus.setText(String.format(context.getString(R.string.late), trainStatusInt));
                }
                else {
                    trainStatus.setTextColor(context.getResources().getColor(R.color.text_on_time));
                    trainStatus.setText("On Time");
                }

            }
            else {
                TripObject trip = (TripObject) getItem(position);
                //            Log.d(TAG, "this trip is "+trip.print());

                rowView = mInflater.inflate(R.layout.schedules_trip_listview_item, parent, false);

                TextView trainNumber = (TextView) rowView.findViewById(R.id.schedules_trip_listview_trainnumber);
                TextView timeUntilStart = (TextView) rowView.findViewById(R.id.schedules_trip_listview_timeuntilstart);
                TextView startTime = (TextView) rowView.findViewById(R.id.schedules_trip_listview_starttime);
                TextView tripTime = (TextView) rowView.findViewById(R.id.schedules_trip_listview_triptime);
                TextView endTime = (TextView) rowView.findViewById(R.id.schedules_trip_listview_endtime);

                SimpleDateFormat timeDateFormat = new SimpleDateFormat("kkmm");
                Date startDate = null;
                Date endDate = null;
                Date now = new Date();

                String nowHoursMinutes = timeDateFormat.format(now);
                try {
                    startDate = new SimpleDateFormat("kkmm").parse(CalendarDateUtilities.getStringFromTime(trip.getStartTime().intValue()));
                    endDate = timeDateFormat.parse(CalendarDateUtilities.getStringFromTime(trip.getEndTime().intValue()));
                    now = timeDateFormat.parse(nowHoursMinutes);
                }
                catch (Exception ex) {
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
        }

        return rowView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        View view = null;

        if (position == 0) {
            view =  mInflater.inflate(R.layout.schedules_routeselection_sectionheader_hiddenview, parent, false);
        }
        // If it is the first in service list item, add in service header
        else if (getInServiceItemCount() > 0 && position >= 1 && position < 1 + getInServiceItemCount()) {

                view = mInflater.inflate(R.layout.schedules_routeselection_headerview, parent, false);
                TextView textView = (TextView) view.findViewById(R.id.schedules_routeselection_sectionheader_textview);

                textView.setText(view.getContext().getString(R.string.in_service_header));
                textView.setBackgroundColor(view.getContext().getResources().getColor(R.color.in_service_header_green));

                this.headerView = view;
        }
        // Otherwise, if it is the first list item after in service trains, add remaining trips header
        else {//if (position >= 1 + getInServiceItemCount()) {
            view = mInflater.inflate(R.layout.schedules_routeselection_headerview, parent, false);
            TextView textView = (TextView) view.findViewById(R.id.schedules_routeselection_sectionheader_textview);

            textView.setText(headerViewText);
            textView.setBackgroundColor(view.getContext().getResources().getColor(R.color.remaining_trips_header_red));

            this.headerView = view;
        }

        return view;
    }

    @Override
    public long getHeaderId(int position) {
        if (position == 0) {
            return 0;
        }
        TripObject tripObject = (TripObject) getItem(position);

        //
        if (tripObject.getTrainViewModel() != null) {
            return 1;
        }
        //
        else {
            return 2;
        }
    }

    private int getInServiceItemCount() {
        int i = 0;
        for (TripObject tripObject : trips) {
            if (tripObject.getTrainViewModel() != null) {
                i++;
            }
        }
        return i;
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
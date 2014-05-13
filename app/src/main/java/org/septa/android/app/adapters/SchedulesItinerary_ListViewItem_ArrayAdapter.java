/*
 * SchedulesItinerary_ListViewItem_ArrayAdapter.java
 * Last modified on 05-12-2014 09:48-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.models.RouteTypes;
import org.septa.android.app.models.SchedulesRouteModel;

import java.util.ArrayList;

import roboguice.util.Ln;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class SchedulesItinerary_ListViewItem_ArrayAdapter extends BaseAdapter implements
        StickyListHeadersAdapter, SectionIndexer {
    public static final String TAG = SchedulesItinerary_ListViewItem_ArrayAdapter.class.getName();
    private final Context mContext;
    private LayoutInflater mInflater;

    protected ArrayList<SchedulesRouteModel> routes = new ArrayList<SchedulesRouteModel>();

    String[] resourceEndNames;
    String leftImageStartName;
    String rightImageBackgroundName;

    RouteTypes routeType = null;

    private String[] sectionTitles = new String[]{ "Select Start and End (hidden)", "REMAINING TRIPS FOR TODAY"};

    private View headerView = null;

    public SchedulesItinerary_ListViewItem_ArrayAdapter(Context context, RouteTypes routeType) {
        mContext = context;
        mInflater = LayoutInflater.from(context);

        this.routeType = routeType;

        resourceEndNames = context.getResources().getStringArray(R.array.schedulesfragment_listview_bothimage_endnames);
        leftImageStartName = context.getString(R.string.schedulesfragment_listview_leftimage_startname);
    }

    public void setSchedulesRouteModel(ArrayList<SchedulesRouteModel> routes) {
        this.routes = routes;

        notifyDataSetChanged();
    }

    protected Object[] getItems() {
        ArrayList<Object> items = new ArrayList<Object>(getCount());
        items.addAll(routes);

        return items.toArray();
    }

    @Override
    public int getCount() {
        return routes.size()+1;
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
        if (headerView == null) {
            Ln.d("the headerView is null");
        } else {
            TextView textView = (TextView) headerView.findViewById(R.id.schedules_routeselection_sectionheader_textview);
            if (textView == null) {
                Ln.d("the textView is null");
            }
            textView.setText(text);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView;

        if (position == 0 ) {

            rowView = mInflater.inflate(R.layout.schedules_itinerary_selectstartend_row, parent, false);
        } else {
            String[] routeTypeLabels = mContext.getResources().getStringArray(R.array.schedulesfragment_listview_bothimage_endnames);

            SchedulesRouteModel rtm = (SchedulesRouteModel) getItem(position);
            rowView = mInflater.inflate(R.layout.schedules_routeselection_routes_listview_item, parent, false);
            ImageView leftIconImageView = (ImageView) rowView.findViewById(R.id.schedules_routeselect_item_leftImageView);
            ImageView rightBackgroundImageView = (ImageView) rowView.findViewById(R.id.schedules_routeselection_item_rightImageBackgroundview);
            TextView routeIdTextView = (TextView) rowView.findViewById(R.id.schedules_routeselection_item_routeid);
            TextView routeLongNameTextView = (TextView) rowView.findViewById(R.id.schedules_routeselection_item_routelongname);

            int id = mContext.getResources().getIdentifier(leftImageStartName + routeTypeLabels[routeType.ordinal()] + "_small", "drawable", mContext.getPackageName());
            leftIconImageView.setImageResource(id);

            id = mContext.getResources().getIdentifier(rightImageBackgroundName + routeTypeLabels[routeType.ordinal()], "drawable", mContext.getPackageName());
            rightBackgroundImageView.setImageResource(id);

            routeIdTextView.setText(routes.get((position-1)).getRouteId());

            switch (routes.get((position-1)).getRouteId().length()) {
                case 6: {
                    routeIdTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    break;
                }
                case 5: {
                    routeIdTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                    break;
                }
                case 4: {
                    routeIdTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                    break;
                }
                default: {
                }
            }

            routeLongNameTextView.setText(routes.get((position-1)).getRouteLongName());
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

            textView.setText(sectionTitles[1]);
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

    class HeaderViewHolder {
        TextView text;
    }

    class ViewHolder {
        TextView text;
    }
}
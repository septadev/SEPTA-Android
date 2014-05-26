/*
 * NextToArrive_ListViewItem_ArrayAdapter.java
 * Last modified on 05-22-2014 12:33-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.models.SchedulesFavoriteModel;
import org.septa.android.app.models.SchedulesRecentlyViewedModel;
import org.septa.android.app.models.SchedulesRouteModel;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class NextToArrive_ListViewItem_ArrayAdapter extends BaseAdapter implements
        StickyListHeadersAdapter, SectionIndexer {
    public static final String TAG = NextToArrive_ListViewItem_ArrayAdapter.class.getName();

    private final Context mContext;
    private LayoutInflater mInflater;

    private ArrayList<SchedulesFavoriteModel> favorites = new ArrayList<SchedulesFavoriteModel>();
    private ArrayList<SchedulesRecentlyViewedModel> recentlyViewed = new ArrayList<SchedulesRecentlyViewedModel>();
    protected ArrayList<SchedulesRouteModel> nextToArriveTrainList = new ArrayList<SchedulesRouteModel>();

    private String[] sectionTitles = new String[]{ "Favorites", "Recently Viewed", "Next To Arrive Trains"};

    public NextToArrive_ListViewItem_ArrayAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);

        SchedulesFavoriteModel fm = new SchedulesFavoriteModel();
        favorites.add(fm);
        fm = new SchedulesFavoriteModel();
        favorites.add(fm);
        fm = new SchedulesFavoriteModel();
        favorites.add(fm);

        SchedulesRecentlyViewedModel rvm = new SchedulesRecentlyViewedModel();
        recentlyViewed.add(rvm);
        rvm = new SchedulesRecentlyViewedModel();
        recentlyViewed.add(rvm);

        SchedulesRouteModel rm = new SchedulesRouteModel();
        nextToArriveTrainList.add(rm);
        rm = new SchedulesRouteModel();
        nextToArriveTrainList.add(rm);
        rm = new SchedulesRouteModel();
        nextToArriveTrainList.add(rm);
        rm = new SchedulesRouteModel();
        nextToArriveTrainList.add(rm);
        rm = new SchedulesRouteModel();
        nextToArriveTrainList.add(rm);
        rm = new SchedulesRouteModel();
        nextToArriveTrainList.add(rm);
        rm = new SchedulesRouteModel();
        nextToArriveTrainList.add(rm);
        rm = new SchedulesRouteModel();
        nextToArriveTrainList.add(rm);
        rm = new SchedulesRouteModel();
        nextToArriveTrainList.add(rm);
        rm = new SchedulesRouteModel();
        nextToArriveTrainList.add(rm);
        rm = new SchedulesRouteModel();
        nextToArriveTrainList.add(rm);
        rm = new SchedulesRouteModel();
        nextToArriveTrainList.add(rm);
        rm = new SchedulesRouteModel();
        nextToArriveTrainList.add(rm);
        rm = new SchedulesRouteModel();
        nextToArriveTrainList.add(rm);
        rm = new SchedulesRouteModel();
        nextToArriveTrainList.add(rm);
        rm = new SchedulesRouteModel();
        nextToArriveTrainList.add(rm);
        rm = new SchedulesRouteModel();
        nextToArriveTrainList.add(rm);
        rm = new SchedulesRouteModel();
        nextToArriveTrainList.add(rm);
        rm = new SchedulesRouteModel();
        nextToArriveTrainList.add(rm);
        rm = new SchedulesRouteModel();
        nextToArriveTrainList.add(rm);
    }

    public void setNextToArriveTrainList(ArrayList<SchedulesRouteModel> nextToArriveTrainList) {
        this.nextToArriveTrainList = nextToArriveTrainList;

        notifyDataSetChanged();
    }

    public void clearNextToArriveTrainList() {
        this.nextToArriveTrainList = new ArrayList<SchedulesRouteModel>();

        notifyDataSetChanged();
    }

    protected Object[] getItems() {
        ArrayList<Object> items = new ArrayList<Object>(getCount());
        items.addAll(favorites); items.addAll(recentlyViewed); items.addAll(nextToArriveTrainList);

        return items.toArray();
    }

    public boolean isFavorite(int position) {

        return getItems()[position] instanceof SchedulesFavoriteModel;
    }

    public boolean isRecentlyViewed(int position) {

        return getItems()[position] instanceof SchedulesRecentlyViewedModel;
    }

    @Override
    public int getCount() {

        return favorites.size()+recentlyViewed.size()+nextToArriveTrainList.size();
    }

    @Override
    public Object getItem(int position) {

        return getItems()[position];
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = null;

        if (isFavorite(position)) {     // favorite position rows
            rowView = mInflater.inflate(R.layout.nexttoarrive_favoriteandrecentlyviewed_listview_item, parent, false);

            TextView startStopNameTextView = (TextView)rowView.findViewById(R.id.nexttoarrive_favoriteandrecentlyviewed_start_textview);
            TextView endStopNameTextView = (TextView)rowView.findViewById(R.id.nexttoarrive_favoriteandrecentlyviewed_end_textview);

            startStopNameTextView.setText("testing start");
            endStopNameTextView.setText("testing end");

            // to create a larger space in the ListView, each row has a transparent view built in
            // if we are not the last row in the logical section, make it gone, else visible
            View transparentView = (View)rowView.findViewById(R.id.nexttoarrive_favoriteandrecentlyviewed_transparent_view);
            if (position == (favorites.size()-1)) {
                transparentView.setVisibility(View.VISIBLE);
            } else {
                transparentView.setVisibility(View.GONE);
            }
        } else {                        // recently viewed position rows
            if (isRecentlyViewed(position)) {
                rowView = mInflater.inflate(R.layout.nexttoarrive_favoriteandrecentlyviewed_listview_item, parent, false);

//                SchedulesRecentlyViewedModel rm = (SchedulesRecentlyViewedModel)getItem(position);

                TextView startStopNameTextView = (TextView)rowView.findViewById(R.id.nexttoarrive_favoriteandrecentlyviewed_start_textview);
                TextView endStopNameTextView = (TextView)rowView.findViewById(R.id.nexttoarrive_favoriteandrecentlyviewed_end_textview);

                startStopNameTextView.setText("testing start");
                endStopNameTextView.setText("testing end");

                // to create a larger space in the ListView, each row has a transparent view built in
                // if we are not the last row in the logical section, make it gone, else visible
                View transparentView = (View)rowView.findViewById(R.id.nexttoarrive_favoriteandrecentlyviewed_transparent_view);
                if (position == ((favorites.size()+recentlyViewed.size())-1)) {
                    transparentView.setVisibility(View.VISIBLE);
                } else {
                    transparentView.setVisibility(View.GONE);
                }
            } else{
//                String[] routeTypeLabels = mContext.getResources().getStringArray(R.array.schedulesfragment_listview_bothimage_endnames);
//
                SchedulesRouteModel rtm = (SchedulesRouteModel)getItem(position);
                rowView = mInflater.inflate(R.layout.nexttoarrive_nexttoarrivetrains_listview_item, parent, false);
//                ImageView leftIconImageView = (ImageView)rowView.findViewById(R.id.schedules_routeselect_item_leftImageView);
//                ImageView rightBackgroundImageView = (ImageView)rowView.findViewById(R.id.schedules_routeselection_item_rightImageBackgroundview);
//                TextView routeIdTextView = (TextView)rowView.findViewById(R.id.schedules_routeselection_item_routeid);
//                TextView routeLongNameTextView = (TextView)rowView.findViewById(R.id.schedules_routeselection_item_routelongname);
//
//                int id = mContext.getResources().getIdentifier(leftImageStartName + routeTypeLabels[routeType.ordinal()] + "_small", "drawable", mContext.getPackageName());
//                leftIconImageView.setImageResource(id);
//
//                id = mContext.getResources().getIdentifier(rightImageBackgroundName + routeTypeLabels[routeType.ordinal()], "drawable", mContext.getPackageName());
//                rightBackgroundImageView.setImageResource(id);
//
//                routeIdTextView.setText(routes.get(position).getRouteId());
//
//                switch (routes.get(position).getRouteId().length()) {
//                    case 6: {
//                        routeIdTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
//                        break;
//                    }
//                    case 5: {
//                        routeIdTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
//                        break;
//                    }
//                    case 4: {
//                        routeIdTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
//                        break;
//                    }
//                    default: {
//                    }
//                }
//
//                routeLongNameTextView.setText(routes.get(position).getRouteLongName());
            }
        }

        return rowView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;

        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = mInflater.inflate(R.layout.schedules_routeselection_headerview, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.schedules_routeselection_sectionheader_textview);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        if (favorites.size()>0 && position<favorites.size()) {
            holder.text.setText(sectionTitles[0]);
            holder.text.setBackgroundColor(Color.parseColor("#990DA44A"));
        } else {
            if (recentlyViewed.size()>0 && (position-favorites.size())<recentlyViewed.size()) {
                holder.text.setText(sectionTitles[1]);
                holder.text.setBackgroundColor(Color.parseColor("#990DA44A"));
            } else {
                holder.text.setText(sectionTitles[2]);

                // TODO: adjust this to the correct color
                holder.text.setBackgroundColor(Color.parseColor("#99F04E43"));
            }
        }

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        if (isFavorite(position)) {

            return 0;
        } else {
            if (isRecentlyViewed(position)) {

                return 1;
            }
        }

        return 2;
    }

    @Override
    public int getPositionForSection(int section) {
        switch (section) {
            case 0: {

                return 0;
            }
            case 1: {

                return favorites.size();
            }
            case 2: {

                return favorites.size()+recentlyViewed.size();
            }
            default: {

                return 0;
            }
        }
    }

    @Override
    public int getSectionForPosition(int position) {
        if (isFavorite(position)) {
            return 0;
        } else {
            if (isRecentlyViewed(position)) {
                return 1;
            }
        }

        return 2;
    }

    @Override
    public Object[] getSections() {

        return sectionTitles;
    }

    class HeaderViewHolder {
        TextView text;
    }
}
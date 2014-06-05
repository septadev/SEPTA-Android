/*
 * NextToArrive_ListViewItem_ArrayAdapter.java
 * Last modified on 05-22-2014 12:33-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.adapters;

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

    private String[] sectionTitles = new String[]{ "<start_end_import>", "Favorites", "Recently Viewed", "Next To Arrive Trains"};

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

        if (position == 0) {
            rowView = mInflater.inflate(R.layout.nexttoarrive_favoriteandrecentlyviewed_listview_item, parent, false);
            rowView.setVisibility(View.GONE);

            return rowView;
        }

        if (isFavorite(position)) {     // favorite position rows
            Log.d(TAG, "getView for position is a fav at position "+position);
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
                Log.d(TAG, "getView for position is a recentlyviewed at position "+position);
                rowView = mInflater.inflate(R.layout.nexttoarrive_favoriteandrecentlyviewed_listview_item, parent, false);

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
                Log.d(TAG, "getView for position is a train schedule entry at position "+position);

                SchedulesRouteModel rtm = (SchedulesRouteModel)getItem(position);
                rowView = mInflater.inflate(R.layout.nexttoarrive_nexttoarrivetrains_listview_item, parent, false);
            }
        }

        return rowView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(R.layout.schedules_routeselection_headerview, parent, false);
        TextView text = (TextView) convertView.findViewById(R.id.schedules_routeselection_sectionheader_textview);

        if (position == 0) {
            convertView = mInflater.inflate(R.layout.nexttoarrive_selectstartend_row, parent, false);
        } else {

            if (favorites.size() > 0 && position < favorites.size()) {
                text.setText(sectionTitles[1]);
                text.setBackgroundColor(Color.parseColor("#990DA44A"));
            } else {
                if (recentlyViewed.size() > 0 && (position - favorites.size()) < recentlyViewed.size()) {
                    text.setText(sectionTitles[2]);
                    text.setBackgroundColor(Color.parseColor("#990DA44A"));
                } else {
                    text.setText(sectionTitles[3]);

                    text.setBackgroundColor(Color.parseColor("#99F04E43"));
                }
            }
        }

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        if (position == 0) {

            return 0;
        }
        if (isFavorite(position)) {

            return 1;
        } else {
            if (isRecentlyViewed(position)) {

                return 2;
            }
        }

        return 3;
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
            case 2: {

                return favorites.size();
            }
            case 3: {

                return favorites.size()+recentlyViewed.size();
            }
            default: {

                return 0;
            }
        }
    }

    @Override
    public int getSectionForPosition(int position) {
        Log.d(TAG, "getSectionForPosition for the position "+position);
        if (isFavorite(position)) {
            return 1;
        } else {
            if (isRecentlyViewed(position)) {
                return 2;
            }
        }
        return 3;
    }

    @Override
    public Object[] getSections() {
    Log.d(TAG, "getSections");
        return sectionTitles;
    }
}
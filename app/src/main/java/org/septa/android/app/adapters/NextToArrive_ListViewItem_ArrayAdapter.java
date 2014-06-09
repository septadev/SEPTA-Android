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
import org.septa.android.app.managers.NextToArriveFavoritesAndRecentlyViewedStore;
import org.septa.android.app.models.NextToArriveFavoriteModel;
import org.septa.android.app.models.NextToArriveRecentlyViewedModel;
import org.septa.android.app.models.NextToArriveStoredTripModel;
import org.septa.android.app.models.ObjectFactory;
import org.septa.android.app.models.SchedulesFavoriteModel;
import org.septa.android.app.models.SchedulesRecentlyViewedModel;
import org.septa.android.app.models.SchedulesRouteModel;
import org.septa.android.app.models.servicemodels.NextToArriveModel;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class NextToArrive_ListViewItem_ArrayAdapter extends BaseAdapter implements
        StickyListHeadersAdapter, SectionIndexer {
    public static final String TAG = NextToArrive_ListViewItem_ArrayAdapter.class.getName();

    private final Context mContext;
    private LayoutInflater mInflater;

    private ArrayList<NextToArriveFavoriteModel> favorites = new ArrayList<NextToArriveFavoriteModel>();
    private ArrayList<NextToArriveRecentlyViewedModel> recentlyViewed = new ArrayList<NextToArriveRecentlyViewedModel>();
    protected ArrayList<NextToArriveModel> nextToArriveTrainList = new ArrayList<NextToArriveModel>();

    private String[] sectionTitles = new String[]{ "<start_end_import>", "Favorites", "Recently Viewed", "Next To Arrive Trains"};

    private String startStopName;
    private String destinationStopName;

    private NextToArriveFavoritesAndRecentlyViewedStore nextToArriveFavoritesAndRecentlyViewedStore;

    public NextToArrive_ListViewItem_ArrayAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);

        nextToArriveFavoritesAndRecentlyViewedStore = new NextToArriveFavoritesAndRecentlyViewedStore(mContext);
        recentlyViewed = nextToArriveFavoritesAndRecentlyViewedStore.getRecentlyViewedList();

        // add a single record with no values as a "dummy" record that will be made View.GONE but will keep the header showing
        // a bit of hack to get the current library being used for sticky headers to work.
        NextToArriveModel rm = new NextToArriveModel();
        nextToArriveTrainList.add(rm);
    }

    public void setNextToArriveTrainList(ArrayList<NextToArriveModel> nextToArriveTrainList) {
        this.nextToArriveTrainList = nextToArriveTrainList;

        recentlyViewed = nextToArriveFavoritesAndRecentlyViewedStore.getRecentlyViewedList();

        notifyDataSetChanged();
    }

    public void clearNextToArriveTrainList() {
        this.nextToArriveTrainList = new ArrayList<NextToArriveModel>();

        notifyDataSetChanged();
    }

    protected Object[] getItems() {
        ArrayList<Object> items = new ArrayList<Object>(getCount());
        items.addAll(favorites); items.addAll(recentlyViewed); items.addAll(nextToArriveTrainList);

        return items.toArray();
    }

    public boolean isFavorite(int position) {

        return getItems()[position] instanceof NextToArriveFavoriteModel;
    }

    public boolean isRecentlyViewed(int position) {

        return getItems()[position] instanceof NextToArriveRecentlyViewedModel;
    }

    @Override
    public int getCount() {

        return 1+favorites.size()+recentlyViewed.size()+nextToArriveTrainList.size();
    }

    @Override
    public Object getItem(int position) {

        return getItems()[position];
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    private int adjustedPosition(int position) {
        return --position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = null;

        if (position == 0) {
            rowView = mInflater.inflate(R.layout.nexttoarrive_favoriteandrecentlyviewed_listview_item, parent, false);
            rowView.setVisibility(View.GONE);

            return rowView;
        }

        if (isFavorite(adjustedPosition(position))) {     // favorite position rows
            rowView = mInflater.inflate(R.layout.nexttoarrive_favoriteandrecentlyviewed_listview_item, parent, false);

            TextView startStopNameTextView = (TextView)rowView.findViewById(R.id.nexttoarrive_favoriteandrecentlyviewed_start_textview);
            TextView endStopNameTextView = (TextView)rowView.findViewById(R.id.nexttoarrive_favoriteandrecentlyviewed_end_textview);


            NextToArriveFavoriteModel favoriteModel = (NextToArriveFavoriteModel)getItems()[adjustedPosition(position)];
            startStopNameTextView.setText(favoriteModel.getStartStopName());
            endStopNameTextView.setText(favoriteModel.getDestinationStopName());

            // to create a larger space in the ListView, each row has a transparent view built in
            // if we are not the last row in the logical section, make it gone, else visible
            View transparentView = (View)rowView.findViewById(R.id.nexttoarrive_favoriteandrecentlyviewed_transparent_view);
            if (adjustedPosition(position) == (favorites.size()-1)) {
                transparentView.setVisibility(View.VISIBLE);
            } else {
                transparentView.setVisibility(View.GONE);
            }
        } else {                        // recently viewed position rows
            if (isRecentlyViewed(adjustedPosition(position))) {
                rowView = mInflater.inflate(R.layout.nexttoarrive_favoriteandrecentlyviewed_listview_item, parent, false);

                TextView startStopNameTextView = (TextView)rowView.findViewById(R.id.nexttoarrive_favoriteandrecentlyviewed_start_textview);
                TextView endStopNameTextView = (TextView)rowView.findViewById(R.id.nexttoarrive_favoriteandrecentlyviewed_end_textview);

                NextToArriveRecentlyViewedModel recentlyViewedModel = (NextToArriveRecentlyViewedModel)getItems()[adjustedPosition(position)];
                startStopNameTextView.setText(recentlyViewedModel.getStartStopName());
                endStopNameTextView.setText(recentlyViewedModel.getDestinationStopName());

                // to create a larger space in the ListView, each row has a transparent view built in
                // if we are not the last row in the logical section, make it gone, else visible
                View transparentView = (View)rowView.findViewById(R.id.nexttoarrive_favoriteandrecentlyviewed_transparent_view);
                if (adjustedPosition(position) == ((favorites.size()+recentlyViewed.size())-1)) {
                    transparentView.setVisibility(View.VISIBLE);
                } else {
                    transparentView.setVisibility(View.GONE);
                }
            } else{
                NextToArriveModel ntarm = (NextToArriveModel)getItem(adjustedPosition(position));
                rowView = mInflater.inflate(R.layout.nexttoarrive_nexttoarrivetrains_listview_item, parent, false);

                TextView trainNumberTextView = (TextView)rowView.findViewById(R.id.nexttoarrive_nexttoarrivetrains_trainnumber_textview);
                TextView latenessTextView = (TextView)rowView.findViewById(R.id.nexttoarrive_nexttoarrivetrains_lateness_textview);
                TextView startTimeTextView = (TextView)rowView.findViewById(R.id.nexttoarrive_nexttoarrivetrains_starttime_textview);
                TextView endTimeTextView = (TextView)rowView.findViewById(R.id.nexttoarrive_nexttoarrivetrains_endtime_textview);
                TextView routeNameTextView = (TextView)rowView.findViewById(R.id.nexttoarrive_nexttoarrivetrains_routename_textview);

                NextToArriveModel nextToArriveModel = (NextToArriveModel)getItems()[adjustedPosition(position)];

                if (nextToArriveModel.getOriginalTrain() == null) { // found a dummy blank row
                    rowView.setVisibility(View.GONE);
                    return rowView;
                } else {
                    rowView.setVisibility(View.VISIBLE);
                }

                trainNumberTextView.setText(nextToArriveModel.getOriginalTrain());
                latenessTextView.setText(nextToArriveModel.getOriginalDelay());
                startTimeTextView.setText(nextToArriveModel.getOriginalArrivalTime());
                endTimeTextView.setText(nextToArriveModel.getOriginalDepartureTime());
                routeNameTextView.setText(nextToArriveModel.getOriginalLine());

                if (nextToArriveModel.getOriginalDelay().equals("On time")) {
                    latenessTextView.setTextColor(Color.parseColor("#FF00A354"));
                } else {
                    latenessTextView.setTextColor(Color.parseColor("#FFFC4745"));
                }
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
            TextView startStopNameTextView = (TextView)convertView.findViewById(R.id.nexttoarrive_selectstartend_start_textview);
            TextView destinationStopNameTextView = (TextView)convertView.findViewById(R.id.nexttoarrive_selectstartend_end_textview);

            startStopNameTextView.setText(this.startStopName);
            destinationStopNameTextView.setText(this.destinationStopName);
        } else {

            if (favorites.size() > 0 && adjustedPosition(position) < favorites.size()) {
                text.setText(sectionTitles[1]);
                text.setBackgroundColor(Color.parseColor("#990DA44A"));
            } else {
                if (recentlyViewed.size() > 0 && (adjustedPosition(position) - favorites.size()) < recentlyViewed.size()) {
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
        if (isFavorite(adjustedPosition(position))) {

            return 1;
        } else {
            if (isRecentlyViewed(adjustedPosition(position))) {

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

    public void setStartStopName(String startStopName) {
        this.startStopName = startStopName;

        notifyDataSetChanged();
    }

    public void setDestinationStopName(String destinationStopName) {
        this.destinationStopName = destinationStopName;

        notifyDataSetChanged();
    }

    public NextToArriveStoredTripModel getSelectedFavoriteOrRecentlyViewed(int position) {

        return (NextToArriveStoredTripModel)getItems()[adjustedPosition(position)];
    }
}
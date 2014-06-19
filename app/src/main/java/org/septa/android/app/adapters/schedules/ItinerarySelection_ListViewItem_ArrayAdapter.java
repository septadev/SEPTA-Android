/*
 * ItinerarySelection_ListViewItem_ArrayAdapter.java
 * Last modified on 05-13-2014 10:33-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.adapters.schedules;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.models.RouteTypes;
import org.septa.android.app.models.StopModel;
import org.septa.android.app.models.TripDataModel;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class ItinerarySelection_ListViewItem_ArrayAdapter extends BaseAdapter implements
        StickyListHeadersAdapter, SectionIndexer {
    public static final String TAG = ItinerarySelection_ListViewItem_ArrayAdapter.class.getName();
    private final Context mContext;
    private LayoutInflater mInflater;

    String[] resourceEndNames;

    String[] directionHeadingLabels = null;
    ArrayList<StopModel> stopsForDirection0 = new ArrayList<StopModel>();
    ArrayList<StopModel> stopsForDirection1 = new ArrayList<StopModel>();

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

    public void setTripDataForDirection0(ArrayList<StopModel>stopsForDirection0) {

        this.stopsForDirection0 = stopsForDirection0;
    }

    public void setTripDataForDirection1(ArrayList<StopModel>stopsForDirection1) {

        this.stopsForDirection1 = stopsForDirection1;
    }

    public Object[] getItems() {
        StopModel[] stopModelsArray = new StopModel[stopsForDirection0.size()+stopsForDirection1.size()];

        int i = 0;
        for (StopModel stopModel : stopsForDirection0) {
            stopModelsArray[i++] = stopModel;
        }
        for (StopModel stopModel : stopsForDirection1) {
            stopModelsArray[i++] = stopModel;
        }

        return stopModelsArray;
    }

    @Override
    public int getCount() {

        return getItems().length;
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
        View rowView;

        rowView = mInflater.inflate(R.layout.nexttoarrive_listview_stop_item, parent, false);

        ImageView icon_imageView = (ImageView) rowView.findViewById(R.id.nexttoarrive_stopselection_accessibilityicon_imageview);
        TextView text_TextView = (TextView) rowView.findViewById(R.id.nexttoarrive_stopselection_textview);

        if (((StopModel)getItem(position)).hasWheelBoardingFeature()) {
            icon_imageView.setVisibility(View.VISIBLE);
        } else {
            icon_imageView.setVisibility(View.INVISIBLE);
        }

        text_TextView.setText(((StopModel)getItem(position)).getStopName());

        if (position > stopsForDirection0.size()) {
            LinearLayout mainLayout = (LinearLayout)rowView.findViewById(R.id.nettoarrive_stopselection_mainlayout);
            mainLayout.setBackgroundColor(Color.parseColor("#FFCACACB"));
        }

        return rowView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "getHeaderView for position "+position);
        View view = null;

        String backgroundColor = mContext.getResources().getStringArray(R.array.schedules_routeselection_routesheader_colors)[5];

        view = mInflater.inflate(R.layout.schedules_routeselection_headerview, parent, false);
        TextView textView = (TextView) view.findViewById(R.id.schedules_routeselection_sectionheader_textview);

        LinearLayout mainLayout = (LinearLayout)view.findViewById(R.id.schedules_routeselection_sectionheader_view);
        mainLayout.setBackgroundColor(Color.parseColor(backgroundColor));

        if (position < stopsForDirection0.size()) {
            textView.setText(directionHeadingLabels[0]);
        } else {
            textView.setText(directionHeadingLabels[1]);
        }

        return view;
    }

    @Override
    public long getHeaderId(int position) {
        Log.d(TAG, "getHeaderId for position "+position);
        if (position < stopsForDirection0.size()) {
            return 0;
        }

        return 1;
    }

    @Override
    public int getPositionForSection(int section) {
        Log.d(TAG, "getPositionForSection for section "+section);
        switch (section) {
            case 0: {

                return 0;
            }
            case 1: {

                return stopsForDirection0.size();
            }
            default: {

                return 0;
            }
        }
    }

    @Override
    public int getSectionForPosition(int position) {
        if (position < stopsForDirection0.size()) {
            Log.d(TAG, "getSectionForPosition returning 0 for position "+position+" given size of 0 as "+stopsForDirection0.size());

            return 0;
        } else {
            Log.d(TAG, "getSectionForPosition returning 1 for position "+position+" given size of 0 as "+stopsForDirection0.size());
            return 1;
        }
    }

    @Override
    public Object[] getSections() {

        return directionHeadingLabels;
    }
}
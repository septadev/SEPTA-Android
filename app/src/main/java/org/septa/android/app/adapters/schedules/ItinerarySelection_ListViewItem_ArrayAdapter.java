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
    String routeShortName;

    private View headerView = null;

    public ItinerarySelection_ListViewItem_ArrayAdapter(Context context, RouteTypes routeType, String routeShortName) {
        mContext = context;
        mInflater = LayoutInflater.from(context);

        this.routeType = routeType;
        this.routeShortName = routeShortName;

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
        View view = null;

        int colorPosition =0;
        switch (routeType) {
            case TROLLEY: {
                colorPosition = 0;
                break;
            }
            case MFL: {
                if (routeShortName.equals("MFO")) {
                    colorPosition = 3;
                } else {
                    colorPosition = 1;
                }
                break;
            }
            case BUS: {
                colorPosition = 3;
                break;
            }
            case BSL: {
                if (routeShortName.equals("BSO")) {
                    colorPosition = 3;
                } else {
                    colorPosition = 4;
                }
                break;
            }
            case NHSL: {
                colorPosition = 5;
                break;
            }
        }

        String backgroundColor = mContext.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[colorPosition];


        view = mInflater.inflate(R.layout.schedules_routeselection_headerview, parent, false);
        TextView textView = (TextView) view.findViewById(R.id.schedules_routeselection_sectionheader_textview);

        LinearLayout mainLayout = (LinearLayout)view.findViewById(R.id.schedules_routeselection_sectionheader_view);
        mainLayout.setBackgroundColor(Color.parseColor(backgroundColor));

        if (position < stopsForDirection0.size()) {
            textView.setText("To "+directionHeadingLabels[0]);
        } else {
            textView.setText("To "+directionHeadingLabels[1]);
        }

        return view;
    }

    @Override
    public long getHeaderId(int position) {
        if (position < stopsForDirection0.size()) {
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
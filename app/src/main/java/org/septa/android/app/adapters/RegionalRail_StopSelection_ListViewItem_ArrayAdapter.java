/*
 * NextToArrive_StopSelection_ListViewItem_ArrayAdapter.java
 * Last modified on 06-04-2014 16:41-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.adapters;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.models.StopModel;
import org.septa.android.app.utilities.Constants;
import org.septa.android.app.utilities.StopModelDistanceComparator;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionalRail_StopSelection_ListViewItem_ArrayAdapter extends ArrayAdapter<StopModel> implements SectionIndexer {
    public static final String TAG = RegionalRail_StopSelection_ListViewItem_ArrayAdapter.class.getName();

    private final Context context;
    private List<StopModel> values;
    private List<String> sections;
    private Map<Integer, Integer> positions;
    private Map<Integer, Integer> startPositions;
    private boolean useLocations = false;
    NumberFormat numberFormat = new DecimalFormat("#.##mi");

    public RegionalRail_StopSelection_ListViewItem_ArrayAdapter(Context context, List<StopModel> tripDataModelArrayList) {
        super(context, R.layout.nexttoarrive_listview_stop_item, tripDataModelArrayList);
        this.context = context;
        this.values = tripDataModelArrayList;
        sections = new ArrayList<String>();
        positions = new HashMap<Integer, Integer>();
        startPositions = new HashMap<Integer, Integer>();

        for(int i=0; i<tripDataModelArrayList.size(); i++) {
            StopModel stopModel = tripDataModelArrayList.get(i);
            String section = stopModel.getStopName();
            if(section != null && section.length() > 0) {
                section = section.substring(0, 1);
                if(!sections.contains(section)) {
                    sections.add(section);
                    startPositions.put(sections.indexOf(section), i);
                }
                positions.put(i, sections.indexOf(section));
            }
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.nexttoarrive_listview_stop_item, parent, false);

        ImageView icon_imageView = (ImageView) rowView.findViewById(R.id.nexttoarrive_stopselection_accessibilityicon_imageview);
        TextView text_TextView = (TextView) rowView.findViewById(R.id.nexttoarrive_stopselection_textview);

        if (values.get(position).hasWheelBoardingFeature()) {
            icon_imageView.setVisibility(View.VISIBLE);
        } else {
            icon_imageView.setVisibility(View.INVISIBLE);
        }

        if(useLocations) {
            TextView distance_textview = (TextView) rowView.findViewById(R.id.nexttoarrive_stopselection_distance_textview);
            distance_textview.setVisibility(View.VISIBLE);
            distance_textview.setText(numberFormat.format(values.get(position).getDistance()));
        }


        text_TextView.setText(values.get(position).getStopName());

        return rowView;
    }

    public void setStopData(ArrayList<StopModel>stopsList) {

        this.values = stopsList;
    }

    @Override
    public boolean areAllItemsEnabled() {

        return true;
    }

    @Override
    public boolean isEnabled(int position) {

        return true;
    }

    @Override
    public Object[] getSections() {
        return sections.toArray();
    }

    @Override
    public int getPositionForSection(int i) {
        return startPositions.get(i);
    }

    @Override
    public int getSectionForPosition(int i) {
        return positions.get(i);
    }

    /**
     * Sort list of stops by location
     * @param userLocation
     */
    public void sortByLocation(Location userLocation) {
        for(StopModel stopModel : values) {
            Location stopLocation = new Location("");
            stopLocation.setLatitude(stopModel.getLatitude());
            stopLocation.setLongitude(stopModel.getLongitude());

            stopModel.setDistance(Constants.METER_IN_MILES * userLocation.distanceTo(stopLocation));
        }

        Collections.sort(values, new StopModelDistanceComparator());

        useLocations = true;
        notifyDataSetChanged();
    }
}
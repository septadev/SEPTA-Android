/*
 * SchedulesTransportType_ListViewItem_ArrayAdapter.java
 * Last modified on 05-05-2014 16:51-0400 by brianhmayo
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
import org.septa.android.app.models.SchedulesFavoriteModel;
import org.septa.android.app.models.SchedulesRecentlyViewedModel;
import org.septa.android.app.models.RouteTypes;
import org.septa.android.app.models.SchedulesRouteModel;

import java.util.ArrayList;
import java.util.List;

import roboguice.util.Ln;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class SchedulesRouteSelectionListViewItemArrayAdapter extends BaseAdapter implements
        StickyListHeadersAdapter, SectionIndexer  {
    public static final String TAG = SchedulesRouteSelectionListViewItemArrayAdapter.class.getName();
    private final Context mContext;
    private LayoutInflater mInflater;

    private ArrayList<SchedulesFavoriteModel> favorites = new ArrayList<SchedulesFavoriteModel>();
    private ArrayList<SchedulesRecentlyViewedModel> recentlyViewed = new ArrayList<SchedulesRecentlyViewedModel>();
    protected ArrayList<SchedulesRouteModel> routes = new ArrayList<SchedulesRouteModel>();

    String[] resourceEndNames;
    String leftImageStartName;
    String rightImageBackgroundName;

    RouteTypes routeType = null;

    private int[] mSectionIndices = {0,1,2};
    private String[] sectionTitles = new String[]{ "Favorites", "Recently Viewed", "Routes"};

    public SchedulesRouteSelectionListViewItemArrayAdapter(Context context, RouteTypes routeType) {
        mContext = context;
        mInflater = LayoutInflater.from(context);

        this.routeType = routeType;

        resourceEndNames = context.getResources().getStringArray(R.array.schedulesfragment_listview_bothimage_endnames);
        leftImageStartName = context.getString(R.string.schedulesfragment_listview_leftimage_startname);
        rightImageBackgroundName = context.getString(R.string.schedulesfragment_listview_rightimage_startname);
    }

    public void setSchedulesRouteModel(ArrayList<SchedulesRouteModel> routes) {
        this.routes = routes;

        notifyDataSetChanged();
    }

    protected Object[] getItems() {
        ArrayList<Object> items = new ArrayList<Object>(getCount());
        items.addAll(favorites); items.addAll(recentlyViewed); items.addAll(routes);

        return items.toArray();
    }

    protected boolean isFavorite(int position) {

        return getItems()[position] instanceof SchedulesFavoriteModel;
    }

    protected boolean isRecentlyViewed(int position) {

        return getItems()[position] instanceof SchedulesRecentlyViewedModel;
    }

    @Override
    public int getCount() {
        return favorites.size()+recentlyViewed.size()+routes.size();
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

        if (isFavorite(position)) {
            SchedulesFavoriteModel fm = (SchedulesFavoriteModel)getItem(position);
            rowView = mInflater.inflate(R.layout.schedules_routeselection_favoriteandrecentlyviewed_listview_item, parent, false);
            TextView routeIdTextView = (TextView)rowView.findViewById(R.id.schedulesrouteselection_favoriterecentlyviewed_routeid_textview);
            TextView startRouteTextView = (TextView)rowView.findViewById(R.id.schedulesrouteselection_favoriterecentlyviewed_start_textview);
            TextView endRouteTextView = (TextView)rowView.findViewById(R.id.schedulesrouteselection_favoriterecentlyviewed_end_textview);

            routeIdTextView.setText(""+position);
            startRouteTextView.setText("Start: "+position+" startroute");
            endRouteTextView.setText("End: "+position+" end route");

            View transparentView = (View)rowView.findViewById(R.id.schedules_routeselection_favoriteandrecentlyviewed_transparentview);
            if (position == (favorites.size()-1)) {
                Ln.d("making the favorites as position "+position);
                transparentView.setVisibility(View.VISIBLE);
            } else {
                Ln.d("making the favorites gone.");
                transparentView.setVisibility(View.GONE);
            }
        } else {
            if (isRecentlyViewed(position)) {
                SchedulesRecentlyViewedModel rm = (SchedulesRecentlyViewedModel)getItem(position);
                rowView = mInflater.inflate(R.layout.schedules_routeselection_favoriteandrecentlyviewed_listview_item, parent, false);
                TextView routeIdTextView = (TextView)rowView.findViewById(R.id.schedulesrouteselection_favoriterecentlyviewed_routeid_textview);
                TextView startRouteTextView = (TextView)rowView.findViewById(R.id.schedulesrouteselection_favoriterecentlyviewed_start_textview);
                TextView endRouteTextView = (TextView)rowView.findViewById(R.id.schedulesrouteselection_favoriterecentlyviewed_end_textview);

                routeIdTextView.setText(""+position);
                startRouteTextView.setText("Start: "+position+" startroute");
                endRouteTextView.setText("End: "+position+" end route");

                View transparentView = (View)rowView.findViewById(R.id.schedules_routeselection_favoriteandrecentlyviewed_transparentview);
                if (position == (recentlyViewed.size()-1)) {
                    Ln.d("making the recently viewed as position "+position);
                    transparentView.setVisibility(View.VISIBLE);
                } else {
                    Ln.d("making the recently viewed gone.");
                    transparentView.setVisibility(View.GONE);
                }
            } else{
                String[] routeTypeLabels = mContext.getResources().getStringArray(R.array.schedulesfragment_listview_bothimage_endnames);

                SchedulesRouteModel rtm = (SchedulesRouteModel)getItem(position);
                rowView = mInflater.inflate(R.layout.schedules_routeselection_routes_listview_item, parent, false);
                ImageView leftIconImageView = (ImageView)rowView.findViewById(R.id.schedules_routeselect_item_leftImageView);
                ImageView rightBackgroundImageView = (ImageView)rowView.findViewById(R.id.schedules_routeselection_item_rightImageBackgroundview);
                TextView routeIdTextView = (TextView)rowView.findViewById(R.id.schedules_routeselection_item_routeid);
                TextView routeLongNameTextView = (TextView)rowView.findViewById(R.id.schedules_routeselection_item_routelongname);

                int id = mContext.getResources().getIdentifier(leftImageStartName + routeTypeLabels[routeType.ordinal()] + "_small", "drawable", mContext.getPackageName());
                leftIconImageView.setImageResource(id);

                id = mContext.getResources().getIdentifier(rightImageBackgroundName + routeTypeLabels[routeType.ordinal()], "drawable", mContext.getPackageName());
                rightBackgroundImageView.setImageResource(id);

                routeIdTextView.setText(routes.get(position).getRouteId());

                switch (routes.get(position).getRouteId().length()) {
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

                routeLongNameTextView.setText(routes.get(position).getRouteLongName());
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

        if (position < favorites.size()) { // in the favorites part of the list
            holder.text.setText(sectionTitles[0]);
            holder.text.setBackgroundColor(Color.parseColor("#990DA44A"));
        } else {
            if (favorites.size() == position) {
                holder.text.setText(sectionTitles[1]);
                holder.text.setBackgroundColor(Color.parseColor("#990DA44A"));
            } else {
                holder.text.setText(sectionTitles[2]);

                // get the color from the looking array given the ordinal position of the route type
                String color = mContext.getResources().getStringArray(R.array.schedules_routeselection_routesheader_colors)[routeType.ordinal()];
                holder.text.setBackgroundColor(Color.parseColor(color));
            }
        }

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        Ln.d("get header id for position "+position);
        if (isFavorite(position)) {
            Ln.d("isFavorite, yes");
            return 0;
        } else {
            if (isRecentlyViewed(position)) {
                Ln.d("isRecentlyViewed, yes");
                return 1;
            }
        }

        Ln.d("not fav nor recently viewed so return 2");
        return 2;
    }

    @Override
    public int getPositionForSection(int section) {
        Ln.d("asking for position for section "+section);
        switch (section) {
            case 0: {
                Ln.d("... returning 0");
                return 0;
            }
            case 1: {
                Ln.d("... returning fs:"+favorites.size());
                return favorites.size();
            }
            case 2: {
                Ln.d("... returning fs+rv:"+favorites.size()+recentlyViewed.size());
                return favorites.size()+recentlyViewed.size();
            }
            default: {
                Ln.d("... returning default: 0");
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

    class ViewHolder {
        TextView text;
    }
}
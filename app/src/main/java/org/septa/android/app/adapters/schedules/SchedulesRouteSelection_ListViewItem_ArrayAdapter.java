/*
 * SchedulesTransportType_ListViewItem_ArrayAdapter.java
 * Last modified on 05-05-2014 16:51-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.adapters.schedules;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.managers.AlertManager;
import org.septa.android.app.managers.SchedulesFavoritesAndRecentlyViewedStore;
import org.septa.android.app.models.ObjectFactory;
import org.septa.android.app.models.RouteTypes;
import org.septa.android.app.models.SchedulesFavoriteModel;
import org.septa.android.app.models.SchedulesRecentlyViewedModel;
import org.septa.android.app.models.SchedulesRouteModel;
import org.septa.android.app.models.servicemodels.AlertModel;
import org.septa.android.app.services.adaptors.AlertsAdaptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class SchedulesRouteSelection_ListViewItem_ArrayAdapter extends BaseAdapter implements
        StickyListHeadersAdapter, SectionIndexer  {
    public static final String TAG = SchedulesRouteSelection_ListViewItem_ArrayAdapter.class.getName();
    private final Context mContext;
    private LayoutInflater mInflater;

    private ArrayList<SchedulesFavoriteModel> favorites = new ArrayList<SchedulesFavoriteModel>();
    private ArrayList<SchedulesRecentlyViewedModel> recentlyViewed = new ArrayList<SchedulesRecentlyViewedModel>();
    protected ArrayList<SchedulesRouteModel> routes = new ArrayList<SchedulesRouteModel>();

    private SchedulesFavoritesAndRecentlyViewedStore schedulesFavoritesAndRecentlyViewedStore;

    String[] resourceEndNames;
    String leftImageStartName;
    String rightImageBackgroundName;

    RouteTypes routeType = null;

    private String[] sectionTitles = new String[]{"Favorites", "Recently Viewed", "Routes"};

    private List<String> sections;


    public SchedulesRouteSelection_ListViewItem_ArrayAdapter(Context context, RouteTypes routeType) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        sections = new ArrayList<String>();

        this.routeType = routeType;

        schedulesFavoritesAndRecentlyViewedStore = ObjectFactory.getInstance().getSchedulesFavoritesAndRecentlyViewedStore(context);
        reloadFavoriteAndRecentlyViewedLists();

        resourceEndNames = context.getResources().getStringArray(R.array.schedulesfragment_listview_bothimage_endnames);
        leftImageStartName = context.getString(R.string.schedulesfragment_listview_leftimage_startname);
        rightImageBackgroundName = context.getString(R.string.schedulesfragment_listview_rightimage_startname);
    }

    public void reloadFavoriteAndRecentlyViewedLists() {
        recentlyViewed = schedulesFavoritesAndRecentlyViewedStore.getRecentlyViewedList(routeType.name());
        favorites = schedulesFavoritesAndRecentlyViewedStore.getFavoriteList(routeType.name());
    }

    public void setSchedulesRouteModel(ArrayList<SchedulesRouteModel> routes) {
        this.routes = routes;

        Log.d(TAG, "about to sort the routes");
        if ((routeType == RouteTypes.TROLLEY) || (routeType == RouteTypes.BUS)) {
            // sort numerically taking care for bus routeIds that have a trailing letter
            Collections.sort(this.routes, new NumericComparator());
        } else {

            Collections.sort(this.routes);
        }
        Log.d(TAG, "sorted the routes");

        sections = new ArrayList<String>();

        List<SchedulesRouteModel> routeModels = new ArrayList<SchedulesRouteModel>();
        routeModels.addAll(this.favorites);
        routeModels.addAll(this.recentlyViewed);
        routeModels.addAll(this.routes);

        // Compute section indexes for fast scroll
        for (SchedulesRouteModel schedulesRouteModel : routeModels) {
            String section = schedulesRouteModel.getRouteId();
            if (section != null && section.length() > 0) {
                section = section.substring(0, 1);
                sections.add(section);
            }
        }

        notifyDataSetChanged();
    }

    protected Object[] getItems() {
        ArrayList<Object> items = new ArrayList<Object>(getCount());

        items.addAll(favorites); items.addAll(recentlyViewed); items.addAll(routes);

        return items.toArray();
    }

    public boolean isFavorite(int position) {

        return getItem(position) instanceof SchedulesFavoriteModel;
    }

    public boolean isRecentlyViewed(int position) {

        return getItem(position) instanceof SchedulesRecentlyViewedModel;
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

    private int adjustedPosition(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = null;

        // favorite position rows
        if (isFavorite(adjustedPosition(position))) {
            rowView = mInflater.inflate(R.layout.schedules_routeselection_favoriteandrecentlyviewed_listview_item, parent, false);

            TextView routeIdTextView = (TextView)rowView.findViewById(R.id.schedules_routeselection_favoriterecentlyviewed_routeid_textview);
            TextView startStopNameTextView = (TextView)rowView.findViewById(R.id.schedules_routeselection_favoriterecentlyviewed_start_textview);
            TextView endStopNameTextView = (TextView)rowView.findViewById(R.id.schedules_routeselection_favoriterecentlyviewed_end_textview);

            SchedulesFavoriteModel favoriteModel = (SchedulesFavoriteModel)getItems()[adjustedPosition(position)];

            routeIdTextView.setText(favoriteModel.getRouteId());
            startStopNameTextView.setText(favoriteModel.getRouteStartName());
            endStopNameTextView.setText(favoriteModel.getRouteEndName());

            // to create a larger space in the ListView, each row has a transparent view built in
            // if we are not the last row in the logical section, make it gone, else visible
            View transparentView = (View)rowView.findViewById(R.id.schedules_routeselection_favoriteandrecentlyviewed_transparentview);
            if (adjustedPosition(position) == favorites.size()) {
                transparentView.setVisibility(View.VISIBLE);
            }
            else {
                transparentView.setVisibility(View.GONE);
            }
        }
        // recently viewed position rows
        else {
            if (isRecentlyViewed(adjustedPosition(position))) {
                rowView = mInflater.inflate(R.layout.schedules_routeselection_favoriteandrecentlyviewed_listview_item, parent, false);

                TextView routeIdTextView = (TextView)rowView.findViewById(R.id.schedules_routeselection_favoriterecentlyviewed_routeid_textview);
                TextView startStopNameTextView = (TextView)rowView.findViewById(R.id.schedules_routeselection_favoriterecentlyviewed_start_textview);
                TextView endStopNameTextView = (TextView)rowView.findViewById(R.id.schedules_routeselection_favoriterecentlyviewed_end_textview);

                SchedulesRecentlyViewedModel recentlyViewedModel = (SchedulesRecentlyViewedModel)getItems()[adjustedPosition(position)];

                routeIdTextView.setText(recentlyViewedModel.getRouteId());
                startStopNameTextView.setText(recentlyViewedModel.getRouteStartName());
                endStopNameTextView.setText(recentlyViewedModel.getRouteEndName());

                // to create a larger space in the ListView, each row has a transparent view built in
                // if we are not the last row in the logical section, make it gone, else visible
                View transparentView = (View)rowView.findViewById(R.id.schedules_routeselection_favoriteandrecentlyviewed_transparentview);
                if ((adjustedPosition(position)+1) == (favorites.size()+recentlyViewed.size())) {
                    transparentView.setVisibility(View.VISIBLE);
                } else {
                    transparentView.setVisibility(View.GONE);
                }
            }
            else{
                String[] routeTypeLabels = mContext.getResources().getStringArray(R.array.schedulesfragment_listview_bothimage_endnames);

                SchedulesRouteModel rtm = (SchedulesRouteModel)getItem(position);

                rowView = mInflater.inflate(R.layout.schedules_routeselection_routes_listview_item, parent, false);
                ImageView leftIconImageView = (ImageView)rowView.findViewById(R.id.schedules_routeselect_item_leftImageView);

                ImageView serviceAdvisoryImageView = (ImageView) rowView.findViewById(R.id.schedules_routeselection_item_serviceadvisory);
                ImageView detourImageView = (ImageView) rowView.findViewById(R.id.schedules_routeselection_item_detour);
                ImageView serviceAlertImageView = (ImageView) rowView.findViewById(R.id.schedules_routeselection_item_servicealert);
                ImageView lineSuspensionImageView = (ImageView) rowView.findViewById(R.id.schedules_routeselection_item_linesuspension);

                RelativeLayout rowRelativeLayout = (RelativeLayout)rowView.findViewById(R.id.schedules_routeselection_rl_background);
                TextView routeIdTextView = (TextView)rowView.findViewById(R.id.schedules_routeselection_item_routeid);
                TextView routeLongNameTextView = (TextView)rowView.findViewById(R.id.schedules_routeselection_item_routelongname);

                // special icon and color rules for MFO and BSO
                if (rtm.getRouteId().equals("MFO") || rtm.getRouteId().equals("BSO")) {
                    int id = mContext.getResources().getIdentifier(leftImageStartName + routeTypeLabels[RouteTypes.BUS.ordinal()] + "_small", "drawable", mContext.getPackageName());
                    leftIconImageView.setImageResource(id);

                    id = mContext.getResources().getIdentifier(rightImageBackgroundName + routeTypeLabels[RouteTypes.BUS.ordinal()], "drawable", mContext.getPackageName());
                    rowRelativeLayout.setBackgroundResource(id);
                } else {
                    int id = mContext.getResources().getIdentifier(leftImageStartName + routeTypeLabels[routeType.ordinal()] + "_small", "drawable", mContext.getPackageName());
                    leftIconImageView.setImageResource(id);

                    id = mContext.getResources().getIdentifier(rightImageBackgroundName + routeTypeLabels[routeType.ordinal()], "drawable", mContext.getPackageName());
                    rowRelativeLayout.setBackgroundResource(id);
                }
                SchedulesRouteModel route = (SchedulesRouteModel)getItems()[adjustedPosition(position)];

                routeIdTextView.setText(route.getRouteId());

                // Check for alerts to display
                AlertManager alertManager = AlertManager.getInstance();
                String routeId = AlertsAdaptor.getServiceRouteName(route, routeType);
                if (alertManager != null && !TextUtils.isEmpty(routeId)) {
                    AlertModel alertModel = alertManager.getAlertForRouteShortName(routeId);
                    if (alertModel != null) {
                        // If route is suspended, display line suspension icon and no others
                        if (alertModel.hasSuspendedFlag()) {
                            lineSuspensionImageView.setVisibility(View.VISIBLE);
                        }
                        // Otherwise, check for other alert icons to display
                        else {
                            // Remove line suspension icon from view
                            lineSuspensionImageView.setVisibility(View.GONE);

                            // If route has service advisory, display service advisory icon
                            if (alertModel.hasAdvisoryFlag()) {
                                serviceAdvisoryImageView.setVisibility(View.VISIBLE);
                            }
                            else {
                                serviceAdvisoryImageView.setVisibility(View.GONE);
                            }
                            // If route has detour, display detour icon
                            if (alertModel.hasDetourFlag()) {
                                detourImageView.setVisibility(View.VISIBLE);
                            }
                            else {
                                detourImageView.setVisibility(View.GONE);
                            }
                            // If route has service alert, display service alert icon
                            if (alertModel.hasAlertFlag()) {
                                serviceAlertImageView.setVisibility(View.VISIBLE);
                            }
                            else {
                                serviceAlertImageView.setVisibility(View.GONE);
                            }
                        }
                    }
                }

                switch (route.getRouteId().length()) {
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

                routeLongNameTextView.setText(route.getRouteLongName());
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

                // get the color from the looking array given the ordinal position of the route type
                String color = mContext.getResources().getStringArray(R.array.schedules_routeselection_routesheader_colors)[routeType.ordinal()];
                holder.text.setBackgroundColor(Color.parseColor(color));
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
    public Object[] getSections() {
        return sections.toArray();
    }

    @Override
    public int getPositionForSection(int i) {
        return i;
    }

    @Override
    public int getSectionForPosition(int i) {
        return i;
    }

    class HeaderViewHolder {
        TextView text;
    }
}

// this numeric comparator makes the assumption that the strings are either mostly numerics
// with possibly a training letter or have no numerics at all.
class NumericComparator implements Comparator<SchedulesRouteModel> {
    @Override
    public int compare(SchedulesRouteModel o1, SchedulesRouteModel o2) {
        int o1RouteIdAsInt = 0;
        int o2RouteIdAsInt = 0;

        String o1NumericString = o1.getRouteId().replaceAll("[^\\d.]", "");
        String o2NumericString = o2.getRouteId().replaceAll("[^\\d.]", "");

        // first check if either or both the strings are length 0, meaning they have no numerics
        if (o1NumericString.length() == 0) {     // this is all non numerics
            if (o2NumericString.length() == 0) {
                return o1.getRouteId().compareTo(o2.getRouteId());
            } else {
                return 1;
            }
        } else {
            if (o2NumericString.length() == 0) {
                return -1;
            }
        }

        o1RouteIdAsInt = Integer.parseInt(o1NumericString);
        o2RouteIdAsInt = Integer.parseInt(o2NumericString);

        if (o1RouteIdAsInt == o2RouteIdAsInt) return 0;
        if (o1RouteIdAsInt < o2RouteIdAsInt) return -1;

        return 1;
    }
}
package org.septa.android.app.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.models.LocationBasedRouteModel;
import org.septa.android.app.models.LocationModel;
import org.septa.android.app.models.RouteTypes;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class FindNearestLocation_RouteDetails_ListViewItem_ArrayAdapter extends BaseAdapter implements
        StickyListHeadersAdapter, SectionIndexer {
    public static final String TAG = FindNearestLocation_RouteDetails_ListViewItem_ArrayAdapter.class.getName();
    private final Context context;
    private LayoutInflater mInflater;

    private String[] sectionTitles = new String[]{};

    private LocationModel locationModel = new LocationModel();

    public FindNearestLocation_RouteDetails_ListViewItem_ArrayAdapter(Context context, RouteTypes routeType) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
    }

//
//    protected Object[] getItems() {
//        ArrayList<Object> items = new ArrayList<Object>(getCount());
//        items.addAll(trips);
//
//        return items.toArray();
//    }

    @Override
    public int getCount() {
        int count = 0;

        for (LocationBasedRouteModel locationBasedRouteModel : locationModel.getRoutes()) {
            count += locationBasedRouteModel.getTimeDayPairCount();
        }

        return count;
    }

    @Override
    public Object getItem(int position) {
        int relativePosition = position;

        for (LocationBasedRouteModel locationBasedRouteModel: locationModel.getRoutes()) {
            if (relativePosition < locationBasedRouteModel.getTimeDayPairCount()) {
                // we are in the right locationModel
                locationBasedRouteModel.setTimeDayPairIndex(position);
                return locationBasedRouteModel;
            } else {
                relativePosition -= locationBasedRouteModel.getTimeDayPairCount();
            }
        }

        Log.d(TAG, "returning null, not right");
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView;
        LocationBasedRouteModel locationBasedRouteModel = (LocationBasedRouteModel) getItem(position);

        rowView = mInflater.inflate(R.layout.findnearestlocation_routedetails_listview_item, parent, false);

        ImageView routeTypeImageView = (ImageView)parent.findViewById(R.id.findnearestlocation_routedetails_listviewitem_routetypeimage);
        TextView routeShortNameTextView = (TextView)parent.findViewById(R.id.findnearestlocation_routedetails_listviewitem_routeshortname);

        ImageView alertImageView = (ImageView)parent.findViewById(R.id.findnearestlocation_routedetails_listviewitem_alertalert);
        ImageView detourImageView = (ImageView)parent.findViewById(R.id.findnearestlocation_routedetails_listviewitem_alertdetour);
        ImageView advisoryImageView = (ImageView)parent.findViewById(R.id.findnearestlocation_routedetails_listviewitem_alertadvisory);

        TextView timeTextView = (TextView)parent.findViewById(R.id.findnearestlocation_routedetails_listviewitem_time);
        TextView dayTextView = (TextView)parent.findViewById(R.id.findnearestlocation_routedetails_listviewitem_day);

        int routeType = locationBasedRouteModel.getRouteType();
        switch (routeType) {
            case 0:
                // trolley
                routeTypeImageView.setImageResource(R.drawable.transitview_listitem_trolley);
                break;
            case 1:
                //subway
                routeTypeImageView.setImageResource(R.drawable.transitview_listitem_bus);
                break;
            case 2:
                //rail
                routeTypeImageView.setImageResource(R.drawable.transitview_listitem_bus);
                break;
            case 3:
                //bus
                routeTypeImageView.setImageResource(R.drawable.transitview_listitem_bus);
                break;
            default:
                Log.d(TAG, "got here");
                break;
        }

        routeShortNameTextView.setText(locationBasedRouteModel.getRouteShortName());
        if (locationModel.hasAlert()) {
            alertImageView.setVisibility(View.VISIBLE);
        } else {
            alertImageView.setVisibility(View.INVISIBLE);
        }

        if (locationModel.hasDetour()) {
            detourImageView.setVisibility(View.VISIBLE);
        } else {
            detourImageView.setVisibility(View.INVISIBLE);
        }

        if (locationModel.hasAdvisory()) {
            advisoryImageView.setVisibility(View.VISIBLE);
        } else {
            advisoryImageView.setVisibility(View.INVISIBLE);
        }

        timeTextView.setText(locationBasedRouteModel.getTimeDayPairFromIndex().getTime());
        dayTextView.setText(locationBasedRouteModel.getTimeDayPairFromIndex().getDay());

        return rowView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        View view = null;

        view =  mInflater.inflate(R.layout.findnearestlocation_routedetails_listview_headerview, parent, false);

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
}
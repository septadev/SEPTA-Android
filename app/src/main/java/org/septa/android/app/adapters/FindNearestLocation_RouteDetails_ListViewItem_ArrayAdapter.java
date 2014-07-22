package org.septa.android.app.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.databases.SEPTADatabase;
import org.septa.android.app.managers.AlertManager;
import org.septa.android.app.models.LocationBasedRouteModel;
import org.septa.android.app.models.servicemodels.AlertModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class FindNearestLocation_RouteDetails_ListViewItem_ArrayAdapter extends BaseAdapter implements
        StickyListHeadersAdapter {
    public static final String TAG = FindNearestLocation_RouteDetails_ListViewItem_ArrayAdapter.class.getName();
    private final Context context;
    private LayoutInflater mInflater;

    private HashMap<String, String> headerCache;
    private SparseArray<AlertModel> alertCache;

    private List<LocationBasedRouteModel> routeList;

    public FindNearestLocation_RouteDetails_ListViewItem_ArrayAdapter(Context context){
        this.context = context;
        this.routeList = new ArrayList<LocationBasedRouteModel>();
        this.headerCache = new HashMap<String, String>();
        this.alertCache = new SparseArray<AlertModel>();
        mInflater = LayoutInflater.from(context);
    }

    public void addRoute(LocationBasedRouteModel route){
        routeList.add(route);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        int count = 0;

        for (LocationBasedRouteModel locationBasedRouteModel : routeList) {
            count += locationBasedRouteModel.getTimeDayPairCount();
        }
        return count;
    }

    @Override
    public Object getItem(int position) {
        int relativePosition = position;

        int count = 0;
        for (LocationBasedRouteModel locationBasedRouteModel: routeList) {

            if (relativePosition >= count && relativePosition < (count += locationBasedRouteModel.getTimeDayPairCount())) {
                return locationBasedRouteModel;
            }
        }

        return null;
    }

    public AlertModel getAlert(int position){
        AlertModel model = alertCache.get(position);
        if(model != null){
            return model;
        }

        return null;
    }

    private int getTimeIndexForPosition(int position){
        int count = 0;
        for (LocationBasedRouteModel locationBasedRouteModel: routeList) {
            int relativePosition = 0;
            for(LocationBasedRouteModel.TimeDayPairModel time : locationBasedRouteModel.getTimeDayPairs()){
                if (count == position) {
                    return relativePosition;
                }
                relativePosition++;
                count++;
            }
        }

        return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        AlertModel model = alertCache.get(position);
        if(model == null){
            return false;
        }

        return model.hasAdvisoryFlag() || model.hasAlertFlag() || model.hasDetourFlag();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = mInflater.inflate(R.layout.findnearestlocation_routedetails_listview_item, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        LocationBasedRouteModel locationBasedRouteModel = (LocationBasedRouteModel) getItem(position);

        holder.routeTypeImage.setImageResource(getDrawableForModel(locationBasedRouteModel));
        holder.routeShortName.setText(locationBasedRouteModel.getRouteShortName());

        //Retrieve and cache
        AlertModel model = alertCache.get(position);
        if(model == null){
            model = AlertManager.getInstance().getAlertForRoute(locationBasedRouteModel);
        }

        int alertVisible = View.INVISIBLE;
        int detourVisible = View.INVISIBLE;
        int advisoryVisible = View.INVISIBLE;
        if(model != null){
            alertCache.put(position, model);
            alertVisible = model.hasAlertFlag()? View.VISIBLE : View.INVISIBLE;
            detourVisible = model.hasDetourFlag()? View.VISIBLE : View.INVISIBLE;
            advisoryVisible = model.hasAdvisoryFlag() ? View.VISIBLE : View.INVISIBLE;
        }

        holder.alertImage.setVisibility(alertVisible);
        holder.detourImage.setVisibility(detourVisible);
        holder.advisoryImage.setVisibility(advisoryVisible);

        int index = getTimeIndexForPosition(position);
        LocationBasedRouteModel.TimeDayPairModel time = locationBasedRouteModel.getTimeDayPairs().get(index);
        holder.time.setText(time.getTime());
        holder.day.setText(time.getDay());

        return view;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        HeaderViewHolder holder;
        if (convertView == null) {

            view =  mInflater.inflate(R.layout.findnearestlocation_routedetails_listview_headerview, parent, false);
            holder = new HeaderViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        LocationBasedRouteModel model = (LocationBasedRouteModel)getItem(position);
        if(model != null){
            holder.routeDescription.setText(getHeaderValue(model.getRouteShortName(), model.getDirectionCode()));
            holder.routeId.setText(model.getRouteShortNameWithDirection());
        }

        return view;
    }

    //@TODO This should take a callback as an argument and return the values async once sql access is moved off ui thread
    public String getHeaderValue(String routeShortName, int directionCode){
        String key =  routeShortName + directionCode;
        String header = headerCache.get(key);
        if(header == null){
            header = getDirectionHeader(routeShortName, directionCode);
            headerCache.put(key, header);
        }

        return header;

    }

    @Override
    public long getHeaderId(int position) {
        return ((LocationBasedRouteModel)getItem(position)).getRouteShortName().hashCode();
    }

    //@TODO move this into a manager and off the main thread.
    private String getDirectionHeader(String routeShortName, int dirCode) {
        String queryString;
        String header = null;
        SEPTADatabase septaDatabase = new SEPTADatabase(context);
        SQLiteDatabase database = septaDatabase.getReadableDatabase();

        Log.d("f", "setting querystring with route short name as " + routeShortName);
        queryString = "SELECT DirectionDescription FROM bus_stop_directions WHERE Route=\"" + routeShortName + "\" AND dircode=" + dirCode;
        Log.d("f", queryString);
        Cursor cursor = null;

        if (queryString != null) {
            cursor = database.rawQuery(queryString, null);
        }

        if (cursor != null) {
            if (cursor.moveToFirst()) {
               header = cursor.getString(0);
            }

            cursor.close();
        } else {
            Log.d("f", "cursor is null");
        }

        database.close();

        return header;
    }

    private int getDrawableForModel(LocationBasedRouteModel route){
        if(route.getRouteSpecialType() == LocationBasedRouteModel.RouteSpecialType.NONE){
            switch (route.getTransportationType()){
                case TROLLEY:
                    return R.drawable.ic_systemstatus_trolley_green;
                case SUBWAY:
                    return R.drawable.ic_systemstatus_rrl_blue;
                case RAIL:
                    return R.drawable.ic_systemstatus_rrl_blue;
                case BUS:
                    return R.drawable.ic_systemstatus_bus_black;
                default:
                    return R.drawable.ic_systemstatus_bus_black;
            }

        } else{

            switch (route.getRouteSpecialType()){
                case NHSL:
                    return R.drawable.ic_systemstatus_nhsl_purple;
                case BSS:
                    return R.drawable.ic_systemstatus_bsl_orange;
                case BSO:
                    return R.drawable.ic_systemstatus_bsl_owl;
                case MFL:
                    return R.drawable.ic_systemstatus_mfl_blue;
                case MFO:
                    return R.drawable.ic_systemstatus_mfl_owl;
                default:
                    return  R.drawable.ic_systemstatus_bus_black;
            }
        }
    }

    public void alertsDidChange(){
        alertCache = new SparseArray<AlertModel>();
        notifyDataSetChanged();
    }

    class HeaderViewHolder {
        @InjectView(R.id.routeId) TextView routeId;
        @InjectView(R.id.routeDescription) TextView routeDescription;

        HeaderViewHolder(View view){
            ButterKnife.inject(this, view);
        }
    }

    static class ViewHolder {
        @InjectView(R.id.findnearestlocation_routedetails_listviewitem_routetypeimage)
        ImageView routeTypeImage;
        @InjectView(R.id.findnearestlocation_routedetails_listviewitem_routeshortname)
        TextView routeShortName;
        @InjectView(R.id.findnearestlocation_routedetails_listviewitem_alertalert)
        ImageView alertImage;
        @InjectView(R.id.findnearestlocation_routedetails_listviewitem_alertdetour)
        ImageView detourImage;
        @InjectView(R.id.findnearestlocation_routedetails_listviewitem_alertadvisory)
        ImageView advisoryImage;
        @InjectView(R.id.findnearestlocation_routedetails_listviewitem_time)
        TextView time;
        @InjectView(R.id.findnearestlocation_routedetails_listviewitem_day)
        TextView day;

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

}
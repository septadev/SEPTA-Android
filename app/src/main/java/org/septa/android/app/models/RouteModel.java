/*
 * ServiceHoursModel.java
 * Last modified on 04-21-2014 16:10-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models;

import android.content.Context;
import android.util.Log;

import org.septa.android.app.databases.SEPTADatabase;
import org.septa.android.app.managers.DatabaseManager;
import org.septa.android.app.utilities.CalendarDateUtilities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class RouteModel implements Comparable<RouteModel> {
    private static final String TAG = RouteModel.class.getName();

    private String routeId;
    private String routeShortName;
    private String routeLongName;
    private Number routeType;

    private Number directionId;
    private Number startStopId;
    private Number endStopId;

    private String startStopName;
    private String endStopName;

    private boolean inService;

    private HashMap<String, MinMaxHoursModel> hours;

    public String print() {
        String objectValue = "route_id:"+routeId+", route_short_name:"+routeShortName+", route_long_name:"+routeLongName+", route_type:"+routeType;
        for (MinMaxHoursModel minMaxHours : hours.values()) {
            objectValue +=", minMaxHours: min:"+minMaxHours.getMinimum()+", max:"+minMaxHours.getMaximum();
        }

        return objectValue;
    }

    public RouteModel() {

        this.hours = new HashMap<String, MinMaxHoursModel>();
    }

    public List<MinMaxHoursModel> getMinMaxHoursForServiceIds(List<Integer> serviceIds) {
        List<MinMaxHoursModel> models = new ArrayList<MinMaxHoursModel>();
        for (Integer serviceId : serviceIds) {
            MinMaxHoursModel model = hours.get(serviceId.toString());
            if (model != null) {
                models.add(model);
            }
        }
        return models;
    }

    public void addMinMaxHoursToRoute(String serviceId, MinMaxHoursModel minMaxHours) {
        this.hours.put(serviceId, minMaxHours);
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public void setRouteShortName(String routeShortName) {
        this.routeShortName = routeShortName;
    }

    public String getRouteLongName() {
        return routeLongName;
    }

    public void setRouteLongName(String routeLongName) {
        this.routeLongName = routeLongName;
    }

    public Number getRouteType() {
        return routeType;
    }

    public void setRouteType(Number routeType) {
        this.routeType = routeType;
    }

    @Override
    public int compareTo(RouteModel other){
        int result = 0;
        Integer thisRouteShortName = null;
        Integer otherRouteShortName = null;
        boolean thisIsString = false;
        boolean otherIsString = false;

        // we assume a route short name is either a number (only numerics), a number with a trailing character, or
        //  not a number (all characters.
        // first check if it is a number, then remove the last character and check for a number
        // if those two fail, it must not be or have a number
        try {
            thisRouteShortName = Integer.valueOf(this.routeShortName.replaceAll("[^\\d]", ""));
        } catch (NumberFormatException nfe) {
            thisIsString = true;
        }

        try {
            otherRouteShortName = Integer.valueOf(other.routeShortName.replaceAll("[^\\d]", ""));
        } catch (NumberFormatException nfe) {
            otherIsString = true;
        }

        // this is a string and other is not, thus other comes first
        if (thisIsString && !otherIsString) {

            return 1;
        }

        // this is not a string and other is, thus this comes first
        if (!thisIsString && otherIsString) {

            return -1;
        }

        // both are strings, just compare outright;
        if (thisIsString && otherIsString) {

            return this.routeShortName.compareTo(other.routeShortName);
        }

        // if we got here, we converted both to Integers and can compare outright.
        return thisRouteShortName.compareTo(otherRouteShortName);
    }

    public Number getDirectionId() {
        return directionId;
    }

    public void setDirectionId(Number directionId) {
        this.directionId = directionId;
    }

    public Number getStartStopId() {
        return startStopId;
    }

    public void setStartStopId(Number startStopId) {
        this.startStopId = startStopId;
    }

    public Number getEndStopId() {
        return endStopId;
    }

    public void setEndStopId(Number endStopId) {
        this.endStopId = endStopId;
    }

    public String getStartStopName() {
        return startStopName;
    }

    public void setStartStopName(String startStopName) {
        this.startStopName = startStopName;
    }

    public String getEndStopName() {
        return endStopName;
    }

    public void setEndStopName(String endStopName) {
        this.endStopName = endStopName;
    }

    public boolean isInService(Context context) {
        Calendar calendar = Calendar.getInstance();
        List<Integer> serviceIds = DatabaseManager.serviceIdsForDayOfWeek(new SEPTADatabase(context).getReadableDatabase(),
                calendar.get(Calendar.DAY_OF_WEEK), RouteTypes.values()[routeType.intValue()]);
        List<MinMaxHoursModel> models = getMinMaxHoursForServiceIds(serviceIds);

        int nowTime = CalendarDateUtilities.getNowTimeFormatted();

        if (models.isEmpty()) {
            Log.d(TAG, "the minMaxHoursModelForToday is empty, we must not have this for this service Id");
            return false;
        }

        // Check if in range of today's schedule
        if (!models.isEmpty()) {
            for (MinMaxHoursModel model : models) {
                if (model.inMinMaxRange(nowTime)) {
                    return true;
                }
            }
        }

        // we must check if we are actually in the range of yesterday's service
        calendar.add(Calendar.DATE, -1);
        serviceIds = DatabaseManager.serviceIdsForDayOfWeek(new SEPTADatabase(context).getReadableDatabase(),
                calendar.get(Calendar.DAY_OF_WEEK), RouteTypes.values()[routeType.intValue()]);
        models = getMinMaxHoursForServiceIds(serviceIds);

        if (models.isEmpty()) {
            Log.d(TAG, "the minMaxHoursModelForYesterday is null, we must not have this for this service Id");
            return false;
        }

        // since we are winding the day back we need to add 24 hours to our nowTime
        nowTime+=2400;

        // Check if in range of today's schedule
        if (!models.isEmpty()) {
            for (MinMaxHoursModel model : models) {
                if (model.inMinMaxRange(nowTime)) {
                    return true;
                }
            }
        }

        // Even after checking yesterday's schedule still not in service
        return false;
    }

    public void setInService(boolean inService) {
        this.inService = inService;
    }
}
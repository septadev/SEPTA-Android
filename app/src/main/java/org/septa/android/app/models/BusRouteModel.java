/*
 * ServiceHoursModel.java
 * Last modified on 04-21-2014 16:10-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models;

import android.util.Log;

import java.util.HashMap;

public class BusRouteModel implements Comparable<BusRouteModel> {
    private static final String TAG = BusRouteModel.class.getName();

    private String routeId;
    private String routeShortName;
    private Number routeType;

    private HashMap<String, MinMaxHoursModel> hours;

    public String print() {
        String objectValue = "route_id:"+routeId+", route_short_name:"+routeShortName+", route_type:"+routeType;
        for (MinMaxHoursModel minMaxHours : hours.values()) {
            objectValue +=", minMaxHours: min:"+minMaxHours.getMinimum()+", max:"+minMaxHours.getMaximum();
        }

        return objectValue;
    }

    public BusRouteModel() {

        this.hours = new HashMap<String, MinMaxHoursModel>();
    }

    public MinMaxHoursModel getMinMaxHoursForServiceId(String serviceId) {

        return hours.get(serviceId);
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

    public Number getRouteType() {
        return routeType;
    }

    public void setRouteType(Number routeType) {
        this.routeType = routeType;
    }

    @Override
    public int compareTo(BusRouteModel other){
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
            thisRouteShortName = Integer.valueOf(this.routeShortName);
        } catch (NumberFormatException nfe) {
            thisIsString = true;
        }

        try {
            otherRouteShortName = Integer.valueOf(other.routeShortName);
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
}
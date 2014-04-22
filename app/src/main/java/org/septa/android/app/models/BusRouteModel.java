/*
 * ServiceHoursModel.java
 * Last modified on 04-21-2014 16:10-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models;

import java.util.HashMap;

public class BusRouteModel implements Comparable<BusRouteModel> {
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

        return this.routeShortName.compareTo(other.routeShortName);
    }
}
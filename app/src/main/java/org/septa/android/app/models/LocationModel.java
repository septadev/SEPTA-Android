/*
 * Location.java
 * Last modified on 03-27-2014 13:22-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocationModel {
    public static final String TAG = LocationModel.class.getName();

    private float distance;
    private LocationDataModel location_data;
    private int location_id;
    private float location_lat;
    private float location_lon;
    private String location_name;
    private String location_type;
    private HashMap<String, LocationBasedRouteModel> routes;
    private HashMap<String, Number> directionBinary;

    public LocationModel() {

        this.routes = new HashMap<String, LocationBasedRouteModel>();
        directionBinary = new HashMap<String, Number>();
    }

    public float getDistance() {
        return distance;
    }

    public LocationDataModel getLocationData() {
        return location_data;
    }

    public int getLocationId() {
        return location_id;
    }

    public float getLocationLatitude() {
        return location_lat;
    }

    public float getLocationLongitude() {
        return location_lon;
    }

    public String getLocationName() {
        return location_name;
    }

    public String getLocationType() {
        return location_type;
    }

    public List<LocationBasedRouteModel> getRoutes() {
        return new ArrayList(routes.values());
    }

    public void addRoute(String routeShortName, LocationBasedRouteModel.DirectionCode direction, int routeType) {
        LocationBasedRouteModel routeModel = routes.containsKey(routeShortName)?routes.get(routeShortName):new LocationBasedRouteModel();

        routeModel.setDirectionBinaryPower(routeModel.getDirectionBinaryPower()+(int)Math.pow(2, direction.ordinal()));
        routeModel.setRouteShortName(routeShortName);
        routeModel.setRouteType(routeType);

        this.routes.put(routeShortName, routeModel);
    }

    public String print() {
        String output = "";
        output += "distance:"+distance;
        output += " locationId:"+location_id;
        output += " locationLatitude:"+location_lat;
        output += " locationLongitude:"+location_lon;
        output += " locationName:"+location_name;
        output += " locationType:"+location_type;

        if (routes.size()>0) {
            output += " routes:";
            for (LocationBasedRouteModel route : routes.values()) {
                output += route.getRouteShortName() + ",";
            }
        }

        return output;
    }
}
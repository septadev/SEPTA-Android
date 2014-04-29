/*
 * Location.java
 * Last modified on 03-27-2014 13:22-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class LocationModel {

    public float distance;
    public LocationDataModel location_data;
    public int location_id;
    public float location_lat;
    public float location_lon;
    public String location_name;
    public String location_type;
    public List<String> routes;

    public LocationModel() {

        this.routes = new ArrayList<String>();
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

    public List<String> getRoutes() {
        return routes;
    }

    public void addRoute(String routeShortName) {

        this.routes.add(routeShortName);
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
            for (String route : routes) {
                output += route + ",";
            }
        }

        return output;
    }
}
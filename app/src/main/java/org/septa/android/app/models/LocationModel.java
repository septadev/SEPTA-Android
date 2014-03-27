/*
 * Location.java
 * Last modified on 03-27-2014 13:22-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models;

public class LocationModel {

    public float distance;
    public LocationDataModel location_data;
    public int location_id;
    public float location_lat;
    public float location_lon;
    public String location_name;
    public String location_type;

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
}
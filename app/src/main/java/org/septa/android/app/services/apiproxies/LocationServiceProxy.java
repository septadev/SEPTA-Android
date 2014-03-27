/*
 * LocationServiceProxy.java
 * Last modified on 03-27-2014 16:00-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.services.apiproxies;

import org.septa.android.app.models.LocationModel;
import org.septa.android.app.services.adaptors.LocationAdaptor;
import org.septa.android.app.services.apiinterfaces.LocationService;

import retrofit.Callback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LocationServiceProxy {

    public void getLocation(double longitude, double latitiude, float radiusInMiles, String type, Callback<ArrayList<LocationModel>> callBack) {
        LocationService locationService = LocationAdaptor.getLocationService();

        Map<String, String> options = new HashMap<String, String>();
        options.put("lon", String.valueOf(longitude));
        options.put("lat", String.valueOf(latitiude));
        options.put("radius", String.valueOf(radiusInMiles));
        options.put("number_of_results", String.valueOf(400));
        options.put("type", type);

        locationService.location(options, callBack);
    }
}

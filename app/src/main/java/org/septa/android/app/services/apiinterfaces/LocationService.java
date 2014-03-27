/*
 * LocationService.java
 * Last modified on 03-27-2014 13:16-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.services.apiinterfaces;

import org.septa.android.app.models.LocationModel;

import java.util.ArrayList;
import java.util.Map;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.QueryMap;

public interface LocationService {
    @GET("/hackathon/locations/get_locations.php")
    void location (
            @QueryMap Map<String, String> options,
            Callback<ArrayList<LocationModel>> callback
    );
}
/*
 * NextToArriveService.java
 * Last modified on 06-05-2014 22:36-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.services.apiinterfaces;

import org.septa.android.app.models.servicemodels.NextToArriveModel;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

public interface NextToArriveService {
    @GET("/hackathon/NextToArrive/{startStation}/{destinationStation}/{numberOfResults}")
    void views(@Path("startStation") String startStation,
               @Path("destinationStation") String destinationStation,
               @Path("numberOfResults") String numberOfResults,
               Callback<ArrayList<NextToArriveModel>> callback);
}

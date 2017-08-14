/*
 * NextToArriveService.java
 * Last modified on 06-05-2014 22:36-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.services.apiinterfaces;

import org.septa.android.app.services.apiinterfaces.model.NextToArriveModel;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface NextToArriveService {

    @GET("/hackathon/NextToArrive/{startStation}/{destinationStation}/{numberOfResults}")
    Call<ArrayList<NextToArriveModel>> views(@Path("startStation") String startStation,
                                                          @Path("destinationStation") String destinationStation,
                                                          @Path("numberOfResults") int numberOfResults);
}

/*
 * TransitViewService.java
 * Last modified on 04-22-2014 12:52-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.services.apiinterfaces;

import org.septa.android.app.models.servicemodels.TransitViewModel;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

public interface TransitViewService {
    @GET("/hackathon/TransitView/{routeShortName}")
    void views(@Path("routeShortName") String routeShortName,
               Callback<TransitViewModel> callback);
}

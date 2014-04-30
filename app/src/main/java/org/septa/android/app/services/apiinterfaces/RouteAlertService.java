/*
 * RouteAlertService.java
 * Last modified on 04-30-2014 08:40-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.services.apiinterfaces;

import org.septa.android.app.models.servicemodels.RouteAlertDataModel;

import java.util.ArrayList;
import java.util.Map;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.QueryMap;

public interface RouteAlertService {
    @GET("/hackathon/Alerts/get_alert_data.php")
    void routeAlertData (
            @QueryMap Map<String, String> options,
            Callback<ArrayList<RouteAlertDataModel>> callback
    );
}

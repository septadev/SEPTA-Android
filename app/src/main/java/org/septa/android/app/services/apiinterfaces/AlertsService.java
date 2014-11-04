/*
 * AlertsServiceProxy.java
 * Last modified on 05-16-2014 20:23-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.services.apiinterfaces;

import org.septa.android.app.models.servicemodels.AlertModel;
import org.septa.android.app.models.servicemodels.ServiceAdvisoryModel;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public interface AlertsService {
    @GET("/hackathon/Alerts/")
    void alerts(
            Callback<ArrayList<AlertModel>> callback
    );

    @GET("/hackathon/Alerts/get_alert_data.php?req1=all")
    void getAllAlerts(
            Callback<ArrayList<AlertModel>> callback
    );

    @GET("/hackathon/Alerts/get_alert_data.php")
    void getAlertsForRouteName(
            @Query("req1") String routeName
            , Callback<ArrayList<AlertModel>> callback
    );

    @GET("/hackathon/Alerts/get_alert_data.php")
    void getAlertsForRoute(
            @Query("req1") String routeName
            , Callback<ArrayList<ServiceAdvisoryModel>> callback
    );
}

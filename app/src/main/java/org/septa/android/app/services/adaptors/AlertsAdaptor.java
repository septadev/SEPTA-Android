/*
 * AlertsAdaptor.java
 * Last modified on 05-16-2014 20:46-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.services.adaptors;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;

import org.septa.android.app.models.RouteTypes;
import org.septa.android.app.models.RoutesModel;
import org.septa.android.app.services.ServiceErrorHandler;
import org.septa.android.app.services.apiinterfaces.AlertsService;
import org.septa.android.app.services.apiinterfaces.TrainViewService;

import java.util.Date;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class AlertsAdaptor {
    public static final String TAG = AlertsAdaptor.class.getName();

    public static AlertsService getAlertsService() {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .registerTypeAdapter(Date.class, new DateAdapter())
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://www3.septa.org")       // The base API endpoint.
                .setConverter(new GsonConverter(gson))
                .setErrorHandler(new ServiceErrorHandler())
                .build();

        return restAdapter.create(AlertsService.class);
    }

    public static String getServiceRouteName(String routeShortName, RouteTypes routeType) {
        switch (routeType) {
            case BUS:
                return "bus_route_" + routeShortName.toUpperCase();
            case TROLLEY:
                return "trolley_route_" + routeShortName.toLowerCase();
            default:
                return "rr_route_" + routeShortName.toLowerCase();
        }
    }
}
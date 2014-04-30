/*
 * RouteAlertServiceProxy.java
 * Last modified on 04-30-2014 08:39-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.services.apiproxies;

import org.septa.android.app.models.servicemodels.RouteAlertDataModel;
import org.septa.android.app.services.adaptors.RouteAlertDataAdaptor;
import org.septa.android.app.services.apiinterfaces.RouteAlertService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;

public class RouteAlertServiceProxy {

    public void getRouteAlertData(String routeName, Callback<ArrayList<RouteAlertDataModel>> callBack) {
        RouteAlertService routeAlertService = RouteAlertDataAdaptor.getRouteAlertDataService();

        Map<String, String> options = new HashMap<String, String>();
        options.put("req1", String.valueOf(routeName));

        routeAlertService.routeAlertData(options, callBack);
    }
}

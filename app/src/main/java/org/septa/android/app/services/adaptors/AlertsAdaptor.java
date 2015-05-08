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

import org.septa.android.app.models.RouteTypes;
import org.septa.android.app.models.SchedulesRouteModel;
import org.septa.android.app.services.ServiceErrorHandler;
import org.septa.android.app.services.apiinterfaces.AlertsService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class AlertsAdaptor {
    public static final String TAG = AlertsAdaptor.class.getName();
    private static Map<String, String> railCodes = new HashMap<String, String>();

    static {
        railCodes.put("AIR", "apt");
        railCodes.put("CHE", "che");
        railCodes.put("CHW", "chw");
        railCodes.put("CYN", "cyn");
        railCodes.put("FOX", "fxc");
        railCodes.put("LAN", "landdoy");
        railCodes.put("DOY", "landdoy");
        railCodes.put("MED", "med");
        railCodes.put("NOR", "nor");
        railCodes.put("PAO", "pao");
        railCodes.put("TRE", "trent");
        railCodes.put("WAR", "warm");
        railCodes.put("WIL", "wilm");
        railCodes.put("WTR", "wtren");
        railCodes.put("GC", "gc");
    }

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

    public static String getServiceRouteName(SchedulesRouteModel schedulesRouteModel, RouteTypes routeType) {
        switch (routeType) {
            case BUS:
                return "bus_route_" + schedulesRouteModel.getRouteShortName().toUpperCase();
            case TROLLEY:
                return "trolley_route_" + schedulesRouteModel.getRouteShortName().toLowerCase();
            case RAIL:
                return "rr_route_" + railCodes.get(schedulesRouteModel.getRouteId().toUpperCase());
            default:
                return "rr_route_" + schedulesRouteModel.getRouteShortName().toLowerCase();
        }
    }
}
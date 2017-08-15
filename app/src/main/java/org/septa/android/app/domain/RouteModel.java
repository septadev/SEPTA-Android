package org.septa.android.app.domain;

import android.support.annotation.NonNull;

/**
 * Created by jkampf on 8/15/17.
 */

public class RouteModel  {
    private static final String TAG = RouteModel.class.getName();

    private String routeId;
    private String routeShortName;
    private String routeLongName;
    private Number routeType;

    public RouteModel(String routeId, String routeShortName, String routeLongName, Number routeType) {
        this.routeId = routeId;
        this.routeShortName = routeShortName;
        this.routeLongName = routeLongName;
        this.routeType = routeType;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public String getRouteLongName() {
        return routeLongName;
    }

    public Number getRouteType() {
        return routeType;
    }
}
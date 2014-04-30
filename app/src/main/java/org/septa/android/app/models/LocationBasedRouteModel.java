/*
 * LocationBasedRouteModel.java
 * Last modified on 04-30-2014 10:49-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models;

public class LocationBasedRouteModel {
        private String routeShortName;
        private int directionBinaryPower = 0;
        private int routeType = 0;

    public int getDirectionBinaryPower() {
        return directionBinaryPower;
    }

    public void setDirectionBinaryPower(int directionBinaryPower) {
        this.directionBinaryPower = directionBinaryPower;
    }

    public int getRouteType() {
        return routeType;
    }

    public void setRouteType(int routeType) {
        this.routeType = routeType;
    }

    public enum DirectionCode {
        N, S, E, W, X
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public void setRouteShortName(String routeShortName) {
        this.routeShortName = routeShortName;
    }
}

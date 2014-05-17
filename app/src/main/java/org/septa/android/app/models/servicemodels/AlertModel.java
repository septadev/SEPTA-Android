/*
 * AlertModel.java
 * Last modified on 05-16-2014 20:24-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models.servicemodels;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class AlertModel {
    public static final String TAG = AlertModel.class.getName();

    @SerializedName("isSnow") private String isSnow;
    @SerializedName("isadvisory") private String isAdvisory;
    @SerializedName("isalert") private String isAlert;
    @SerializedName("isdetour") private String isDetour;
    @SerializedName("issuspended") private String isSuspended;
    @SerializedName("last_update") private Date lastUpdate;
    @SerializedName("mode") private String mode;
    @SerializedName("route_id") private String routeId;
    @SerializedName("route_name") private String routeName;

    public boolean isBSL() {
        if (mode.equals("Broad Street Line")) {
            return true;
        }

        return false;
    }

    public boolean isBus() {
        if (mode.equals("Bus")) {
            return true;
        }

        return false;
    }

    public boolean isTrolley () {
        if (mode.equals("Trolley")) {
            return true;
        }

        return false;
    }

    public boolean isRegionalRail() {
        if (mode.equals("Regional Rail")) {
            return true;
        }

        return false;
    }

    public boolean isMFL() {
        if (mode.equals("Market/ Frankford")) {
            return true;
        }

        return false;
    }

    public boolean isNHSL() {
        if (mode.equals("Norristown High Speed Line")) {
            return true;
        }

        return false;
    }

    public boolean isGeneric() {
        if (mode.equals("generic")) {
            return true;
        }

        return false;
    }

    public boolean hasSnowFlag() {
        if (isSnow.toUpperCase().equals("Y")) {
            return true;
        }

        return false;
    }

    public boolean hasAdvisoryFlag() {
        if (isAdvisory.toUpperCase().equals("Y")) {
            return true;
        }

        return false;
    }

    public boolean hasAlertFlag() {
        if (isAlert.toUpperCase().equals("Y")) {
            return true;
        }

        return false;
    }

    public boolean hasDetourFlag() {
        if (isDetour.toUpperCase().equals("Y")) {
            return true;
        }

        return false;
    }

    public boolean hasSuspendedFlag() {
        if (isSuspended.toUpperCase().equals("Y")) {
            return true;
        }

        return false;
    }


    public boolean hasFlag() {
        if (hasAlertFlag() || hasAdvisoryFlag() || hasDetourFlag()) {
            return true;
        }

        return false;
    }

    public String getRouteName() {
        return routeName;
    }

//    public void setRouteName(String routeName) {
//        this.routeName = routeName;
//    }
}

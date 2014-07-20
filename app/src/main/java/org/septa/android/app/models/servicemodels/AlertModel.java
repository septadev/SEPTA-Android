/*
 * AlertModel.java
 * Last modified on 05-16-2014 20:24-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models.servicemodels;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class AlertModel implements Comparable<AlertModel> {
    public static final String TAG = AlertModel.class.getName();

    @SerializedName("isSnow") private String isSnow;
    @SerializedName("isadvisory") private String isAdvisory;
    @SerializedName("isalert") private String isAlert;
    @SerializedName("isdetour") private String isDetour;
    @SerializedName("issuppend") private String isSuspended;
    @SerializedName("last_updated") private Date lastUpdate;
    @SerializedName("mode") private String mode;
    @SerializedName("route_id") private String routeId;
    @SerializedName("route_name") private String routeName;
    @SerializedName("current_message") private String currentMessage;

    public AlertModel(){
        this.mode = "Empty";
        this.routeId = null;
        this.routeName = "Empty";
        this.currentMessage = "Empty";
        this.isAlert = "N";
        this.isDetour = "N";
        this.isSuspended ="N";
        this.isAdvisory = "No";
        this.isSnow = "N";
        this.lastUpdate = new Date();
    }

    public boolean isGeneral() {
        if (mode.equals("generic")) {

            return true;
        }

        return false;
    }

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

    public boolean hasSnowFlag() {
        if (isSnow.toUpperCase().equals("Y")) {
            return true;
        }

        return false;
    }

    public boolean hasAdvisoryFlag() {
        if (isAdvisory.toUpperCase().equals("YES")) {
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

    public boolean isSuspended() {

        return hasSuspendedFlag();
    }


    public boolean hasFlag() {
        if (hasSuspendedFlag() || hasAlertFlag() || hasAdvisoryFlag() || hasDetourFlag()) {
            return true;
        }

        return false;
    }

    public String getRouteName() {
        return routeName;
    }

    public String getRouteId() {

        return routeId;
    }

    @Override
    public int compareTo(AlertModel another) {
        int result = 0;
        Integer thisRouteName = null;
        Integer otherRouteName = null;
        boolean thisIsString = false;
        boolean otherIsString = false;

        // first check if the row is for elevators, if yes, it will go at the top
        if (mode.equals("elevator")) {

            return -1;
        } else {
            if (another.mode.equals("elevator")) {
                return 1;
            }
        }

        // next check if the row is for general, if yes, it goes just below the elevator
        if (mode.equals("generic")) {
            return -1;
        } else {
            if (another.mode.equals("generic")) {
                return 1;
            }
        }

        // we assume a route short name is either a number (only numerics), a number with a trailing character, or
        //  not a number (all characters.
        // first check if it is a number, then remove the last character and check for a number
        // if those two fail, it must not be or have a number
        try {
            thisRouteName = Integer.valueOf(this.routeName);
        } catch (NumberFormatException nfe) {
            thisIsString = true;
        }

        try {
            otherRouteName = Integer.valueOf(another.routeName);
        } catch (NumberFormatException nfe) {
            otherIsString = true;
        }

        // this is a string and other is not, thus other comes first
        if (thisIsString && !otherIsString) {

            return 1;
        }

        // this is not a string and other is, thus this comes first
        if (!thisIsString && otherIsString) {

            return -1;
        }

        // both are strings, just compare outright;
        if (thisIsString && otherIsString) {

            return this.routeName.compareTo(another.routeName);
        }

        // if we got here, we converted both to Integers and can compare outright.
        return thisRouteName.compareTo(otherRouteName);
    }

    public String getCurrentMessage() {
        return currentMessage;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

}

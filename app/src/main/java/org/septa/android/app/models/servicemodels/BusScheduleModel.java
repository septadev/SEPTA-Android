/*
 * TransitViewVehicleModel.java
 * Last modified on 04-22-2014 15:33-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models.servicemodels;

import com.google.gson.annotations.SerializedName;

public class BusScheduleModel {
    public static final String TAG = TransitViewVehicleModel.class.getName();

    @SerializedName("StopName")
    private String stopName;
    @SerializedName("Route")
    private String route;
    @SerializedName("date")
    private String date;
    @SerializedName("day")
    private String day;
    @SerializedName("Direction")
    private String direction;
    @SerializedName("DateCalendar")
    private String dateCalendar;

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getDateCalendar() {
        return dateCalendar;
    }

    public void setDateCalendar(String dateCalendar) {
        this.dateCalendar = dateCalendar;
    }
}

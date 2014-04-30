/*
 * RouteAlertDataModel.java
 * Last modified on 04-30-2014 08:50-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models.servicemodels;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class RouteAlertDataModel {
    public static final String TAG = RouteAlertDataModel.class.getName();

    @SerializedName("advisory_message") private String advisoryMessage;
    @SerializedName("current_message") private String currentMessage;
    @SerializedName("detour_end_date_time") private Date detourEndDateTime;
    @SerializedName("detour_message") private String detourMessage;
    @SerializedName("detour_reason") private String detourReason;
    @SerializedName("detour_start_date_time") private Date detourStartDateTime;
    @SerializedName("detour_start_location") private String detourStartLocation;
    @SerializedName("last_updated") private Date lastUpdated;
    @SerializedName("route_id") private String routeId;
    @SerializedName("route_name") private String routeName;

    public String getAdvisoryMessage() {
        return advisoryMessage;
    }

    public void setAdvisoryMessage(String advisoryMessage) {
        this.advisoryMessage = advisoryMessage;
    }

    public String getCurrentMessage() {
        return currentMessage;
    }

    public void setCurrentMessage(String currentMessage) {
        this.currentMessage = currentMessage;
    }

    public Date getDetourEndDateTime() {
        return detourEndDateTime;
    }

    public void setDetourEndDateTime(Date detourEndDateTime) {
        this.detourEndDateTime = detourEndDateTime;
    }

    public String getDetourMessage() {
        return detourMessage;
    }

    public void setDetourMessage(String detourMessage) {
        this.detourMessage = detourMessage;
    }

    public String getDetourReason() {
        return detourReason;
    }

    public void setDetourReason(String detourReason) {
        this.detourReason = detourReason;
    }

    public Date getDetourStartDateTime() {
        return detourStartDateTime;
    }

    public void setDetourStartDateTime(Date detourStartDateTime) {
        this.detourStartDateTime = detourStartDateTime;
    }

    public String getDetourStartLocation() {
        return detourStartLocation;
    }

    public void setDetourStartLocation(String detourStartLocation) {
        this.detourStartLocation = detourStartLocation;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }
}

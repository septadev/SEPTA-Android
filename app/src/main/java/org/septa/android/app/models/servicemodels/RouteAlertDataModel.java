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
    @SerializedName("detour_end_date_time") private String detourEndDateTime;
    @SerializedName("detour_message") private String detourMessage;
    @SerializedName("detour_reason") private String detourReason;
    @SerializedName("detour_start_date_time") private String detourStartDateTime;
    @SerializedName("detour_start_location") private String detourStartLocation;
    @SerializedName("last_updated") private String lastUpdated;
    @SerializedName("route_id") private String routeId;
    @SerializedName("route_name") private String routeName;

    public String getAdvisoryMessage() {
        return advisoryMessage;
    }

    public String getDetourDetailsAsHTML() {

        return String.format("<body><div><br><font face=\"verdana\"><table><tbody><tr bgcolor=\"#CCC\"><td><b>Start Location:</b> </td><td>%s</td></tr><tr><td><b>Start Date:</b> </td><td>%s</td></tr><tr bgcolor=\"#CCC\"><td><b>End Date:</b> </td><td>%s</td></tr><tr><td><b>Reason for Detour:</b> </td><td>%s</td></tr><tr bgcolor=\"#CCC\"><td><b>Details:</b> </td><td>%s</td></tr></tbody></table><br></font></div></body>",detourStartLocation, detourStartDateTime, detourEndDateTime, detourReason, detourMessage);
    }

    public String getCurrentMessage() {
        return currentMessage;
    }

    public String getDetourEndDateTime() {
        return detourEndDateTime;
    }

    public String getDetourMessage() {
        return detourMessage;
    }

    public String getDetourReason() {
        return detourReason;
    }

    public String getDetourStartDateTime() {
        return detourStartDateTime;
    }

    public String getDetourStartLocation() {
        return detourStartLocation;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getRouteName() {
        return routeName;
    }
}

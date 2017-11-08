/*
 * NextToArriveModel.java
 * Last modified on 06-05-2014 22:30-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.services.apiinterfaces.model;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class NextToArriveModel {
    public static final String TAG = NextToArriveModel.class.getName();

    private static Map<String, String> rrRouteId = new HashMap<String, String>();

    static {
        rrRouteId.put("Chestnut Hill East", "rr_route_che");
        rrRouteId.put("Chestnut Hill West", "rr_route_chw");
        rrRouteId.put("Cynwyd", "rr_route_cyn");
        rrRouteId.put("Fox Chase", "rr_route_fxc");
        rrRouteId.put("Glenside Combined", "rr_route_gc");
        rrRouteId.put("Lansdale/Doylestown", "rr_route_landdoy");
        rrRouteId.put("Media/Elwyn", "rr_route_med");
        rrRouteId.put("Manayunk/Norristown", "rr_route_nor");
        rrRouteId.put("Paoli/Thorndale", "rr_route_pao");
        rrRouteId.put("Trenton", "rr_route_trent");
        rrRouteId.put("Warminster", "rr_route_warm");
        rrRouteId.put("Wilmington/Newark", "rr_route_wilm");
        rrRouteId.put("West Trenton", "rr_route_wtren");
    }

    @SerializedName("orig_train") private String originalTrain;
    @SerializedName("orig_line") private String originalLine;
    @SerializedName("orig_departure_time") private String originalDepartureTime;
    @SerializedName("orig_arrival_time") private String OriginalArrivalTime;
    @SerializedName("orig_delay") private String originalDelay;

    @SerializedName("term_train") private String terminalTrain;
    @SerializedName("term_line") private String terminalLine;
    @SerializedName("term_depart_time") private String terminalDepartureTime;
    @SerializedName("term_arrival_time") private String terminalArrivalTime;
    @SerializedName("term_delay") private String terminalDelay;

    @SerializedName("isdirect") private String isDirect;

    @SerializedName("Connection") private String connection;

    public String getOriginalTrain() {
        return originalTrain;
    }

    public String getOriginalLine() {
        return originalLine;
    }

    public String getOriginalLineId() {
        return rrRouteId.get(originalLine);
    }

    public String getOriginalDepartureTime() {
        return originalDepartureTime;
    }

    public String getOriginalArrivalTime() {
        return OriginalArrivalTime;
    }

    public String getOriginalDelay() {
        return originalDelay;
    }

    public String getTerminalTrain() {
        return terminalTrain;
    }

    public String getTerminalLine() {
        return terminalLine;
    }

    public String getTerminalLineId() {
        return rrRouteId.get(terminalLine);
    }

    public String getTerminalDepartureTime() {
        return terminalDepartureTime;
    }

    public String getTerminalArrivalTime() {
        return terminalArrivalTime;
    }

    public String getTerminalDelay() {
        return terminalDelay;
    }

    public boolean isDirect() {
        return isDirect.equals("true");
    }

    public String getConnection() {
        return connection;
    }
}

package org.septa.android.app.models;

import java.lang.String;

public class SchedulesRouteModel {
    private int routeType;
    private String routeId;
    private String routeShortName;
    private String routeLongName;
    private String routeStart;
    private String routeEnd;
    private int routeStartStopId;
    private int routeEndStopId;

    public SchedulesRouteModel() { };

    public SchedulesRouteModel(int routeType,
                               String routeId,
                               String routeShortName,
                               String routeLongName,
                               String routeStart,
                               String routeEnd,
                               int routeStartStopId,
                               int routeEndStopId) {
        this.setRouteType(routeType);
        this.setRouteId(routeId);
        this.setRouteShortName(routeShortName);
        this.setRouteLongName(routeLongName);
        this.setRouteStart(routeStart);
        this.setRouteEnd(routeEnd);
        this.setRouteStartStopId(routeStartStopId);
        this.setRouteEndStopId(routeEndStopId);
    }

    public int getRouteType() {
        return routeType;
    }

    public void setRouteType(int routeType) {
        this.routeType = routeType;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getRouteStart() {
        return routeStart;
    }

    public void setRouteStart(String routeStart) {
        this.routeStart = routeStart;
    }

    public String getRouteEnd() {
        return routeEnd;
    }

    public void setRouteEnd(String routeEnd) {
        this.routeEnd = routeEnd;
    }

    public int getRouteStartStopId() {
        return routeStartStopId;
    }

    public void setRouteStartStopId(int routeStartStopId) {
        this.routeStartStopId = routeStartStopId;
    }

    public int getRouteEndStopId() {
        return routeEndStopId;
    }

    public void setRouteEndStopId(int routeEndStopId) {
        this.routeEndStopId = routeEndStopId;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public void setRouteShortName(String routeShortName) {
        this.routeShortName = routeShortName;
    }

    public String getRouteLongName() {
        return routeLongName;
    }

    public void setRouteLongName(String routeLongName) {
        this.routeLongName = routeLongName;
    }
}

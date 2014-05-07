package org.septa.android.app.models;

import java.lang.String;

public class SchedulesRouteModel {
    private String routeType;
    private String routeId;
    private String routeTitle;
    private String routeStart;
    private String routeEnd;
    private int routeStartStopId;
    private int routeEndStopId;

    public SchedulesRouteModel() { };

    public SchedulesRouteModel(String travelType, String routeId, String routeTitle, String routeStart, String routeEnd, int routeStartStopId, int routeEndStopId) {
        this.setRouteType(travelType);
        this.setRouteId(routeId);
        this.setRouteTitle(routeTitle);
        this.setRouteStart(routeStart);
        this.setRouteEnd(routeEnd);
        this.setRouteStartStopId(routeStartStopId);
        this.setRouteEndStopId(routeEndStopId);
    }

    public String getRouteType() {
        return routeType;
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getRouteTitle() {
        return routeTitle;
    }

    public void setRouteTitle(String routeTitle) {
        this.routeTitle = routeTitle;
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
}

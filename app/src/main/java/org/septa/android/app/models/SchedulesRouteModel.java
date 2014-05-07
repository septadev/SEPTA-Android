package org.septa.android.app.models;

import org.apache.http.message.BasicNameValuePair;import java.lang.String;

public class SchedulesRouteModel {
    private String routeId;
    private String routeTitle;
    private String routeStart;
    private String routeEnd;

    public SchedulesRouteModel() { };

    public SchedulesRouteModel(String routeId, String routeTitle, String routeStart, String routeEnd) {
        this.setRouteId(routeId);
        this.setRouteTitle(routeTitle);
        this.setRouteStart(routeStart);
        this.setRouteEnd(routeEnd);
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
}

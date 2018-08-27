package org.septa.android.app.services.apiinterfaces.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RouteNotifSubscription implements Serializable {

    @SerializedName("route_id")
    private String routeId;

    private boolean alerts;

    private boolean detour;

    private boolean delays;

    public RouteNotifSubscription() {
    }

    public RouteNotifSubscription(String routeId) {
        this.routeId = routeId;
        this.alerts = true;
        this.detour = true;
        this.delays = true;
    }

    public RouteNotifSubscription(String routeId, boolean alerts, boolean detour, boolean delays) {
        this.routeId = routeId;
        this.alerts = alerts;
        this.detour = detour;
        this.delays = delays;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public boolean isAlerts() {
        return alerts;
    }

    public void setAlerts(boolean alerts) {
        this.alerts = alerts;
    }

    public boolean isDetour() {
        return detour;
    }

    public void setDetour(boolean detour) {
        this.detour = detour;
    }

    public boolean isDelays() {
        return delays;
    }

    public void setDelays(boolean delays) {
        this.delays = delays;
    }

    @Override
    public String toString() {
        return "RouteNotifSubscription{" +
                "routeId='" + routeId + '\'' +
                ", alerts=" + alerts +
                ", detour=" + detour +
                ", delays=" + delays +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RouteNotifSubscription that = (RouteNotifSubscription) o;

        if (alerts != that.alerts) return false;
        if (detour != that.detour) return false;
        if (delays != that.delays) return false;
        return routeId.equals(that.routeId);
    }

    @Override
    public int hashCode() {
        int result = routeId.hashCode();
        result = 31 * result + (alerts ? 1 : 0);
        result = 31 * result + (detour ? 1 : 0);
        result = 31 * result + (delays ? 1 : 0);
        return result;
    }
}
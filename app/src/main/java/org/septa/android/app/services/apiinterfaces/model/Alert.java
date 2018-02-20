package org.septa.android.app.services.apiinterfaces.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by jkampf on 9/21/17.
 */

public class Alert {

    @SerializedName("route_id")
    String routeId;

    @SerializedName("route_name")
    String routeName;

    @SerializedName("mode")
    String mode;

    @SerializedName("advisory")
    boolean advisory;

    @SerializedName("detour")
    boolean detour;

    @SerializedName("alert")
    boolean alert;

    @SerializedName("suppend")
    boolean suspended;

    @SerializedName("last_updated")
    String lastUpdate;

    @SerializedName("snow")
    boolean snow;

    @SerializedName("description")
    String description;

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

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isAdvisory() {
        return advisory;
    }

    public void setAdvisory(boolean advisory) {
        this.advisory = advisory;
    }

    public boolean isDetour() {
        return detour;
    }

    public void setDetour(boolean detour) {
        this.detour = detour;
    }

    public boolean isAlert() {
        return alert;
    }

    public void setAlert(boolean alert) {
        this.alert = alert;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean isSnow() {
        return snow;
    }

    public void setSnow(boolean snow) {
        this.snow = snow;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Alert{" +
                "routeId='" + routeId + '\'' +
                ", routeName='" + routeName + '\'' +
                ", mode='" + mode + '\'' +
                ", advisory=" + advisory +
                ", detour=" + detour +
                ", alert=" + alert +
                ", suspended=" + suspended +
                ", lastUpdate='" + lastUpdate + '\'' +
                ", snow=" + snow +
                ", description='" + description + '\'' +
                '}';
    }
}
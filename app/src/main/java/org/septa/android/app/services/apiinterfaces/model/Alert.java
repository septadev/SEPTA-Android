package org.septa.android.app.services.apiinterfaces.model;

import com.google.gson.annotations.SerializedName;

public class Alert {

    @SerializedName("route_id")
    private String routeId;

    @SerializedName("route_name")
    private String routeName;

    @SerializedName("mode")
    private String mode;

    @SerializedName("advisory")
    private boolean advisory;

    @SerializedName("detour")
    private boolean detour;

    @SerializedName("alert")
    private boolean alert;

    @SerializedName("suppend")
    private boolean suspended;

    @SerializedName("last_updated")
    private String lastUpdate;

    @SerializedName("snow")
    private boolean snow;

    @SerializedName("description")
    private String description;

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
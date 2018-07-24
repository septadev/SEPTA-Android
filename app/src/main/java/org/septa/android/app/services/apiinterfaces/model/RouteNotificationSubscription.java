package org.septa.android.app.services.apiinterfaces.model;

import org.septa.android.app.TransitType;

import java.io.Serializable;

public class RouteNotificationSubscription implements Serializable {

    private String routeId;

    private TransitType transitType;

    private boolean isEnabled;

    public RouteNotificationSubscription(String routeId, TransitType transitType) {
        this.routeId = routeId;
        this.transitType = transitType;
        this.isEnabled = true;
    }

    public RouteNotificationSubscription(String routeId, TransitType transitType, boolean isEnabled) {
        this.routeId = routeId;
        this.transitType = transitType;
        this.isEnabled = isEnabled;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public TransitType getTransitType() {
        return transitType;
    }

    public void setTransitType(TransitType transitType) {
        this.transitType = transitType;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    /**
     * ignores the value of isEnabled
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RouteNotificationSubscription that = (RouteNotificationSubscription) o;

        if (!routeId.equals(that.routeId)) {
            return false;
        }
        return transitType == that.transitType;
    }

    @Override
    public int hashCode() {
        int result = routeId.hashCode();
        result = 31 * result + transitType.hashCode();
        result = 31 * result + (isEnabled ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RouteNotificationSubscription{" +
                "routeId='" + routeId + '\'' +
                ", transitType=" + transitType +
                ", isEnabled=" + isEnabled +
                '}';
    }
}
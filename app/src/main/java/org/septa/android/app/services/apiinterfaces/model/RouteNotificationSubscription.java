package org.septa.android.app.services.apiinterfaces.model;

import org.septa.android.app.TransitType;

import java.io.Serializable;

public class RouteNotificationSubscription implements Serializable {

    private String routeId;

    private String routeName;

    private TransitType transitType;

    private boolean isEnabled;

    public RouteNotificationSubscription(String routeId, String routeName, TransitType transitType) {
        this.routeId = routeId;
        this.routeName = routeName;
        this.transitType = transitType;
        this.isEnabled = true;
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
        if (!routeName.equals(that.routeName)) {
            return false;
        }
        return transitType == that.transitType;
    }

    @Override
    public int hashCode() {
        int result = routeId.hashCode();
        result = 31 * result + routeName.hashCode();
        result = 31 * result + transitType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "RouteNotificationSubscription{" +
                "routeId='" + routeId + '\'' +
                ", routeName='" + routeName + '\'' +
                ", transitType=" + transitType +
                ", isEnabled=" + isEnabled +
                '}';
    }
}
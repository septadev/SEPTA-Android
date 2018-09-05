package org.septa.android.app.services.apiinterfaces.model;

import android.support.annotation.NonNull;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;
import org.septa.android.app.TransitType;

import java.io.Serializable;

@Parcel
public class RouteSubscription implements Serializable, Comparable<RouteSubscription> {

    String routeId;

    String routeName;

    TransitType transitType;

    boolean isEnabled;

    public RouteSubscription() {
    }

    @ParcelConstructor
    public RouteSubscription(String routeId, String routeName, TransitType transitType) {
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

        RouteSubscription that = (RouteSubscription) o;

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
        return "RouteSubscription{" +
                "routeId='" + routeId + '\'' +
                ", routeName='" + routeName + '\'' +
                ", transitType=" + transitType +
                ", isEnabled=" + isEnabled +
                '}';
    }

    /**
     * sort by transit type and then route ID
     * @param other
     * @return
     */
    @Override
    public int compareTo(@NonNull RouteSubscription other) {
        int i = this.transitType.compareTo(other.transitType);
        if (i != 0) {
            return i;
        }

        i = this.routeId.compareTo(other.routeId);
        if (i != 0) {
            return i;
        }

        i = this.routeName.compareTo(other.routeName);
        return i;
    }

}
package org.septa.android.app.services.apiinterfaces.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Arrays;

public class PushNotifSubscriptionRequest implements Serializable {

    @SerializedName("device_id")
    private String deviceId;

    @SerializedName("reg_token")
    private String regToken;

    @SerializedName("special_announcements")
    private Boolean specialAnnouncements;

    @SerializedName("time_windows")
    private TimeSlot[] timeWindows;

    @SerializedName("route_subscriptions")
    private RouteNotifSubscription[] routeSubscriptions;

    public PushNotifSubscriptionRequest() {
    }

    public PushNotifSubscriptionRequest(String deviceId) {
        this.deviceId = deviceId;
        this.regToken = null;
        this.specialAnnouncements = null;
        this.timeWindows = null;
        this.routeSubscriptions = null;
    }

    public PushNotifSubscriptionRequest(String deviceId, String regToken, Boolean specialAnnouncements, TimeSlot[] timeWindows, RouteNotifSubscription[] routeSubscriptions) {
        this.deviceId = deviceId;
        this.regToken = regToken;
        this.specialAnnouncements = specialAnnouncements;
        this.timeWindows = timeWindows;
        this.routeSubscriptions = routeSubscriptions;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getRegToken() {
        return regToken;
    }

    public void setRegToken(String regToken) {
        this.regToken = regToken;
    }

    public Boolean getSpecialAnnouncements() {
        return specialAnnouncements;
    }

    public void setSpecialAnnouncements(Boolean specialAnnouncements) {
        this.specialAnnouncements = specialAnnouncements;
    }

    public TimeSlot[] getTimeWindows() {
        return timeWindows;
    }

    public void setTimeWindows(TimeSlot[] timeWindows) {
        this.timeWindows = timeWindows;
    }

    public RouteNotifSubscription[] getRouteSubscriptions() {
        return routeSubscriptions;
    }

    public void setRouteSubscriptions(RouteNotifSubscription[] routeSubscriptions) {
        this.routeSubscriptions = routeSubscriptions;
    }

    @Override
    public String toString() {
        return "PushNotifSubscriptionRequest{" +
                "deviceId='" + deviceId + '\'' +
                ", regToken='" + regToken + '\'' +
                ", specialAnnouncements=" + specialAnnouncements +
                ", timeWindows=" + Arrays.toString(timeWindows) +
                ", routeSubscriptions=" + Arrays.toString(routeSubscriptions) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PushNotifSubscriptionRequest that = (PushNotifSubscriptionRequest) o;

        if (!deviceId.equals(that.deviceId)) return false;
        if (regToken != null ? !regToken.equals(that.regToken) : that.regToken != null)
            return false;
        if (specialAnnouncements != null ? !specialAnnouncements.equals(that.specialAnnouncements) : that.specialAnnouncements != null)
            return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(timeWindows, that.timeWindows)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(routeSubscriptions, that.routeSubscriptions);
    }

    @Override
    public int hashCode() {
        int result = deviceId.hashCode();
        result = 31 * result + (regToken != null ? regToken.hashCode() : 0);
        result = 31 * result + (specialAnnouncements != null ? specialAnnouncements.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(timeWindows);
        result = 31 * result + Arrays.hashCode(routeSubscriptions);
        return result;
    }

}
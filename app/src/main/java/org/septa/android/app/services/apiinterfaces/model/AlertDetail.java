package org.septa.android.app.services.apiinterfaces.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class AlertDetail implements Serializable {
    @SerializedName("route")
    String route;
    @SerializedName("route_name")
    String routeName;
    @SerializedName("results")
    int results;
    @SerializedName("alerts")
    List<Detail> alerts;

    public class Detail implements Serializable {
        @SerializedName("message")
        String message;
        @SerializedName("advisory_message")
        String advisoryMessage;
        @SerializedName("detour")
        Detour detour;
        @SerializedName("last_updated")
        String lastUpdated;
        @SerializedName("snow")
        boolean snow;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getAdvisoryMessage() {
            return advisoryMessage;
        }

        public void setAdvisoryMessage(String advisoryMessage) {
            this.advisoryMessage = advisoryMessage;
        }

        public Detour getDetour() {
            return detour;
        }

        public void setDetour(Detour detour) {
            this.detour = detour;
        }

        public String getLastUpdated() {
            return lastUpdated;
        }

        public void setLastUpdated(String lastUpdated) {
            this.lastUpdated = lastUpdated;
        }

        public boolean isSnow() {
            return snow;
        }

        public void setSnow(boolean snow) {
            this.snow = snow;
        }

        @Override
        public String toString() {
            return "Detail{" +
                    "message='" + message + '\'' +
                    ", advisoryMessage='" + advisoryMessage + '\'' +
                    ", detour=" + detour +
                    ", lastUpdated='" + lastUpdated + '\'' +
                    ", snow=" + snow +
                    '}';
        }
    }


    public class Detour implements Serializable {
        @SerializedName("message")
        String message;
        @SerializedName("start_location")
        String startLocation;
        @SerializedName("start_date_time")
        String startDateTime;
        @SerializedName("end_date_time")
        String endDateTime;
        @SerializedName("reason")
        String reason;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getStartLocation() {
            return startLocation;
        }

        public void setStartLocation(String startLocation) {
            this.startLocation = startLocation;
        }

        public String getStartDateTime() {
            return startDateTime;
        }

        public void setStartDateTime(String startDateTime) {
            this.startDateTime = startDateTime;
        }

        public String getEndDateTime() {
            return endDateTime;
        }

        public void setEndDateTime(String endDateTime) {
            this.endDateTime = endDateTime;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public int getResults() {
        return results;
    }

    public void setResults(int results) {
        this.results = results;
    }

    public List<Detail> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<Detail> alerts) {
        this.alerts = alerts;
    }

    @Override
    public String toString() {
        return "AlertDetail{" +
                "route='" + route + '\'' +
                ", routeName='" + routeName + '\'' +
                ", results=" + results +
                ", alerts=" + alerts +
                '}';
    }
}


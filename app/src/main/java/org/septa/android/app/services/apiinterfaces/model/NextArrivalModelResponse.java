package org.septa.android.app.services.apiinterfaces.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by jkampf on 8/20/17.
 */

public class NextArrivalModelResponse implements Serializable {
    @SerializedName("origin")
    private int startStationId;
    @SerializedName("destination")
    private int destStationId;
    @SerializedName("type")
    private String transType;
    @SerializedName("route")
    private String routeId;
    @SerializedName("success")
    private boolean success;
    @SerializedName("status")
    private int status;
    @SerializedName("arrivals")
    private List<NextArrivalRecord> nextArrivalRecords;

    public int getStartStationId() {
        return startStationId;
    }

    public int getDestStationId() {
        return destStationId;
    }

    public String getTransType() {
        return transType;
    }

    public String getRouteId() {
        return routeId;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getStatus() {
        return status;
    }

    public List<NextArrivalRecord> getNextArrivalRecords() {
        return nextArrivalRecords;
    }

    @Override
    public String toString() {
        return "NextArrivalModelResponse{" +
                "startStationId=" + startStationId +
                ", destStationId=" + destStationId +
                ", transType='" + transType + '\'' +
                ", routeId='" + routeId + '\'' +
                ", success=" + success +
                ", status=" + status +
                ", nextArrivalRecords=" + nextArrivalRecords +
                '}';
    }

    public class NextArrivalRecord implements Serializable {
        @SerializedName("orig_line_route_id")
        private String origRouteId;
        @SerializedName("orig_line_route_name")
        private String origRouteName;
        @SerializedName("term_line_route_id")
        private String termRouteId;
        @SerializedName("term_line_route_name")
        private String termRouteName;
        @SerializedName("connection_station_id")
        private Integer connectionStationId;
        @SerializedName("connection_station_name")
        private String connectionStationName;
        @SerializedName("orig_line_trip_id")
        private String origLineTripId;
        @SerializedName("term_line_trip_id")
        private String termLineTripId;
        @SerializedName("orig_departure_time")
        private Date origDepartureTime;
        @SerializedName("orig_arrival_time")
        private Date origArrivalTime;
        @SerializedName("term_departure_time")
        private Date termDepartureTime;
        @SerializedName("term_arrival_time")
        private Date termArrivalTime;
        @SerializedName("orig_delay_minutes")
        private int origDelayMinutes;
        @SerializedName("term_delay_minutes")
        private int termDelayMinutes;
        @SerializedName("orig_last_stop_id")
        private int origLastStopId;
        @SerializedName("orig_last_stop_name")
        private String origLastStopName;
        @SerializedName("term_last_stop_id")
        private int termLastStopId;
        @SerializedName("term_last_stop_name")
        private String termLastStopName;
        @SerializedName("orig_line_direction")
        private String origLineDirection;
        @SerializedName("term_line_direction")
        private String termLineDirection;
        @SerializedName("orig_vehicle_lat")
        private Double origVehicleLat;
        @SerializedName("orig_vehicle_lon")
        private Double origVehicleLon;
        @SerializedName("term_vehicle_lat")
        private Double termVehicleLat;
        @SerializedName("term_vehicle_lon")
        private Double termVehicleLon;
        @SerializedName("orig_vehicle_line")
        String origVehicleLine;
        @SerializedName("term_vehicle_line")
        String termVehicleLine;
        @SerializedName("orig_realtime")
        boolean origRealtime = false;
        @SerializedName("term_realtime")
        boolean termRealtime = false;


        public String getOrigRouteId() {
            return origRouteId;
        }

        public void setOrigRouteId(String origRouteId) {
            this.origRouteId = origRouteId;
        }

        public String getOrigRouteName() {
            return origRouteName;
        }

        public void setOrigRouteName(String origRouteName) {
            this.origRouteName = origRouteName;
        }

        public String getTermRouteId() {
            return termRouteId;
        }

        public void setTermRouteId(String termRouteId) {
            this.termRouteId = termRouteId;
        }

        public String getTermRouteName() {
            return termRouteName;
        }

        public void setTermRouteName(String termRouteName) {
            this.termRouteName = termRouteName;
        }

        public Integer getConnectionStationId() {
            return connectionStationId;
        }

        public void setConnectionStationId(Integer connectionStationId) {
            this.connectionStationId = connectionStationId;
        }

        public String getConnectionStationName() {
            return connectionStationName;
        }

        public void setConnectionStationName(String connectionStationName) {
            this.connectionStationName = connectionStationName;
        }

        public String getOrigLineTripId() {
            return origLineTripId;
        }

        public void setOrigLineTripId(String origLineTripId) {
            this.origLineTripId = origLineTripId;
        }

        public String getTermLineTripId() {
            return termLineTripId;
        }

        public void setTermLineTripId(String termLineTripId) {
            this.termLineTripId = termLineTripId;
        }

        public Date getOrigDepartureTime() {
            return origDepartureTime;
        }

        public void setOrigDepartureTime(Date origDepartureTime) {
            this.origDepartureTime = origDepartureTime;
        }

        public Date getOrigArrivalTime() {
            return origArrivalTime;
        }

        public void setOrigArrivalTime(Date origArrivalTime) {
            this.origArrivalTime = origArrivalTime;
        }

        public Date getTermDepartureTime() {
            return termDepartureTime;
        }

        public void setTermDepartureTime(Date termDepartureTime) {
            this.termDepartureTime = termDepartureTime;
        }

        public Date getTermArrivalTime() {
            return termArrivalTime;
        }

        public void setTermArrivalTime(Date termArrivalTime) {
            this.termArrivalTime = termArrivalTime;
        }

        public int getOrigDelayMinutes() {
            return origDelayMinutes;
        }

        public void setOrigDelayMinutes(int origDelayMinutes) {
            this.origDelayMinutes = origDelayMinutes;
        }

        public int getTermDelayMinutes() {
            return termDelayMinutes;
        }

        public void setTermDelayMinutes(int termDelayMinutes) {
            this.termDelayMinutes = termDelayMinutes;
        }

        public int getOrigLastStopId() {
            return origLastStopId;
        }

        public void setOrigLastStopId(int origLastStopId) {
            this.origLastStopId = origLastStopId;
        }

        public String getOrigLastStopName() {
            return origLastStopName;
        }

        public void setOrigLastStopName(String origLastStopName) {
            this.origLastStopName = origLastStopName;
        }

        public int getTermLastStopId() {
            return termLastStopId;
        }

        public void setTermLastStopId(int termLastStopId) {
            this.termLastStopId = termLastStopId;
        }

        public String getTermLastStopName() {
            return termLastStopName;
        }

        public void setTermLastStopName(String termLastStopName) {
            this.termLastStopName = termLastStopName;
        }

        public String getOrigLineDirection() {
            return origLineDirection;
        }

        public void setOrigLineDirection(String origLineDirection) {
            this.origLineDirection = origLineDirection;
        }

        public String getTermLineDirection() {
            return termLineDirection;
        }

        public void setTermLineDirection(String termLineDirection) {
            this.termLineDirection = termLineDirection;
        }

        public Double getOrigVehicleLat() {
            return origVehicleLat;
        }

        public void setOrigVehicleLat(Double origVehicleLat) {
            this.origVehicleLat = origVehicleLat;
        }

        public Double getOrigVehicleLon() {
            return origVehicleLon;
        }

        public void setOrigVehicleLon(Double origVehicleLon) {
            this.origVehicleLon = origVehicleLon;
        }

        public Double getTermVehicleLat() {
            return termVehicleLat;
        }

        public void setTermVehicleLat(Double termVehicleLat) {
            this.termVehicleLat = termVehicleLat;
        }

        public Double getTermVehicleLon() {
            return termVehicleLon;
        }

        public void setTermVehicleLon(Double termVehicleLon) {
            this.termVehicleLon = termVehicleLon;
        }

        public String getOrigVehicleLine() {
            return origVehicleLine;
        }

        public void setOrigVehicleLine(String origVehicleLine) {
            this.origVehicleLine = origVehicleLine;
        }

        public String getTermVehicleLine() {
            return termVehicleLine;
        }

        public void setTermVehicleLine(String termVehicleLine) {
            this.termVehicleLine = termVehicleLine;
        }

        public boolean isOrigRealtime() {
            return origRealtime;
        }

        public void setOrigRealtime(boolean origRealtime) {
            this.origRealtime = origRealtime;
        }

        public boolean isTermRealtime() {
            return termRealtime;
        }

        public void setTermRealtime(boolean termRealtime) {
            this.termRealtime = termRealtime;
        }

        @Override
        public String toString() {
            return "NextArrivalRecord{" +
                    "origRouteId='" + origRouteId + '\'' +
                    ", origRouteName='" + origRouteName + '\'' +
                    ", termRouteId='" + termRouteId + '\'' +
                    ", termRouteName='" + termRouteName + '\'' +
                    ", connectionStationId=" + connectionStationId +
                    ", connectionStationName='" + connectionStationName + '\'' +
                    ", origLineTripId='" + origLineTripId + '\'' +
                    ", termLineTripId='" + termLineTripId + '\'' +
                    ", origDepartureTime=" + origDepartureTime +
                    ", origArrivalTime=" + origArrivalTime +
                    ", termDepartureTime=" + termDepartureTime +
                    ", termArrivalTime=" + termArrivalTime +
                    ", origDelayMinutes=" + origDelayMinutes +
                    ", termDelayMinutes=" + termDelayMinutes +
                    ", origLastStopId=" + origLastStopId +
                    ", origLastStopName='" + origLastStopName + '\'' +
                    ", termLastStopId=" + termLastStopId +
                    ", termLastStopName='" + termLastStopName + '\'' +
                    ", origLineDirection='" + origLineDirection + '\'' +
                    ", termLineDirection='" + termLineDirection + '\'' +
                    ", origVehicleLat=" + origVehicleLat +
                    ", origVehicleLon=" + origVehicleLon +
                    ", termVehicleLat=" + termVehicleLat +
                    ", termVehicleLon=" + termVehicleLon +
                    ", origVehicleLine='" + origVehicleLine + '\'' +
                    ", termVehicleLine='" + termVehicleLine + '\'' +
                    '}';
        }
    }
}

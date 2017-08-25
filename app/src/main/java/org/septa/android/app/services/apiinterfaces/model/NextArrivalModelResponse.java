package org.septa.android.app.services.apiinterfaces.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * Created by jkampf on 8/20/17.
 */

public class NextArrivalModelResponse {
    @SerializedName("start_station_id")
    private int startStationId;
    @SerializedName("dest_station_id")
    private int destStationId;
    @SerializedName("trans_type")
    private String transType;
    @SerializedName("route_id")
    private String routeId;
    @SerializedName("success")
    private boolean success;
    @SerializedName("status")
    private int status;
    @SerializedName("data")
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

    public class NextArrivalRecord {
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
        @SerializedName("vehicle_lat")
        private Double vehicle_lat;
        @SerializedName("vehicle_lon")
        private Double vehicle_lon;


        public String getOrigRouteId() {
            return origRouteId;
        }

        public String getOrigRouteName() {
            return origRouteName;
        }

        public String getTermRouteId() {
            return termRouteId;
        }

        public String getTermRouteName() {
            return termRouteName;
        }

        public Integer getConnectionStationId() {
            return connectionStationId;
        }

        public String getConnectionStationName() {
            return connectionStationName;
        }

        public String getOrigLineTripId() {
            return origLineTripId;
        }

        public String getTermLineTripId() {
            return termLineTripId;
        }

        public Date getOrigDepartureTime() {
            return origDepartureTime;
        }

        public Date getOrigArrivalTime() {
            return origArrivalTime;
        }

        public int getOrigDelayMinutes() {
            return origDelayMinutes;
        }

        public Date getTermDepartureTime() {
            return termDepartureTime;
        }

        public Date getTermArrivalTime() {
            return termArrivalTime;
        }

        public int getTermDelayMinutes() {
            return termDelayMinutes;
        }

        public int getOrigLastStopId() {
            return origLastStopId;
        }

        public String getOrigLastStopName() {
            return origLastStopName;
        }

        public int getTermLastStopId() {
            return termLastStopId;
        }

        public String getTermLastStopName() {
            return termLastStopName;
        }

        public String getOrigLineDirection() {
            return origLineDirection;
        }

        public String getTermLineDirection() {
            return termLineDirection;
        }

        public Double getVehicle_lat() {
            return vehicle_lat;
        }

        public Double getVehicle_lon() {
            return vehicle_lon;
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
                    ", vehicle_lat=" + vehicle_lat +
                    ", vehicle_lon=" + vehicle_lon +
                    '}';
        }
    }
}

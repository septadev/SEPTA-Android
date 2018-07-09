package org.septa.android.app.services.apiinterfaces.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class NextArrivalDetails {

    @SerializedName("tripid")
    private String tripId;

    @SerializedName("destination")
    private String destination;

    @SerializedName("results")
    private int results;

    @SerializedName("details")
    private Details details;

    public class NextStop {

        @SerializedName("station")
        private String station;

        @SerializedName("arrival_time")
        private String arrivalTime;

        @SerializedName("delay")
        private int late;

        public String getStation() {
            return station;
        }

        public void setStation(String station) {
            this.station = station;
        }

        public String getArrivalTime() {
            return arrivalTime;
        }

        public void setArrivalTime(String arrivalTime) {
            this.arrivalTime = arrivalTime;
        }

        public int getLate() {
            return late;
        }

        public void setLate(int late) {
            this.late = late;
        }
    }

    public class Destination {

        @SerializedName("station")
        private String station;

        @SerializedName("arrival_time")
        private String arrival_time;

        @SerializedName("delay")
        private int delay;

        public String getStation() {
            return station;
        }

        public void setStation(String station) {
            this.station = station;
        }

        public String getArrival_time() {
            return arrival_time;
        }

        public void setArrival_time(String arrival_time) {
            this.arrival_time = arrival_time;
        }

        public int getDelay() {
            return delay;
        }

        public void setDelay(int delay) {
            this.delay = delay;
        }
    }

    public class Details {

        @SerializedName("tripid")
        private String tripId;

        @SerializedName("latitude")
        private double latitude;

        @SerializedName("longitude")
        private double longitude;

        @SerializedName("line")
        private String line;

        @SerializedName("track")
        private String track;

        @SerializedName("trackChange")
        private String trackChange;

        @SerializedName("speed")
        private String speed;

        @SerializedName("vehicleid")
        private String vehicleId;

        @SerializedName("blockid")
        private String blockId;

        @SerializedName("direction")
        private String direction;

        @SerializedName("service")
        private String service;

        @SerializedName("source")
        private String source;

        @SerializedName("nextstop")
        private NextStop nextStop;

        @SerializedName("consist")
        private List<String> consist = new ArrayList<>();

        @SerializedName("destination")
        private Destination destination;

        public String getTripId() {
            return tripId;
        }

        public void setTripId(String tripId) {
            this.tripId = tripId;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public String getLine() {
            return line;
        }

        public void setLine(String line) {
            this.line = line;
        }

        public String getTrack() {
            return track;
        }

        public void setTrack(String track) {
            this.track = track;
        }

        public String getTrackChange() {
            return trackChange;
        }

        public void setTrackChange(String trackChange) {
            this.trackChange = trackChange;
        }

        public String getSpeed() {
            return speed;
        }

        public void setSpeed(String speed) {
            this.speed = speed;
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }

        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public Details getDetails() {
            return details;
        }

        public NextStop getNextStop() {
            return nextStop;
        }

        public void setNextStop(NextStop nextStop) {
            this.nextStop = nextStop;
        }

        public List<String> getConsist() {
            return consist;
        }

        public void setConsist(List<String> consist) {
            this.consist = consist;
        }

        public Destination getDestination() {
            return destination;
        }

        public void setDestination(Destination destination) {
            this.destination = destination;
        }

        public String getVehicleId() {
            return vehicleId;
        }

        public void setVehicleId(String vehicleId) {
            this.vehicleId = vehicleId;
        }

        public String getBlockId() {
            return blockId;
        }

        public void setBlockId(String blockId) {
            this.blockId = blockId;
        }
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getResults() {
        return results;
    }

    public void setResults(int results) {
        this.results = results;
    }

    public Details getDetails() {
        return details;
    }

    public void setDetails(Details details) {
        this.details = details;
    }
}
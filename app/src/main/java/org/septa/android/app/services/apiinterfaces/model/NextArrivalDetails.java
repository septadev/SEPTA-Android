package org.septa.android.app.services.apiinterfaces.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by jkampf on 10/3/17.
 */

public class NextArrivalDetails {

    @SerializedName("tripid")
    String tripId;
    @SerializedName("latitude")
    double latitiude;
    @SerializedName("longitude")
    double longitude;
    @SerializedName("line")
    String line;
    @SerializedName("vehicleid")
    String vehicleId;
    @SerializedName("blockid")
    String blockId;
    @SerializedName("track")
    String track;
    @SerializedName("trackChange")
    String trackChange;
    @SerializedName("speed")
    String speed;
    @SerializedName("direction")
    String direction;
    @SerializedName("service")
    String service;
    @SerializedName("source")
    String source;
    @SerializedName("consist")
    String[] consist;

    @SerializedName("nextstop")
    NextStop nextStop;
    @SerializedName("destination")
    Destination destination;

    public class NextStop {
        @SerializedName("station")
        String station;
        @SerializedName("arrival_time")
        String arrivalTime;
        @SerializedName("late")
        int late;

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
        String station;
        @SerializedName("arrival_time")
        String arrival_time;
        @SerializedName("late")
        int late;

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

        public int getLate() {
            return late;
        }

        public void setLate(int late) {
            this.late = late;
        }
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public double getLatitiude() {
        return latitiude;
    }

    public void setLatitiude(double latitiude) {
        this.latitiude = latitiude;
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

    public String[] getConsist() {
        return consist;
    }

    public void setConsist(String[] consist) {
        this.consist = consist;
    }

    public NextStop getNextStop() {
        return nextStop;
    }

    public void setNextStop(NextStop nextStop) {
        this.nextStop = nextStop;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }
}

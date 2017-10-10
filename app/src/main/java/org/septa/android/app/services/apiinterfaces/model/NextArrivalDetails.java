package org.septa.android.app.services.apiinterfaces.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jkampf on 10/3/17.
 */

public class NextArrivalDetails {

    @SerializedName("tripid")
    String tripId;
    @SerializedName("destination")
    String destination;
    @SerializedName("results")
    int results;
    @SerializedName("details")
    Details details;


    public class NextStop {
        @SerializedName("station")
        String station;
        @SerializedName("arrival_time")
        String arrivalTime;
        @SerializedName("delay")
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
        @SerializedName("delay")
        int delay;

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
        String tripId;
        @SerializedName("latitude")
        double latitiude;
        @SerializedName("longitude")
        double longitude;
        @SerializedName("line")
        String line;
        @SerializedName("track")
        String track;
        @SerializedName("trackChange")
        String trackChange;
        @SerializedName("speed")
        String speed;
        @SerializedName("vehicleid")
        String vehicleId;
        @SerializedName("blockid")
        String blockId;

        @SerializedName("direction")
        String direction;
        @SerializedName("service")
        String service;
        @SerializedName("source")
        String source;
        @SerializedName("nextstop")
        NextStop nextStop;
        @SerializedName("consist")
        List<String> consist = new ArrayList<String>();
        @SerializedName("destination")
        Destination destination;

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
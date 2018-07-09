package org.septa.android.app.services.apiinterfaces.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class TransitViewModelResponse implements Serializable {

    @SerializedName("routes")
    private List<Map<String, List<TransitViewRecord>>> results;

    public List<Map<String, List<TransitViewRecord>>> getResults() {
        return results;
    }

    @Override
    public String toString() {
        return "TransitViewModelResponse{" +
                "results=" + results +
                '}';
    }

    public class TransitViewRecord implements Serializable {

        @SerializedName("lat")
        private Double latitude;

        @SerializedName("lng")
        private Double longitude;

        @SerializedName("label")
        private String label;

        @SerializedName("VehicleID")
        private String vehicleId;

        @SerializedName("BlockID")
        private String blockId;

        @SerializedName("Direction")
        private String direction;

        @SerializedName("destination")
        private String destination;

        @SerializedName("Offset")
        private Integer offset;

        @SerializedName("heading")
        private Integer heading;

        @SerializedName("late")
        private Integer late;

        @SerializedName("Offset_sec")
        private Integer offsetSeconds;

        @SerializedName("trip")
        private Integer tripId;

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
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

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }

        public String getDestination() {
            return destination;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }

        public Integer getOffset() {
            return offset;
        }

        public void setOffset(Integer offset) {
            this.offset = offset;
        }

        public Integer getHeading() {
            return heading;
        }

        public void setHeading(Integer heading) {
            this.heading = heading;
        }

        public Integer getLate() {
            return late;
        }

        public void setLate(Integer late) {
            this.late = late;
        }

        public Integer getOffsetSeconds() {
            return offsetSeconds;
        }

        public void setOffsetSeconds(Integer offsetSeconds) {
            this.offsetSeconds = offsetSeconds;
        }

        public Integer getTripId() {
            return tripId;
        }

        public void setTripId(Integer tripId) {
            this.tripId = tripId;
        }

        @Override
        public String toString() {
            return "TransitViewRecord{" +
                    "latitude=" + latitude +
                    ", longitude=" + longitude +
                    ", label='" + label + '\'' +
                    ", vehicleId='" + vehicleId + '\'' +
                    ", blockId='" + blockId + '\'' +
                    ", direction='" + direction + '\'' +
                    ", destination='" + destination + '\'' +
                    ", offset=" + offset +
                    ", heading=" + heading +
                    ", late=" + late +
                    ", offsetSeconds=" + offsetSeconds +
                    ", tripId=" + tripId +
                    '}';
        }
    }
}

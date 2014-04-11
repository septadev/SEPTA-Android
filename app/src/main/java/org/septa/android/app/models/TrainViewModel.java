/*
 * TrainViewModel.java
 * Last modified on 04-11-2014 08:18-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

public class TrainViewModel {

    @SerializedName("lat") private double latitude;
    @SerializedName("lon") private double longitude;

    @SerializedName("trainno") private int trainNumber;
    @SerializedName("dest") private String destination;
    @SerializedName("nextstop") private String nextStop;
    private int late;
    @SerializedName("SOURCE") private String source;

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

    public int getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(int trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getNextStop() {
        return nextStop;
    }

    public void setNextStop(String nextStop) {
        this.nextStop = nextStop;
    }

    public int getLate() {
        return late;
    }

    public void setLate(int late) {
        this.late = late;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isLate() {
        return getLate() > 0;
    }

    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }

    public boolean isSouthBound() {
        return (trainNumber % 2 == 0);
    }

}

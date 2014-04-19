/*
 * TrainViewModel.java
 * Last modified on 04-11-2014 08:18-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models.servicemodels;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;
import org.septa.android.app.utilities.Core;

public class TrainViewModel {

    @SerializedName("lat") private double latitude;
    @SerializedName("lon") private double longitude;

    @SerializedName("trainno") private String trainNumber;
    @SerializedName("dest") private String destination;
    @SerializedName("nextstop") private String nextStop;
    @SerializedName("late") private int late;
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

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
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
        int localTrainNumber = 0;

        char lastChar = trainNumber.charAt(trainNumber.length() - 1);
        if (Core.isInteger("" + lastChar)) {
            localTrainNumber = Integer.parseInt(trainNumber);
        } else {
            String trainNumberString = trainNumber.substring(0,trainNumber.length() - 1);
            localTrainNumber = Integer.parseInt(trainNumberString);
        }

        return (localTrainNumber % 2 == 0);
    }

}

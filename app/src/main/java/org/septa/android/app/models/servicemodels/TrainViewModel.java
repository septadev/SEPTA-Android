/*
 * TrainViewModel.java
 * Last modified on 04-11-2014 08:18-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models.servicemodels;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;
import org.septa.android.app.utilities.Core;

public class TrainViewModel implements Comparable<TrainViewModel> {
    public static final String TAG = TrainViewModel.class.getName();

    @SerializedName("lat") private double latitude;
    @SerializedName("lon") private double longitude;

    @SerializedName("trainno") private String trainNumber;
    @SerializedName("dest") private String destination;
    @SerializedName("nextstop") private String nextStop;
    @SerializedName("late") private int late;
    @SerializedName("SOURCE") private String source;
    @SerializedName("TRACK") private String track;
    @SerializedName("TRACK_CHANGE") private String trackChange;

    private double distanceFromCurrentLocation;

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
            try {
                localTrainNumber = Integer.parseInt(trainNumber);
            } catch (Exception ex) {
                return false;
            }
        } else {
            String trainNumberString = trainNumber.substring(0,trainNumber.length() - 1);

            // TODO: this is a workaround until I get an answer to what a trainno can be.
            if (!Core.isInteger(trainNumberString)) {
                Log.d(TAG, "this train number could be all string");
                return false;
            }

            localTrainNumber = Integer.parseInt(trainNumberString);
        }

        return (localTrainNumber % 2 == 0);
    }

    public double getDistanceFromCurrentLocation() {
        return distanceFromCurrentLocation;
    }

    public void setDistanceFromCurrentLocation(double distanceFromCurrentLocation) {
        this.distanceFromCurrentLocation = distanceFromCurrentLocation;
    }

    // sort this model object by distance, which will default to 0 if not set.
    @Override
    public int compareTo(TrainViewModel another) {
        if (this.getDistanceFromCurrentLocation() < another.getDistanceFromCurrentLocation()) {
            return -1;
        } else {
            if (this.getDistanceFromCurrentLocation() > another.getDistanceFromCurrentLocation()) {
                return 1;
            }
        }

        return 0;
    }
}

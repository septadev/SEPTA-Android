/*
 * TripDataModel.java
 * Last modified on 05-13-2014 11:50-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models;

import android.os.Parcel;
import android.os.Parcelable;

public class TripDataModel implements Parcelable {

    private String startStopNameTitle;
    private String startStopName;

    private String destinationStopNameTitle;
    private String destinationStopName;

    private String startStopId;
    private String destinationStopId;

    private String startArrivalTime;
    private String destinationArrivalTime;

    private String tripId;
    private Integer startStopSequence;
    private Integer destinationStopSequence;

    private Integer directionId;
    private String trainNumber;

    public String getStartStopNameTitle() {
        return startStopNameTitle;
    }

    public void setStartStopNameTitle(String startStopNameTitle) {
        this.startStopNameTitle = startStopNameTitle;
    }

    public String getStartStopName() {
        return startStopName;
    }

    public void setStartStopName(String startStopName) {
        this.startStopName = startStopName;
    }

    public String getDestinationStopNameTitle() {
        return destinationStopNameTitle;
    }

    public void setDestinationStopNameTitle(String destinationStopNameTitle) {
        this.destinationStopNameTitle = destinationStopNameTitle;
    }

    public String getDestinationStopName() {
        return destinationStopName;
    }

    public void setDestinationStopName(String destinationStopName) {
        this.destinationStopName = destinationStopName;
    }

    public String getStartStopId() {
        return startStopId;
    }

    public void setStartStopId(String startStopId) {
        this.startStopId = startStopId;
    }

    public String getDestinationStopId() {
        return destinationStopId;
    }

    public void setDestinationStopId(String destinationStopId) {
        this.destinationStopId = destinationStopId;
    }

    public String getStartArrivalTime() {
        return startArrivalTime;
    }

    public void setStartArrivalTime(String startArrivalTime) {
        this.startArrivalTime = startArrivalTime;
    }

    public String getDestinationArrivalTime() {
        return destinationArrivalTime;
    }

    public void setDestinationArrivalTime(String destinationArrivalTime) {
        this.destinationArrivalTime = destinationArrivalTime;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public Integer getStartStopSequence() {
        return startStopSequence;
    }

    public void setStartStopSequence(Integer startStopSequence) {
        this.startStopSequence = startStopSequence;
    }

    public Integer getDestinationStopSequence() {
        return destinationStopSequence;
    }

    public void setDestinationStopSequence(Integer destinationStopSequence) {
        this.destinationStopSequence = destinationStopSequence;
    }

    public Integer getDirectionId() {
        return directionId;
    }

    public void setDirectionId(Integer directionId) {
        this.directionId = directionId;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public void reverseStartAndDestinationStops() {
        String tempStartStopName = getStartStopName();
        String tempStartStopId = getStartStopId();

        setStartStopId(destinationStopId);
        setStartStopName(destinationStopName);

        setDestinationStopId(tempStartStopId);
        setDestinationStopName(tempStartStopName);
    }

    public void clear() {
        startStopNameTitle = null;
        startStopName = null;

        destinationStopNameTitle = null;
        destinationStopName = null;

        startStopId = null;
        destinationStopId = null;

        startArrivalTime = null;
        destinationArrivalTime = null;

        tripId = null;
        startStopSequence = null;
        destinationStopSequence = null;

        directionId = null;
        trainNumber = null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.startStopNameTitle);
        dest.writeString(this.startStopName);
        dest.writeString(this.destinationStopNameTitle);
        dest.writeString(this.destinationStopName);
        dest.writeString(this.startStopId);
        dest.writeString(this.destinationStopId);
        dest.writeString(this.startArrivalTime);
        dest.writeString(this.destinationArrivalTime);
        dest.writeString(this.tripId);
        dest.writeValue(this.startStopSequence);
        dest.writeValue(this.destinationStopSequence);
        dest.writeValue(this.directionId);
        dest.writeString(this.trainNumber);
    }

    public TripDataModel() {
    }

    private TripDataModel(Parcel in) {
        this.startStopNameTitle = in.readString();
        this.startStopName = in.readString();
        this.destinationStopNameTitle = in.readString();
        this.destinationStopName = in.readString();
        this.startStopId = in.readString();
        this.destinationStopId = in.readString();
        this.startArrivalTime = in.readString();
        this.destinationArrivalTime = in.readString();
        this.tripId = in.readString();
        this.startStopSequence = (Integer) in.readValue(Integer.class.getClassLoader());
        this.destinationStopSequence = (Integer) in.readValue(Integer.class.getClassLoader());
        this.directionId = (Integer) in.readValue(Integer.class.getClassLoader());
        this.trainNumber = in.readString();
    }

    public static final Parcelable.Creator<TripDataModel> CREATOR = new Parcelable.Creator<TripDataModel>() {
        public TripDataModel createFromParcel(Parcel source) {
            return new TripDataModel(source);
        }

        public TripDataModel[] newArray(int size) {
            return new TripDataModel[size];
        }
    };
}

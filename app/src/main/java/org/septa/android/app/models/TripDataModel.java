/*
 * TripDataModel.java
 * Last modified on 05-13-2014 11:50-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models;

public class TripDataModel {

    private String startStopNameTitle;
    private String startStopName;
    private String endStopName;

    private Integer startStopId;
    private Integer endStopId;

    private String startArrivalTime;
    private String endArrivalTime;

    private String tripId;
    private Integer startStopSequence;
    private Integer endStopSequence;

    private Integer directionId;
    private String trainNumber;

    private boolean wheelBoardingFeature;

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

    public String getEndStopName() {
        return endStopName;
    }

    public void setEndStopName(String endStopName) {
        this.endStopName = endStopName;
    }

    public Integer getStartStopId() {
        return startStopId;
    }

    public void setStartStopId(Integer startStopId) {
        this.startStopId = startStopId;
    }

    public Integer getEndStopId() {
        return endStopId;
    }

    public void setEndStopId(Integer endStopId) {
        this.endStopId = endStopId;
    }

    public String getStartArrivalTime() {
        return startArrivalTime;
    }

    public void setStartArrivalTime(String startArrivalTime) {
        this.startArrivalTime = startArrivalTime;
    }

    public String getEndArrivalTime() {
        return endArrivalTime;
    }

    public void setEndArrivalTime(String endArrivalTime) {
        this.endArrivalTime = endArrivalTime;
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

    public Integer getEndStopSequence() {
        return endStopSequence;
    }

    public void setEndStopSequence(Integer endStopSequence) {
        this.endStopSequence = endStopSequence;
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

    public boolean hasWheelBoardingFeature() {
        return wheelBoardingFeature;
    }

    public void setWheelBoardingFeature(boolean wheelBoardingFeature) {
        this.wheelBoardingFeature = wheelBoardingFeature;
    }
}

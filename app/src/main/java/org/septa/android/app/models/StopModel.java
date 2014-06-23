/*
 * StopModel.java
 * Last modified on 06-05-2014 12:35-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models;

public class StopModel {

    private String stopId;
    private String stopName;
    private int stopSequence = 0;
    private boolean wheelchairBoarding;

    public StopModel(String stopId, String stopName, boolean wheelchairBoarding) {
        this.stopId = stopId;
        this.stopName = stopName;
        this.wheelchairBoarding = wheelchairBoarding;
    }

    public StopModel(String stopId, String stopName, int stopSequence, boolean wheelchairBoarding) {
        this(stopId, stopName, wheelchairBoarding);
        this.stopSequence = stopSequence;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public void setWheelchairBoarding(boolean wheelchairBoarding) {
        this.wheelchairBoarding = wheelchairBoarding;
    }

    public boolean hasWheelBoardingFeature() {

        return wheelchairBoarding;
    }

}



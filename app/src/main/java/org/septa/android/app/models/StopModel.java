/*
 * StopModel.java
 * Last modified on 06-05-2014 12:35-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models;

public class StopModel implements Comparable<StopModel> {

    private String stopId;
    private String stopName;
    private int stopSequence = 0;
    private boolean wheelchairBoarding;
    private double latitude;
    private double longitude;
    private float distance;
    private int directionId;

    public StopModel(String stopId, String stopName, boolean wheelchairBoarding) {
        this.stopId = stopId;
        this.setStopName(stopName);
        this.wheelchairBoarding = wheelchairBoarding;
    }

    public StopModel(String stopId, String stopName, int stopSequence, boolean wheelchairBoarding) {
        this(stopId, stopName, wheelchairBoarding);
        this.stopSequence = stopSequence;
    }

    public StopModel(String stopId, String stopName, int stopSequence, boolean wheelchairBoarding,
                     String latitude, String longitude) {
        this(stopId, stopName, wheelchairBoarding, latitude, longitude);
        this.stopSequence = stopSequence;
    }

    public StopModel(String stopId, String stopName, boolean wheelchairBoarding,
                     String latitude, String longitude) {
        this(stopId, stopName, wheelchairBoarding);
        this.latitude = Double.valueOf(latitude);
        this.longitude = Double.valueOf(longitude);
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public String getStopName() {
        if (stopName.equals("Highland Avenue")) {
            return "Highland Avenue (WIL)";
        }
        if (stopName.equals("Highland")) {
            return "Highland (NOR)";
        }
        if (stopName.equals("Norristown")) {
            return "Elm Street (NOR)";
        }
        if (stopName.equals("Main Street")) {
            return "Main Street (NOR)";
        }

        return stopName;
    }

    public void setStopName(String stopName) {
        if (stopName.equals("Highland Avenue")) {
            this.stopName = "Highland Avenue (WIL)";
        }
        if (stopName.equals("Highland")) {
            this.stopName = "Highland (NOR)";
        }
        if (stopName.equals("Norristown")) {
            this.stopName = "Elm Street (NOR)";
        }
        if (stopName.equals("Main Street")) {
            this.stopName = "Main Street (NOR)";
        }

        this.stopName = stopName;
    }

    public void setWheelchairBoarding(boolean wheelchairBoarding) {
        this.wheelchairBoarding = wheelchairBoarding;
    }

    public boolean hasWheelBoardingFeature() {

        return wheelchairBoarding;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
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

    public int getStopSequence() {
        return stopSequence;
    }

    public void setStopSequence(int stopSequence) {
        this.stopSequence = stopSequence;
    }

    public int getDirectionId() {
        return directionId;
    }

    public void setDirectionId(int directionId) {
        this.directionId = directionId;
    }

    @Override
    public int compareTo(StopModel another) {
        return this.getStopName().compareTo(another.getStopName());
    }
}



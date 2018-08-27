/*
 * StopModel.java
 * Last modified on 06-05-2014 12:35-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.domain;

import java.io.Serializable;
import java.util.Comparator;

public class StopModel implements Comparable<StopModel>, Serializable {

    private String stopId;
    private String stopName;

    private int stopSequence = 0;
    private boolean wheelchairBoarding;
    private double latitude;
    private double longitude;
    private float distance;
    private int directionId;

    public StopModel(){}

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StopModel stopModel = (StopModel) o;

        if (stopSequence != stopModel.stopSequence) {
            return false;
        }
        if (wheelchairBoarding != stopModel.wheelchairBoarding) {
            return false;
        }
        if (Double.compare(stopModel.latitude, latitude) != 0) {
            return false;
        }
        if (Double.compare(stopModel.longitude, longitude) != 0) {
            return false;
        }
        if (Float.compare(stopModel.distance, distance) != 0) {
            return false;
        }
        if (directionId != stopModel.directionId) {
            return false;
        }
        if (stopId != null ? !stopId.equals(stopModel.stopId) : stopModel.stopId != null) {
            return false;
        }
        return stopName != null ? stopName.equals(stopModel.stopName) : stopModel.stopName == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = stopId != null ? stopId.hashCode() : 0;
        result = 31 * result + (stopName != null ? stopName.hashCode() : 0);
        result = 31 * result + stopSequence;
        result = 31 * result + (wheelchairBoarding ? 1 : 0);
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (distance != +0.0f ? Float.floatToIntBits(distance) : 0);
        result = 31 * result + directionId;
        return result;
    }

    @Override
    public String toString() {
        return "StopModel{" +
                "stopId='" + stopId + '\'' +
                ", stopName='" + stopName + '\'' +
                ", stopSequence=" + stopSequence +
                ", wheelchairBoarding=" + wheelchairBoarding +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", distance=" + distance +
                ", directionId=" + directionId +
                '}';
    }

    // allows for sorting in route order
    public static class StopModelSequenceComparator implements Comparator<StopModel> {
        @Override
        public int compare(StopModel stop1, StopModel stop2) {
            return stop1.getStopSequence() - stop2.getStopSequence();
        }
    }
}



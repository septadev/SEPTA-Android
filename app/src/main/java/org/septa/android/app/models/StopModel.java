/*
 * StopModel.java
 * Last modified on 06-05-2014 12:35-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models;

import android.util.Log;

import java.util.Comparator;

public class StopModel implements Comparable<StopModel> {

    private String stopId;
    private String stopName;
    private int stopSequence = 0;
    private boolean wheelchairBoarding;

    public StopModel(String stopId, String stopName, boolean wheelchairBoarding) {
        this.stopId = stopId;
        this.setStopName(stopName);
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

    @Override
    public int compareTo(StopModel another) {
        return this.getStopName().compareTo(another.getStopName());
    }
}



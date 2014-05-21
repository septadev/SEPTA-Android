/*
 * ElevatorOutagesModel.java
 * Last modified on 05-21-2014 11:00-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models.servicemodels;

import com.google.gson.annotations.SerializedName;

public class ElevatorOutagesModel {

    @SerializedName("meta") protected ElevatorOutagesMetaModel meta;
    @SerializedName("results") protected String[] results;

    public static ElevatorOutagesModel EmptyElevatorOutagesModel() {
        ElevatorOutagesModel elevatorOutages = new ElevatorOutagesModel();
        elevatorOutages.meta = ElevatorOutagesMetaModel.EmptyElevatorOutagesMetaModel();
        elevatorOutages.results = new String[]{};

        return elevatorOutages;
    }
}

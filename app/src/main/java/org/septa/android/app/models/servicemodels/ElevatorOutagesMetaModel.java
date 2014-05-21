/*
 * ElevatorOutagesMetaModel.java
 * Last modified on 05-21-2014 10:53-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models.servicemodels;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class ElevatorOutagesMetaModel {
    public static final String TAG = ElevatorOutagesMetaModel.class.getName();

    @SerializedName("elevators_out") protected int elevatorsOut;
    @SerializedName("updated") protected Date updatedDateTime;

    public static ElevatorOutagesMetaModel EmptyElevatorOutagesMetaModel() {
        ElevatorOutagesMetaModel elevatorOutagesMetaModel = new ElevatorOutagesMetaModel();
        elevatorOutagesMetaModel.elevatorsOut = 0;
        elevatorOutagesMetaModel.updatedDateTime = new Date();

        return elevatorOutagesMetaModel;
    }

    public int getElevatorsOut() {

        return elevatorsOut;
    }

    public Date getUpdatedDateTime() {

        return updatedDateTime;
    }
}

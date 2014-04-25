/*
 * TransitViewModel.java
 * Last modified on 04-22-2014 12:48-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models.servicemodels;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class TransitViewModel {
    @SerializedName("bus")
    private List<TransitViewVehicleModel>vehicleModelList;

    public List<TransitViewVehicleModel> getVehicleModelList() {
        return vehicleModelList;
    }

    public List<TransitViewVehicleModel> getActiveVehicleModelList() {
        List<TransitViewVehicleModel> vehicleList = new ArrayList<TransitViewVehicleModel>(0);
        for (TransitViewVehicleModel vehicle : vehicleModelList) {
            if (!vehicle.getDirection().trim().isEmpty()) {
                vehicleList.add(vehicle);
            }
        }

        return vehicleList;
    }

    public void setVehicleModelList(List<TransitViewVehicleModel> vehicleModelList) {
        this.vehicleModelList = vehicleModelList;
    }
}


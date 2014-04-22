/*
 * BusRoutesModel.java
 * Last modified on 04-21-2014 16:25-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models.servicemodels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.septa.android.app.models.BusRouteModel;

public class BusRoutesModel {
    private HashMap<String, BusRouteModel> busRoutes;

    public BusRoutesModel(int size) {

        this.busRoutes = new HashMap<String, BusRouteModel>(size);
    }

    public List<BusRouteModel>getBusRouteModels() {

        ArrayList<BusRouteModel> arrayList = new ArrayList<BusRouteModel>(this.busRoutes.values());

        return arrayList;
    }

    public BusRouteModel getBusRouteByRouteId(String routeId) {

        return busRoutes.get(routeId);
    }

    public void setBusRouteByRouteId(String routeId, BusRouteModel busRoute) {

        busRoutes.put(routeId, busRoute);
    }

    public int getBusRoutesCount() {

        return busRoutes.size();
    }
}

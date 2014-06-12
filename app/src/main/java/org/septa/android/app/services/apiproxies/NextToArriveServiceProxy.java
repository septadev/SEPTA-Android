/*
 * NextToArriveServiceProxy.java
 * Last modified on 06-05-2014 22:35-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.services.apiproxies;

import org.septa.android.app.models.servicemodels.NextToArriveModel;
import org.septa.android.app.services.adaptors.NextToArriveAdaptor;
import org.septa.android.app.services.apiinterfaces.NextToArriveService;

import java.util.ArrayList;

import retrofit.Callback;

public class NextToArriveServiceProxy {
    public void getNextToArrive(String startStation, String destinationStation, String numberOfResults, Callback<ArrayList<NextToArriveModel>> callBack) {
        NextToArriveService nextToArriveService = NextToArriveAdaptor.getNextToArriveService();

        nextToArriveService.views(startStation, destinationStation, numberOfResults, callBack);
    }
}

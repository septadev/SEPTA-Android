/*
 * TrainViewServiceProxy.java
 * Last modified on 04-11-2014 08:35-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.services.apiproxies;

import org.septa.android.app.models.TrainViewModel;
import org.septa.android.app.services.adaptors.TrainViewAdaptor;
import org.septa.android.app.services.apiinterfaces.TrainViewService;

import java.util.ArrayList;

import retrofit.Callback;

public class TrainViewServiceProxy {

    public void getTrainView(Callback<ArrayList<TrainViewModel>> callBack) {
        TrainViewService trainViewService = TrainViewAdaptor.getTrainViewService();

        trainViewService.views(callBack);
    }
}
/*
 * TrainViewService.java
 * Last modified on 04-11-2014 08:29-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.services.apiinterfaces;

import org.septa.android.app.models.servicemodels.TrainViewModel;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.http.GET;

public interface TrainViewService {
    @GET("/hackathon/TrainView/")
    void views (
            Callback<ArrayList<TrainViewModel>> callback
    );
}

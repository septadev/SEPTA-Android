/*
 * TransitViewServiceProxy.java
 * Last modified on 04-22-2014 12:58-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.services.apiproxies;

import org.septa.android.app.models.servicemodels.TransitViewModel;
import org.septa.android.app.services.adaptors.TransitViewAdaptor;
import org.septa.android.app.services.apiinterfaces.TransitViewService;

import java.util.ArrayList;

import retrofit.Callback;

public class TransitViewServiceProxy {

    public void getTransitViewForRoute(String routeShortName, Callback<TransitViewModel> callBack) {
        TransitViewService transitViewService = TransitViewAdaptor.getTransitViewService();

        transitViewService.views(routeShortName, callBack);
    }
}

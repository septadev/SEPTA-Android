/*
 * AlertsServiceProxy.java
 * Last modified on 05-16-2014 20:48-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.services.apiproxies;

import org.septa.android.app.models.servicemodels.AlertModel;
import org.septa.android.app.services.adaptors.AlertsAdaptor;
import org.septa.android.app.services.apiinterfaces.AlertsService;

import java.util.ArrayList;

import retrofit.Callback;

public class AlertsServiceProxy {

    public void getAlerts(Callback<ArrayList<AlertModel>> callBack) {
        AlertsService alertsService = AlertsAdaptor.getAlertsService();

        alertsService.alerts(callBack);
    }
}
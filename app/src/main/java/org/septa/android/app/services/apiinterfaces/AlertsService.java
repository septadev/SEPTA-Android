package org.septa.android.app.services.apiinterfaces;

import org.septa.android.app.TransitType;
import org.septa.android.app.services.apiinterfaces.model.Alerts;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by jkampf on 9/21/17.
 */

public interface AlertsService {

    @GET("/prod/alerts")
    public Call<Alerts> getAlerts();



}

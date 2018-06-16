package org.septa.android.app.services.apiinterfaces;

import org.septa.android.app.services.apiinterfaces.model.Alerts;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by jkampf on 9/21/17.
 */

public interface AlertsService {

    @GET("/prod/alerts")
    Call<Alerts> getAlerts();

}

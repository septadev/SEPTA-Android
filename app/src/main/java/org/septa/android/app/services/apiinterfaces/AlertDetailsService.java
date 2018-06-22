package org.septa.android.app.services.apiinterfaces;

import org.septa.android.app.services.apiinterfaces.model.AlertDetail;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by jkampf on 9/12/17.
 */

public interface AlertDetailsService {

    @GET("/prod/alert-details")
    Call<AlertDetail> getAlertDetails(@Query("route-name") String route_id);
}

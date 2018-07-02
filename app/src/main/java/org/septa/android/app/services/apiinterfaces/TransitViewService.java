package org.septa.android.app.services.apiinterfaces;

import org.septa.android.app.services.apiinterfaces.model.TransitViewModelResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TransitViewService {

    @GET("/prod/transitviewall")
    Call<TransitViewModelResponse> getAllTransitViewResults();

    @GET("/prod/transitviewall")
    Call<TransitViewModelResponse> getTransitViewResults(@Query("routes") String routeIds);

}
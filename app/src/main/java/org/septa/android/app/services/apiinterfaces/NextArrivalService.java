package org.septa.android.app.services.apiinterfaces;

import org.septa.android.app.services.apiinterfaces.model.NextArrivalDetails;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalModelResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NextArrivalService {

    @GET("/prod/realtimearrivals")
    Call<NextArrivalModelResponse> getNextArrival(@Query("origin") int startStationId, @Query("destination") int destStationId, @Query("type") String transType, @Query("route") String routeId);

    @GET("prod/realtimearrivals/details")
    Call<NextArrivalDetails> getNextArrivalDetails(@Query("destination") String destination, @Query("route") String route, @Query("id") String id);

}
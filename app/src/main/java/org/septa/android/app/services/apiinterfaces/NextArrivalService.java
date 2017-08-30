package org.septa.android.app.services.apiinterfaces;

import org.septa.android.app.services.apiinterfaces.model.NextArrivalModelResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by jkampf on 8/20/17.
 */

public interface NextArrivalService {

    @GET("/prod/realtimearrivals")
    public Call<NextArrivalModelResponse> getNextArriaval(@Query("start_station_id") int startStationId, @Query("dest_station_id") int destStationId, @Query("type") String transType, @Query("route_id") String routeId);
}

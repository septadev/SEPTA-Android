package org.septa.android.app.services.apiinterfaces;

import org.septa.android.app.models.servicemodels.BusSchedulesModel;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.QueryMap;

public interface BusSchedulesService {
    @GET("/hackathon/BusSchedules/")
    void busSchedules (
            @QueryMap Map<String, String> options,
            Callback<BusSchedulesModel> callback
    );
}

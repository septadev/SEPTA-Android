package org.septa.android.app.services.apiinterfaces;

import org.septa.android.app.services.apiinterfaces.model.PlacePredictions;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GooglePlaceAutoCompleteService {

    @GET("maps/api/place/autocomplete/json?language=en&radius=500")
    Call<PlacePredictions> getAutoComplete(@Query("input") String input, @Query("location") String location);
}

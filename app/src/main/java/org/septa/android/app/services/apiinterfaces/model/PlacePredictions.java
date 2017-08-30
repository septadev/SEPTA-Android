package org.septa.android.app.services.apiinterfaces.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PlacePredictions {



    public ArrayList<PlaceAutoComplete> getPlaces() {
        return predictions;
    }

    public void setPlaces(ArrayList<PlaceAutoComplete> places) {
        this.predictions = places;
    }

    @SerializedName("predictions")
    private ArrayList<PlaceAutoComplete> predictions;
}

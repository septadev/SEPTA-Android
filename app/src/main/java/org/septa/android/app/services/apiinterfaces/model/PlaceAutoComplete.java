package org.septa.android.app.services.apiinterfaces.model;


import com.google.gson.annotations.SerializedName;

public class PlaceAutoComplete {

    @SerializedName("place_id")
    private String place_id;

    @SerializedName("description")
    private String description;

    public String getPlaceDesc() {
        return description;
    }

    public void setPlaceDesc(String placeDesc) {
        description = placeDesc;
    }

    public String getPlaceID() {
        return place_id;
    }

    public void setPlaceID(String placeID) {
        place_id = placeID;
    }

}

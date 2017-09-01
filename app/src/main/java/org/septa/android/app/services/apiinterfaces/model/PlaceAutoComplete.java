package org.septa.android.app.services.apiinterfaces.model;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PlaceAutoComplete implements Serializable {

    @SerializedName("place_id")
    private String place_id;

    @SerializedName("description")
    private String description;

    @SerializedName("structured_formatting")
    private StructuredFormatting structuredFormatting;

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

    public StructuredFormatting getStructuredFormatting() {
        return structuredFormatting;
    }

    public void setStructuredFormatting(StructuredFormatting structuredFormatting) {
        this.structuredFormatting = structuredFormatting;
    }

    @Override
    public String toString() {
        return description;
    }

    public class StructuredFormatting implements Serializable {

        @SerializedName("main_text")
        private String mainText;

        @SerializedName("secondary_text")
        private String secondaryText;

        public String getMainText() {
            return mainText;
        }

        public void setMainText(String mainText) {
            this.mainText = mainText;
        }

        public String getSecondaryText() {
            return secondaryText;
        }

        public void setSecondaryText(String secondaryText) {
            this.secondaryText = secondaryText;
        }
    }

}

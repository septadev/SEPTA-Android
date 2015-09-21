package org.septa.android.app.events.model;

import com.google.gson.annotations.SerializedName;

import org.septa.android.app.events.model.GsonObject;

/**
 * Created by jhunchar on 9/3/15.
 */
public class SpecialEvent extends GsonObject {

    @SerializedName("start_datetime")
    private String startDateTime;

    @SerializedName("end_datetime")
    private String endDateTime;

    @SerializedName("message")
    private String message;

    @SerializedName("url")
    private String url;

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
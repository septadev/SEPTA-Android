package org.septa.android.app.events.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by jhunchar on 9/3/15.
 */
public class Message extends GsonObject {

    @SerializedName("md5")
    private String md5;

    @SerializedName("title")
    private String title;

    @SerializedName("message")
    private String message;

    @SerializedName("effective_date")
    private String effectiveDate;

    @SerializedName("version")
    private String version;

    @SerializedName("change_log")
    private String changeLog;

    @SerializedName("special_event")
    private SpecialEvent specialEvent;

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(String effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getChangeLog() {
        return changeLog;
    }

    public void setChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }

    public SpecialEvent getSpecialEvent() {
        return specialEvent;
    }

    public void setSpecialEvent(SpecialEvent specialEvent) {
        this.specialEvent = specialEvent;
    }

    public String getSpecialEventEndDate() {

        String specialEventEndDate = null;

        SpecialEvent specialEvent = getSpecialEvent();
        if (specialEvent != null) {
            specialEventEndDate = specialEvent.getEndDateTime();
        }

        return specialEventEndDate;
    }

    public String getSpecialEventStartDate() {

        String specialEventStartDate = null;

        SpecialEvent specialEvent = getSpecialEvent();
        if (specialEvent != null) {
            specialEventStartDate = specialEvent.getStartDateTime();
        }

        return specialEventStartDate;
    }

    public String getSpecialEventMessage() {

        String specialEventMessage = null;

        SpecialEvent specialEvent = getSpecialEvent();
        if (specialEvent != null) {
            specialEventMessage = specialEvent.getMessage();
        }

        return specialEventMessage;
    }

    public String getSpecialEventUrl() {

        String specialEventUrl = null;

        SpecialEvent specialEvent = getSpecialEvent();
        if (specialEvent != null) {
            specialEventUrl= specialEvent.getUrl();
        }

        return specialEventUrl;
    }
}
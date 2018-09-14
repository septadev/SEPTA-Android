package org.septa.android.app.services.apiinterfaces.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Arrays;

public class TimeSlot implements Serializable {

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("end_time")
    private String endTime;

    @SerializedName("subscribed_days")
    private int[] subscribedDays;

    public TimeSlot() {
    }

    public TimeSlot(String startTime, String endTime, int[] subscribedDays) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.subscribedDays = subscribedDays;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int[] getSubscribedDays() {
        return subscribedDays;
    }

    public void setSubscribedDays(int[] subscribedDays) {
        this.subscribedDays = subscribedDays;
    }

    @Override
    public String toString() {
        return "TimeSlot{" +
                "startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", subscribedDays=" + Arrays.toString(subscribedDays) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeSlot timeSlot = (TimeSlot) o;

        if (!startTime.equals(timeSlot.startTime)) return false;
        if (!endTime.equals(timeSlot.endTime)) return false;
        return Arrays.equals(subscribedDays, timeSlot.subscribedDays);
    }

    @Override
    public int hashCode() {
        int result = startTime.hashCode();
        result = 31 * result + endTime.hashCode();
        result = 31 * result + Arrays.hashCode(subscribedDays);
        return result;
    }
}
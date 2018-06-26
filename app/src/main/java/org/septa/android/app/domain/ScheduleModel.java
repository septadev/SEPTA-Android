package org.septa.android.app.domain;

import android.support.annotation.NonNull;

import org.septa.android.app.support.GeneralUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ScheduleModel implements Comparable<ScheduleModel> {
    Date departureDate;
    Date arrivalDate;
    String blockId;


    public ScheduleModel(String blockId, int arrivalTime, int departureTime) {
        this.blockId = blockId;

        Calendar departure = Calendar.getInstance();
        departure.setTimeInMillis(0);
        departure.set(Calendar.HOUR_OF_DAY, departureTime / 100);
        departure.set(Calendar.MINUTE, departureTime % 100);
        departureDate = departure.getTime();

        Calendar arrival = Calendar.getInstance();
        arrival.setTimeInMillis(0);
        arrival.set(Calendar.HOUR_OF_DAY, arrivalTime / 100);
        arrival.set(Calendar.MINUTE, arrivalTime % 100);
        arrivalDate = arrival.getTime();
    }


    @Override
    public int compareTo(@NonNull ScheduleModel o) {
        int arrivalCompare = arrivalDate.compareTo(o.arrivalDate);
        if (arrivalCompare != 0)
            return arrivalCompare;

        int departureCompare = departureDate.compareTo(o.departureDate);
        if (departureCompare != 0)
            return departureCompare;

        return blockId.compareTo(o.blockId);
    }

    public String getDurationAsString() {
        return GeneralUtils.getDurationAsString(arrivalDate.getTime() - departureDate.getTime(), TimeUnit.MILLISECONDS);
    }

    public String getDurationAsLongString() {
        return GeneralUtils.getDurationAsLongString(arrivalDate.getTime() - departureDate.getTime(), TimeUnit.MILLISECONDS);
    }

    public Date getArrivalDate() {
        return arrivalDate;
    }

    public String getBlockId() {
        return blockId;
    }

    public Date getDepartureDate() {
        return departureDate;
    }
}

package org.septa.android.app.nextarrive;

import org.septa.android.app.services.apiinterfaces.model.NextArrivalModelResponse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jkampf on 9/8/17.
 */

public class NextToArriveLine {
    List<NextArrivalModelResponse.NextArrivalRecord> nextToArriveModels = new ArrayList<NextArrivalModelResponse.NextArrivalRecord>();
    public String lineName;
    Date soonestDeparture;
    boolean multiStop;

    NextToArriveLine(String lineName, boolean multiStop) {
        this.lineName = lineName;
        this.multiStop = multiStop;
    }

    public List<NextArrivalModelResponse.NextArrivalRecord> getList() {
        return nextToArriveModels;
    }

    void addItem(NextArrivalModelResponse.NextArrivalRecord item) {
        nextToArriveModels.add(item);
        if (soonestDeparture != null) {
            if (item.getOrigDepartureTime().getTime() + (item.getOrigDelayMinutes() * 60000) > soonestDeparture.getTime())
                return;
        }

        soonestDeparture = new Date(item.getOrigDepartureTime().getTime() + (item.getOrigDelayMinutes() * 60000));
    }

    public Date getSoonestDeparture() {
        return soonestDeparture;
    }

    public boolean isMultiStop() {
        return multiStop;
    }

    @Override
    public int hashCode() {
        if (lineName == null)
            return 0;

        return lineName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null && lineName == null)
            return true;
        if (lineName != null)
            return lineName.equals(obj);

        return false;
    }


}


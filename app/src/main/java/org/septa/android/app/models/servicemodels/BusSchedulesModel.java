package org.septa.android.app.models.servicemodels;

import java.util.ArrayList;
import java.util.List;

public class BusSchedulesModel {
//    @SerializedName("bus")

    public List<BusScheduleModel> busScheduleList;

    public BusSchedulesModel() {
        busScheduleList = new ArrayList<BusScheduleModel>();
    }



    public void addBusScheduleModel(BusScheduleModel busScheduleModel) {

        this.busScheduleList.add(busScheduleModel);
    }

    public List<BusScheduleModel> getBusScheduleList() {
        return busScheduleList;
    }
}

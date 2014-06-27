package org.septa.android.app.services.apiproxies;

import org.septa.android.app.models.servicemodels.BusSchedulesModel;
import org.septa.android.app.services.adaptors.BusSchedulesAdaptor;
import org.septa.android.app.services.apiinterfaces.BusSchedulesService;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;

public class BusSchedulesServiceProxy {

    public void getBusSchedules(int stopId, String routeShortName, String directionLetter, int numberOfResults, Callback<BusSchedulesModel> callBack) {
        BusSchedulesService busSchedulesService = BusSchedulesAdaptor.getBusSchedulesService(routeShortName);

        Map<String, String> options = new HashMap<String, String>();
        options.put("req1", String.valueOf(stopId));
        options.put("req2", routeShortName);
        if (directionLetter != null) {
            options.put("req3", directionLetter);
        }
        options.put("req6", String.valueOf(numberOfResults));

        busSchedulesService.busSchedules(options, callBack);
    }
}

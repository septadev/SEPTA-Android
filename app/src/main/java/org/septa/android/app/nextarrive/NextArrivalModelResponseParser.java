package org.septa.android.app.nextarrive;

import com.google.android.gms.maps.model.LatLng;

import org.septa.android.app.services.apiinterfaces.model.NextArrivalModelResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jkampf on 9/8/17.
 */

public class NextArrivalModelResponseParser {

    public static final int SINGLE_STOP_TRIP = 0;
    public static final int MULTIPLE_STOP_TRIP = 1;
    public static final int BOTH = 2;


    private Map<LatLng, NextArrivalModelResponse.NextArrivalRecord> latLngMap = new HashMap<LatLng, NextArrivalModelResponse.NextArrivalRecord>();
    private Set<String> routeIdSet = new HashSet<String>();
    private int tripType;
    Map<String, NextToArriveLine> map = new HashMap<String, NextToArriveLine>();
    NextArrivalModelResponse response;

    public NextArrivalModelResponseParser(NextArrivalModelResponse response) {
        this.response = response;
        int multiStopCount = 0;
        int singleStopCount = 0;

        for (NextArrivalModelResponse.NextArrivalRecord data : response.getNextArrivalRecords()) {
            String key;
            if (data.getOrigRouteId().equals(data.getTermRouteId())) {
                key = data.getOrigRouteId();
                singleStopCount++;
            } else {
                key = data.getOrigRouteId() + "." + data.getTermRouteId();
                multiStopCount++;
            }

            if (!map.containsKey(key)) {
                map.put(key, new NextToArriveLine(data.getOrigRouteName(), (data.getOrigRouteId() != data.getTermRouteId())));
            }

            map.get(key).addItem(data);

            if (data.getOrigVehicleLat() != null && data.getOrigVehicleLon() != null) {
                LatLng vehicleLatLng = new LatLng(data.getOrigVehicleLat(), data.getOrigVehicleLon());
                latLngMap.put(vehicleLatLng, data);
            }

            if (!routeIdSet.contains(data.getOrigRouteId())) {
                routeIdSet.add(data.getOrigRouteId());
            }

            if (data.getConnectionStationId() != null)
                if (!routeIdSet.contains(data.getTermRouteId())) {
                    routeIdSet.add(data.getTermRouteId());
                }
        }

        if (multiStopCount > 0 && singleStopCount > 0)
            tripType = BOTH;
        else if (multiStopCount == 0)
            tripType = SINGLE_STOP_TRIP;
        else tripType = MULTIPLE_STOP_TRIP;

    }

    public Map<LatLng, NextArrivalModelResponse.NextArrivalRecord> getLatLngMap() {
        return latLngMap;
    }

    public Set<String> getRouteIdSet() {
        return routeIdSet;
    }

    public int getTripType() {
        return tripType;
    }

    public List<NextToArriveLine> getNextToArriveLineList() {
        ArrayList<NextToArriveLine> list = new ArrayList<NextToArriveLine>(map.size());
        list.addAll(map.values());

        return list;
    }

    public List<NextArrivalModelResponse.NextArrivalRecord> getNextArrivalRecordList() {
        return response.getNextArrivalRecords();
    }

}

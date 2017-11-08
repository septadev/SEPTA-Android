package org.septa.android.app.nextarrive;

import com.google.android.gms.maps.model.LatLng;

import org.septa.android.app.services.apiinterfaces.model.NextArrivalModelResponse;

import java.util.Collections;
import java.util.Comparator;
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


    private Map<LatLng, NextArrivalModelResponse.NextArrivalRecord> origLatLngMap = new HashMap<LatLng, NextArrivalModelResponse.NextArrivalRecord>();
    private Map<LatLng, NextArrivalModelResponse.NextArrivalRecord> termLatLngMap = new HashMap<LatLng, NextArrivalModelResponse.NextArrivalRecord>();

    private Set<String> routeIdSet = new HashSet<String>();
    List<NextArrivalModelResponse.NextArrivalRecord> results;

    public NextArrivalModelResponseParser(NextArrivalModelResponse response) {
        results = response.getNextArrivalRecords();

        Collections.sort(results, new Comparator<NextArrivalModelResponse.NextArrivalRecord>() {
            @Override
            public int compare(NextArrivalModelResponse.NextArrivalRecord o1, NextArrivalModelResponse.NextArrivalRecord o2) {
                int result = o1.getOrigDepartureTime().compareTo(o2.getOrigDepartureTime());
                if (result != 0) return result;
                result = o1.getOrigArrivalTime().compareTo(o2.getOrigArrivalTime());
                return result;
            }
        });

        for (NextArrivalModelResponse.NextArrivalRecord data : results) {
            if (data.getOrigVehicleLat() != null && data.getOrigVehicleLon() != null) {
                LatLng vehicleLatLng = new LatLng(data.getOrigVehicleLat(), data.getOrigVehicleLon());
                origLatLngMap.put(vehicleLatLng, data);
            }

            if (data.getTermVehicleLat() != null && data.getTermVehicleLon() != null) {
                LatLng vehicleLatLng = new LatLng(data.getTermVehicleLat(), data.getTermVehicleLon());
                termLatLngMap.put(vehicleLatLng, data);
            }

            if (!routeIdSet.contains(data.getOrigRouteId())) {
                routeIdSet.add(data.getOrigRouteId());
            }

            if (data.getConnectionStationId() != null) {
                if (!routeIdSet.contains(data.getTermRouteId())) {
                    routeIdSet.add(data.getTermRouteId());
                }
            }
        }


    }

    public Map<LatLng, NextArrivalModelResponse.NextArrivalRecord> getOrigLatLngMap() {
        return origLatLngMap;
    }

    public Map<LatLng, NextArrivalModelResponse.NextArrivalRecord> getTermLatLngMap() {
        return termLatLngMap;
    }

    public Set<String> getRouteIdSet() {
        return routeIdSet;
    }


    public List<NextArrivalModelResponse.NextArrivalRecord> getResults() {
        return results;
    }
}

package org.septa.android.app.transitview;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import org.septa.android.app.services.apiinterfaces.model.TransitViewModelResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransitViewModelResponseParser {

    private Map<String, Map<TransitViewModelResponse.TransitViewRecord, LatLng>> results;

    public TransitViewModelResponseParser(@NonNull TransitViewModelResponse response) {
        results = new HashMap<>();

        Map<String, List<TransitViewModelResponse.TransitViewRecord>> routesMap = response.getResults().get(0);

        for (String routeId : routesMap.keySet()) {
            List<TransitViewModelResponse.TransitViewRecord> vehiclesOnRoute = routesMap.get(routeId);

            Map<TransitViewModelResponse.TransitViewRecord, LatLng> vehicleCoordinatesOnRoute = new HashMap<>();

            for (TransitViewModelResponse.TransitViewRecord data : vehiclesOnRoute) {
                if (data.getLatitude() != null && data.getLongitude() != null) {
                    LatLng vehicleLatLng = new LatLng(data.getLatitude(), data.getLongitude());
                    vehicleCoordinatesOnRoute.put(data, vehicleLatLng);
                }
            }

            results.put(routeId, vehicleCoordinatesOnRoute);
        }
    }

    public Map<String, Map<TransitViewModelResponse.TransitViewRecord, LatLng>> getResults() {
        return results;
    }

    public Map<TransitViewModelResponse.TransitViewRecord, LatLng> getResultsForRoute(String routeId) {
        return results.get(routeId);
    }
}

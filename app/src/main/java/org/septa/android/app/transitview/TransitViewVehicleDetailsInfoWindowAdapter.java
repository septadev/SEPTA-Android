package org.septa.android.app.transitview;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.septa.android.app.R;
import org.septa.android.app.services.apiinterfaces.model.TransitViewModelResponse;
import org.septa.android.app.support.CrashlyticsManager;
import org.septa.android.app.support.GeneralUtils;
import org.septa.android.app.view.TextView;

import java.util.concurrent.TimeUnit;

import static org.septa.android.app.transitview.TransitViewUtils.isTrolley;

public class TransitViewVehicleDetailsInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private static final String TAG = TransitViewVehicleDetailsInfoWindowAdapter.class.getSimpleName();

    private Activity context;
    private TransitViewVehicleDetailsInfoWindowAdapterListener mListener;

    private String[] vehicleMarkerKey;
    private String routeId;

    TransitViewVehicleDetailsInfoWindowAdapter(Activity context) {
        this.context = context;
        if (context instanceof TransitViewVehicleDetailsInfoWindowAdapterListener) {
            mListener = (TransitViewVehicleDetailsInfoWindowAdapterListener) context;
        } else {
            IllegalArgumentException iae = new IllegalArgumentException("Context Must Implement TransitViewVehicleDetailsInfoWindowAdapterListener");
            CrashlyticsManager.log(Log.ERROR, TAG, iae.toString());
            throw iae;
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        vehicleMarkerKey = marker.getTitle().split(TransitViewResultsActivity.VEHICLE_MARKER_KEY_DELIM);
        routeId = vehicleMarkerKey[0];

        String activeRouteId = mListener.getActiveRouteId();

        // if vehicle is on inactive route, activate that route
        if (!activeRouteId.equalsIgnoreCase(routeId)) {
            mListener.changeActiveRoute(activeRouteId, routeId);
        }

        // returning null will call getInfoContents
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        // create custom info window with vehicle details
        View contents = context.getLayoutInflater().inflate(R.layout.vehicle_map_details, null);
        render(marker, contents);

        return contents;
    }

    private void render(Marker marker, View contents) {
        // dynamic vehicle details
        TextView markerTransitType = contents.findViewById(R.id.vehicle_details_transit_type);
        TextView markerRouteId = contents.findViewById(R.id.vehicle_details_route_id);
        TextView direction = contents.findViewById(R.id.vehicle_details_direction);
        TextView blockId = contents.findViewById(R.id.vehicle_details_block_id);
        TextView vehicleNumber = contents.findViewById(R.id.vehicle_details_vehicle_number);
        TextView status = contents.findViewById(R.id.vehicle_details_status);
        TextView numberRailCars = contents.findViewById(R.id.vehicle_details_rail_number_cars);

        // static headings
        TextView blockIdHeading = contents.findViewById(R.id.vehicle_details_block_id_heading);
        TextView vehicleNumberHeading = contents.findViewById(R.id.vehicle_details_vehicle_number_heading);
        TextView statusHeading = contents.findViewById(R.id.vehicle_details_status_heading);
        TextView numberRailCarsHeading = contents.findViewById(R.id.vehicle_details_rail_number_cars_heading);
        
        // look up vehicle details
        TransitViewModelResponse.TransitViewRecord vehicleRecord = mListener.getVehicleRecord(marker.getTitle());

        // hide unused fields
        numberRailCars.setVisibility(View.GONE);
        numberRailCarsHeading.setVisibility(View.GONE);

        // transit type and route id
        if (isTrolley(routeId)) {
            markerTransitType.setText(context.getString(R.string.heading_trolley));
        } else {
            markerTransitType.setText(context.getString(R.string.heading_bus));
        }
        markerRouteId.setText(routeId);

        // add vehicle details to info window
        if (vehicleRecord != null) {

            // direction, block id, vehicle number
            direction.setText(context.getString(R.string.heading_destination, vehicleRecord.getDestination()));
            blockId.setText(vehicleRecord.getBlockId());
            vehicleNumber.setText(vehicleRecord.getVehicleId());

            // status
            if (vehicleRecord.getLate() != null && vehicleRecord.getLate() > 0) {
                status.setText(context.getString(R.string.heading_status_delay, GeneralUtils.getDurationAsLongString(vehicleRecord.getLate(), TimeUnit.MINUTES)));
            } else {
                status.setText(context.getString(R.string.nta_on_time));
            }

        } else {
            Log.e(TAG, "Could not find vehicle with marker ID: " + marker.getTitle());
            direction.setVisibility(View.GONE);
            blockId.setVisibility(View.GONE);
            blockIdHeading.setVisibility(View.GONE);
            vehicleNumber.setVisibility(View.GONE);
            vehicleNumberHeading.setVisibility(View.GONE);
            status.setVisibility(View.GONE);
            statusHeading.setVisibility(View.GONE);
        }
    }

    public interface TransitViewVehicleDetailsInfoWindowAdapterListener {
        TransitViewModelResponse.TransitViewRecord getVehicleRecord(String vehicleRecordKey);

        String getActiveRouteId();

        void changeActiveRoute(String oldActiveRoute, String newActiveRoute);
    }

}
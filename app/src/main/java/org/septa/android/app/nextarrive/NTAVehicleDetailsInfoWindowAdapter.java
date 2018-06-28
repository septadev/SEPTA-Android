package org.septa.android.app.nextarrive;

import android.app.Activity;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalDetails;
import org.septa.android.app.support.GeneralUtils;
import org.septa.android.app.view.TextView;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NTAVehicleDetailsInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Activity context;
    private MarkerOptions startMarker, destMarker;
    private TransitType transitType;
    private Map<String, NextArrivalDetails> details;

    NTAVehicleDetailsInfoWindowAdapter(Activity context, TransitType transitType, MarkerOptions startMarker, MarkerOptions destMarker, Map<String, NextArrivalDetails> details) {
        this.context = context;
        this.startMarker = startMarker;
        this.destMarker = destMarker;
        this.transitType = transitType;
        this.details = details;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        // returning null will call getInfoContents
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        if (startMarker.getTitle().equals(marker.getTitle()) || destMarker.getTitle().equals(marker.getTitle())) {
            // use default info window
            return null;
        }

        // create custom info window with vehicle details
        View contents = context.getLayoutInflater().inflate(R.layout.vehicle_map_details, null);
        render(marker, contents);

        return contents;
    }

    private void render(Marker marker, View contents) {
        // dynamic vehicle details
        TextView markerTransitType = contents.findViewById(R.id.vehicle_details_transit_type);
        TextView routeId = contents.findViewById(R.id.vehicle_details_route_id);
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

        NextArrivalDetails detail = details.get(marker.getTitle());

        if (transitType == TransitType.RAIL) {
            // rail vehicle details
            direction.setVisibility(View.GONE);
            blockId.setVisibility(View.GONE);
            blockIdHeading.setVisibility(View.GONE);
            vehicleNumber.setVisibility(View.GONE);
            vehicleNumberHeading.setVisibility(View.GONE);

            // train #
            markerTransitType.setText(context.getString(R.string.heading_rail));
            routeId.setText(marker.getTitle());

            if (detail != null && detail.getDetails() != null) {
                // vehicle status
                if (detail.getDetails().getNextStop() != null && detail.getDetails().getNextStop().getLate() > 0) {
                    status.setText(context.getString(R.string.heading_status_delay, GeneralUtils.getDurationAsLongString(detail.getDetails().getDestination().getDelay(), TimeUnit.MINUTES)));
                } else {
                    status.setText(context.getString(R.string.nta_on_time));
                }

                // # of train cars
                List<String> consist = detail.getDetails().getConsist();
                if (consist != null) {
                    int numTrainCars = consist.size();
                    if (numTrainCars > 0) {
                        if (!(numTrainCars == 1 && consist.get(0).trim().isEmpty())) {
                            numberRailCars.setText(String.valueOf(numTrainCars));
                        }
                    }
                } else {
                    numberRailCars.setVisibility(View.GONE);
                    numberRailCarsHeading.setVisibility(View.GONE);
                }
            } else {
                status.setVisibility(View.GONE);
                statusHeading.setVisibility(View.GONE);
                numberRailCars.setVisibility(View.GONE);
                numberRailCarsHeading.setVisibility(View.GONE);
            }
        } else {
            // bus or trolley vehicle details
            markerTransitType.setVisibility(View.GONE);
            routeId.setVisibility(View.GONE);
            direction.setVisibility(View.GONE);
            numberRailCars.setVisibility(View.GONE);
            numberRailCarsHeading.setVisibility(View.GONE);

            // block ID
            blockId.setText(marker.getTitle());

            if (detail != null && detail.getDetails() != null) {
                // vehicle number
                vehicleNumber.setText(detail.getDetails().getVehicleId());

                // status
                if (detail.getDetails().getDestination() != null && detail.getDetails().getDestination().getDelay() > 0) {
                    status.setText(context.getString(R.string.heading_status_delay, GeneralUtils.getDurationAsLongString(detail.getDetails().getDestination().getDelay(), TimeUnit.MINUTES)));
                } else {
                    status.setText(context.getString(R.string.nta_on_time));
                }
            } else {
                vehicleNumber.setVisibility(View.GONE);
                vehicleNumberHeading.setVisibility(View.GONE);
                status.setVisibility(View.GONE);
                statusHeading.setVisibility(View.GONE);
            }
        }
    }

}
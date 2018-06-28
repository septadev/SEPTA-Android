package org.septa.android.app.nextarrive;

import android.app.Activity;
import android.content.Context;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalDetails;
import org.septa.android.app.support.GeneralUtils;
import org.septa.android.app.view.TextView;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class VehicleDetailsInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Activity context;
    private MarkerOptions startMarker, destMarker;
    private TransitType transitType;
    private Map<String, NextArrivalDetails> details;

    private static final String HTML_NEW_LINE = "<br/>";

    VehicleDetailsInfoWindowAdapter(Activity context, TransitType transitType, MarkerOptions startMarker, MarkerOptions destMarker, Map<String, NextArrivalDetails> details) {
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
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        LayoutInflater inflater = LayoutInflater.from(context);
        View contents = inflater.inflate(R.layout.vehicle_map_details, null);
        render(marker, contents);

        return contents;
    }

    private void render(Marker marker, View contents) {
        // TODO: bold info window headings

        TextView infoWindowText = contents.findViewById(R.id.vehicle_map_details);
        NextArrivalDetails detail = details.get(marker.getTitle());

        // rail vehicle details
        if (transitType == TransitType.RAIL) {
            SpannableStringBuilder builder = new SpannableStringBuilder(context.getString(R.string.heading_rail, marker.getTitle()));
            if (detail != null && detail.getDetails() != null) {

                // vehicle status
                builder.append(HTML_NEW_LINE);
                if (detail.getDetails().getNextStop() != null && detail.getDetails().getNextStop().getLate() > 0) {
                    builder.append(context.getString(R.string.heading_status_delay, GeneralUtils.getDurationAsLongString(detail.getDetails().getDestination().getDelay(), TimeUnit.MINUTES)));
                } else {
                    builder.append(context.getString(R.string.heading_status_on_time));
                }

                // # of train cars
                if (detail.getDetails().getConsist() != null) {
                    int numTrainCars = detail.getDetails().getConsist().size();
                    if (numTrainCars > 0) {
                        if (!(numTrainCars == 1 && detail.getDetails().getConsist().get(0).trim().isEmpty())) {
                            builder.append(HTML_NEW_LINE).append(context.getString(R.string.heading_rail_number_cars, numTrainCars));
                        }
                    }
                }
            }
            infoWindowText.setHtml(builder.toString());

        } else {
            // bus or trolley vehicle details

            // block ID
            SpannableStringBuilder builder = new SpannableStringBuilder(context.getString(R.string.heading_block_id, marker.getTitle()));

            if (detail != null && detail.getDetails() != null) {
                // vehicle number
                builder.append(HTML_NEW_LINE).append(context.getString(R.string.heading_vehicle_number, detail.getDetails().getVehicleId()));

                // status
                builder.append(HTML_NEW_LINE);
                if (detail.getDetails().getDestination() != null && detail.getDetails().getDestination().getDelay() > 0) {
                    builder.append(context.getString(R.string.heading_status_delay, GeneralUtils.getDurationAsLongString(detail.getDetails().getDestination().getDelay(), TimeUnit.MINUTES)));
                } else {
                    builder.append(context.getString(R.string.heading_status_on_time));
                }

            }

            infoWindowText.setHtml(builder.toString());
        }
    }

}
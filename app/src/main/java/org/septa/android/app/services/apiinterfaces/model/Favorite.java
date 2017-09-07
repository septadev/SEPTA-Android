package org.septa.android.app.services.apiinterfaces.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import org.septa.android.app.TransitType;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.domain.StopModel;

import java.io.Serializable;

/**
 * Created by jkampf on 9/6/17.
 */

public class Favorite implements Serializable {
    @SerializedName("name")
    private String name;

    @SerializedName("start_id")
    private String startId;
    @SerializedName("destination_id")
    private String destinationId;
    @SerializedName("transit_type")
    private TransitType transitType;
    @SerializedName("line_id")
    private String lineId;
    @SerializedName("direction_code")
    private String directionCode;


    public Favorite(@NonNull StopModel start, @NonNull StopModel destination, @NonNull TransitType transitType, @Nullable RouteDirectionModel routeDirectionModel) {
        this.startId = start.getStopId();
        this.destinationId = destination.getStopId();
        this.transitType = transitType;
        if (routeDirectionModel != null) {
            this.lineId = routeDirectionModel.getRouteId();
            this.directionCode = routeDirectionModel.getDirectionCode();
        }

        name = "default value here:" + startId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartId() {
        return startId;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public TransitType getTransitType() {
        return transitType;
    }

    public String getLineId() {
        return lineId;
    }

    public String getKey() {
        return generateKey(transitType, startId, destinationId, lineId, directionCode);
    }

    public static String generateKey(TransitType transitType, String startId, String destinationId, String lineId, String directionCode) {
        return transitType.name() + "_" + startId + "_" + destinationId + "_" + lineId + "_" + directionCode;
    }

    public static String generateKey(@NonNull StopModel start, @NonNull StopModel destination, @NonNull TransitType transitType, @Nullable RouteDirectionModel routeDirectionModel) {
        String startId = start.getStopId();
        String destinationId = destination.getStopId();
        String lineId = null;
        String directionCode = null;
        if (routeDirectionModel != null) {
            lineId = routeDirectionModel.getRouteId();
            directionCode = routeDirectionModel.getDirectionCode();
        }

        return generateKey(transitType, startId, destinationId, lineId, directionCode);
    }
}


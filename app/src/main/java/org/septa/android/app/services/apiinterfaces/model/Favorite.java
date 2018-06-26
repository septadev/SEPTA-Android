package org.septa.android.app.services.apiinterfaces.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import org.septa.android.app.BuildConfig;
import org.septa.android.app.TransitType;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.domain.StopModel;

import java.io.Serializable;

public class Favorite implements Serializable {
    @SerializedName("name")
    private String name;

    @SerializedName("start")
    private StopModel start;
    @SerializedName("destination")
    private StopModel destination;
    @SerializedName("route")
    RouteDirectionModel routeDirectionModel;
    @SerializedName("transit_type")
    private TransitType transitType;

    @SerializedName("created_with_version")
    private int createdWithVersion = 0;


    public Favorite(@NonNull StopModel start, @NonNull StopModel destination, @NonNull TransitType transitType, @Nullable RouteDirectionModel routeDirectionModel) {
        this.start = start;
        this.destination = destination;
        this.transitType = transitType;
        this.routeDirectionModel = routeDirectionModel;

        this.transitType = transitType;
        if (routeDirectionModel != null) {
            name = routeDirectionModel.getRouteShortName() + " to " + destination.getStopName();
        } else {
            name = "To " + destination.getStopName();
        }

        createdWithVersion = BuildConfig.VERSION_CODE;
    }

    public String getKey() {
        return generateKey(start, destination, transitType, routeDirectionModel);
    }

    public static String generateKey(TransitType transitType, String startId, String destinationId, String lineId, String directionCode) {
        return transitType.name() + "_" + startId + "_" + destinationId + "_" + lineId + "_" + directionCode;
    }

    public static String generateKey(@NonNull StopModel start, @NonNull StopModel destination, @NonNull TransitType transitType, @Nullable RouteDirectionModel routeDirectionModel) {
        String startId = start.getStopId();
        String destinationId = destination.getStopId();
        String lineId = null;
        String directionCode = null;
        if (transitType != TransitType.RAIL && routeDirectionModel != null) {
            lineId = routeDirectionModel.getRouteId();
            directionCode = routeDirectionModel.getDirectionCode();
        }

        return generateKey(transitType, startId, destinationId, lineId, directionCode);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StopModel getStart() {
        return start;
    }

    public StopModel getDestination() {
        return destination;
    }

    public TransitType getTransitType() {
        return transitType;
    }

    public RouteDirectionModel getRouteDirectionModel() {
        return routeDirectionModel;
    }

    public int getCreatedWithVersion() {
        return createdWithVersion;
    }

     @Override
    public String toString() {

        StringBuilder builder = new StringBuilder("Favorite{name='").append(name).append('\'');

        builder.append(", start=");
        if (start != null)
            builder.append(start.toString());
        else builder.append("NULL");

        builder.append(", destination=");
        if (destination != null)
            builder.append(destination.toString());
        else builder.append("NULL");

        builder.append(", routeDirectionModel=");
        if (routeDirectionModel != null)
            builder.append(routeDirectionModel.toString());
        else builder.append("NULL");

        builder.append(", transitType=");
        if (transitType != null)
            builder.append(transitType.toString());
        else builder.append("NULL");

        builder.append('}');

        return builder.toString();

    }
}


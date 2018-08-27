package org.septa.android.app.services.apiinterfaces.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import org.septa.android.app.BuildConfig;
import org.septa.android.app.TransitType;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.domain.StopModel;

public class NextArrivalFavorite extends Favorite {

    @SerializedName("start")
    private StopModel start;

    @SerializedName("destination")
    private StopModel destination;

    @SerializedName("route")
    private RouteDirectionModel routeDirectionModel;

    @SerializedName("transit_type")
    private TransitType transitType;

    public NextArrivalFavorite(@NonNull StopModel start, @NonNull StopModel destination, @NonNull TransitType transitType, @Nullable RouteDirectionModel routeDirectionModel) {
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

    @Override
    public String getKey() {
        return generateKey(start, destination, transitType, routeDirectionModel);
    }

    private static String generateKey(TransitType transitType, String startId, String destinationId, String lineId, String directionCode) {
        StringBuilder favoriteKey = new StringBuilder(transitType.name());
        favoriteKey.append(FAVORITE_KEY_DELIM).append(startId)
                .append(FAVORITE_KEY_DELIM).append(destinationId)
                .append(FAVORITE_KEY_DELIM).append(lineId)
                .append(FAVORITE_KEY_DELIM).append(directionCode);
        return favoriteKey.toString();
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

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder("NextArrivalFavorite{name='").append(name).append('\'');

        builder.append(", start=");
        if (start != null) {
            builder.append(start.toString());
        } else {
            builder.append("NULL");
        }

        builder.append(", destination=");
        if (destination != null) {
            builder.append(destination.toString());
        } else {
            builder.append("NULL");
        }

        builder.append(", routeDirectionModel=");
        if (routeDirectionModel != null) {
            builder.append(routeDirectionModel.toString());
        } else {
            builder.append("NULL");
        }

        builder.append(", transitType=");
        if (transitType != null) {
            builder.append(transitType.toString());
        } else {
            builder.append("NULL");
        }

        builder.append('}');

        return builder.toString();

    }
}


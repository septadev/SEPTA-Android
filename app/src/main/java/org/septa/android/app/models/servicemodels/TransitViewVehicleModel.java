/*
 * TransitViewVehicleModel.java
 * Last modified on 04-22-2014 15:33-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models.servicemodels;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import org.septa.android.app.utilities.Core;

public class TransitViewVehicleModel {
    @SerializedName("BlockID")
    private int blockId;
    @SerializedName("Direction")
    private String direction;
    @SerializedName("Offset")
    private int offset;
    @SerializedName("VehicleID")
    private int vehicleId;
    @SerializedName("destination")
    private String destination;

    @SerializedName("label")
    private String label;
    @SerializedName("lat")
    private double latitude;
    @SerializedName("lng")
    private double longitude;

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LatLng getLatLng() {
        return new LatLng(getLatitude(), getLongitude());
    }

    public boolean isSouthBound() {

        return (vehicleId % 2 == 0);
    }
}

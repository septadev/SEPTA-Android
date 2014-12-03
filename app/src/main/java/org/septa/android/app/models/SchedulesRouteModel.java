package org.septa.android.app.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.lang.String;

public class SchedulesRouteModel implements Comparable<SchedulesRouteModel>,Parcelable {
    private int routeType;
    private String routeId;
    private String routeShortName;
    private String routeLongName;
    private String routeStart;
    private String routeEnd;
    private String routeStartStopId;
    private String routeEndStopId;
    private Number blockId;
    private Number stopSequence;
    private Number arriveTime;
    private Number serviceId;
    private String tripId;
    private int directionId;

    public SchedulesRouteModel() { };

    public SchedulesRouteModel(int routeType,
                               String routeId,
                               String routeShortName,
                               String routeLongName,
                               String routeStart,
                               String routeEnd,
                               String routeStartStopId,
                               String routeEndStopId) {
        this.setRouteType(routeType);
        this.setRouteId(routeId);
        this.setRouteShortName(routeShortName);
        this.setRouteLongName(routeLongName);
        this.setRouteStartName(routeStart);
        this.setRouteEndName(routeEnd);
        this.setRouteStartStopId(routeStartStopId);
        this.setRouteEndStopId(routeEndStopId);
    }

//    public int getRouteType() {
//        return routeType;
//    }

    public RouteTypes getRouteType() { return RouteTypes.values()[routeType]; }

    public void setRouteType(int routeType) {
        this.routeType = routeType;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getRouteStartName() {
        return routeStart;
    }

    public void setRouteStartName(String routeStart) {
        this.routeStart = routeStart;
    }

    public String getRouteEndName() {
        return routeEnd;
    }

    public void setRouteEndName(String routeEnd) {
        this.routeEnd = routeEnd;
    }

    public String getRouteStartStopId() {
        return routeStartStopId;
    }

    public void setRouteStartStopId(String routeStartStopId) {
        this.routeStartStopId = routeStartStopId;
    }

    public String getRouteEndStopId() {
        return routeEndStopId;
    }

    public void setRouteEndStopId(String routeEndStopId) {
        this.routeEndStopId = routeEndStopId;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public void setRouteShortName(String routeShortName) {
        this.routeShortName = routeShortName;
    }

    public String getRouteLongName() {
        return routeLongName;
    }

    public void setRouteLongName(String routeLongName) {
        this.routeLongName = routeLongName;
    }

    public void print() {
        Log.d("gg", "Route type: "+routeType+"  route id: "+ routeId +"     route short name: "+routeShortName+"     route long name: "+routeLongName);
        Log.d("gg", "Route start name: "+routeStart+"   Route end name "+routeEnd+"     Route start id "+routeStartStopId+"     route end id "+routeEndStopId);
    }

    public boolean hasStartAndEndSelected() {
        if (getRouteStartName() != null &&
            getRouteEndName() != null &&
            !getRouteStartName().isEmpty() &&
            !getRouteEndName().isEmpty()) {
            return true;
        }

        return false;
    }

    @Override
    public int compareTo(SchedulesRouteModel another) {
        if ((this.getRouteId().equals(another.getRouteId())) &&
            (this.getRouteStartName().equals(another.getRouteStartName())) &&
            (this.getRouteEndStopId().equals(another.getRouteEndStopId())) &&
            (this.getRouteEndName().equals(another.getRouteEndName()))) {

            return 0;
        }

        return 1;
    }

    public void reverseStartAndDestinationStops() {
        String tempStartStopName = getRouteStartName();
        String tempStartStopId = getRouteStartStopId();

        setRouteStartStopId(getRouteEndStopId());
        setRouteStartName(getRouteEndName());

        setRouteEndStopId(tempStartStopId);
        setRouteEndName(tempStartStopName);
    }

    public Number getBlockId() {
        return blockId;
    }

    public void setBlockId(Number blockId) {
        this.blockId = blockId;
    }

    public Number getStopSequence() {
        return stopSequence;
    }

    public void setStopSequence(Number stopSequence) {
        this.stopSequence = stopSequence;
    }

    public Number getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(Number arriveTime) {
        this.arriveTime = arriveTime;
    }

    public Number getServiceId() {
        return serviceId;
    }

    public void setServiceId(Number serviceId) {
        this.serviceId = serviceId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public int getDirectionId() {
        return directionId;
    }

    public void setDirectionId(int directionId) {
        this.directionId = directionId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.routeType);
        dest.writeString(this.routeId);
        dest.writeString(this.routeShortName);
        dest.writeString(this.routeLongName);
        dest.writeString(this.routeStart);
        dest.writeString(this.routeEnd);
        dest.writeString(this.routeStartStopId);
        dest.writeString(this.routeEndStopId);
        dest.writeSerializable(this.blockId);
        dest.writeSerializable(this.stopSequence);
        dest.writeSerializable(this.arriveTime);
        dest.writeSerializable(this.serviceId);
        dest.writeString(this.tripId);
    }

    private SchedulesRouteModel(Parcel in) {
        this.routeType = in.readInt();
        this.routeId = in.readString();
        this.routeShortName = in.readString();
        this.routeLongName = in.readString();
        this.routeStart = in.readString();
        this.routeEnd = in.readString();
        this.routeStartStopId = in.readString();
        this.routeEndStopId = in.readString();
        this.blockId = (Number) in.readSerializable();
        this.stopSequence = (Number) in.readSerializable();
        this.arriveTime = (Number) in.readSerializable();
        this.serviceId = (Number) in.readSerializable();
        this.tripId = in.readString();
    }

    public static final Parcelable.Creator<SchedulesRouteModel> CREATOR = new Parcelable.Creator<SchedulesRouteModel>() {
        public SchedulesRouteModel createFromParcel(Parcel source) {
            return new SchedulesRouteModel(source);
        }

        public SchedulesRouteModel[] newArray(int size) {
            return new SchedulesRouteModel[size];
        }
    };
}

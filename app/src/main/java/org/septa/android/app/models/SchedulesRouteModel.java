package org.septa.android.app.models;

import android.util.Log;

import java.lang.String;

public class SchedulesRouteModel implements Comparable<SchedulesRouteModel> {
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
        Log.d("tt", "in compareTo");
        this.print();
        Log.d("tt", "-----");
        another.print();
        Log.d("tt", "done in compareTo");

        if ((this.getRouteId().equals(another.getRouteId())) &&
            (this.getRouteStartName().equals(another.getRouteStartName())) &&
            (this.getRouteEndStopId().equals(another.getRouteEndStopId())) &&
            (this.getRouteEndName().equals(another.getRouteEndName()))) {

            return 0;
        }

        return this.getRouteId().compareTo(another.getRouteId());
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
}

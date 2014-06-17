package org.septa.android.app.models;

import android.util.Log;

import java.lang.String;

public class SchedulesRouteModel implements Comparable<SchedulesRouteModel> {
    private int routeType;
    private String routeCode;
    private String routeShortName;
    private String routeLongName;
    private String routeStart;
    private String routeEnd;
    private String routeStartStopId;
    private String routeEndStopId;

    public SchedulesRouteModel() { };

    public SchedulesRouteModel(int routeType,
                               String routeCode,
                               String routeShortName,
                               String routeLongName,
                               String routeStart,
                               String routeEnd,
                               String routeStartStopId,
                               String routeEndStopId) {
        this.setRouteType(routeType);
        this.setRouteCode(routeCode);
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

    public String getRouteCode() {
        return routeCode;
    }

    public void setRouteCode(String routeCode) {
        this.routeCode = routeCode;
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
        Log.d("gg", "Route type: "+routeType+"  route id: "+ routeCode +"     route short name: "+routeShortName+"     route long name: "+routeLongName);
        Log.d("gg", "Route start name: "+routeStart+"   Route end name "+routeEnd+"     Route start id "+routeStartStopId+"     route end id "+routeEndStopId);
    }

    @Override
    public int compareTo(SchedulesRouteModel another) {
        if ((this.getRouteCode().equals(another.getRouteCode())) &&
                (this.getRouteShortName()==another.getRouteShortName()) &&
                (this.getRouteStartName().equals(another.getRouteStartName())) &&
                (this.getRouteEndStopId()==another.getRouteEndStopId()) &&
                (this.getRouteEndName().equals(another.getRouteEndName()))) {

            return 0;
        }

        return -1;
    }

    public void reverseStartAndDestinationStops() {
        String tempStartStopName = getRouteStartName();
        String tempStartStopId = getRouteStartStopId();

        setRouteStartStopId(getRouteEndStopId());
        setRouteStartName(getRouteEndName());

        setRouteEndStopId(tempStartStopId);
        setRouteEndName(tempStartStopName);
    }
}

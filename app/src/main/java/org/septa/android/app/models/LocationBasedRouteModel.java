/*
 * LocationBasedRouteModel.java
 * Last modified on 04-30-2014 10:49-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocationBasedRouteModel {

    private static HashMap<String, RouteSpecialType> routeSpecialTypes;
    static {
        routeSpecialTypes = new HashMap<String, RouteSpecialType>();
        routeSpecialTypes.put("NHSL", RouteSpecialType.NHSL);
        routeSpecialTypes.put("BSS", RouteSpecialType.BSS);
        routeSpecialTypes.put("MFL", RouteSpecialType.MFL);
        routeSpecialTypes.put("MFO", RouteSpecialType.MFO);
        routeSpecialTypes.put("BSO", RouteSpecialType.BSO);
    }
    private String routeShortName;
    private int directionBinaryPower = 0;
    private int routeType = 0;
    private ArrayList<TimeDayPairModel> timeDayPairArrayList = new ArrayList<TimeDayPairModel>();
    private int dircode;

    public int getDirectionBinaryPower() {
        return directionBinaryPower;
    }

    public void setDirectionBinaryPower(int directionBinaryPower) {
        this.directionBinaryPower = directionBinaryPower;
    }

    public int getDirectionCode(){
        return dircode;
    }

    public void setDirectionCode(int code){
        this.dircode = code;
    }

    public int getRouteType() {
        return routeType;
    }

    public void setRouteType(int routeType) {
        this.routeType = routeType;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public void setRouteShortName(String routeShortName) {
        this.routeShortName = routeShortName;
    }

    public int getDirectionCount() {
        int count = 0;
        int value = getDirectionBinaryPower();

        if (value >= 8) {
            count++;
            value -= 8;
        } else {
            if (value >= 4) {
                count++;
                value -= 4;
            } else {
                if (value >= 2) {
                    count++;
                    value -= 2;
                } else {
                    if (value >= 1) {
                        count++;
                        value -= 1;
                    }
                }
            }
        }

        return count;
    }

    public String getRouteShortNameWithDirection() {
        String routeText;

        switch (getDirectionBinaryPower()) {
            case 1: {
                routeText = getRouteShortName() + "N";
                break;
            }
            case 2: {
                routeText = getRouteShortName() + "S";
                break;
            }
            case 4: {
                routeText = getRouteShortName() + "E";
                break;
            }
            case 8: {
                routeText = getRouteShortName() + "W";
                break;
            }
            default: {
                routeText = getRouteShortName();
                break;
            }
        }

        return routeText;
    }


    public List<TimeDayPairModel> getTimeDayPairs(){
        return timeDayPairArrayList;
    }

    public void addTimeDayPair(String time, String day) {
        TimeDayPairModel timeDayPair = new TimeDayPairModel(time, day);
        this.timeDayPairArrayList.add(timeDayPair);
    }


    public int getTimeDayPairCount() {
        return this.timeDayPairArrayList.size();
    }

    public RouteSpecialType getRouteSpecialType() {
        RouteSpecialType type = routeSpecialTypes.get(getRouteShortName());

        if (type == null) {
            return RouteSpecialType.NONE;
        }

        return type;
    }

    public TransportationType getTransportationType() {
        return TransportationType.getType(getRouteType());
    }
    public enum DirectionCode {
        N, S, E, W, X, Loop, LOOP
    }

    /**
     * Some routes require special handling.
     */
    public enum RouteSpecialType {
        NHSL, BSS, MFL, MFO, BSO, NONE
    }

    public class TimeDayPairModel {
        private String time;
        private String day;

        public TimeDayPairModel(String time, String day) {
            this.time = time;
            this.day = day;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }
    }

    public static LocationBasedRouteModel routeModelForDirection(LocationBasedRouteModel originalRoute, String direction){

        LocationBasedRouteModel route = new LocationBasedRouteModel();
        route.setDirectionCode(originalRoute.getDirectionCode());
        route.setRouteShortName(originalRoute.getRouteShortName() + direction.toUpperCase());
        route.setRouteType(originalRoute.getRouteType());
        return route;
    }
}

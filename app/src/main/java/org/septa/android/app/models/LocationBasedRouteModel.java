/*
 * LocationBasedRouteModel.java
 * Last modified on 04-30-2014 10:49-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models;

import java.util.ArrayList;

public class LocationBasedRouteModel {
    private String routeShortName;
    private int directionBinaryPower = 0;
    private int routeType = 0;

    private ArrayList<TimeDayPairModel> timeDayPairArrayList = new ArrayList<TimeDayPairModel>();
    private int timeDayPairIndex = -1;


    public int getDirectionBinaryPower() {
        return directionBinaryPower;
    }

    public void setDirectionBinaryPower(int directionBinaryPower) {
        this.directionBinaryPower = directionBinaryPower;
    }

    public int getRouteType() {
        return routeType;
    }

    public void setRouteType(int routeType) {
        this.routeType = routeType;
    }

    public enum DirectionCode {
        N, S, E, W, X, Loop, LOOP
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

    public void setTimeDayPairIndex(int index) {

        this.timeDayPairIndex = index;
    }

    public TimeDayPairModel getTimeDayPairFromIndex() {
        if (timeDayPairIndex > -1) {
            return timeDayPairArrayList.get(timeDayPairIndex);
        }

        return null;
    }

    public void addTimeDayPair(String time, String day) {
        TimeDayPairModel timeDayPair = new TimeDayPairModel(time, day);
        this.timeDayPairArrayList.add(timeDayPair);
    }

    public String[] getTimesFromTimeDayPairs() {
        String[] timeArray = new String[this.timeDayPairArrayList.size()];
        for (int i = 0; i < this.timeDayPairArrayList.size(); i++) {
            timeArray[i] = this.timeDayPairArrayList.get(i).getTime();
        }

        return timeArray;
    }

    public String[] getDaysFromTimeDayPairs() {
        String[] dayArray = new String[this.timeDayPairArrayList.size()];
        for (int i = 0; i < this.timeDayPairArrayList.size(); i++) {
            dayArray[i] = this.timeDayPairArrayList.get(i).getDay();
        }

        return dayArray;
    }

    public int getTimeDayPairCount() {
        return this.timeDayPairArrayList.size();
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
}

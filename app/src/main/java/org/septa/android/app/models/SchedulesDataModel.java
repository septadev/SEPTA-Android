package org.septa.android.app.models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.septa.android.app.databases.SEPTADatabase;
import org.septa.android.app.utilities.CalendarDateUtilities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class SchedulesDataModel {
    private static final String TAG = SchedulesDataModel.class.getName();
    private Context context = null;

    private HashMap<String, TripObject>startBasedTrips = new HashMap<String, TripObject>();
    private HashMap<String, TripObject>endBasedTrips;

    private ArrayList<TripObject>masterTripsList = new ArrayList<TripObject>();
    private ArrayList<TripObject>filteredTripsList = new ArrayList<TripObject>();

    private SchedulesRouteModel route;

    private int currentDisplayDirection = 0;

    public SchedulesDataModel(Context context) {

        this.context = context;
    }

    public void loadStartBasedTrips(RouteTypes routeType) {
        if (routeType == RouteTypes.TROLLEY) {
            routeType = RouteTypes.BUS;
        }

        String queryString = "SELECT route_id, block_id, stop_sequence, arrival_time, direction_id, service_id, stop_times_"+routeType+".trip_id trip_id FROM stop_times_"+routeType+" JOIN trips_"+routeType+" ON trips_"+routeType+".trip_id=stop_times_"+routeType+".trip_id WHERE trips_"+routeType+".trip_id IN (SELECT trip_id FROM trips_"+routeType+" WHERE route_id=\""+ getRoute().getRouteId()+"\" ) AND route_id=\""+ getRoute().getRouteId()+"\" AND stop_id="+ getRoute().getRouteStartStopId()+" ORDER BY arrival_time";

        SEPTADatabase septaDatabase = new SEPTADatabase(context);
        SQLiteDatabase database = septaDatabase.getReadableDatabase();

        Cursor cursor = null;

        if (queryString != null) {
            cursor = database.rawQuery(queryString, null);
        }

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                Log.d("f", "cursor is not null and moving to first.");
                do {
                    String tripId = cursor.getString(6);
                    Number trainNumber = cursor.getInt(1);
                    Number startSequence = cursor.getInt(2);
                    Number startTime = cursor.getInt(3);
                    Number directionId = cursor.getInt(4);
                    Number serviceId = cursor.getInt(5);

                    TripObject trip = new TripObject();
                    trip.setTripId(tripId);
                    trip.setTrainNo(trainNumber);
                    trip.setStartSeq(startSequence);
                    trip.setStartTime(startTime);
                    trip.setDirectionId(directionId);
                    trip.setServiceId(serviceId);

                    setCurrentDisplayDirection(trip.getDirectionId().intValue());

                    startBasedTrips.put(tripId, trip);
                } while (cursor.moveToNext());
            }

            cursor.close();
        } else {
            Log.d("f", "cursor is null");
        }

        database.close();
    }

    public boolean loadAndProcessEndStopsWithStartStops(RouteTypes routeType) {
        Log.d(TAG, "load and process end stops with start stops starting...");
        String queryString = null;
        boolean flippedOnce = false;
        boolean firstTime = true;

        switch(routeType) {
            case RAIL: {
                queryString = "SELECT route_id, block_id, stop_sequence, arrival_time, direction_id, service_id, stop_times_rail.trip_id trip_id FROM stop_times_rail JOIN trips_rail ON trips_rail.trip_id=stop_times_rail.trip_id WHERE trips_rail.trip_id IN (SELECT trip_id FROM trips_rail WHERE route_id=\""+route.getRouteId()+"\" ) AND route_id=\""+route.getRouteId()+"\" AND stop_id="+route.getRouteEndStopId()+" ORDER BY arrival_time";
                break;
            }
            case MFL:
            case BSL:
            case NHSL: {
                queryString = "SELECT route_id, block_id, stop_sequence, arrival_time, direction_id, service_id, st.trip_id trip_id FROM stop_times_"+routeType.name()+" as st JOIN trips_"+routeType.name()+" as t ON t.trip_id=st.trip_id WHERE t.trip_id IN (SELECT trip_id FROM trips_"+routeType.name()+" WHERE route_id=\""+route.getRouteId()+"\" ) AND route_id=\""+route.getRouteId()+"\" AND stop_id="+route.getRouteEndStopId()+" ORDER BY arrival_time";
                break;
            }
            case TROLLEY:
            case BUS: {
                queryString = "SELECT route_id, block_id, stop_sequence, arrival_time, direction_id, service_id, stop_times_bus.trip_id trip_id FROM stop_times_bus JOIN trips_bus ON trips_bus.trip_id=stop_times_bus.trip_id WHERE trips_bus.trip_id IN (SELECT trip_id FROM trips_bus WHERE route_id=\""+route.getRouteId()+"\" AND direction_id="+ getCurrentDisplayDirection() +" ) AND route_id=\""+route.getRouteId()+"\" AND stop_id="+route.getRouteEndStopId()+" ORDER BY arrival_time";
                break;
            }
            default: {
                Log.d(TAG, "fell through the setting of the query string");
            }
        }

        SEPTADatabase septaDatabase = new SEPTADatabase(context);
        SQLiteDatabase database = septaDatabase.getReadableDatabase();

        Cursor cursor = null;

        if (queryString != null) {
            cursor = database.rawQuery(queryString, null);
        }

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                Log.d(TAG, "move to first record in the cursor");
                do {
                    String tripId = cursor.getString(6);

                    if (startBasedTrips.get(tripId) != null) {
                        TripObject trip = startBasedTrips.get(tripId);
                        trip.setDirectionId(cursor.getInt(4));

                        int startSequence = trip.getStartSeq().intValue();
                        int endSequence = cursor.getInt(2);

                        if (startSequence < endSequence) {
                            Log.d(TAG, "start sequence is less than end sequence... no more manipulation needed");
                            trip.setEndSeq(endSequence);
                            trip.setEndTime(cursor.getInt(3));

                            if (!firstTime) {
                                if (routeType == RouteTypes.RAIL) {
                                    // comments in iOS say not sure
                                }

                                firstTime = false;
                            }

                            setCurrentDisplayDirection(trip.getDirectionId().intValue());
                        } else {
                            Log.d(TAG, "start sequence is more than end sequence... more manipulation is needed, maybe a flip");
                            trip.setEndSeq(trip.getStartSeq());
                            trip.setEndTime(trip.getStartTime());

                            trip.setStartSeq(cursor.getInt(2));
                            trip.setStartTime(cursor.getInt(3));

                            if (routeType == RouteTypes.TROLLEY || routeType == RouteTypes.BUS) {
                                if (!flippedOnce) {
                                    Log.d(TAG, "flipped trips!");
                                    flippedOnce = true;
                                    // TODO: flipStops here
                                }
                            } else {
                                Log.d(TAG, "not trolley or bus");
                                if (routeType == RouteTypes.RAIL) {
                                    Log.d(TAG, "rail, do nothing");
                                    // do nothing
                                } else {
                                    Log.d(TAG, "not rail");
                                    Log.d(TAG, "direction id here is "+trip.getDirectionId());
                                    if (trip.getDirectionId().intValue() == 0) {
                                        setCurrentDisplayDirection(1);
                                    } else {
                                        setCurrentDisplayDirection(0);
                                    }
                                }
                            }
                        }

                        masterTripsList.add(trip);
                        startBasedTrips.remove(trip);

                    }


                } while (cursor.moveToNext());
            }

            cursor.close();
        } else {
            Log.d("f", "cursor is null");
        }

        database.close();

        return flippedOnce;
    }

    public ArrayList<TripObject> createFilteredTripsList(int tab) {
        filteredTripsList.clear();

        int nowTime = -1;
        int serviceId = -1;


        switch(tab) {
            case 0: { // Now
                serviceId = CalendarDateUtilities.getServiceIdForNow(context);
                nowTime = CalendarDateUtilities.getNowTimeFormatted();
                break;
            }
            case 1: {  // Weekday
                serviceId = CalendarDateUtilities.getServiceIdForDay(Calendar.FRIDAY);
                break;
            }
            case 2: {  // Saturday
                serviceId = CalendarDateUtilities.getServiceIdForDay(Calendar.SATURDAY);
                break;
            }
            case 3: {  // Sunday
                serviceId = CalendarDateUtilities.getServiceIdForDay(Calendar.SUNDAY);
            }
        }

        for (TripObject trip : masterTripsList) {
            if (trip.getServiceId().intValue() == serviceId &&
               (trip.getStartTime().intValue() > nowTime) &&
               (trip.getDirectionId().intValue() == getCurrentDisplayDirection())) {

                this.filteredTripsList.add(trip);
            } else {
                if (serviceId == 4) {  // special case for Friday, where both weekday and Friday applies.
                    if (trip.getServiceId().intValue() == 1 &&
                       (trip.getStartTime().intValue() > nowTime) &&
                       (trip.getDirectionId().intValue() == getCurrentDisplayDirection())) {
                        this.filteredTripsList.add(trip);
                    }
                }
            }
        }

        if (filteredTripsList == null) {
            filteredTripsList = new ArrayList<TripObject>();
        }

        Collections.sort(filteredTripsList, new TripSorter());

        return this.filteredTripsList;
    }

    public SchedulesRouteModel getRoute() {
        return route;
    }

    public void setRoute(SchedulesRouteModel route) {
        this.route = route;
    }

    public int getCurrentDisplayDirection() {
        return currentDisplayDirection;
    }

    public void setCurrentDisplayDirection(int currentDisplayDirection) {
        this.currentDisplayDirection = currentDisplayDirection;
    }
}

class TripSorter implements Comparator<TripObject> {
    @Override
    public int compare(TripObject tripObject1, TripObject tripObject2) {
        if (tripObject1.getStartTime().intValue() < tripObject2.getStartTime().intValue()) {
            return -1;
        }
        if (tripObject1.getStartTime().intValue() == tripObject2.getStartTime().intValue()) {
            return 0;
        }

        return 1;
    }
}
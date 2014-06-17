package org.septa.android.app.models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.septa.android.app.databases.SEPTADatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class SchedulesDataModel {
    private static final String TAG = SchedulesDataModel.class.getName();
    private Context context = null;

    private HashMap<String, TripObject>startBasedTrips = new HashMap<String, TripObject>();
    private HashMap<String, TripObject>endBasedTrips;

    private ArrayList<TripObject>masterTripsArray = new ArrayList<TripObject>();

    private SchedulesRouteModel route;

    private int currentDisplayDirection = -1;

    public SchedulesDataModel(Context context) {

        this.context = context;
    }

    public void loadStartBasedTrips(RouteTypes routeType) {
        //TODO: set database based on routeType, figure that out.
        Log.d(TAG, "the routeType via name is "+routeType);
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
                    trip.setTripID(tripId);
                    trip.setTrainNo(trainNumber);
                    trip.setStartSeq(startSequence);
                    trip.setStartTime(startTime);
                    trip.setDirectionID(directionId);
                    trip.setServiceID(serviceId);

                    startBasedTrips.put(tripId, trip);
                } while (cursor.moveToNext());
            }

            cursor.close();
        } else {
            Log.d("f", "cursor is null");
        }

        database.close();

        Log.d(TAG, "load start is done with "+startBasedTrips.size()+" number of rows");

    }

    public void loadAndProcessEndStopsWithStartStops(RouteTypes routeType) {
        String queryString = null;
        boolean flippedOnce = false;
        boolean firstTime = true;

        switch(routeType) {
            case RAIL: {
                queryString = "SELECT route_id, block_id, stop_sequence, arrival_time, direction_id, service_id, stop_times_rail.trip_id trip_id FROM stop_times_rail JOIN trips_rail ON tripsDB.trip_id=stop_times_rail.trip_id WHERE trips_rail.trip_id IN (SELECT trip_id FROM trips_rail WHERE route_id="+route.getRouteId()+" ) AND route_id="+route.getRouteId()+" AND stop_id="+route.getRouteEndStopId()+" ORDER BY arrival_time";
                break;
            }
            case MFL:
            case BSL:
            case NHSL: {
                queryString = "SELECT route_id, block_id, stop_sequence, arrival_time, direction_id, service_id, stop_timesDB.trip_id trip_id FROM stop_timesDB JOIN tripsDB ON tripsDB.trip_id=stop_timesDB.trip_id WHERE tripsDB.trip_id IN (SELECT trip_id FROM tripsDB WHERE route_id="+route.getRouteShortName()+" ) AND route_id="+route.getRouteShortName()+" AND stop_id="+route.getRouteEndStopId()+" ORDER BY arrival_time";
                break;
            }
            case BUS: {
                queryString = "SELECT route_id, block_id, stop_sequence, arrival_time, direction_id, service_id, stop_timesDB.trip_id trip_id FROM stop_times_bus JOIN tripsDB ON trips_bus.trip_id=stop_times_bus.trip_id WHERE trips_bus.trip_id IN (SELECT trip_id FROM trips_bus WHERE route_id="+route.getRouteShortName()+" AND direction_id="+currentDisplayDirection+" ) AND route_id="+route.getRouteShortName()+" AND stop_id="+route.getRouteEndStopId()+" ORDER BY arrival_time";
                break;
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
                Log.d("f", "cursor is not null and moving to first.");
                do {
                    String tripId = cursor.getString(6);

                    if (startBasedTrips.get(tripId) != null) {
                        Log.d(TAG, "found the trip");
                        TripObject trip = startBasedTrips.get(tripId);
                        trip.setDirectionID(cursor.getInt(4));

                        int startSequence = trip.getStartSeq().intValue();
                        int endSequence = cursor.getInt(2);

                        if (startSequence < endSequence) {
                            Log.d(TAG, "the start sequence is less than end, ideal");
                            trip.setEndSeq(endSequence);
                            trip.setEndTime(cursor.getInt(3));

                            if (!firstTime) {
                                if (routeType == RouteTypes.RAIL) {
                                    // comments in iOS say not sure
                                }

                                firstTime = false;
                            }

                            currentDisplayDirection = trip.getDirectionID().intValue();
                        } else {
                            Log.d(TAG, "the end sequence is less than the start, not ideal");
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
                                if (routeType == RouteTypes.RAIL) {
                                    // do nothing
                                } else {
                                    if (trip.getDirectionID().intValue() == 0) {
                                        currentDisplayDirection = 1;
                                    } else {
                                        currentDisplayDirection = 0;
                                    }
                                }
                            }
                        }

                        masterTripsArray.add(trip);
                        startBasedTrips.remove(trip);

                    } else {
                        Log.d("dd", "no trip, did not find one");
                    }


                } while (cursor.moveToNext());
            }

            cursor.close();
        } else {
            Log.d("f", "cursor is null");
        }

        database.close();
    }

    public SchedulesRouteModel getRoute() {
        return route;
    }

    public void setRoute(SchedulesRouteModel route) {
        this.route = route;
    }
}

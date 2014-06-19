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
                    trip.setTripId(tripId);
                    trip.setTrainNo(trainNumber);
                    trip.setStartSeq(startSequence);
                    trip.setStartTime(startTime);
                    trip.setDirectionId(directionId);
                    trip.setServiceId(serviceId);

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
                queryString = "SELECT route_id, block_id, stop_sequence, arrival_time, direction_id, service_id, stop_times_rail.trip_id trip_id FROM stop_times_rail JOIN trips_rail ON trips_rail.trip_id=stop_times_rail.trip_id WHERE trips_rail.trip_id IN (SELECT trip_id FROM trips_rail WHERE route_id=\""+route.getRouteId()+"\" ) AND route_id=\""+route.getRouteId()+"\" AND stop_id="+route.getRouteEndStopId()+" ORDER BY arrival_time";
                break;
            }
            case MFL:
            case BSL:
            case NHSL: {
                Log.d(TAG, "try just routeType:"+routeType+"    or as name():"+routeType.name());
                queryString = "SELECT route_id, block_id, stop_sequence, arrival_time, direction_id, service_id, st.trip_id trip_id FROM stop_times_"+routeType.name()+" as st JOIN trips_"+routeType.name()+" as t ON t.trip_id=st.trip_id WHERE t.trip_id IN (SELECT trip_id FROM trips_"+routeType.name()+" WHERE route_id=\""+route.getRouteId()+"\" ) AND route_id=\""+route.getRouteId()+"\" AND stop_id="+route.getRouteEndStopId()+" ORDER BY arrival_time";
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
//                Log.d("f", "cursor is not null and moving to first.");
                do {
                    String tripId = cursor.getString(6);

                    if (startBasedTrips.get(tripId) != null) {
//                        Log.d(TAG, "found the trip");
                        TripObject trip = startBasedTrips.get(tripId);
                        trip.setDirectionId(cursor.getInt(4));

                        int startSequence = trip.getStartSeq().intValue();
                        int endSequence = cursor.getInt(2);

//                        Log.d(TAG, "start seq is "+startSequence+"   with end seq being "+endSequence+"   for trainno "+cursor.getInt(1));
                        if (startSequence < endSequence) {
//                            Log.d(TAG, "the start sequence is less than end, ideal");
                            trip.setEndSeq(endSequence);
                            trip.setEndTime(cursor.getInt(3));

                            if (!firstTime) {
                                if (routeType == RouteTypes.RAIL) {
                                    // comments in iOS say not sure
                                }

                                firstTime = false;
                            }

                            currentDisplayDirection = trip.getDirectionId().intValue();
                        } else {
//                            Log.d(TAG, "the end sequence is less than the start, not ideal");
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
                                    if (trip.getDirectionId().intValue() == 0) {
                                        currentDisplayDirection = 1;
                                    } else {
                                        currentDisplayDirection = 0;
                                    }
                                }
                            }
                        }

                        masterTripsList.add(trip);
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

        Log.d(TAG,"load and process done with masterTripsArray having "+masterTripsList.size()+" rows");
    }

    public ArrayList<TripObject> createFilteredTripsList(int tab) {
        Log.d(TAG, "create filtered trips list with tab of "+tab);
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
                serviceId = CalendarDateUtilities.getServiceIdForNow(context);
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
               (trip.getDirectionId().intValue() == currentDisplayDirection)) {

                this.filteredTripsList.add(trip);
            } else {
                if (serviceId == 4) {  // special case for Friday, where both weekday and Friday applies.
                    if (trip.getServiceId().intValue() == 1 &&
                       (trip.getStartTime().intValue() > nowTime) &&
                       (trip.getDirectionId().intValue() == currentDisplayDirection)) {
                        this.filteredTripsList.add(trip);
                    }
                }
            }
        }

        if (filteredTripsList == null) {
            filteredTripsList = new ArrayList<TripObject>();
        }

        Collections.sort(filteredTripsList, new TripSorter());

        Log.d(TAG, "createFilteredTripsList done with filteredTripsList having size "+filteredTripsList.size());
        return this.filteredTripsList;
    }

    public SchedulesRouteModel getRoute() {
        return route;
    }

    public void setRoute(SchedulesRouteModel route) {
        this.route = route;
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
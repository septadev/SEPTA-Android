package org.septa.android.app;

public interface Constants {
    String REQUEST_CODE = "request_code";
    String TRANSIT_TYPE = "transit_type";
    String ROUTE_DIRECTION_MODEL = "line_id";
    String STARTING_STATION = "starting_station";
    String DESTINATION_STATION = "destination_station";

    String ARRIVAL_RECORD = "arrival_record";
    String TRIP_ID = "trip_id";
    String EDIT_FAVORITES_FLAG = "edit_favorites_flag";

    // request codes used to pass data back between activities
    int NTA_REQUEST = 1;
    int NOTIFICATIONS_REQUEST = 2;
    int SYSTEM_STATUS_REQUEST = 3;
    int SCHEDULES_REQUEST = 4;
    int TRANSITVIEW_REQUEST = 5;

    // result codes to access MainActivity's fragments
    int VIEW_SCHEDULE = 1;
    int VIEW_SYSTEM_STATUS_PICKER = 2;
    int VIEW_NOTIFICATION_MANAGEMENT = 3;

    String SERVICE_ADVISORY_EXPANDED = "service_advisory_expanded";
    String SERVICE_ALERT_EXPANDED = "service_alert_expanded";
    String ACTIVE_DETOUR_EXPANDED = "active_detour_expanded";
    String WEATHER_ALERTS_EXPANDED = "weather_alerts_expanded";
    String ROUTE_ID = "route_id";
    String ROUTE_NAME = "route_name";
    String TARGET_URL = "target_url";
    String TITLE = "activity_title";
    String TERM_TRIP = "termTrip";
    String VEHICLE_ID = "vehicleId";
    String ROUTE_DESCRIPTION = "routeDescription";

    // push notification intent
    int RAIL_DELAY_PUSH_NOTIF_REQUEST = 6;
    String DESTINATION_STOP_ID = "destination_stop_id";
    String EXPIRATION_TIMESTAMP = "expiration_timestamp";

}

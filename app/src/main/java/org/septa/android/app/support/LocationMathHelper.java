package org.septa.android.app.support;

import com.google.android.gms.maps.model.LatLng;

public class LocationMathHelper {


    //Miles
    public final static double EARTH_RADIUS = 3959;

    /**
     * Calculate the distance between 2 points in Miles
     *
     * @param location1 First location
     * @param location2 Second Location
     * @return
     */
    public static double distance (LatLng location1, LatLng location2){
        return distance(location1.latitude, location1.longitude, location2.latitude, location2.longitude);
    }


    /**
     *
     * Calculate the distance between 2 points in Miles
     *
     * @param lat1 First Latitude
     * @param lon1 First Longitude
     * @param lat2 Second Latitude
     * @param lon2 Second Longitude
     * @return
     */
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }



    /**
     * Calculates the end-point from a given source at a given range (meters)
     * and bearing (degrees). This methods uses simple geometry equations to
     * calculate the end-point.
     *
     * @param point
     *            Point of origin
     * @param range
     *            Range in miles
     * @param bearing
     *            Bearing in degrees
     * @return End-point from the source given the desired range and bearing.
     */
    public static LatLng calculateDerivedPosition(LatLng point,
                                                  double range, double bearing)
    {
        double latA = Math.toRadians(point.latitude);
        double lonA = Math.toRadians(point.longitude);
        double angularDistance = range / EARTH_RADIUS;
        double trueCourse = Math.toRadians(bearing);

        double lat = Math.asin(
                Math.sin(latA) * Math.cos(angularDistance) +
                        Math.cos(latA) * Math.sin(angularDistance)
                                * Math.cos(trueCourse));

        double dlon = Math.atan2(
                Math.sin(trueCourse) * Math.sin(angularDistance)
                        * Math.cos(latA),
                Math.cos(angularDistance) - Math.sin(latA) * Math.sin(lat));

        double lon = ((lonA + dlon + Math.PI) % (Math.PI * 2)) - Math.PI;

        lat = Math.toDegrees(lat);
        lon = Math.toDegrees(lon);

        LatLng newPoint = new LatLng((float) lat, (float) lon);

        return newPoint;

    }

}

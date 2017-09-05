package org.septa.android.app.domain;

/***********************************************************
 * Class: Schedule Item
 * Purpose: Plan java pojo used to bind schedule data
 *          from the db cursors info for schedules
 * Created by ttuggerson on 9/1/17.
 */

public class ScheduleItem {

    private String _departureTime = null;
    private String _arrivalTime = null;
    private String _duration = null;

    public ScheduleItem(){}

    public ScheduleItem(String departureTime, String arrivalTime)
    {
        _departureTime = departureTime;
        _arrivalTime = arrivalTime;
    }

    public ScheduleItem(String departureTime, String arrivalTime, String duration)
    {
        _departureTime = departureTime;
        _arrivalTime = arrivalTime;
        _duration = duration;
    }

    public String getDepartureTime (){return _departureTime;}
    public String getArrivalTime (){return _arrivalTime;}
    public String getDuration(){ return _duration;} //will need to calculate duration
}

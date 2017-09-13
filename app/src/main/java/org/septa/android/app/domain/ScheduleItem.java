package org.septa.android.app.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

/***********************************************************
 * Class: Schedule Item
 * Purpose: Plan java pojo used to bind schedule data
 *          from the db cursors info for schedules
 * Created by ttuggerson on 9/1/17.
 */

public class ScheduleItem extends StopModel{

    private String _trip_ID = null;
    private String _departureTime_24HrClock = null;
    private String _arrivalTime_24HrClock = null;
    private String _duration = null;


    //----------------------------------------------------------------------------------------------
    //Constructors:  Default and overloaded constructors for this class
    //Purpose: takes initial parameters and populates pojo with values class extends StopModel
    //
    //return converted time
    //----------------------------------------------------------------------------------------------
    public ScheduleItem(){}

    public ScheduleItem(String departureTime, String arrivalTime)
    {
        _departureTime_24HrClock = departureTime;
        _arrivalTime_24HrClock = arrivalTime;
    }

    public ScheduleItem(String departureTime, String arrivalTime, String duration)
    {
        _departureTime_24HrClock = departureTime;
        _arrivalTime_24HrClock = arrivalTime;

    }

    //stop id, stop sequence and direction id are in the base object stopModel
    public ScheduleItem(String stopName, String trip_ID, String arrivalTime, String stop_ID, int stop_sequence)
    {
        _trip_ID = trip_ID;
        _arrivalTime_24HrClock = arrivalTime;
        this.setStopId(stop_ID);
        this.setStopName(stopName);
        this.setStopSequence(stop_sequence);
    }

    public void setTripID(String tripID){_trip_ID = tripID;}
    public void set_arrivalTime_24HrClock (String time_24HrClock){
        _arrivalTime_24HrClock = time_24HrClock;
    }


    //----------------------------------------------------------------------------------------------
    //Method:  ConvertToTwelveHourTime
    //Purpose: takes military time and converts it to the necessary 12hr format
    //
    //return converted time
    //----------------------------------------------------------------------------------------------
    public String ConvertToTwelveHourTime(String militaryTime){

        String convertedTime = "";
        try {

             if (militaryTime != null) {
            /*
            Date date = new SimpleDateFormat("hhmm").parse(String.format("%04d", militaryTime));
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            convertedTime = sdf.format(date).toString();
            //System.out.println(sdf.format(date));*/

                 return militaryTime;
                 /*
                 final SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
                 final Date dateObj = sdf.parse(militaryTime);

                 SimpleDateFormat timeConversion = new SimpleDateFormat("K:mm".format(String.valueOf(dateObj)));
                */
             }

        }catch(Exception ex){
            System.out.println("Error occured in date conversion"+ex.toString());
        }

        return convertedTime;
    }

    //----------------------------------------------------------------------------------------------
    //Method:  getDepartureTime
    //Purpose: takes military time and converts it to the necessary 12hr format
    //
    //return converted time
    //---------------------------------------------------------------------------------
    public String getDepartureTime(boolean rtn24HourClock){

        String _departureTime_12HrClock = null;

        if(rtn24HourClock)
            return _departureTime_24HrClock;
        else if (_departureTime_24HrClock!= null)
            _departureTime_12HrClock = ConvertToTwelveHourTime(_departureTime_24HrClock);

        return _departureTime_12HrClock;
    }

    //----------------------------------------------------------------------------------------------
    //Method:  getArrivalTime
    //Purpose: returns the arrival time format in either 12 or 24hour clock
    //
    //return converted time
    //---------------------------------------------------------------------------------
    public String getArrivalTime(boolean rtn24HourClock){

        String _arrivalTime_12HrClock = null;

        if(rtn24HourClock)
            return _arrivalTime_24HrClock;
        else if(_arrivalTime_24HrClock!=null)
            _arrivalTime_12HrClock = ConvertToTwelveHourTime(_arrivalTime_24HrClock);

        return _arrivalTime_12HrClock;
    }


    public String getDuration(){ return _duration;} //will need to calculate duration
}

package org.septa.android.app.models;

public class SchedulesStoredTripModel implements Comparable<SchedulesStoredTripModel> {

    private String startStopName;
    private String startStopId;
    private String destinationStopName;
    private String destintationStopId;

    public String getStartStopName() {
        return startStopName;
    }

    public void setStartStopName(String startStopName) {
        this.startStopName = startStopName;
    }

    public String getStartStopId() {
        return startStopId;
    }

    public void setStartStopId(String startStopId) {
        this.startStopId = startStopId;
    }

    public String getDestinationStopName() {
        return destinationStopName;
    }

    public void setDestinationStopName(String destinationStopName) {
        this.destinationStopName = destinationStopName;
    }

    public String getDestintationStopId() {
        return destintationStopId;
    }

    public void setDestintationStopId(String destintationStopId) {
        this.destintationStopId = destintationStopId;
    }

    @Override
    public int compareTo(SchedulesStoredTripModel another) {
        if ((this.getStartStopId().equals(another.getStartStopId())) &&
                (this.getStartStopName().equals(another.getStartStopName())) &&
                (this.getDestintationStopId().equals(another.getDestintationStopId())) &&
                (this.getDestinationStopName().equals(another.getDestinationStopName()))) {

            return 0;
        }

        return -1;
    }
}
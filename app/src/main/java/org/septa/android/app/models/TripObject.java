package org.septa.android.app.models;

public class TripObject {

    private Number startTime;
    private Number endTime;

    private Number trainNo;
    private Number routeName;

    private Number serviceID;
    private Number directionID;

    private Number startSeq;
    private Number endSeq;

    private String tripID;


    public Number getStartTime() {
        return startTime;
    }

    public void setStartTime(Number startTime) {
        this.startTime = startTime;
    }

    public Number getEndTime() {
        return endTime;
    }

    public void setEndTime(Number endTime) {
        this.endTime = endTime;
    }

    public Number getTrainNo() {
        return trainNo;
    }

    public void setTrainNo(Number trainNo) {
        this.trainNo = trainNo;
    }

    public Number getRouteName() {
        return routeName;
    }

    public void setRouteName(Number routeName) {
        this.routeName = routeName;
    }

    public Number getServiceID() {
        return serviceID;
    }

    public void setServiceID(Number serviceID) {
        this.serviceID = serviceID;
    }

    public Number getDirectionID() {
        return directionID;
    }

    public void setDirectionID(Number directionID) {
        this.directionID = directionID;
    }

    public Number getStartSeq() {
        return startSeq;
    }

    public void setStartSeq(Number startSeq) {
        this.startSeq = startSeq;
    }

    public Number getEndSeq() {
        return endSeq;
    }

    public void setEndSeq(Number endSeq) {
        this.endSeq = endSeq;
    }

    public String getTripID() {
        return tripID;
    }

    public void setTripID(String tripID) {
        this.tripID = tripID;
    }

}

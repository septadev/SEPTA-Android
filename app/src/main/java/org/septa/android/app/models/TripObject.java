package org.septa.android.app.models;

public class TripObject {

    private Number startTime;
    private Number endTime;

    private Number trainNo;
    private Number routeName;

    private Number serviceId;
    private Number directionId;

    private Number startSeq;
    private Number endSeq;

    private String tripId;

    public String print() {
        return "startTime:"+startTime+"  endTime:"+endTime+"   trainNo:"+trainNo+"   routeName:"+routeName+"   serviceId:"+serviceId+"   directionId:"+directionId+"   startSeq:"+startSeq+"   endSeq:"+endSeq+"   tripId:"+ tripId;
    }

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

    public Number getServiceId() {
        return serviceId;
    }

    public void setServiceId(Number serviceId) {
        this.serviceId = serviceId;
    }

    public Number getDirectionId() {
        return directionId;
    }

    public void setDirectionId(Number directionId) {
        this.directionId = directionId;
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

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }
}

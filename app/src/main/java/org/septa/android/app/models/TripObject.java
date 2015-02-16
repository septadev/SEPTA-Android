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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TripObject that = (TripObject) o;

        if (directionId != null ? !directionId.equals(that.directionId) : that.directionId != null)
            return false;
        if (endSeq != null ? !endSeq.equals(that.endSeq) : that.endSeq != null) return false;
        if (endTime != null ? !endTime.equals(that.endTime) : that.endTime != null) return false;
        if (routeName != null ? !routeName.equals(that.routeName) : that.routeName != null)
            return false;
        if (serviceId != null ? !serviceId.equals(that.serviceId) : that.serviceId != null)
            return false;
        if (startSeq != null ? !startSeq.equals(that.startSeq) : that.startSeq != null)
            return false;
        if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null)
            return false;
        if (trainNo != null ? !trainNo.equals(that.trainNo) : that.trainNo != null) return false;
        if (tripId != null ? !tripId.equals(that.tripId) : that.tripId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = startTime != null ? startTime.hashCode() : 0;
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
        result = 31 * result + (trainNo != null ? trainNo.hashCode() : 0);
        result = 31 * result + (routeName != null ? routeName.hashCode() : 0);
        result = 31 * result + (serviceId != null ? serviceId.hashCode() : 0);
        result = 31 * result + (directionId != null ? directionId.hashCode() : 0);
        result = 31 * result + (startSeq != null ? startSeq.hashCode() : 0);
        result = 31 * result + (endSeq != null ? endSeq.hashCode() : 0);
        result = 31 * result + (tripId != null ? tripId.hashCode() : 0);
        return result;
    }
}

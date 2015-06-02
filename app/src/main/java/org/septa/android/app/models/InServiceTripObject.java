package org.septa.android.app.models;

import org.septa.android.app.models.servicemodels.TrainViewModel;

/**
 * Created by jhunchar on 6/1/15.
 */
public class InServiceTripObject {

    TrainViewModel trainViewModel;
    TripObject tripObject;

    public InServiceTripObject(TrainViewModel trainViewModel, TripObject tripObject) {
        this.trainViewModel = trainViewModel;
        this.tripObject = tripObject;
    }

    public TrainViewModel getTrainViewModel() {
        return trainViewModel;
    }

    public void setTrainViewModel(TrainViewModel trainViewModel) {
        this.trainViewModel = trainViewModel;
    }

    public TripObject getTripObject() {
        return tripObject;
    }

    public void setTripObject(TripObject tripObject) {
        this.tripObject = tripObject;
    }
}

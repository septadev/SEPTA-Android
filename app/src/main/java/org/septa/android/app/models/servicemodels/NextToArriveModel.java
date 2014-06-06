/*
 * NextToArriveModel.java
 * Last modified on 06-05-2014 22:30-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models.servicemodels;

import com.google.gson.annotations.SerializedName;

public class NextToArriveModel {
    public static final String TAG = TrainViewModel.class.getName();

    @SerializedName("orig_train") private String originalTrain;
    @SerializedName("orig_line") private String originalLine;
    @SerializedName("orig_departure_time") private String originalDepartureTime;
    @SerializedName("orig_arrival_time") private String OriginalArrivalTime;
    @SerializedName("orig_delay") private String originalDelay;

    @SerializedName("term_train") private String terminalTrain;
    @SerializedName("term_line") private String terminalLine;
    @SerializedName("term_departure_time") private String terminalDepartureTime;
    @SerializedName("term_arrival_time") private String terminalArrivalTime;
    @SerializedName("term_delay") private String terminalDelay;

    @SerializedName("isdirect") private boolean isDirect;

    @SerializedName("Connection") private String Connection;

}

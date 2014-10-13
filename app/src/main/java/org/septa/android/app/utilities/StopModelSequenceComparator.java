/**
 * Created by acampbell on 10/1/14.
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.utilities;

import org.septa.android.app.models.StopModel;

import java.util.Comparator;

public class StopModelSequenceComparator implements Comparator<StopModel> {
    @Override
    public int compare(StopModel stopModel, StopModel stopModel2) {
        if(stopModel.getStopSequence() < stopModel2.getStopSequence()) {
            return -1;
        } else if(stopModel.getStopSequence() > stopModel2.getStopSequence()) {
            return 1;
        } else {
            return 0;
        }
    }
}

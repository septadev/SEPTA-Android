/**
 * Created by acampbell on 7/20/14.
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.utilities;

import org.septa.android.app.models.StopModel;

import java.util.Comparator;

public class StopModelDistanceComparator implements Comparator<StopModel> {
    @Override
    public int compare(StopModel stopModel, StopModel stopModel2) {
        if(stopModel.getDistance() < stopModel2.getDistance()) {
            return -1;
        } else if(stopModel.getDistance() > stopModel2.getDistance()) {
            return 1;
        } else {
            return 0;
        }
    }
}

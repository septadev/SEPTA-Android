/**
 * Created by acampbell on 10/1/14.
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.utilities;

import org.septa.android.app.models.StopModel;

import java.util.Comparator;

public class StopModelNameComparator implements Comparator<StopModel> {

    @Override
    public int compare(StopModel stopModel, StopModel stopModel2) {
        return stopModel.getStopName().compareTo(stopModel2.getStopName());
    }
}

/*
 * MinMaxModel.java
 * Last modified on 04-21-2014 16:17-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models;

import android.util.Log;

public class MinMaxHoursModel {
    private Number minimum;
    private Number maximum;

    public MinMaxHoursModel(Number minimum, Number maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public Number getMinimum() {
        return minimum;
    }

    public void setMinimum(Number minimum) {
        this.minimum = minimum;
    }

    public Number getMaximum() {
        return maximum;
    }

    public void setMaximum(Number maximum) {
        this.maximum = maximum;
    }

    public boolean inMinMaxRange(int time) {
        Log.d("dd", "comparing time:"+time+"  against the min of:"+minimum+"    and max of:"+maximum);
        if (time < minimum.intValue() || time > maximum.intValue()) {
            return false;
        }

        return true;
    }
}

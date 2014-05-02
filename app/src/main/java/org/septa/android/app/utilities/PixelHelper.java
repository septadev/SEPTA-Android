/*
 * PixelHelper.java
 * Last modified on 05-02-2014 13:49-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.utilities;

import android.content.Context;
import android.util.TypedValue;

public class PixelHelper {

    public static int pixelsToDensityIndependentPixels(Context context, int pixels) {

        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels, context.getResources().getDisplayMetrics());
    }
}

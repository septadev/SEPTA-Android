package org.septa.android.app.views;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.utilities.PixelHelper;

/**
 * Created by rrobinson on 7/20/14.
 *
 * Small rounded rectangle used to display route short names.
 */
public class RouteTextView extends TextView {

    public RouteTextView(Context context) {
        super(context);
        int width = PixelHelper.pixelsToDensityIndependentPixels(context, 42);
        LinearLayout.LayoutParams textViewLayoutParams = new LinearLayout.LayoutParams(width,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        textViewLayoutParams.setMargins(0, 0, 5, 0); // llp.setMargins(left, top, right, bottom);
        this.setLayoutParams(textViewLayoutParams);
        this.setPadding(5, 5, 5, 5);
        this.setGravity(Gravity.CENTER);
        this.setTextColor(Color.WHITE);
        this.setTextSize(12.0f);
        this.setBackgroundResource(R.drawable.findnearestlocation_roundedbutton_corners);
    }
}

/*
 * FindNearestLocationEditRadiusDialogFragment.java
 * Last modified on 04-30-2014 15:52-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import org.septa.android.app.R;
import org.septa.android.app.utilities.PixelHelper;

public class FindNearestLocationEditRadiusDialog extends Dialog {
    public static final String TAG = FindNearestLocationEditRadiusDialog.class.getName();
    private Activity activity = null;

    private float mapSearchRadius;

    public FindNearestLocationEditRadiusDialog(Activity activity, float mapSearchRadius) {
        super(activity);

        this.activity = activity;
        this.mapSearchRadius = mapSearchRadius;
    }

    public FindNearestLocationEditRadiusDialog(Activity activity, int theme, float mapSearchRadius) {
        super(activity, theme);

        this.activity = activity;
        this.mapSearchRadius = mapSearchRadius;
    }

    private void populateEditTextFields(float mapSearchRadius) {
        String mapSearchRadiusString = String.valueOf(mapSearchRadius);

        // make sure the string is in the format of x.xxx by padding zeros
        while (mapSearchRadiusString.length() < 5) {
            mapSearchRadiusString += "0";
        }

        setOnesValue(mapSearchRadiusString.substring(0,1));
        setTenthsValue(mapSearchRadiusString.substring(2,3));
        setHundredthsValue(mapSearchRadiusString.substring(3,4));
        setThousandthsValue(mapSearchRadiusString.substring(4,5));
    }

    public void setOnesValue(String onesValue) {
        Log.d(TAG, "trying to access the ones field");
        EditText radiusEditTextViewOnes = (EditText) findViewById(R.id.radiusedit_textview_ones);
        radiusEditTextViewOnes.setText(onesValue);
    }

    public String getOnesValue() {
        EditText radiusEditTextViewOnes = (EditText) findViewById(R.id.radiusedit_textview_ones);
        return radiusEditTextViewOnes.getText().toString();
    }

    public void setTenthsValue(String tenthsValue) {
        EditText radiusEditTextViewTenths = (EditText) findViewById(R.id.radiusedit_textview_tenths);
        radiusEditTextViewTenths.setText(tenthsValue);
    }

    public String getTenthsValue() {
        EditText radiusEditTextViewTenths = (EditText) findViewById(R.id.radiusedit_textview_tenths);
        return radiusEditTextViewTenths.getText().toString();
    }

    public void setHundredthsValue(String hundredthsValue) {
        EditText radiusEditTextViewHundredths = (EditText) findViewById(R.id.radiusedit_textview_hundredths);
        radiusEditTextViewHundredths.setText(hundredthsValue);
    }

    public String getHundredthsValue() {
        EditText radiusEditTextViewHundredths = (EditText) findViewById(R.id.radiusedit_textview_hundredths);
        return radiusEditTextViewHundredths.getText().toString();
    }

    public void setThousandthsValue(String thousandthsValue) {
        EditText radiusEditTextViewThousandths = (EditText) findViewById(R.id.radiusedit_textview_thounsandths);
        radiusEditTextViewThousandths.setText(thousandthsValue);
    }

    public String getThousandthsValue() {
        EditText radiusEditTextViewThousandths = (EditText) findViewById(R.id.radiusedit_textview_thounsandths);
        return radiusEditTextViewThousandths.getText().toString();
    }

    public float getMapSearchRadius() {
        String mapSearchRadiusString = getOnesValue() + "." + getTenthsValue() + getHundredthsValue() + getThousandthsValue();
        return Float.parseFloat(mapSearchRadiusString);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setCanceledOnTouchOutside(false);
        setCancelable(true);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

        // make the dialog window background transparent to allow the gradient on the layout work.
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // to stop the dimming of the screen behind the dialog.
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        Window window = getWindow();
        WindowManager.LayoutParams windowsLayoutParams = window.getAttributes();
        windowsLayoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
        windowsLayoutParams.x = PixelHelper.pixelsToDensityIndependentPixels(getContext(), 10);   //x position
        windowsLayoutParams.y = PixelHelper.pixelsToDensityIndependentPixels(getContext(), 50);   //y position
        window.setAttributes(windowsLayoutParams);

        setContentView(R.layout.findnearestlocation_editradius_dialog);

        View mainLayout = findViewById(R.id.radiuserdit_dialog_layout);

        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {0xD9131313,0xD9616261});
        gd.setCornerRadius(20f);

        mainLayout.setBackgroundDrawable(gd);

        populateEditTextFields(mapSearchRadius);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(event.getAction() == MotionEvent.ACTION_OUTSIDE){


            this.dismiss();
        }
        return false;
    }
}


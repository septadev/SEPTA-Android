/*
 * FindNearestLocationEditRadiusDialogFragment.java
 * Last modified on 04-30-2014 15:52-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import org.septa.android.app.R;
import org.septa.android.app.utilities.PixelHelper;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class FindNearestLocationEditRadiusDialog extends Dialog {
    public static final String TAG = FindNearestLocationEditRadiusDialog.class.getName();
    @InjectView(R.id.radiusedit_textview_ones)
    EditText mTextviewOnes;
    @InjectView(R.id.radiusedit_textview_tenths)
    EditText mTextviewTenths;
    @InjectView(R.id.radiusedit_textview_hundredths)
    EditText mTextviewHundredths;
    @InjectView(R.id.radiusedit_textview_thounsandths)
    EditText mTextviewThounsandths;

    private float mapSearchRadius;

    public FindNearestLocationEditRadiusDialog(Activity activity, float mapSearchRadius) {
        super(activity);

        this.mapSearchRadius = mapSearchRadius;

    }

    private void populateEditTextFields(float mapSearchRadius) {
        String mapSearchRadiusString = String.valueOf(mapSearchRadius);

        // make sure the string is in the format of x.xxx by padding zeros
        while (mapSearchRadiusString.length() < 5) {
            mapSearchRadiusString += "0";
        }

        mTextviewOnes.setText(mapSearchRadiusString.substring(0, 1));
        mTextviewTenths.setText(mapSearchRadiusString.substring(2, 3));
        mTextviewHundredths.setText(mapSearchRadiusString.substring(3, 4));
        mTextviewThounsandths.setText(mapSearchRadiusString.substring(4, 5));

    }

    public float getMapSearchRadius() {
        String mapSearchRadiusString = mTextviewOnes.getText().toString()
                + "."
                + mTextviewTenths.getText().toString()
                + mTextviewHundredths.getText().toString()
                + mTextviewThounsandths.getText().toString();

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
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // to stop the dimming of the screen behind the dialog.
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        Window window = getWindow();
        WindowManager.LayoutParams windowsLayoutParams = window.getAttributes();
        windowsLayoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
        windowsLayoutParams.x = PixelHelper.pixelsToDensityIndependentPixels(getContext(), 10);   //x position
        windowsLayoutParams.y = PixelHelper.pixelsToDensityIndependentPixels(getContext(), 50);   //y position
        window.setAttributes(windowsLayoutParams);

        setContentView(R.layout.findnearestlocation_editradius_dialog);
        ButterKnife.inject(this);

        View mainLayout = findViewById(R.id.radiuserdit_dialog_layout);

        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xD9131313, 0xD9616261});
        gd.setCornerRadius(20f);


        mainLayout.setBackgroundDrawable(gd);

        populateEditTextFields(mapSearchRadius);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            this.dismiss();
        }
        return false;
    }
}


/*
 * FindNearestLocationEditRadiusDialogFragment.java
 * Last modified on 04-30-2014 15:52-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import org.septa.android.app.R;

public class FindNearestLocationEditRadiusDialog extends Dialog {
    public static final String TAG = FindNearestLocationEditRadiusDialog.class.getName();

    public FindNearestLocationEditRadiusDialog(Activity activity) {
        super(activity);
    }

    public FindNearestLocationEditRadiusDialog(Activity activity, int theme) {
        super(activity, theme);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setCanceledOnTouchOutside(false);
        setCancelable(true);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

        setContentView(R.layout.findnearestlocation_editradius_dialog);
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


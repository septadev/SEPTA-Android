/**
 * Created by Trey Robinson on 7/10/14.
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app;

import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.septa.android.app.managers.AlertManager;
import org.septa.android.app.managers.SharedPreferencesManager;

public class MainApplication extends Application{

    private static final String TAG = "MainApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        // Disable Crashlytics for debug builds (unsigned)
        if(!BuildConfig.DEBUG) {
            Crashlytics.start(this);
            Log.i(TAG, "Starting Crashlytics");
        } else {
            Log.i(TAG, "Crashlytics disabled for DEBUG builds");
        }
        SharedPreferencesManager.getInstance().init(this);

        //@TODO putting timing reqs around this and load on demand
        AlertManager.getInstance().fetchAlerts();
    }
}

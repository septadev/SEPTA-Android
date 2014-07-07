package org.septa.android.app;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import org.septa.android.app.managers.SharedPreferencesManager;

/**
 * Created by Trey Robinson on 7/10/14.
 */
public class MainApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        Crashlytics.start(this);
        SharedPreferencesManager.getInstance().init(this);
    }
}

package org.septa.android.app;

import android.app.Application;
import android.util.Log;

import org.septa.android.app.database.DatabaseManager;
import com.crashlytics.android.Crashlytics;

public class MainApplication extends Application {
    public static final String TAG = MainApplication.class.getSimpleName();
    @Override
    public void onCreate() {
        super.onCreate();
        if(!BuildConfig.DEBUG) {
            Crashlytics.start(this);
            Log.i(TAG, "Starting Crashlytics");
        } else {
            Log.i(TAG, "Crashlytics disabled for DEBUG builds");
        }

        // Initialize database, can be moved to logo screen
        DatabaseManager.getInstance(this);
    }
}

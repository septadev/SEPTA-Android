package org.septa.android.app;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import org.septa.android.app.database.DatabaseManager;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;

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

        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(MainApplication.class.getPackage().getName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            String myApiKey = bundle.getString("com.google.android.geo.API_KEY");
            SeptaServiceFactory.setGoogleKey(myApiKey);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
        }
        // Initialize database, can be moved to logo screen
        DatabaseManager.getInstance(this);
    }
}

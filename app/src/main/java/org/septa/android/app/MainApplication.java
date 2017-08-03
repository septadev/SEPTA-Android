package org.septa.android.app;

import android.app.Application;

import org.septa.android.app.database.DatabaseManager;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize database, can be moved to logo screen
        DatabaseManager.getInstance(this);
    }
}

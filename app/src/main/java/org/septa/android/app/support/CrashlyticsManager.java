package org.septa.android.app.support;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

/**
 * Created by jkampf on 11/21/17.
 */

public class CrashlyticsManager {
    private static boolean initalized = false;


    public static void init(Context context) {
        Crashlytics.start(context);
        initalized = true;
    }


    public static void log(int priority, String tag, String msg) {
        if (msg == null)
            msg = "";

        if (initalized)
            Crashlytics.log(priority, tag, msg);
        else
            android.util.Log.println(priority, tag, msg);

    }

    public static void logException(String tag, Throwable e) {
        if (initalized)
            Crashlytics.logException(e);
        else Log.e(tag, "", e);
    }


}

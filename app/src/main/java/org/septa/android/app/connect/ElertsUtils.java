package org.septa.android.app.connect;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public abstract class ElertsUtils {

    private static final String packageName = "com.elerts.septa";

    public static void jumpToElertsApp(Context context) {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);

        // jump to elerts app if already downloaded
        if (launchIntent != null) {
            context.startActivity(launchIntent);

        } else {
            // otherwise jump to playstore
            openElertsAppInPlaystore(context);
        }
    }

    private static void openElertsAppInPlaystore(Context context) {
        Uri uri = Uri.parse("market://details?id=" + packageName);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)));
        }
    }
}

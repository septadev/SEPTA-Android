package org.septa.android.app.support;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public abstract class GeneralUtils {

    public static String getDurationAsString(long duration, TimeUnit timeUnit) {
        long totalMinutes = TimeUnit.MINUTES.convert(
                duration, timeUnit);

        long hours = totalMinutes / 60;
        long minutes = totalMinutes - hours * 60;
        StringBuilder builder = new StringBuilder();
        if (hours > 0) {
            builder.append(hours).append(" h");
            if (minutes > 0) {
                builder.append(" ").append(minutes).append(" m");
            }
        } else
            builder.append(minutes).append(" m");

        return builder.toString();
    }

    public static String getDurationAsLongString(long duration, TimeUnit timeUnit) {
        long totalMinutes = TimeUnit.MINUTES.convert(
                duration, timeUnit);

        long hours = totalMinutes / 60;
        long minutes = totalMinutes - hours * 60;
        StringBuilder builder = new StringBuilder();
        if (hours > 0) {
            builder.append(hours).append(" hours");
            if (minutes > 0) {
                builder.append(" ").append(minutes).append(" minutes");
            }
        } else
            builder.append(minutes).append(" mins");

        return builder.toString();
    }

    public static String updateUrls(String inString){
        return inString.replace("href=\"/", "href=\"http://www.septa.org/");
    }

    public static String readRawTextFile(Context ctx, int resId) {
        InputStream inputStream = ctx.getResources().openRawResource(resId);
        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        StringBuilder text = new StringBuilder();
        try {
            while (( line = buffreader.readLine()) != null) {
                text.append(line);
                text.append("\n");
            }
        } catch (IOException e) {
            return null;
        }
        return text.toString();
    }

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (NetworkInfo anInfo : info) {
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}

package org.septa.android.app.support;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public abstract class GeneralUtils {

    public static final String TIME_AM = "am", TIME_PM = "pm";

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
        } else {
            builder.append(minutes).append(" m");
        }

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
        } else {
            builder.append(minutes).append(" mins");
        }

        return builder.toString();
    }

    public static String updateUrls(String inString) {
        return inString.replace("href=\"/", "href=\"http://www.septa.org/");
    }

    public static String readRawTextFile(Context ctx, int resId) {
        InputStream inputStream = ctx.getResources().openRawResource(resId);
        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        StringBuilder text = new StringBuilder();
        try {
            while ((line = buffreader.readLine()) != null) {
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

    @NonNull
    public static String getTimeFromInt(int hourOfDay, int minute) {
        // get 12H am / pm time from 24H time
        String amPm = TIME_AM;
        if (hourOfDay > 12) {
            hourOfDay -= 12;
            amPm = TIME_PM;
        }
        StringBuilder time = new StringBuilder(String.valueOf(hourOfDay));
        time.append(":")
                .append(new DecimalFormat("00").format(minute))
                .append(" ")
                .append(amPm);
        return time.toString();
    }

    public static int roundUpToNearestInterval(int minute, int interval) {
        int minuteFloor = minute - (minute % interval);
        minute = minuteFloor + (minute == minuteFloor + 1 ? interval : 0);
        if (minute == 60) {
            minute = 0;
        }
        return minute;
    }

}
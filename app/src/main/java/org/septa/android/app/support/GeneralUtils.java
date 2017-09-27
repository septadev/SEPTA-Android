package org.septa.android.app.support;

import android.util.Log;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by jkampf on 9/19/17.
 */

public class GeneralUtils {

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

    public static String updateUrls(String inString){
        String outString = inString.replace("href=\"/", "href=\"http://www.septa.org/");
        return outString;
    }

}

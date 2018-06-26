package org.septa.android.app.support;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import org.septa.android.app.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

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

    public static String updateUrls(String inString) {
        String outString = inString.replace("href=\"/", "href=\"http://www.septa.org/");
        return outString;
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

    public static BitmapDescriptor getDirectionalIconForTransitType(Context context, int transitTypeDrawableId, int angle) {
        // the vehicle direction icon points South by default
        // but the angle is 0 for North, 90 for East, 180 for South, etc.
        final int ANGLE_OFFSET = 180;

        Bitmap transitType = drawableToBitmap(ContextCompat.getDrawable(context, transitTypeDrawableId));
        Bitmap direction = drawableToBitmap(ContextCompat.getDrawable(context, R.drawable.ic_vehicle_direction));

        BitmapDrawable finalDirection = rotateBitmap(context, direction, angle + ANGLE_OFFSET);

        return BitmapDescriptorFactory.fromBitmap(mergeToPin(finalDirection.getBitmap(), transitType));
    }

    public static Bitmap mergeToPin(Bitmap back, Bitmap front) {
        Bitmap result = Bitmap.createBitmap(back.getWidth(), back.getHeight(), back.getConfig());
        Canvas canvas = new Canvas(result);
        int widthBack = back.getWidth();
        int widthFront = front.getWidth();
        float move = (widthBack - widthFront) / 2;
        canvas.drawBitmap(back, 0f, 0f, null);
        canvas.drawBitmap(front, move, move, null);
        Log.e("mergeToPin", "Back Width: " + widthBack + " Front Width: " + widthFront + " Move Width: " + move); // TODO: remove
        return result;
    }

    public static BitmapDrawable rotateBitmap(Context context, Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap bitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        return new BitmapDrawable(context.getResources(), bitmap);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}

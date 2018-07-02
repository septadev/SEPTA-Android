package org.septa.android.app.transitview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import org.septa.android.app.R;
import org.septa.android.app.services.apiinterfaces.model.Favorite;
import org.septa.android.app.services.apiinterfaces.model.TransitViewFavorite;

import java.util.Arrays;

public abstract class TransitViewUtils {

    public static BitmapDescriptor getDirectionalIconForTransitType(Context context, int transitTypeDrawableId, int angle) {
        // the vehicle direction icon points South by default
        // but the angle is 0 for North, 90 for East, 180 for South, etc.
        final int ANGLE_OFFSET = 180;

        Bitmap transitType = drawableToBitmap(ContextCompat.getDrawable(context, transitTypeDrawableId));
        Bitmap direction = drawableToBitmap(ContextCompat.getDrawable(context, R.drawable.ic_vehicle_direction));

        BitmapDrawable finalDirection = rotateBitmap(context, direction, angle + ANGLE_OFFSET);

        return BitmapDescriptorFactory.fromBitmap(mergeToPin(finalDirection.getBitmap(), transitType));
    }

    private static Bitmap mergeToPin(Bitmap back, Bitmap front) {
        Bitmap result = Bitmap.createBitmap(back.getWidth(), back.getHeight(), back.getConfig());
        Canvas canvas = new Canvas(result);
        int widthBack = back.getWidth();
        int widthFront = front.getWidth();
        float move = (widthBack - widthFront) / 2;
        canvas.drawBitmap(back, 0f, 0f, null);
        canvas.drawBitmap(front, move, move, null);
        return result;
    }

    private static BitmapDrawable rotateBitmap(Context context, Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap bitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        return new BitmapDrawable(context.getResources(), bitmap);
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
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

    public static boolean isTrolley(String routeId) {
        String[] trolleyRouteIds = new String[]{"10", "11", "13", "15", "34", "36", "101", "102"};
        return Arrays.asList(trolleyRouteIds).contains(routeId);
    }

    public static boolean isATransitViewFavorite(String favoriteKey) {
        return TransitViewFavorite.TRANSITVIEW.equals(favoriteKey.split(Favorite.FAVORITE_KEY_DELIM)[0]);
    }
}

package org.septa.android.app.locationpicker;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.septa.android.app.domain.StopModel;
import org.septa.android.app.support.Consumer;
import org.septa.android.app.support.Criteria;
import org.septa.android.app.support.CursorAdapterSupplier;
import org.septa.android.app.support.LocationMathHelper;

import java.util.LinkedList;
import java.util.List;

public class FinderClosestStopTask extends AsyncTask<Location, Void, StopModel> {
    private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;
    private Context context;
    private Consumer<StopModel> consumer;

    FinderClosestStopTask(Context context, CursorAdapterSupplier<StopModel> cursorAdapterSupplier, Consumer<StopModel> consumer) {
        this.cursorAdapterSupplier = cursorAdapterSupplier;
        this.context = context;
        this.consumer = consumer;
    }

    @Override
    protected StopModel doInBackground(Location... locations) {
        Location location = locations[0];

        StopModel closestStop = null;
        if (location != null) {
            LatLng center = new LatLng(location.getLatitude(), location.getLongitude());

            List<Criteria> criteria = new LinkedList<>();
            criteria.add(new Criteria("stop_lon", Criteria.Operation.GT, LocationMathHelper.calculateDerivedPosition(center, 2, 270).longitude));
            criteria.add(new Criteria("stop_lon", Criteria.Operation.LT, LocationMathHelper.calculateDerivedPosition(center, 2, 90).longitude));
            criteria.add(new Criteria("stop_lat", Criteria.Operation.LT, LocationMathHelper.calculateDerivedPosition(center, 2, 0).latitude));
            criteria.add(new Criteria("stop_lat", Criteria.Operation.GT, LocationMathHelper.calculateDerivedPosition(center, 2, 180).latitude));

            Cursor cursor = null;
            try {
                cursor = cursorAdapterSupplier.getCursor(context, criteria);

                double closestDistance = Double.MAX_VALUE;
                if (cursor.moveToFirst()) {
                    StopModel stop = cursorAdapterSupplier.getCurrentItemFromCursor(cursor);
                    while (stop != null) {
                        LatLng stopPoint = new LatLng(stop.getLatitude(), stop.getLongitude());
                        double distance = LocationMathHelper.distance(center, stopPoint);
                        if (distance < closestDistance) {
                            closestStop = stop;
                            closestDistance = distance;
                        }
                        if (cursor.moveToNext())
                            stop = cursorAdapterSupplier.getCurrentItemFromCursor(cursor);
                        else stop = null;
                    }
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }


        return closestStop;
    }

    @Override
    protected void onPostExecute(StopModel stopModel) {
        consumer.accept(stopModel);
    }

}

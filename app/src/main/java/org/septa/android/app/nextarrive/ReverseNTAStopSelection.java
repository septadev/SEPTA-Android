package org.septa.android.app.nextarrive;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import org.septa.android.app.TransitType;
import org.septa.android.app.database.DatabaseManager;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.support.CrashlyticsManager;
import org.septa.android.app.support.Criteria;
import org.septa.android.app.support.CursorAdapterSupplier;

import java.util.ArrayList;
import java.util.List;

public class ReverseNTAStopSelection extends AsyncTask<Void, Void, Void> {

    private static final String TAG = ReverseNTAStopSelection.class.getSimpleName();

    private boolean found = false;

    private Context context;
    private ReverseNTAStopSelectionListener mListener;
    private TransitType transitType;
    private StopModel oldStart, oldDestination, newStart, newDestination;
    private RouteDirectionModel oldRouteDirectionModel, newRouteDirectionModel;

    private CursorAdapterSupplier<RouteDirectionModel> reverseRouteCursorAdapterSupplier;
    private CursorAdapterSupplier<StopModel> reverseStopCursorAdapterSupplier;

    ReverseNTAStopSelection(Context context, TransitType transitType, RouteDirectionModel routeDirectionModel, StopModel start, StopModel destination) {
        this.context = context;
        if (context instanceof ReverseNTAStopSelectionListener) {
            this.mListener = (ReverseNTAStopSelectionListener) context;
        } else {
            IllegalArgumentException iae = new IllegalArgumentException("Context Must Implement CleanOldDBListener");
            CrashlyticsManager.log(Log.ERROR, TAG, iae.toString());
            throw iae;
        }
        this.transitType = transitType;
        this.oldRouteDirectionModel = routeDirectionModel;
        this.oldStart = start;
        this.oldDestination = destination;

        if (transitType == TransitType.RAIL) {
            reverseRouteCursorAdapterSupplier = DatabaseManager.getInstance(context).getRailRouteCursorAdapterSupplier();
        } else {
            reverseStopCursorAdapterSupplier = DatabaseManager.getInstance(context).getNonRailReverseAdapterSupplier();
            reverseRouteCursorAdapterSupplier = DatabaseManager.getInstance(context).getNonRailReverseRouteCursorAdapterSupplier();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (this.transitType != TransitType.RAIL) {
            // look up reverse direction stop IDs
            newDestination = getReverseStopID(oldStart.getStopId(), oldRouteDirectionModel.getRouteShortName());
            newStart = getReverseStopID(oldDestination.getStopId(), oldRouteDirectionModel.getRouteShortName());

            if (newDestination != null && newStart != null) {
                found = true;
            }
        } else {
            // reverse rail stops
            found = true;
            newDestination = oldStart;
            newStart = oldDestination;
        }

        if (found) {
            List<Criteria> criterias = new ArrayList<>(2);
            criterias.add(new Criteria("dircode", Criteria.Operation.EQ, oldRouteDirectionModel.getReverseDirectionCode()));
            criterias.add(new Criteria("route_id", Criteria.Operation.EQ, oldRouteDirectionModel.getRouteId()));

            // look up new route direction model
            Cursor cursor = reverseRouteCursorAdapterSupplier.getCursor(context, criterias);
            if (cursor.moveToFirst()) {
                newRouteDirectionModel = reverseRouteCursorAdapterSupplier.getCurrentItemFromCursor(cursor);
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (isCancelled()) {
            return;
        }

        if (found) {
            mListener.reverseTrip(transitType, newRouteDirectionModel, newStart, newDestination);
        } else {
            mListener.noReverseStopsFound();
        }
    }

    private StopModel getReverseStopID(String stopId, String routeShortName) {
        List<Criteria> criteria = new ArrayList<>(2);
        criteria.add(new Criteria("route_short_name", Criteria.Operation.EQ, routeShortName));
        criteria.add(new Criteria("stop_id", Criteria.Operation.EQ, stopId));
        Cursor cursor = reverseStopCursorAdapterSupplier.getCursor(context, criteria);
        if (cursor.moveToFirst()) {
            return reverseStopCursorAdapterSupplier.getCurrentItemFromCursor(cursor);
        } else {
            return null;
        }
    }

    public interface ReverseNTAStopSelectionListener {
        void noReverseStopsFound();

        void reverseTrip(TransitType transitType, RouteDirectionModel newRouteDirectionModel, StopModel newStart, StopModel newDestination);
    }
}

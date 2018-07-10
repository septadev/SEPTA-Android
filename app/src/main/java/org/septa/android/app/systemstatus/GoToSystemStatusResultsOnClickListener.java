package org.septa.android.app.systemstatus;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import org.septa.android.app.Constants;
import org.septa.android.app.MainActivity;
import org.septa.android.app.TransitType;
import org.septa.android.app.nextarrive.NextToArriveResultsActivity;
import org.septa.android.app.support.AnalyticsManager;
import org.septa.android.app.transitview.TransitViewResultsActivity;

public class GoToSystemStatusResultsOnClickListener implements View.OnClickListener {

    private static final String TAG = GoToSystemStatusResultsOnClickListener.class.getSimpleName();

    private String statusType;
    private Context context;
    private TransitType transitType;
    private String routeId;
    private String routeName;

    public GoToSystemStatusResultsOnClickListener(String statusType, Context activity, TransitType transitType, String routeId, String routeName) {
        this.statusType = statusType;
        this.context = activity;
        this.transitType = transitType;
        this.routeId = routeId;
        this.routeName = routeName;
    }

    @Override
    public void onClick(View v) {
        if (context != null) {
            Intent intent = new Intent(context, SystemStatusResultsActivity.class);
            intent.putExtra(Constants.ROUTE_NAME, routeName);
            intent.putExtra(Constants.ROUTE_ID, routeId);
            intent.putExtra(Constants.TRANSIT_TYPE, transitType);
            intent.putExtra(statusType, Boolean.TRUE);

            // track analytics about user origin
            if (NextToArriveResultsActivity.class.equals(context.getClass())) {
                AnalyticsManager.logContentType(TAG, AnalyticsManager.CUSTOM_EVENT_SYSTEM_STATUS_FROM_NTA, AnalyticsManager.CUSTOM_EVENT_ID_SYSTEM_STATUS, null);
            } else if (TransitViewResultsActivity.class.equals(context.getClass())) {
                AnalyticsManager.logContentType(TAG, AnalyticsManager.CUSTOM_EVENT_SYSTEM_STATUS_FROM_TRANSITVIEW, AnalyticsManager.CUSTOM_EVENT_ID_SYSTEM_STATUS, null);
            } else if (MainActivity.class.equals(context.getClass())) {
                AnalyticsManager.logContentType(TAG, AnalyticsManager.CUSTOM_EVENT_SYSTEM_STATUS_FROM_FAVORITES, AnalyticsManager.CUSTOM_EVENT_ID_SYSTEM_STATUS, null);
            } else {
                Log.e(TAG, String.format("Could not track event analytics for target class: %s", context.getClass()));
            }

            context.startActivity(intent);
        }
    }
}
package org.septa.android.app.systemstatus;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import org.septa.android.app.ActivityClass;
import org.septa.android.app.Constants;
import org.septa.android.app.TransitType;
import org.septa.android.app.support.AnalyticsManager;

public class GoToSystemStatusResultsOnClickListener implements View.OnClickListener {

    private static final String TAG = GoToSystemStatusResultsOnClickListener.class.getSimpleName();

    private String statusType;
    private Activity activity;
    private TransitType transitType;
    private String routeId;
    private String routeName;
    private ActivityClass origin;

    public GoToSystemStatusResultsOnClickListener(String statusType, Activity activity, TransitType transitType, String routeId, String routeName, ActivityClass origin) {
        this.statusType = statusType;
        this.activity = activity;
        this.transitType = transitType;
        this.routeId = routeId;
        this.routeName = routeName;
        this.origin = origin;
    }

    @Override
    public void onClick(View v) {
        if (activity != null) {
            Intent intent = new Intent(activity, SystemStatusResultsActivity.class);
            intent.putExtra(Constants.ROUTE_NAME, routeName);
            intent.putExtra(Constants.ROUTE_ID, routeId);
            intent.putExtra(Constants.TRANSIT_TYPE, transitType);
            intent.putExtra(statusType, Boolean.TRUE);

            // track analytics about user origin
            if (ActivityClass.NEXT_TO_ARRIVE.equals(origin)) {
                AnalyticsManager.logContentViewEvent(TAG, AnalyticsManager.CONTENT_VIEW_EVENT_SYSTEM_STATUS_FROM_NTA, AnalyticsManager.CONTENT_ID_SYSTEM_STATUS, null);
            } else if (ActivityClass.TRANSITVIEW.equals(origin)) {
                AnalyticsManager.logContentViewEvent(TAG, AnalyticsManager.CONTENT_VIEW_EVENT_SYSTEM_STATUS_FROM_TRANSITVIEW, AnalyticsManager.CONTENT_ID_SYSTEM_STATUS, null);
            } else if (ActivityClass.MAIN.equals(origin)) {
                AnalyticsManager.logContentViewEvent(TAG, AnalyticsManager.CONTENT_VIEW_EVENT_SYSTEM_STATUS_FROM_FAVORITES, AnalyticsManager.CONTENT_ID_SYSTEM_STATUS, null);
            } else {
                Log.e(TAG, String.format("Could not track event analytics for target class: %s", origin));
            }

            activity.startActivityForResult(intent, Constants.SYSTEM_STATUS_REQUEST);
        }
    }
}
package org.septa.android.app.systemstatus;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import org.septa.android.app.Constants;
import org.septa.android.app.TransitType;

public class GoToSystemStatusResultsOnClickListener implements View.OnClickListener {
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
            context.startActivity(intent);
        }
    }
}
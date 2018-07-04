package org.septa.android.app.transitview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.services.apiinterfaces.model.Alert;
import org.septa.android.app.support.CrashlyticsManager;
import org.septa.android.app.systemstatus.SystemStatusState;
import org.septa.android.app.view.TextView;

public class TransitViewRouteCard extends LinearLayout {

    private static final String TAG = TransitViewRouteCard.class.getSimpleName();

    private Context context;
    private TransitViewRouteCardListener mListener;
    private String routeId;
    private int sequence;
    private boolean isAdvisory, isAlert, isDetour, isWeather;

    // layout variables
    private ImageView deleteButton;
    private TextView routeIdText;
    private View alertIconsContainer;
    private ImageView advisoryIcon, alertIcon, detourIcon, weatherIcon;

    public TransitViewRouteCard(@NonNull Context context, String routeId, int sequence) {
        super(context);

        if (context instanceof TransitViewRouteCardListener) {
            mListener = (TransitViewRouteCardListener) context;
        } else {
            IllegalArgumentException iae = new IllegalArgumentException("Context Must Implement TransitViewRouteCardListener");
            CrashlyticsManager.log(Log.ERROR, TAG, iae.toString());
            throw iae;
        }

        this.context = context;
        this.routeId = routeId;
        this.sequence = sequence;

        initializeView();
    }

    private void initializeView() {
        View rootView = inflate(context, R.layout.item_transitview_route_card, this);

        deleteButton = rootView.findViewById(R.id.delete_route);
        routeIdText = rootView.findViewById(R.id.transitview_card_route_id);
        alertIconsContainer = rootView.findViewById(R.id.route_alert_icons);
        advisoryIcon = rootView.findViewById(R.id.advisory_icon);
        alertIcon = rootView.findViewById(R.id.alert_icon);
        detourIcon = rootView.findViewById(R.id.detour_icon);
        weatherIcon = rootView.findViewById(R.id.weather_icon);

        routeIdText.setText(routeId);
        // TODO: set transit type icon

        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.removeRoute(sequence);
            }
        });

        alertIconsContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onAlertIconsClicked(routeId);
            }
        });

        // TODO: fetch new alerts every 30 seconds
        refreshAlertsView();
    }

    private void refreshAlertsView() {
        TransitType transitType = TransitViewUtils.isTrolley(routeId) ? TransitType.TROLLEY : TransitType.BUS;
        Alert routeAlerts = SystemStatusState.getAlertForLine(transitType, routeId);
        isAdvisory = routeAlerts.isAdvisory();
        isAlert = routeAlerts.isAlert() || routeAlerts.isSuspended();
        isDetour = routeAlerts.isDetour();
        isWeather = routeAlerts.isSnow();

        if (isAdvisory) {
            advisoryIcon.setVisibility(View.VISIBLE);
        } else {
            advisoryIcon.setVisibility(View.GONE);
        }

        if (isAlert) {
            alertIcon.setVisibility(View.VISIBLE);
        } else {
            alertIcon.setVisibility(View.GONE);
        }

        if (isDetour) {
            detourIcon.setVisibility(View.VISIBLE);
        } else {
            detourIcon.setVisibility(View.GONE);
        }

        if (isWeather) {
            weatherIcon.setVisibility(View.VISIBLE);
        } else {
            weatherIcon.setVisibility(View.GONE);
        }
    }

    public interface TransitViewRouteCardListener {
        void removeRoute(int sequence);

        void onAlertIconsClicked(String routeId);
    }
}

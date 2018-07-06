package org.septa.android.app.transitview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.services.apiinterfaces.model.Alert;
import org.septa.android.app.support.CrashlyticsManager;
import org.septa.android.app.systemstatus.GoToSystemStatusResultsOnClickListener;
import org.septa.android.app.systemstatus.SystemStatusState;
import org.septa.android.app.view.TextView;

public class TransitViewRouteCard extends LinearLayout {

    private static final String TAG = TransitViewRouteCard.class.getSimpleName();

    private Context context;
    private TransitViewRouteCardListener mListener;
    private RouteDirectionModel route;
    private int sequence;
    private boolean isAdvisory, isAlert, isDetour, isWeather;

    // layout variables
    LinearLayout rootView;
    private ImageView deleteButton;
    private TextView routeIdText;
    private View line;
    private ImageView advisoryIcon, alertIcon, detourIcon, weatherIcon;

    public TransitViewRouteCard(@NonNull Context context, RouteDirectionModel route, int sequence) {
        super(context);

        if (context instanceof TransitViewRouteCardListener) {
            mListener = (TransitViewRouteCardListener) context;
        } else {
            IllegalArgumentException iae = new IllegalArgumentException("Context Must Implement TransitViewRouteCardListener");
            CrashlyticsManager.log(Log.ERROR, TAG, iae.toString());
            throw iae;
        }

        this.context = context;
        this.route = route;
        this.sequence = sequence;

        initializeView();
    }

    private void initializeView() {
        rootView = (LinearLayout) inflate(context, R.layout.item_transitview_route_card, this);

        deleteButton = rootView.findViewById(R.id.delete_route);
        routeIdText = rootView.findViewById(R.id.transitview_card_route_id);
        line = rootView.findViewById(R.id.transitview_card_line);
        advisoryIcon = rootView.findViewById(R.id.advisory_icon);
        alertIcon = rootView.findViewById(R.id.alert_icon);
        detourIcon = rootView.findViewById(R.id.detour_icon);
        weatherIcon = rootView.findViewById(R.id.weather_icon);

        // shorten BLVDDIR and the LUCY lines
        shortenRouteNames();

        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.removeRoute(sequence);
            }
        });

        TransitType transitType = TransitViewUtils.isTrolley(route.getRouteId()) ? TransitType.TROLLEY : TransitType.BUS;
        // set transit type icon
        routeIdText.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, transitType.getTabActiveImageResource()), null, null, null);

        // clicking on advisory icon links to system status
        advisoryIcon.setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.SERVICE_ADVISORY_EXPANDED, context, transitType, route.getRouteId(), route.getRouteShortName()));
        advisoryIcon.setContentDescription(R.string.advisory_icon_content_description_prefix + route.getRouteShortName());

        // clicking on alert icon links to system status
        alertIcon.setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.SERVICE_ALERT_EXPANDED, context, transitType, route.getRouteId(), route.getRouteShortName()));
        alertIcon.setContentDescription(R.string.alert_icon_content_description_prefix + route.getRouteShortName());

        // clicking on detour icon links to system status
        detourIcon.setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.ACTIVE_DETOUR_EXPANDED, context, transitType, route.getRouteId(), route.getRouteShortName()));
        detourIcon.setContentDescription(R.string.detour_icon_content_description_prefix + route.getRouteShortName());

        // clicking on weather icon links to system status
        weatherIcon.setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.WEATHER_ALERTS_EXPANDED, context, transitType, route.getRouteId(), route.getRouteShortName()));
        weatherIcon.setContentDescription(R.string.weather_icon_content_description_prefix + route.getRouteShortName());

        refreshAlertsView();
    }

    public void refreshAlertsView() {
        TransitType transitType = TransitViewUtils.isTrolley(route.getRouteId()) ? TransitType.TROLLEY : TransitType.BUS;
        Alert routeAlerts = SystemStatusState.getAlertForLine(transitType, route.getRouteId());
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

    public void hideAlertIcons() {
        advisoryIcon.setVisibility(View.GONE);
        alertIcon.setVisibility(View.GONE);
        detourIcon.setVisibility(View.GONE);
        weatherIcon.setVisibility(View.GONE);
    }

    public void activateCard() {
        rootView.setBackground(ContextCompat.getDrawable(context, R.drawable.transitview_active_route_border));
        line.setBackgroundColor(ContextCompat.getColor(context, R.color.transitview_card_active_line));
        deleteButton.setVisibility(View.VISIBLE);

        // readd margins that were removed when card background was changed
        LayoutParams params = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        params.setMargins(21, 0, 0, 0);
        rootView.setLayoutParams(params);

        // make alert icons clickable
        advisoryIcon.setClickable(true);
        alertIcon.setClickable(true);
        detourIcon.setClickable(true);
        weatherIcon.setClickable(true);
    }

    public void deactivateCard() {
        rootView.setBackgroundColor(ContextCompat.getColor(context, R.color.transitview_card_inactive_background));
        line.setBackgroundColor(ContextCompat.getColor(context, R.color.transitview_card_inactive_line));
        deleteButton.setVisibility(View.INVISIBLE);

        // readd margins that were removed when card background was changed
        LayoutParams params = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        params.setMargins(21, 0, 0, 0);
        rootView.setLayoutParams(params);

        // disable alert icons
        advisoryIcon.setClickable(false);
        alertIcon.setClickable(false);
        detourIcon.setClickable(false);
        weatherIcon.setClickable(false);
    }

    private void shortenRouteNames() {
        String routeId = route.getRouteId();
        if ("BLVDDIR".equals(routeId)) {
            routeId = "BLVD";
        } else if ("LUCYGO".equals(routeId)) {
            routeId = "LUGO";
        } else if ("LUCYGR".equals(routeId)) {
            routeId = "LUGR";
        }
        routeIdText.setText(routeId);
    }

    public interface TransitViewRouteCardListener {
        void removeRoute(int sequence);
    }
}

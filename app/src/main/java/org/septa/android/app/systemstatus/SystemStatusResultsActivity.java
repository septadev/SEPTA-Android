package org.septa.android.app.systemstatus;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.CompoundButton;

import org.septa.android.app.BaseActivity;
import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.notifications.PushNotificationManager;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.AlertDetail;
import org.septa.android.app.support.GeneralUtils;
import org.septa.android.app.view.TextView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SystemStatusResultsActivity extends BaseActivity {

    private static final String TAG = SystemStatusResultsActivity.class.getSimpleName();
    private TransitType transitType;

    TextView serviceAdvisory;
    TextView serviceAdvisoryDetails;
    boolean serviceAdvisoryExpanded = false;

    TextView serviceAlert;
    TextView serviceAlertDetails;
    boolean serviceAlertExpanded = false;

    TextView activeDetour;
    TextView activeDetourDetails;
    boolean activeDetourExpanded = false;

    TextView weatherAlerts;
    TextView weatherAlertsDetails;
    boolean weatherAlertsExpanded = false;

    String routeId;
    String routeName;

    View notificationPreferences;
    SwitchCompat notifsSubscribeSwitch;

    View progressView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_status_results);
        setTitle("System Status:");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        progressView = findViewById(R.id.progress_view);
        progressView.setVisibility(View.VISIBLE);

        if (savedInstanceState != null) {
            restoreSaveInstanceState(savedInstanceState);
        } else {
            Intent intent = getIntent();
            if (intent != null) {
                restoreSaveInstanceState(getIntent().getExtras());
            }
        }

        if (transitType == null || routeId == null) {
            onBackPressed();
        }

        setTitle(transitType.getString("system_status_results_title", this));

        TextView title = findViewById(R.id.line_title);
        title.setText(routeName + " Status");

        // add line color bullet
        int color;
        try {
            color = ContextCompat.getColor(this, transitType.getLineColor(routeId, this));
        } catch (Exception e) {
            color = ContextCompat.getColor(this, R.color.default_line_color);
        }
        Drawable[] drawables = title.getCompoundDrawables();
        Drawable bullet = drawables[2];
        bullet.setColorFilter(color, PorterDuff.Mode.SRC);
        title.setCompoundDrawablesWithIntrinsicBounds(null, null, bullet, null);

        Drawable inactiveToggle = ContextCompat.getDrawable(this, R.drawable.alert_toggle_closed).mutate();
        inactiveToggle.setAlpha(77);

        serviceAdvisory = findViewById(R.id.service_advisory);
        serviceAdvisoryDetails = findViewById(R.id.service_advisory_details);
        serviceAdvisoryDetails.setMovementMethod(LinkMovementMethod.getInstance());
        Drawable[] serviceAdvisoryDrawables = serviceAdvisory.getCompoundDrawables();
        serviceAdvisory.setCompoundDrawablesWithIntrinsicBounds(serviceAdvisoryDrawables[0], serviceAdvisoryDrawables[1], inactiveToggle, serviceAdvisoryDrawables[3]);

        serviceAlert = findViewById(R.id.service_alert);
        serviceAlertDetails = findViewById(R.id.service_alert_details);
        serviceAlertDetails.setMovementMethod(LinkMovementMethod.getInstance());
        Drawable[] serviceAlertDrawables = serviceAlert.getCompoundDrawables();
        serviceAlert.setCompoundDrawablesWithIntrinsicBounds(serviceAlertDrawables[0], serviceAlertDrawables[1], inactiveToggle, serviceAlertDrawables[3]);

        activeDetour = findViewById(R.id.active_detour);
        activeDetourDetails = findViewById(R.id.active_detour_details);
        activeDetourDetails.setMovementMethod(LinkMovementMethod.getInstance());
        Drawable[] activeDetourDrawables = activeDetour.getCompoundDrawables();
        activeDetour.setCompoundDrawablesWithIntrinsicBounds(activeDetourDrawables[0], activeDetourDrawables[1], inactiveToggle, activeDetourDrawables[3]);

        weatherAlerts = findViewById(R.id.weather_alerts);
        weatherAlertsDetails = findViewById(R.id.weather_alerts_details);
        weatherAlertsDetails.setMovementMethod(LinkMovementMethod.getInstance());
        Drawable[] weatherDrawables = weatherAlerts.getCompoundDrawables();
        weatherAlerts.setCompoundDrawablesWithIntrinsicBounds(weatherDrawables[0], weatherDrawables[1], inactiveToggle, weatherDrawables[3]);

        SeptaServiceFactory.getAlertDetailsService().getAlertDetails(transitType.getAlertId(routeId)).enqueue(new Callback<AlertDetail>() {
            @Override
            public void onResponse(Call<AlertDetail> call, Response<AlertDetail> response) {
                if (response.body() != null) {
                    applyAlerts(response.body());
                }
            }

            @Override
            public void onFailure(Call<AlertDetail> call, Throwable t) {
                SeptaServiceFactory.displayWebServiceError(findViewById(R.id.system_status_results_coordinator), SystemStatusResultsActivity.this);
            }
        });

        // link to notification settings
        notificationPreferences = findViewById(R.id.notification_preferences);
        notificationPreferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNotificationsManagement();
            }
        });

        // set initial checked state of switch
        notifsSubscribeSwitch = findViewById(R.id.subscribe_notifications_switch);
        notifsSubscribeSwitch.setChecked(SeptaServiceFactory.getNotificationsService().isSubscribedToRoute(SystemStatusResultsActivity.this, routeId));

        // switch to create / enable notification for this route
        notifsSubscribeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // enable notifs for route
                    PushNotificationManager.getInstance(SystemStatusResultsActivity.this).createNotificationForRoute(routeId, routeName, transitType);
                } else {
                    // disable notifs for route
                    PushNotificationManager.getInstance(SystemStatusResultsActivity.this).removeNotificationForRoute(routeId, transitType);
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(Constants.ROUTE_ID, routeId);
        outState.putSerializable(Constants.ROUTE_NAME, routeName);
        outState.putSerializable(Constants.TRANSIT_TYPE, transitType);
        outState.putSerializable(Constants.SERVICE_ADVISORY_EXPANDED, serviceAdvisoryExpanded);
        outState.putSerializable(Constants.SERVICE_ALERT_EXPANDED, serviceAlertExpanded);
        outState.putSerializable(Constants.ACTIVE_DETOUR_EXPANDED, activeDetourExpanded);
        outState.putSerializable(Constants.WEATHER_ALERTS_EXPANDED, weatherAlertsExpanded);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void restoreSaveInstanceState(Bundle inState) {
        if (inState.containsKey(Constants.ROUTE_DIRECTION_MODEL) && inState.get(Constants.ROUTE_DIRECTION_MODEL) != null) {
            RouteDirectionModel routeDirectionModel = (RouteDirectionModel) inState.get(Constants.ROUTE_DIRECTION_MODEL);
            routeId = routeDirectionModel.getRouteId();
            routeName = routeDirectionModel.getRouteShortName();
        } else {
            routeId = inState.getString(Constants.ROUTE_ID);
            routeName = inState.getString(Constants.ROUTE_NAME);
        }
        transitType = (TransitType) inState.get(Constants.TRANSIT_TYPE);

        if (inState.containsKey(Constants.SERVICE_ADVISORY_EXPANDED)) {
            serviceAdvisoryExpanded = inState.getBoolean(Constants.SERVICE_ADVISORY_EXPANDED);
        }
        if (inState.containsKey(Constants.SERVICE_ALERT_EXPANDED)) {
            serviceAlertExpanded = inState.getBoolean(Constants.SERVICE_ALERT_EXPANDED);
        }
        if (inState.containsKey(Constants.ACTIVE_DETOUR_EXPANDED)) {
            activeDetourExpanded = inState.getBoolean(Constants.ACTIVE_DETOUR_EXPANDED);
        }
        if (inState.containsKey(Constants.WEATHER_ALERTS_EXPANDED)) {
            weatherAlertsExpanded = inState.getBoolean(Constants.WEATHER_ALERTS_EXPANDED);
        }
    }

    private void handleExpand(boolean expanded, TextView header, TextView details) {
        if (!expanded) {
            Drawable[] drawables = header.getCompoundDrawables();
            header.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1],
                    ContextCompat.getDrawable(this, R.drawable.alert_toggle_open), drawables[3]);
            details.setVisibility(View.VISIBLE);
        } else {
            Drawable[] drawables = header.getCompoundDrawables();
            header.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1],
                    ContextCompat.getDrawable(this, R.drawable.alert_toggle_closed), drawables[3]);
            details.setVisibility(View.GONE);
        }
    }

    private void applyAlerts(AlertDetail alertDetail) {
        List<AlertDetail.Detail> detailList = alertDetail.getAlerts();
        int advisoryCount = 0;
        StringBuilder advisoryBuilder = new StringBuilder();
        int alertCount = 0;
        StringBuilder alertBuilder = new StringBuilder();
        int detourCount = 0;
        StringBuilder detourBuilder = new StringBuilder();
        int weatherCount = 0;
        StringBuilder weatherBuilder = new StringBuilder();

        for (AlertDetail.Detail alertItem : detailList) {
            if (!alertItem.getAdvisoryMessage().trim().isEmpty()) {
                advisoryCount++;
                advisoryBuilder.append("<b>Advisory</b><p>").append(alertItem.getAdvisoryMessage()).append("<p>");
            }

            if (alertItem.getDetour() != null && !alertItem.getDetour().getMessage().trim().isEmpty()) {
                detourCount++;
                detourBuilder.append("<b>Start Location:</b> ").append(alertItem.getDetour().getStartLocation()).append("<br>");
                detourBuilder.append("<b>Start Date:</b> ").append(alertItem.getDetour().getStartDateTime()).append("<br>");
                detourBuilder.append("<b>End Date:</b> ").append(alertItem.getDetour().getEndDateTime()).append("<br>");
                detourBuilder.append("<b>Reason For Detour:</b> ").append(alertItem.getDetour().getReason()).append("<br>");
                detourBuilder.append("<b>Details:</b> ").append(alertItem.getDetour().getMessage()).append("<p>");
            }

            if (alertItem.isSnow()) {
                weatherCount++;
                weatherBuilder.append("<b>Weather</b><p>").append(alertItem.getMessage()).append("<p>");
            } else {
                if (!alertItem.getMessage().trim().isEmpty()) {
                    alertCount++;
                    alertBuilder.append("<b>Alert</b><p>").append(alertItem.getMessage()).append("<p>");
                }
            }

            progressView.setVisibility(View.GONE);
        }

        Drawable active_toggle = ContextCompat.getDrawable(this, R.drawable.alert_toggle_closed);

        if (advisoryCount > 0) {
            serviceAdvisory.setClickable(true);
            serviceAdvisory.setHtml("Service Advisories: <b>" + advisoryCount + "</b>");
            Drawable[] drawables = serviceAdvisory.getCompoundDrawables();
            serviceAdvisory.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], active_toggle, drawables[3]);
            handleExpand(!serviceAdvisoryExpanded, serviceAdvisory, serviceAdvisoryDetails);

            serviceAdvisory
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            handleExpand(serviceAdvisoryExpanded, (TextView) v, serviceAdvisoryDetails);
                            serviceAdvisoryExpanded = !serviceAdvisoryExpanded;
                        }
                    });

            serviceAdvisoryDetails.setHtml(GeneralUtils.updateUrls(advisoryBuilder.toString()));

        }

        if (alertCount > 0) {
            serviceAlert.setClickable(true);
            serviceAlert.setHtml("Service Detail: <b>" + alertCount + "</b>");
            Drawable[] drawables = serviceAlert.getCompoundDrawables();
            serviceAlert.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], active_toggle, drawables[3]);
            handleExpand(!serviceAlertExpanded, serviceAlert, serviceAlertDetails);

            serviceAlert
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            handleExpand(serviceAlertExpanded, (TextView) v, serviceAlertDetails);
                            serviceAlertExpanded = !serviceAlertExpanded;
                        }
                    });

            serviceAlertDetails.setHtml(GeneralUtils.updateUrls(alertBuilder.toString()));
        }
        if (detourCount > 0) {
            activeDetour.setClickable(true);
            activeDetour.setHtml("Active Detours: <b>" + detourCount + "</b>");
            Drawable[] drawables = activeDetour.getCompoundDrawables();
            activeDetour.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], active_toggle, drawables[3]);
            handleExpand(!activeDetourExpanded, activeDetour, activeDetourDetails);
            activeDetour
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            handleExpand(activeDetourExpanded, (TextView) v, activeDetourDetails);
                            activeDetourExpanded = !activeDetourExpanded;
                        }
                    });

            activeDetourDetails.setHtml(GeneralUtils.updateUrls(detourBuilder.toString()));
        }

        if (weatherCount > 0) {
            weatherAlerts.setClickable(true);
            weatherAlerts.setHtml("Active Weather Detail: <b>" + detourCount + "</b>");
            Drawable[] drawables = weatherAlerts.getCompoundDrawables();
            weatherAlerts.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], active_toggle, drawables[3]);
            handleExpand(!weatherAlertsExpanded, weatherAlerts, weatherAlertsDetails);

            weatherAlerts
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            handleExpand(weatherAlertsExpanded, (TextView) v, weatherAlertsDetails);
                            weatherAlertsExpanded = !weatherAlertsExpanded;
                        }
                    });

            weatherAlertsDetails.setHtml(GeneralUtils.updateUrls(weatherBuilder.toString()));
        }
    }

    private void goToNotificationsManagement() {
        setResult(Constants.VIEW_NOTIFICATION_MANAGEMENT, new Intent());
        finish();
    }
}
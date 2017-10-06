package org.septa.android.app.systemstatus;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.View;

import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.AlertDetail;
import org.septa.android.app.support.GeneralUtils;
import org.septa.android.app.view.TextView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by jkampf on 9/13/17.
 */

public class SystemStatusResultsActivity extends AppCompatActivity {

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
    boolean activeDetourtExpanded = false;

    TextView weatherAlerts;
    TextView weatherAlertsDetails;
    boolean weatherAlertsExpanded = false;

    String routeId;
    String routeName;

    View progressView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.system_status_main);
        setTitle("System Status:");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progressView = findViewById(R.id.progress_view);
        progressView.setVisibility(View.VISIBLE);

        Intent intent = getIntent();

        if (intent.getExtras().containsKey(Constants.ROUTE_DIRECTION_MODEL) && intent.getExtras().get(Constants.ROUTE_DIRECTION_MODEL) != null) {
            RouteDirectionModel routeDirectionModel = (RouteDirectionModel) intent.getExtras().get(Constants.ROUTE_DIRECTION_MODEL);
            routeId = routeDirectionModel.getRouteId();
            routeName = routeDirectionModel.getRouteShortName();
        } else {
            routeId = intent.getExtras().getString(Constants.ROUTE_ID);
            routeName = intent.getExtras().getString(Constants.ROUTE_NAME);
        }
        transitType = (TransitType) intent.getExtras().get(Constants.TRANSIT_TYPE);

        if (routeId == null) {
            StringBuilder builder = new StringBuilder();
            for (String key : intent.getExtras().keySet()){

            }
            throw new RuntimeException("routeId is Null: Intent[" + intent.getExtras().toString() + "]");
        }

        if (intent.getExtras().containsKey(Constants.SERVICE_ADVISORY_EXPANDED)) {
            serviceAdvisoryExpanded = intent.getExtras().getBoolean(Constants.SERVICE_ADVISORY_EXPANDED);
        }
        if (intent.getExtras().containsKey(Constants.SERVICE_ALERT_EXPANDED)) {
            serviceAlertExpanded = intent.getExtras().getBoolean(Constants.SERVICE_ALERT_EXPANDED);
        }
        if (intent.getExtras().containsKey(Constants.ACTIVE_DETOURT_EXPANDED)) {
            activeDetourtExpanded = intent.getExtras().getBoolean(Constants.ACTIVE_DETOURT_EXPANDED);
        }
        if (intent.getExtras().containsKey(Constants.WEATHER_ALERTS_EXPANDED)) {
            weatherAlertsExpanded = intent.getExtras().getBoolean(Constants.WEATHER_ALERTS_EXPANDED);
        }

        if (transitType == null || routeId == null)
            return;

        setTitle(transitType.getString("system_status_results_title", this));


        TextView title = (TextView) findViewById(R.id.line_title);
        title.setText(routeName + " Status");

        int color;
        try {
            color = ContextCompat.getColor(this, transitType.getLineColor(routeId, this));
        } catch (Exception e) {
            color = ContextCompat.getColor(this, R.color.default_line_color);
        }

        Drawable[] drawables = title.getCompoundDrawables();
        Drawable bullet = drawables[2];
        bullet.setColorFilter(color, PorterDuff.Mode.SRC);

        Drawable inactiveToggle = ContextCompat.getDrawable(this, R.drawable.alert_toggle_closed).mutate();
        inactiveToggle.setAlpha(77);

        serviceAdvisory = (TextView) findViewById(R.id.service_advisory);
        serviceAdvisoryDetails = (TextView) findViewById(R.id.service_advisory_details);
        serviceAdvisoryDetails.setMovementMethod(LinkMovementMethod.getInstance());
        Drawable[] serviceAdvisoryDrawables = serviceAdvisory.getCompoundDrawables();
        serviceAdvisory.
                setCompoundDrawablesWithIntrinsicBounds(serviceAdvisoryDrawables[0], serviceAdvisoryDrawables[1], inactiveToggle, serviceAdvisoryDrawables[3]);

        serviceAlert = (TextView) findViewById(R.id.service_alert);
        serviceAlertDetails = (TextView) findViewById(R.id.service_alert_details);
        serviceAlertDetails.setMovementMethod(LinkMovementMethod.getInstance());
        Drawable[] serviceAlertDrawables = serviceAlert.getCompoundDrawables();
        serviceAlert.setCompoundDrawablesWithIntrinsicBounds(serviceAlertDrawables[0], serviceAlertDrawables[1], inactiveToggle, serviceAlertDrawables[3]);

        activeDetour = (TextView) findViewById(R.id.active_detour);
        activeDetourDetails = (TextView) findViewById(R.id.active_detour_details);
        activeDetourDetails.setMovementMethod(LinkMovementMethod.getInstance());
        Drawable[] activeDetourDrawables = activeDetour.getCompoundDrawables();
        activeDetour.setCompoundDrawablesWithIntrinsicBounds(activeDetourDrawables[0], activeDetourDrawables[1], inactiveToggle, activeDetourDrawables[3]);


        weatherAlerts = (TextView) findViewById(R.id.weather_alerts);
        weatherAlertsDetails = (TextView) findViewById(R.id.weather_alerts_details);
        weatherAlertsDetails.setMovementMethod(LinkMovementMethod.getInstance());
        Drawable[] weatherDrawables = weatherAlerts.getCompoundDrawables();
        weatherAlerts.setCompoundDrawablesWithIntrinsicBounds(weatherDrawables[0], weatherDrawables[1], inactiveToggle, weatherDrawables[3]);


        SeptaServiceFactory.getAlertDetailsService().getAlertDetails(transitType.getAlertId(routeId)).enqueue(new Callback<AlertDetail>() {
            @Override
            public void onResponse(Call<AlertDetail> call, Response<AlertDetail> response) {
                if (response.body() != null)
                    applyAlerts(response.body());
            }

            @Override
            public void onFailure(Call<AlertDetail> call, Throwable t) {

            }
        });

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
            if (!"".equals(alertItem.getAdvisoryMessage())) {
                advisoryCount++;
                advisoryBuilder.append("<b>Advisory</b><p>").append(alertItem.getAdvisoryMessage()).append("<p>");
            }

            if (alertItem.getDetour() != null && !"".equals(alertItem.getDetour().getMessage())) {
                detourCount++;
                detourBuilder.append("<b>Detour</b><p>").append(alertItem.getDetour().getMessage()).append("<p>");

            }

            if (alertItem.isSnow()) {
                weatherCount++;
                weatherBuilder.append("<b>Weather</b><p>").append(alertItem.getMessage()).append("<p>");
            } else {
                if (!"".equals(alertItem.getMessage())) {
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
            handleExpand(!activeDetourtExpanded, activeDetour, activeDetourDetails);
            activeDetour
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            handleExpand(activeDetourtExpanded, (TextView) v, activeDetourDetails);
                            activeDetourtExpanded = !activeDetourtExpanded;
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}

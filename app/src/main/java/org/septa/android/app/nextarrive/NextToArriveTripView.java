package org.septa.android.app.nextarrive;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.septa.android.app.Constants;
import org.septa.android.app.MainActivity;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.services.apiinterfaces.model.Alert;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalModelResponse;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalModelResponse.NextArrivalRecord;
import org.septa.android.app.support.Consumer;
import org.septa.android.app.support.CrashlyticsManager;
import org.septa.android.app.support.GeneralUtils;
import org.septa.android.app.systemstatus.GoToSystemStatusResultsOnClickListener;
import org.septa.android.app.systemstatus.SystemStatusState;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NextToArriveTripView extends FrameLayout {

    static String TAG = NextToArriveTripView.class.getSimpleName();

    private TransitType transitType;

    private StopModel start;
    private StopModel destination;
    private RouteDirectionModel routeDirectionModel;

    private Consumer<Integer> onFirstElementHeight;

    private TimeUnit resultsTimeUnit;
    private Integer resultsTime;

    private Integer maxResults = 10;

    private static Map<Integer, StopModel> connectionStations = new HashMap<>();

    static {
        // 90004,'30th Street Station','39.9566667','-75.1816667'
        connectionStations.put(90004, new StopModel("90004", "30th Street Station", 0, true, "39.9566667", "-75.1816667"));

        // 90005,'Suburban Station','39.9538889','-75.1677778'
        connectionStations.put(90005, new StopModel("90005", "Suburban Station", 0, true, "39.9538889", "-75.1677778"));

        //90006,'Jefferson Station','39.9525','-75.1580556'
        connectionStations.put(90006, new StopModel("90006", "Jefferson Station", 0, true, "39.9525", "-75.1580556"));

        //90007,'Temple University','39.9813889','-75.1494444'
        connectionStations.put(90007, new StopModel("90007", "Temple University", 0, true, "39.9813889", "-75.1494444"));
    }

    public NextToArriveTripView(@NonNull Context context) {
        super(context);
        init();
    }

    public NextToArriveTripView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NextToArriveTripView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_next_to_arrive_trip, this);
    }

    public void setNextToArriveData(NextArrivalModelResponseParser parser) {
        final LinearLayout listView = findViewById(R.id.lines_list_view);
        listView.removeAllViews();

        if (parser == null)
            return;

        List<NextArrivalModelResponse.NextArrivalRecord> data = parser.getResults();

        Collections.sort(data, new Comparator<NextArrivalRecord>() {
            @Override
            public int compare(NextArrivalRecord o1, NextArrivalRecord o2) {
                int result = o1.getOrigDepartureTime().compareTo(o2.getOrigDepartureTime());
                if (result != 0) return result;
                result = o1.getOrigArrivalTime().compareTo(o2.getOrigArrivalTime());
                return result;
            }
        });

        if (resultsTime != null && resultsTimeUnit != null) {
            int index = 0;
            long cutoff = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(resultsTime, resultsTimeUnit);
            while ((index < data.size() - 1) && (data.get(index).getOrigDepartureTime().getTime() < cutoff)) {
                index++;
            }

            data = data.subList(0, index);
        }

        if (maxResults != null && data.size() > 0) {
            data = data.subList(0, (maxResults <= data.size()) ? maxResults : data.size() - 1);
        }

        String currentLine = null;
        boolean firstPos = true;
        final List<View> peakViews = new LinkedList<>();
        for (NextArrivalModelResponse.NextArrivalRecord item : data) {
            if (item.getConnectionStationId() != null) {
                currentLine = null;
                final View multiView = getMultistopTripView(item);
                if (firstPos && onFirstElementHeight != null) {
                    View peakView = multiView.findViewById(R.id.orig_trip_layout);
                    peakViews.add(peakView);
                    multiView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            int peak = 0;
                            for (View view : peakViews) {
                                peak += view.getMeasuredHeight();
                                peak += view.getPaddingTop();
                                peak += view.getPaddingBottom();
                            }
                            onFirstElementHeight.accept(peak);
                            multiView.getViewTreeObserver().removeOnPreDrawListener(this);
                            return false;
                        }
                    });
                }
                listView.addView(multiView);
            } else {
                if (currentLine == null || !currentLine.equals(item.getOrigRouteId())) {
                    currentLine = item.getOrigRouteId();
                    View headerView = getLineHeader(currentLine, item.getOrigRouteName());
                    if (firstPos) {
                        peakViews.add(headerView);
                    }
                    listView.addView(headerView);
                }
                final View singleView = getSingleStopTripView(item);
                if (firstPos && onFirstElementHeight != null) {
                    peakViews.add(singleView);
                    singleView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            int peak = 0;
                            for (View view : peakViews) {
                                peak += view.getMeasuredHeight();
                                peak += view.getPaddingTop();
                                peak += view.getPaddingBottom();
                            }
                            onFirstElementHeight.accept(peak);

                            singleView.getViewTreeObserver().removeOnPreDrawListener(this);
                            return false;
                        }
                    });
                }
                listView.addView(singleView);
            }

            firstPos = false;
        }

    }

    private View getLineHeader(String lineId, String lineName) {
        Context context = getContext();
        View convertView = LayoutInflater.from(context).inflate(R.layout.next_to_arrive_line, this, false);

        String routeNameForSystemStatus;
        if (transitType == TransitType.RAIL || transitType == TransitType.NHSL) {
            routeNameForSystemStatus = lineName;
        } else {
            routeNameForSystemStatus = routeDirectionModel.getRouteShortName();
        }

        android.widget.TextView lineNameText = convertView.findViewById(R.id.orig_line_name_text);
        lineNameText.setText(lineName);

        ((ImageView) convertView.findViewById(R.id.orig_line_marker_left)).setColorFilter(ContextCompat.getColor(getContext(), transitType.getLineColor(lineId, context)));

        Alert alert = SystemStatusState.getAlertForLine(transitType, lineId);

        if (alert.isAlert()) {
            View targetView = convertView.findViewById(R.id.orig_line_alert_icon);
            targetView.setVisibility(VISIBLE);
            targetView.setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.SERVICE_ALERT_EXPANDED, context, transitType, lineId, routeNameForSystemStatus));
            targetView.setContentDescription(R.string.alert_icon_content_description_prefix + routeNameForSystemStatus);
        }
        if (alert.isAdvisory()) {
            View targetView = convertView.findViewById(R.id.orig_line_advisory_icon);
            targetView.setVisibility(VISIBLE);
            targetView.setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.SERVICE_ADVISORY_EXPANDED, context, transitType, lineId, routeNameForSystemStatus));
            targetView.setContentDescription(R.string.advisory_icon_content_description_prefix + routeNameForSystemStatus);
        }
        if (alert.isDetour()) {
            View targetView = convertView.findViewById(R.id.orig_line_detour_icon);
            targetView.setVisibility(VISIBLE);
            targetView.setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.ACTIVE_DETOUR_EXPANDED, context, transitType, lineId, routeNameForSystemStatus));
            targetView.setContentDescription(R.string.detour_icon_content_description_prefix + routeNameForSystemStatus);
        }
        if (alert.isSnow()) {
            View targetView = convertView.findViewById(R.id.orig_line_weather_icon);
            targetView.setVisibility(VISIBLE);
            targetView.setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.WEATHER_ALERTS_EXPANDED, context, transitType, lineId, routeNameForSystemStatus));
            targetView.setContentDescription(R.string.weather_icon_content_description_prefix + routeNameForSystemStatus);
        }

        return convertView;
    }

    private View getSingleStopTripView(final NextArrivalRecord unit) {

        View line = LayoutInflater.from(getContext()).inflate(R.layout.item_next_to_arrive_unit, this, false);


        DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);

        android.widget.TextView origArrivalTimeText = line.findViewById(R.id.orig_arrival_time_text);
        origArrivalTimeText.setText(dateFormat.format(unit.getOrigDepartureTime()) + " - " + dateFormat.format(unit.getOrigArrivalTime()));
        origArrivalTimeText.setContentDescription("departs " + dateFormat.format(unit.getOrigDepartureTime()) + "  arrives " + dateFormat.format(unit.getOrigArrivalTime()));
        android.widget.TextView origTripNumberText = line.findViewById(R.id.orig_trip_number_text);
        origTripNumberText.setText(unit.getOrigLineTripId() + " to " + unit.getOrigLastStopName());

        android.widget.TextView origDepartureTime = line.findViewById(R.id.orig_depature_time);

        Calendar departureCal = Calendar.getInstance();
        departureCal.setTime(unit.getOrigDepartureTime());
        departureCal.add(Calendar.MINUTE, unit.getOrigDelayMinutes());

        long origDepartureMillis = departureCal.getTimeInMillis() - System.currentTimeMillis();
        if (origDepartureMillis >= 1000 * 60)
            origDepartureTime.setText(GeneralUtils.getDurationAsString(origDepartureMillis, TimeUnit.MILLISECONDS));
        else
            origDepartureTime.setText(R.string.nta_now);

        boolean enableClick = true;
        android.widget.TextView origTardyText = line.findViewById(R.id.orig_tardy_text);
        if (unit.getOrigDelayMinutes() > 0) {
            origTardyText.setText(GeneralUtils.getDurationAsString(unit.getOrigDelayMinutes(), TimeUnit.MINUTES) + " late.");
            origTardyText.setContentDescription(GeneralUtils.getDurationAsLongString(unit.getOrigDelayMinutes(), TimeUnit.MINUTES) + " late.");
            origTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.delay_minutes));
            View origDepartingBorder = line.findViewById(R.id.orig_departing_border);
            origDepartingBorder.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.late_boarder));
            origDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.late_departing));
        } else {
            if (unit.isOrigRealtime()) {
                origTardyText.setText(R.string.nta_on_time);
                origTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.no_delay_minutes));
            } else {
                origTardyText.setText(R.string.nta_scheduled);
                origTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.scheduled));
                enableClick = false;
            }
            origDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.on_time_departing));
        }

        if (transitType == TransitType.SUBWAY || transitType == TransitType.NHSL) {
            enableClick = false;
        }

        // disable chevron for NTA result row when realtime data unavailable or in favorites fragment
        if (!enableClick || getContext() instanceof MainActivity) {
            line.setClickable(false);
            line.findViewById(R.id.trip_details_button).setVisibility(GONE);
        } else {
            line.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), NextToArriveTripDetailActivity.class);
                    CrashlyticsManager.log(Log.INFO, TAG, "getSingleStopTripView");
                    CrashlyticsManager.log(Log.INFO, TAG, start.getStopName());
                    CrashlyticsManager.log(Log.INFO, TAG, destination.getStopName());
                    CrashlyticsManager.log(Log.INFO, TAG, transitType.toString());
                    CrashlyticsManager.log(Log.INFO, TAG, unit.getOrigRouteName());
                    CrashlyticsManager.log(Log.INFO, TAG, unit.getOrigRouteId());
                    CrashlyticsManager.log(Log.INFO, TAG, unit.getOrigLineTripId());
                    CrashlyticsManager.log(Log.INFO, TAG, unit.getOrigVehicleId());


                    intent.putExtra(Constants.DESTINATION_STATION, destination);
                    intent.putExtra(Constants.STARTING_STATION, start);
                    intent.putExtra(Constants.TRANSIT_TYPE, transitType);
                    intent.putExtra(Constants.ROUTE_NAME, unit.getOrigRouteName());
                    intent.putExtra(Constants.ROUTE_ID, unit.getOrigRouteId());
                    intent.putExtra(Constants.TRIP_ID, unit.getOrigLineTripId());
                    intent.putExtra(Constants.VEHICLE_ID, unit.getOrigVehicleId());

                    if (routeDirectionModel != null) {
                        CrashlyticsManager.log(Log.INFO, TAG, routeDirectionModel.getDirectionDescription());
                        intent.putExtra(Constants.ROUTE_DESCRIPTION, routeDirectionModel.getDirectionDescription());
                    }
                    getContext().startActivity(intent);
                }
            });
        }

        return line;
    }

    public View getMultistopTripView(final NextArrivalRecord item) {

        View convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_next_to_arrive_unit_multistop, this, false);

        DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);

        android.widget.TextView origLineNameText = convertView.findViewById(R.id.orig_line_name_text);
        origLineNameText.setText(item.getOrigRouteName());

        ((ImageView) convertView.findViewById(R.id.orig_line_marker_left)).setColorFilter(ContextCompat.getColor(getContext(), transitType.getLineColor(item.getOrigRouteId(), getContext())));
        Alert orig_alert = SystemStatusState.getAlertForLine(transitType, item.getOrigRouteId());

        if (orig_alert.isAlert()) {
            View targetView = convertView.findViewById(R.id.orig_line_alert_icon);
            targetView.setVisibility(VISIBLE);
            targetView.setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.SERVICE_ALERT_EXPANDED, getContext(), transitType, item.getOrigRouteId(), item.getOrigRouteName()));
            targetView.setContentDescription(R.string.alert_icon_content_description_prefix + item.getOrigRouteName());
        }
        if (orig_alert.isAdvisory()) {
            View targetView = convertView.findViewById(R.id.orig_line_advisory_icon);
            targetView.setVisibility(VISIBLE);
            targetView.setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.SERVICE_ADVISORY_EXPANDED, getContext(), transitType, item.getOrigRouteId(), item.getOrigRouteName()));
            targetView.setContentDescription(R.string.advisory_icon_content_description_prefix + item.getOrigRouteName());
        }
        if (orig_alert.isDetour()) {
            View targetView = convertView.findViewById(R.id.orig_line_detour_icon);
            targetView.setVisibility(VISIBLE);
            targetView.setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.ACTIVE_DETOUR_EXPANDED, getContext(), transitType, item.getOrigRouteId(), item.getOrigRouteName()));
            targetView.setContentDescription(R.string.detour_icon_content_description_prefix + item.getOrigRouteName());
        }
        if (orig_alert.isSnow()) {
            View targetView = convertView.findViewById(R.id.orig_line_weather_icon);
            targetView.setVisibility(VISIBLE);
            targetView.setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.WEATHER_ALERTS_EXPANDED, getContext(), transitType, item.getOrigRouteId(), item.getOrigRouteName()));
            targetView.setContentDescription(R.string.weather_icon_content_description_prefix + item.getOrigRouteName());
        }


        android.widget.TextView origArrivalTimeText = convertView.findViewById(R.id.orig_arrival_time_text);
        origArrivalTimeText.setText(dateFormat.format(item.getOrigDepartureTime()) + " - " + dateFormat.format(item.getOrigArrivalTime()));
        origArrivalTimeText.setContentDescription("departs " + dateFormat.format(item.getOrigDepartureTime()) + "  arrives " + dateFormat.format(item.getOrigArrivalTime()));

        android.widget.TextView origTripNumberText = convertView.findViewById(R.id.orig_trip_number_text);
        origTripNumberText.setText(item.getOrigLineTripId() + " to " + item.getOrigLastStopName());

        android.widget.TextView origDepartureTime = convertView.findViewById(R.id.orig_depature_time);

        Calendar departureCal = Calendar.getInstance();
        departureCal.setTime(item.getOrigDepartureTime());
        departureCal.add(Calendar.MINUTE, item.getOrigDelayMinutes());

        long origDepartureMillis = departureCal.getTimeInMillis() - System.currentTimeMillis();
        if (origDepartureMillis >= 1000 * 60)
            origDepartureTime.setText(GeneralUtils.getDurationAsString(origDepartureMillis, TimeUnit.MILLISECONDS));
        else
            origDepartureTime.setText(R.string.nta_now);

        boolean enableOrigClick = true;
        View origTripView = convertView.findViewById(R.id.orig_trip_layout);
        android.widget.TextView origTardyText = convertView.findViewById(R.id.orig_tardy_text);
        if (item.getOrigDelayMinutes() > 0) {
            origTardyText.setText(GeneralUtils.getDurationAsString(item.getOrigDelayMinutes(), TimeUnit.MINUTES) + " late.");
            origTardyText.setContentDescription(GeneralUtils.getDurationAsLongString(item.getOrigDelayMinutes(), TimeUnit.MINUTES) + " late.");
            origTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.delay_minutes));
            View origDepartingBorder = convertView.findViewById(R.id.orig_departing_border);
            origDepartingBorder.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.late_boarder));
            origDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.late_departing));
        } else {
            if (item.isOrigRealtime()) {
                origTardyText.setText(R.string.nta_on_time);
                origTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.no_delay_minutes));
            } else {
                origTardyText.setText(R.string.nta_scheduled);
                origTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.scheduled));
                enableOrigClick = false;
            }
            origDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.on_time_departing));
        }


        if (transitType == TransitType.SUBWAY || transitType == TransitType.NHSL) {
            enableOrigClick = false;
        }

        // disable chevron for NTA result row when realtime data unavailable or in favorites fragment
        if (!enableOrigClick || getContext() instanceof MainActivity) {
            origTripView.setClickable(false);
            origTripView.findViewById(R.id.orig_trip_details_button).setVisibility(GONE);
        } else {
            origTripView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), NextToArriveTripDetailActivity.class);
                    CrashlyticsManager.log(Log.INFO, TAG, "getMultistopTripView Orig");
                    CrashlyticsManager.log(Log.INFO, TAG, start.getStopName());
                    CrashlyticsManager.log(Log.INFO, TAG, destination.getStopName());
                    CrashlyticsManager.log(Log.INFO, TAG, transitType.toString());
                    CrashlyticsManager.log(Log.INFO, TAG, item.getOrigRouteName());
                    CrashlyticsManager.log(Log.INFO, TAG, item.getOrigLineTripId());
                    CrashlyticsManager.log(Log.INFO, TAG, item.getOrigVehicleId());

                    intent.putExtra(Constants.DESTINATION_STATION, connectionStations.get(item.getConnectionStationId()));
                    intent.putExtra(Constants.STARTING_STATION, start);
                    intent.putExtra(Constants.TRANSIT_TYPE, transitType);
                    intent.putExtra(Constants.ROUTE_NAME, item.getOrigRouteName());
                    if (routeDirectionModel != null) {
                        CrashlyticsManager.log(Log.INFO, TAG, routeDirectionModel.getDirectionDescription());
                        intent.putExtra(Constants.ROUTE_ID, routeDirectionModel.getRouteShortName());
                    } else {
                        CrashlyticsManager.log(Log.INFO, TAG, item.getOrigRouteId());
                        intent.putExtra(Constants.ROUTE_ID, item.getOrigRouteId());
                    }

                    intent.putExtra(Constants.TRIP_ID, item.getOrigLineTripId());
                    intent.putExtra(Constants.VEHICLE_ID, item.getOrigVehicleId());

                    getContext().startActivity(intent);
                }
            });
        }

        android.widget.TextView connectionStationText = convertView.findViewById(R.id.connection_station_name);
        connectionStationText.setText("Connect @ " + item.getConnectionStationName());

        android.widget.TextView termLineNameText = convertView.findViewById(R.id.term_line_name_text);
        termLineNameText.setText(item.getTermRouteName());

        ((ImageView) convertView.findViewById(R.id.term_line_marker_left)).setColorFilter(ContextCompat.getColor(getContext(), transitType.getLineColor(item.getTermRouteId(), getContext())));
        Alert alert = SystemStatusState.getAlertForLine(transitType, item.getOrigRouteId());

        if (alert.isAlert()) {
            View targetView = convertView.findViewById(R.id.term_line_alert_icon);
            targetView.setVisibility(VISIBLE);
            targetView.setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.SERVICE_ALERT_EXPANDED, getContext(), transitType, item.getTermRouteId(), item.getTermRouteName()));
            targetView.setContentDescription(R.string.alert_icon_content_description_prefix + item.getTermRouteName());
        }
        if (alert.isAdvisory()) {
            View targetView = convertView.findViewById(R.id.term_line_advisory_icon);
            targetView.setVisibility(VISIBLE);
            targetView.setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.SERVICE_ADVISORY_EXPANDED, getContext(), transitType, item.getTermRouteId(), item.getTermRouteName()));
            targetView.setContentDescription(R.string.advisory_icon_content_description_prefix + item.getTermRouteName());
        }
        if (alert.isDetour()) {
            View targetView = convertView.findViewById(R.id.term_line_detour_icon);
            targetView.setVisibility(VISIBLE);
            targetView.setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.ACTIVE_DETOUR_EXPANDED, getContext(), transitType, item.getTermRouteId(), item.getTermRouteName()));
            targetView.setContentDescription(R.string.detour_icon_content_description_prefix + item.getTermRouteName());
        }
        if (alert.isSnow()) {
            View targetView = convertView.findViewById(R.id.term_line_weather_icon);
            targetView.setVisibility(VISIBLE);
            targetView.setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.WEATHER_ALERTS_EXPANDED, getContext(), transitType, item.getTermRouteId(), item.getTermRouteName()));
            targetView.setContentDescription(R.string.weather_icon_content_description_prefix + item.getTermRouteName());
        }

        android.widget.TextView termArrivalTimeText = convertView.findViewById(R.id.term_arrival_time_text);
        termArrivalTimeText.setText(dateFormat.format(item.getTermDepartureTime()) + " - " + dateFormat.format(item.getTermArrivalTime()));
        termArrivalTimeText.setContentDescription("departs " + dateFormat.format(item.getTermDepartureTime()) + "  arrives " + dateFormat.format(item.getTermArrivalTime()));

        android.widget.TextView termTripNumberText = convertView.findViewById(R.id.term_trip_number_text);
        termTripNumberText.setText(item.getTermLineTripId() + " to " + item.getTermLastStopName());

        android.widget.TextView termDepartureTime = convertView.findViewById(R.id.term_depature_time);

        Calendar termDepartureCal = Calendar.getInstance();
        termDepartureCal.setTime(item.getTermDepartureTime());
        termDepartureCal.add(Calendar.MINUTE, item.getTermDelayMinutes());

        long termDepartureMillis = termDepartureCal.getTimeInMillis() - System.currentTimeMillis();
        if (termDepartureMillis >= 1000 * 60) {
            origDepartureTime.setText(GeneralUtils.getDurationAsString(termDepartureMillis, TimeUnit.MILLISECONDS));
        } else {
            origDepartureTime.setText(R.string.nta_now);
        }

        boolean termEnableClick = true;
        View termTripView = convertView.findViewById(R.id.term_trip_layout);
        android.widget.TextView termTardyText = convertView.findViewById(R.id.term_tardy_text);
        if (item.getTermDelayMinutes() > 0) {
            termTardyText.setText(GeneralUtils.getDurationAsString(item.getTermDelayMinutes(), TimeUnit.MINUTES) + " late.");
            termTardyText.setContentDescription(GeneralUtils.getDurationAsLongString(item.getTermDelayMinutes(), TimeUnit.MINUTES) + " late.");
            termTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.delay_minutes));
            View termDepartingBorder = convertView.findViewById(R.id.orig_departing_border);
            termDepartingBorder.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.late_boarder));
            termDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.late_departing));
        } else {
            if (item.isTermRealtime()) {
                termTardyText.setText(R.string.nta_on_time);
                termTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.no_delay_minutes));
            } else {
                termTardyText.setText(R.string.nta_scheduled);
                termTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.scheduled));
                termEnableClick = false;
            }
            termDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.on_time_departing));
        }

        if (transitType == TransitType.SUBWAY || transitType == TransitType.NHSL) {
            termEnableClick = false;
        }

        // disable chevron for NTA result row when realtime data unavailable or in favorites fragment
        if (!termEnableClick || getContext() instanceof MainActivity) {
            termTripView.setClickable(false);
            termTripView.findViewById(R.id.term_trip_details_button).setVisibility(GONE);
        } else {
            termTripView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), NextToArriveTripDetailActivity.class);
                    CrashlyticsManager.log(Log.INFO, TAG, "getMultistopTripView Term");
                    CrashlyticsManager.log(Log.INFO, TAG, start.getStopName());
                    CrashlyticsManager.log(Log.INFO, TAG, destination.getStopName());
                    CrashlyticsManager.log(Log.INFO, TAG, transitType.toString());
                    CrashlyticsManager.log(Log.INFO, TAG, item.getTermRouteName());
                    CrashlyticsManager.log(Log.INFO, TAG, item.getTermRouteId());
                    CrashlyticsManager.log(Log.INFO, TAG, item.getTermLineTripId());
                    CrashlyticsManager.log(Log.INFO, TAG, item.getTermVehicleId());

                    intent.putExtra(Constants.DESTINATION_STATION, destination);
                    intent.putExtra(Constants.STARTING_STATION, connectionStations.get(item.getConnectionStationId()));
                    intent.putExtra(Constants.TRANSIT_TYPE, transitType);
                    intent.putExtra(Constants.ROUTE_NAME, item.getTermRouteName());
                    intent.putExtra(Constants.ROUTE_ID, item.getTermRouteId());
                    intent.putExtra(Constants.TRIP_ID, item.getTermLineTripId());
                    intent.putExtra(Constants.VEHICLE_ID, item.getTermVehicleId());

                    getContext().startActivity(intent);
                }
            });
        }

        return convertView;
    }

    public void setResults(int value, TimeUnit timeUnit) {
        this.resultsTimeUnit = timeUnit;
        this.resultsTime = value;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public TransitType getTransitType() {
        return transitType;
    }

    public void setTransitType(TransitType transitType) {
        this.transitType = transitType;
    }

    public StopModel getStart() {
        return start;
    }

    public void setStart(StopModel start) {
        this.start = start;
    }

    public StopModel getDestination() {
        return destination;
    }

    public void setDestination(StopModel destination) {
        this.destination = destination;
    }

    public RouteDirectionModel getRouteDirectionModel() {
        return routeDirectionModel;
    }

    public void setRouteDirectionModel(RouteDirectionModel routeDirectionModel) {
        this.routeDirectionModel = routeDirectionModel;
    }

    public void setOnFirstElementHeight(Consumer<Integer> onFirstElementHeight) {
        this.onFirstElementHeight = onFirstElementHeight;
    }

}
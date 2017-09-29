package org.septa.android.app.nextarrive;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.*;

import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.services.apiinterfaces.model.Alert;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalModelResponse;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalModelResponse.NextArrivalRecord;
import org.septa.android.app.support.Consumer;
import org.septa.android.app.support.GeneralUtils;
import org.septa.android.app.systemstatus.GoToSystemStatusResultsOnClickListener;
import org.septa.android.app.systemstatus.SystemStatusResultsActivity;
import org.septa.android.app.systemstatus.SystemStatusState;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NextToArriveTripView extends FrameLayout {

    private TransitType transitType;

    private StopModel start;
    private StopModel destination;
    private RouteDirectionModel routeDirectionModel;

    private Consumer<Integer> onFirstElementHeight;

    private TimeUnit resultsTimeUnit;
    private Integer resultsTime;

    private Integer maxResults = 10;

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


        List<NextArrivalModelResponse.NextArrivalRecord> data = parser.getResults();
        if (data.size() <= 0)
            return;

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

        if (maxResults != null) {
            data = data.subList(0, (maxResults <= data.size()) ? maxResults : data.size() - 1);
        }

        LinearLayout listView = (LinearLayout) findViewById(R.id.lines_list_view);
        listView.removeAllViews();

        String currentLine = null;
        boolean firstPos = true;
        final List<View> peakViews = new LinkedList<View>();
        for (NextArrivalModelResponse.NextArrivalRecord item : data) {
            if (item.getConnectionStationId() != null) {
                currentLine = null;
                final View multiView = getMultistopTripView(item);
                if (firstPos && onFirstElementHeight != null) {
                    View peakView = multiView.findViewById(R.id.orig_layout);
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
                    if (firstPos)
                        peakViews.add(headerView);
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
        View convertView = LayoutInflater.from(getContext()).inflate(R.layout.next_to_arrive_line, this, false);

        String routeNameForSystemStatus;
        if (transitType == TransitType.RAIL || transitType == TransitType.NHSL) {
            routeNameForSystemStatus = lineName;
        } else {
            routeNameForSystemStatus = routeDirectionModel.getRouteShortName();
        }

        android.widget.TextView lineNameText = (android.widget.TextView) convertView.findViewById(R.id.orig_line_name_text);
        lineNameText.setText(lineName);
        ((ImageView) convertView.findViewById(R.id.orig_line_marker_left)).setColorFilter(ContextCompat.getColor(getContext(), transitType.getLineColor(lineId, getContext())));

        Alert alert = SystemStatusState.getAlertForLine(transitType, lineId);

        if (alert.isAlert()) {
            convertView.findViewById(R.id.orig_line_alert_icon).setVisibility(VISIBLE);
            convertView.findViewById(R.id.orig_line_alert_icon)
                    .setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.SERVICE_ALERT_EXPANDED, getContext(), transitType, lineId, routeNameForSystemStatus));
        }
        if (alert.isAdvisory()) {
            convertView.findViewById(R.id.orig_line_advisory_icon).setVisibility(VISIBLE);
            convertView.findViewById(R.id.orig_line_advisory_icon)
                    .setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.SERVICE_ADVISORY_EXPANDED, getContext(), transitType, lineId, routeNameForSystemStatus));
        }
        if (alert.isDetour()) {
            convertView.findViewById(R.id.orig_line_detour_icon).setVisibility(VISIBLE);
            convertView.findViewById(R.id.orig_line_detour_icon)
                    .setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.ACTIVE_DETOURT_EXPANDED, getContext(), transitType, lineId, routeNameForSystemStatus));
        }
        if (alert.isSnow()) {
            convertView.findViewById(R.id.orig_line_weather_icon).setVisibility(VISIBLE);
            convertView.findViewById(R.id.orig_line_weather_icon)
                    .setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.WEATHER_ALERTS_EXPANDED, getContext(), transitType, lineId, routeNameForSystemStatus));
        }

        return convertView;
    }

    private View getSingleStopTripView(NextArrivalRecord unit) {

        View line = LayoutInflater.from(getContext()).inflate(R.layout.next_to_arrive_unit, this, false);


//        line.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                        Intent intent = new Intent(getContext(), NextToArriveTripDetailActivity.class);
//
//                        intent.putExtra(Constants.DESTINATAION_STATION, destStopId);
//                        intent.putExtra(Constants.STARTING_STATION, startStopId);
//                        intent.putExtra(Constants.TRANSIT_TYPE, transitType);
//                        intent.putExtra(Constants.ROUTE_DIRECTION_MODEL, routeId);
//                        intent.putExtra(Constants.ARRIVAL_RECORD, unit);
//                        intent.putExtra(Constants.TRIP_ID, unit.getOrigLineTripId());
//
//                        getContext().startActivity(intent);
//            }
//        });

        DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);

        android.widget.TextView origArrivalTimeText = (android.widget.TextView) line.findViewById(R.id.orig_arrival_time_text);
        origArrivalTimeText.setText(dateFormat.format(unit.getOrigDepartureTime()) + " - " + dateFormat.format(unit.getOrigArrivalTime()));

        android.widget.TextView origTripNumberText = (android.widget.TextView) line.findViewById(R.id.orig_trip_number_text);
        origTripNumberText.setText(unit.getOrigLineTripId() + " to " + unit.getOrigLastStopName());

        android.widget.TextView origDepartureTime = (android.widget.TextView) line.findViewById(R.id.orig_depature_time);

        Calendar departureCal = Calendar.getInstance();
        departureCal.setTime(unit.getOrigDepartureTime());
        departureCal.add(Calendar.MINUTE, unit.getOrigDelayMinutes());

        origDepartureTime.setText(GeneralUtils.getDurationAsString(departureCal.getTimeInMillis() - System.currentTimeMillis(), TimeUnit.MILLISECONDS));

        android.widget.TextView origTardyText = (android.widget.TextView) line.findViewById(R.id.orig_tardy_text);
        if (unit.getOrigDelayMinutes() > 0) {
            origTardyText.setText(unit.getOrigDelayMinutes() + " min late.");
            origTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.delay_minutes));
            View origDepartingBorder = line.findViewById(R.id.orig_departing_border);
            origDepartingBorder.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.late_boarder));
            origDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.late_departing));
        } else {
            if (unit.isOrigRealtime()) {
                origTardyText.setText("On time");
                origTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.no_delay_minutes));
            } else {
                origTardyText.setText("Scheduled");
                origTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.scheduled));
            }
            origDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.on_time_departing));
        }

        return line;
    }


    public View getMultistopTripView(NextArrivalModelResponse.NextArrivalRecord item) {

        View convertView = LayoutInflater.from(getContext()).inflate(R.layout.next_to_arrive_unit_multistop, this, false);

        DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);

        android.widget.TextView origLineNameText = (android.widget.TextView) convertView.findViewById(R.id.orig_line_name_text);
        origLineNameText.setText(item.getOrigRouteName());

        ((ImageView) convertView.findViewById(R.id.orig_line_marker_left)).setColorFilter(ContextCompat.getColor(getContext(), transitType.getLineColor(item.getOrigRouteId(), getContext())));
        Alert orig_alert = SystemStatusState.getAlertForLine(transitType, item.getOrigRouteId());

        if (orig_alert.isAlert()) {
            convertView.findViewById(R.id.orig_line_alert_icon).setVisibility(VISIBLE);
            convertView.findViewById(R.id.orig_line_alert_icon)
                    .setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.SERVICE_ALERT_EXPANDED, getContext(), transitType, item.getOrigRouteId(), item.getOrigRouteName()));
        }
        if (orig_alert.isAdvisory()) {
            convertView.findViewById(R.id.orig_line_advisory_icon).setVisibility(VISIBLE);
            convertView.findViewById(R.id.orig_line_advisory_icon)
                    .setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.SERVICE_ADVISORY_EXPANDED, getContext(), transitType, item.getOrigRouteId(), item.getOrigRouteName()));
        }
        if (orig_alert.isDetour()) {
            convertView.findViewById(R.id.orig_line_detour_icon).setVisibility(VISIBLE);
            convertView.findViewById(R.id.orig_line_detour_icon)
                    .setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.ACTIVE_DETOURT_EXPANDED, getContext(), transitType, item.getOrigRouteId(), item.getOrigRouteName()));
        }
        if (orig_alert.isSnow()) {
            convertView.findViewById(R.id.orig_line_weather_icon).setVisibility(VISIBLE);
            convertView.findViewById(R.id.orig_line_weather_icon)
                    .setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.WEATHER_ALERTS_EXPANDED, getContext(), transitType, item.getOrigRouteId(), item.getOrigRouteName()));
        }


        android.widget.TextView origArrivalTimeText = (android.widget.TextView) convertView.findViewById(R.id.orig_arrival_time_text);
        origArrivalTimeText.setText(dateFormat.format(item.getOrigDepartureTime()) + " - " + dateFormat.format(item.getOrigArrivalTime()));

        android.widget.TextView origTripNumberText = (android.widget.TextView) convertView.findViewById(R.id.orig_trip_number_text);
        origTripNumberText.setText(item.getOrigLineTripId() + " to " + item.getOrigLastStopName());

        android.widget.TextView origDepartureTime = (android.widget.TextView) convertView.findViewById(R.id.orig_depature_time);

        Calendar departureCal = Calendar.getInstance();
        departureCal.setTime(item.getOrigDepartureTime());
        departureCal.add(Calendar.MINUTE, item.getOrigDelayMinutes());

        origDepartureTime.setText(GeneralUtils.getDurationAsString(departureCal.getTimeInMillis() - System.currentTimeMillis(), TimeUnit.MILLISECONDS));

        android.widget.TextView origTardyText = (android.widget.TextView) convertView.findViewById(R.id.orig_tardy_text);
        if (item.getOrigDelayMinutes() > 0) {
            origTardyText.setText(GeneralUtils.getDurationAsString(item.getOrigDelayMinutes(), TimeUnit.MINUTES) + " late.");
            origTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.delay_minutes));
            View origDepartingBorder = convertView.findViewById(R.id.orig_departing_border);
            origDepartingBorder.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.late_boarder));
            origDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.late_departing));
        } else {
            if (item.isOrigRealtime()) {
                origTardyText.setText("On time");
                origTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.no_delay_minutes));
            } else {
                origTardyText.setText("Scheduled");
                origTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.scheduled));
            }
            origDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.on_time_departing));
        }

        android.widget.TextView connectionStationText = (android.widget.TextView) convertView.findViewById(R.id.connection_station_name);
        connectionStationText.setText("Connect @ " + item.getConnectionStationName());

        android.widget.TextView termLineNameText = (android.widget.TextView) convertView.findViewById(R.id.term_line_name_text);
        termLineNameText.setText(item.getTermRouteName());

        ((ImageView) convertView.findViewById(R.id.term_line_marker_left)).setColorFilter(ContextCompat.getColor(getContext(), transitType.getLineColor(item.getTermRouteId(), getContext())));
        Alert alert = SystemStatusState.getAlertForLine(transitType, item.getOrigRouteId());

        if (alert.isAlert()) {
            convertView.findViewById(R.id.term_line_alert_icon).setVisibility(VISIBLE);
            convertView.findViewById(R.id.term_line_alert_icon)
                    .setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.SERVICE_ALERT_EXPANDED, getContext(), transitType, item.getTermRouteId(), item.getTermRouteName()));
        }
        if (alert.isAdvisory()) {
            convertView.findViewById(R.id.term_line_advisory_icon).setVisibility(VISIBLE);
            convertView.findViewById(R.id.term_line_advisory_icon)
                    .setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.SERVICE_ALERT_EXPANDED, getContext(), transitType, item.getTermRouteId(), item.getTermRouteName()));
        }
        if (alert.isDetour()) {
            convertView.findViewById(R.id.term_line_detour_icon).setVisibility(VISIBLE);
            convertView.findViewById(R.id.term_line_detour_icon)
                    .setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.SERVICE_ALERT_EXPANDED, getContext(), transitType, item.getTermRouteId(), item.getTermRouteName()));
        }
        if (alert.isSnow()) {
            convertView.findViewById(R.id.term_line_weather_icon).setVisibility(VISIBLE);
            convertView.findViewById(R.id.term_line_weather_icon)
                    .setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.SERVICE_ALERT_EXPANDED, getContext(), transitType, item.getTermRouteId(), item.getTermRouteName()));
        }

        android.widget.TextView termArrivalTimeText = (android.widget.TextView) convertView.findViewById(R.id.term_arrival_time_text);
        termArrivalTimeText.setText(dateFormat.format(item.getTermDepartureTime()) + " - " + dateFormat.format(item.getTermArrivalTime()));

        android.widget.TextView termTripNumberText = (android.widget.TextView) convertView.findViewById(R.id.term_trip_number_text);
        termTripNumberText.setText(item.getTermLineTripId() + " to " + item.getTermLastStopName());

        android.widget.TextView termDepartureTime = (android.widget.TextView) convertView.findViewById(R.id.term_depature_time);

        Calendar arrivalCal = Calendar.getInstance();
        arrivalCal.setTime(item.getTermDepartureTime());
        arrivalCal.add(Calendar.MINUTE, item.getTermDelayMinutes());
        termDepartureTime.setText(GeneralUtils.getDurationAsString(arrivalCal.getTimeInMillis() - System.currentTimeMillis(), TimeUnit.MILLISECONDS));

        android.widget.TextView termTardyText = (android.widget.TextView) convertView.findViewById(R.id.term_tardy_text);
        if (item.getTermDelayMinutes() > 0) {
            termTardyText.setText(GeneralUtils.getDurationAsString(item.getTermDelayMinutes(), TimeUnit.MINUTES) + " late.");
            termTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.delay_minutes));
            View termDepartingBorder = convertView.findViewById(R.id.orig_departing_border);
            termDepartingBorder.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.late_boarder));
            termDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.late_departing));
        } else {
            if (item.isTermRealtime()) {
                termTardyText.setText("On time");
                termTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.no_delay_minutes));
            } else {
                termTardyText.setText("Scheduled");
                termTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.scheduled));
            }
            termDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.on_time_departing));
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


//    private static class MultiLinesListAdapter extends ArrayAdapter<NextArrivalRecord> {
//
//        private final TransitType transitType;
//        Consumer<Integer> onFirstElementHeight;
//        OnLayoutChangeListener onLayoutChangeListener;
//
//        public MultiLinesListAdapter(@NonNull Context context, List<NextArrivalRecord> list, TransitType transitType, final Consumer<Integer> onFirstElementHeight) {
//            super(context, 0, list);
//            this.transitType = transitType;
//            this.onFirstElementHeight = onFirstElementHeight;
//            onLayoutChangeListener = new OnLayoutChangeListener() {
//                @Override
//                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                    onFirstElementHeight.accept(bottom - top);
//                }
//            };
//        }
//
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            if (convertView == null) {
//                convertView = LayoutInflater.from(getContext()).inflate(R.layout.next_to_arrive_unit_multistop, parent, false);
//            }
//
//            NextArrivalModelResponse.NextArrivalRecord item = getItem(position);
//            DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
//
//            android.widget.TextView origLineNameText = (android.widget.TextView) convertView.findViewById(R.id.orig_line_name_text);
//            origLineNameText.setText(item.getOrigRouteName());
//
//            ((ImageView) convertView.findViewById(R.id.orig_line_marker_left)).setColorFilter(ContextCompat.getColor(getContext(), transitType.getLineColor(item.getOrigRouteId(), getContext())));
//            ((ImageView) convertView.findViewById(R.id.orig_line_marker_right)).setColorFilter(ContextCompat.getColor(getContext(), transitType.getLineColor(item.getOrigRouteId(), getContext())));
//
//
//            android.widget.TextView origArrivalTimeText = (android.widget.TextView) convertView.findViewById(R.id.orig_arrival_time_text);
//            origArrivalTimeText.setText(dateFormat.format(item.getOrigDepartureTime()) + " - " + dateFormat.format(item.getOrigArrivalTime()));
//
//            android.widget.TextView origTripNumberText = (android.widget.TextView) convertView.findViewById(R.id.orig_trip_number_text);
//            origTripNumberText.setText(item.getOrigLineTripId() + " to " + item.getOrigLastStopName());
//
//            android.widget.TextView origDepartureTime = (android.widget.TextView) convertView.findViewById(R.id.orig_depature_time);
//            int origDepartsInMinutes = ((int) (item.getOrigDepartureTime().getTime() + (item.getOrigDelayMinutes() * 60000) - System.currentTimeMillis()) / 60000);
//            origDepartureTime.setText(String.valueOf(origDepartsInMinutes + " Minutes"));
//
//            android.widget.TextView origTardyText = (android.widget.TextView) convertView.findViewById(R.id.orig_tardy_text);
//            if (item.getOrigDelayMinutes() > 0) {
//                origTardyText.setText(item.getOrigDelayMinutes() + " min late.");
//                origTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.delay_minutes));
//                View origDepartingBorder = convertView.findViewById(R.id.orig_departing_border);
//                origDepartingBorder.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.late_boarder));
//                origDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.late_departing));
//            } else {
//                origTardyText.setText("On time");
//                origTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.no_delay_minutes));
//                origDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.late_departing));
//            }
//
//            android.widget.TextView connectionStationText = (android.widget.TextView) convertView.findViewById(R.id.connection_station_name);
//            connectionStationText.setText(item.getConnectionStationName());
//
//            android.widget.TextView termLineNameText = (android.widget.TextView) convertView.findViewById(R.id.term_line_name_text);
//            termLineNameText.setText(item.getTermRouteName());
//
//            ((ImageView) convertView.findViewById(R.id.term_line_marker_left)).setColorFilter(ContextCompat.getColor(getContext(), transitType.getLineColor(item.getTermRouteId(), getContext())));
//            ((ImageView) convertView.findViewById(R.id.term_line_marker_right)).setColorFilter(ContextCompat.getColor(getContext(), transitType.getLineColor(item.getTermRouteId(), getContext())));
//
//            android.widget.TextView termArrivalTimeText = (android.widget.TextView) convertView.findViewById(R.id.term_arrival_time_text);
//            termArrivalTimeText.setText(dateFormat.format(item.getTermDepartureTime()) + " - " + dateFormat.format(item.getTermArrivalTime()));
//
//            android.widget.TextView termTripNumberText = (android.widget.TextView) convertView.findViewById(R.id.term_trip_number_text);
//            termTripNumberText.setText(item.getTermLineTripId() + " to " + item.getTermLastStopName());
//
//            android.widget.TextView termDepartureTime = (android.widget.TextView) convertView.findViewById(R.id.term_depature_time);
//            int termDepartsInMinutes = ((int) (item.getTermDepartureTime().getTime() + (item.getTermDelayMinutes() * 60000) - System.currentTimeMillis()) / 60000);
//            termDepartureTime.setText(String.valueOf(termDepartsInMinutes + " Minutes"));
//
//            android.widget.TextView termTardyText = (android.widget.TextView) convertView.findViewById(R.id.term_tardy_text);
//            if (item.getTermDelayMinutes() > 0) {
//                termTardyText.setText(item.getTermDelayMinutes() + " min late.");
//                termTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.delay_minutes));
//                View termDepartingBorder = convertView.findViewById(R.id.orig_departing_border);
//                termDepartingBorder.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.late_boarder));
//                termDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.late_departing));
//            } else {
//                termTardyText.setText("On time");
//                termTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.no_delay_minutes));
//                termDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.on_time_departing));
//            }
//
//            if (onFirstElementHeight != null) {
//                if (position == 0) {
//                    convertView.addOnLayoutChangeListener(onLayoutChangeListener);
//                } else {
//                    convertView.removeOnLayoutChangeListener(onLayoutChangeListener);
//                }
//            }
//
//            return convertView;
//
//        }
//    }
//
//    private static class SingleLinesListAdapter extends ArrayAdapter<NextToArriveLine> {
//
//        private final TransitType transitType;
//        private final String startStopId;
//        private final String destStopId;
//        private final String routeId;
//        Consumer<Integer> onFirstElementHeight;
//        OnLayoutChangeListener onLayoutChangeListener;
//        private int firstHeaderHeight = 0;
//
//        public SingleLinesListAdapter(@NonNull Context context, List<NextToArriveLine> list, TransitType transitType, String startStopId, String destStopId, String routeId, final Consumer<Integer> onFirstElementHeight) {
//            super(context, 0, list);
//            this.transitType = transitType;
//            this.startStopId = startStopId;
//            this.destStopId = destStopId;
//            this.routeId = routeId;
//            this.onFirstElementHeight = onFirstElementHeight;
//
//            onLayoutChangeListener = new OnLayoutChangeListener() {
//                @Override
//                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                    onFirstElementHeight.accept(bottom - top + firstHeaderHeight);
//                }
//            };
//        }
//
//        @Override
//        public View getView(int position, View convertView, final ViewGroup parent) {
//            List<NextArrivalModelResponse.NextArrivalRecord> tripList = getItem(position).getList();
//            Collections.sort(
//                    tripList, new Comparator<NextArrivalRecord>() {
//                        @Override
//                        public int compare(NextArrivalModelResponse.NextArrivalRecord x, NextArrivalModelResponse.NextArrivalRecord y) {
//                            return x.getOrigDepartureTime().compareTo(y.getOrigDepartureTime());
//                        }
//                    });
//
//            tripList = tripList.subList(0, (3 < tripList.size()) ? 3 : tripList.size());
//
//
//            if (convertView == null) {
//                convertView = LayoutInflater.from(getContext()).inflate(R.layout.next_to_arrive_line, parent, false);
//            }
//
//            android.widget.TextView lineNameText = (android.widget.TextView) convertView.findViewById(R.id.orig_line_name_text);
//            lineNameText.setText(getItem(position).lineName);
//            ((ImageView) convertView.findViewById(R.id.orig_line_marker_left)).setColorFilter(ContextCompat.getColor(getContext(), transitType.getLineColor(getItem(position).getList().get(0).getOrigRouteId(), getContext())));
//            ((ImageView) convertView.findViewById(R.id.orig_line_marker_right)).setColorFilter(ContextCompat.getColor(getContext(), transitType.getLineColor(getItem(position).getList().get(0).getOrigRouteId(), getContext())));
//
//            LinearLayout arrivalList = (LinearLayout) convertView.findViewById(R.id.arrival_list);
//            arrivalList.removeAllViews();
//
//            DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
//
//            View firstLine = null;
//
//            for (final NextArrivalModelResponse.NextArrivalRecord unit : tripList) {
//                View line = LayoutInflater.from(getContext()).inflate(R.layout.next_to_arrive_unit, null, false);
//                if (firstLine == null)
//                    firstLine = line;
//
//                line.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
////                        Intent intent = new Intent(getContext(), NextToArriveTripDetailActivity.class);
////
////                        intent.putExtra(Constants.DESTINATAION_STATION, destStopId);
////                        intent.putExtra(Constants.STARTING_STATION, startStopId);
////                        intent.putExtra(Constants.TRANSIT_TYPE, transitType);
////                        intent.putExtra(Constants.ROUTE_DIRECTION_MODEL, routeId);
////                        intent.putExtra(Constants.ARRIVAL_RECORD, unit);
////                        intent.putExtra(Constants.TRIP_ID, unit.getOrigLineTripId());
////
////                        getContext().startActivity(intent);
//                    }
//                });
//
//
//                android.widget.TextView origArrivalTimeText = (android.widget.TextView) line.findViewById(R.id.orig_arrival_time_text);
//                origArrivalTimeText.setText(dateFormat.format(unit.getOrigDepartureTime()) + " - " + dateFormat.format(unit.getOrigArrivalTime()));
//                arrivalList.addView(line);
//
//                android.widget.TextView origTripNumberText = (android.widget.TextView) line.findViewById(R.id.orig_trip_number_text);
//                origTripNumberText.setText(unit.getOrigLineTripId() + " to " + unit.getOrigLastStopName());
//
//                android.widget.TextView origDepartureTime = (android.widget.TextView) line.findViewById(R.id.orig_depature_time);
//                int origDepartsInMinutes = ((int) (unit.getOrigDepartureTime().getTime() + (unit.getOrigDelayMinutes() * 60000) - System.currentTimeMillis()) / 60000);
//                origDepartureTime.setText(String.valueOf(origDepartsInMinutes + " Minutes"));
//
//                android.widget.TextView origTardyText = (android.widget.TextView) line.findViewById(R.id.orig_tardy_text);
//                if (unit.getOrigDelayMinutes() > 0) {
//                    origTardyText.setText(unit.getOrigDelayMinutes() + " min late.");
//                    origTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.delay_minutes));
//                    View origDepartingBorder = line.findViewById(R.id.orig_departing_border);
//                    origDepartingBorder.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.late_boarder));
//                    origDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.late_departing));
//                } else {
//                    origTardyText.setText("On time");
//                    origTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.no_delay_minutes));
//                    origDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.on_time_departing));
//                }
//            }
//
//            if (onFirstElementHeight != null && firstLine != null) {
//                if (position == 0) {
//                    firstHeaderHeight = convertView.findViewById(R.id.linearLayout1).getHeight();
//                    firstLine.addOnLayoutChangeListener(onLayoutChangeListener);
//                } else {
//                    firstLine.removeOnLayoutChangeListener(onLayoutChangeListener);
//                }
//            }
//
//
//            return convertView;
//        }
//    }
}

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
import android.view.ViewGroup;
import android.widget.*;

import com.google.gson.annotations.SerializedName;

import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.nextarrive.NextArrivalModelResponseParser;
import org.septa.android.app.nextarrive.NextToArriveLine;
import org.septa.android.app.nextarrive.NextToArriveTripDetailActivity;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalModelResponse;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalModelResponse.NextArrivalRecord;
import org.septa.android.app.support.Consumer;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NextToArriveTripView extends FrameLayout {

    private ListView listView;
    private TransitType transitType;

    private StopModel start;
    private StopModel destination;
    private RouteDirectionModel routeDirectionModel;

    private Consumer<Integer> onFirstElementHeight;

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
        listView = (ListView) findViewById(R.id.lines_list_view);
    }

    public void setSingleStopDetails(List<NextToArriveLine> nextToArriveLinesList) {
        String routeId = null;
        if (routeDirectionModel != null)
            routeId = routeDirectionModel.getRouteId();
        listView.setAdapter(new SingleLinesListAdapter(getContext(),
                nextToArriveLinesList, transitType, start.getStopId(), destination.getStopId(), routeId, onFirstElementHeight));
        listView.setEmptyView(findViewById(android.R.id.empty));
    }

    public void setMultipleStopDetails(List<NextArrivalModelResponse.NextArrivalRecord> multiStopList) {
        listView.setAdapter(new MultiLinesListAdapter(getContext(), multiStopList, transitType, onFirstElementHeight));
        listView.setEmptyView(findViewById(android.R.id.empty));
    }

    public void setNextToArriveData(NextArrivalModelResponseParser parser) {
        if (parser.getTripType() == NextArrivalModelResponseParser.BOTH) {
            //TODO Need an error message here.
            throw new RuntimeException("We multi-stop and single stop next to arrive in the same response.  This is unexpected.");
        }

        if (parser.getTripType() == NextArrivalModelResponseParser.SINGLE_STOP_TRIP) {
            List<NextToArriveLine> nextToArriveLinesList = parser.getNextToArriveLineList();
            Collections.sort(nextToArriveLinesList, new Comparator<NextToArriveLine>() {
                @Override
                public int compare(NextToArriveLine x, NextToArriveLine y) {
                    if (x.getSoonestDeparture() != null)
                        return x.getSoonestDeparture().compareTo(y.getSoonestDeparture());
                    else return Integer.MAX_VALUE;
                }
            });


            setSingleStopDetails(nextToArriveLinesList);
        } else {
            List<NextArrivalModelResponse.NextArrivalRecord> nextArrivalRecordList = parser.getNextArrivalRecordList();
            Collections.sort(nextArrivalRecordList, new Comparator<NextArrivalModelResponse.NextArrivalRecord>() {
                @Override
                public int compare(NextArrivalModelResponse.NextArrivalRecord x, NextArrivalModelResponse.NextArrivalRecord y) {
                    if (x == y)
                        return 0;
                    return (int) (x.getOrigDepartureTime().getTime() + (60000 * x.getOrigDelayMinutes())
                            - y.getOrigDepartureTime().getTime() + (60000 * y.getOrigDelayMinutes()));
                }
            });
            List<NextArrivalModelResponse.NextArrivalRecord> multiStopList = nextArrivalRecordList.subList(0, (3 < nextArrivalRecordList.size()) ? 3 : nextArrivalRecordList.size());

            setMultipleStopDetails(multiStopList);
        }

    }

    public void setOnFirstElementHeight(Consumer<Integer> onFirstElementHeight) {
        this.onFirstElementHeight = onFirstElementHeight;
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

    private static class MultiLinesListAdapter extends ArrayAdapter<NextArrivalRecord> {

        private final TransitType transitType;
        Consumer<Integer> onFirstElementHeight;
        OnLayoutChangeListener onLayoutChangeListener;

        public MultiLinesListAdapter(@NonNull Context context, List<NextArrivalRecord> list, TransitType transitType, final Consumer<Integer> onFirstElementHeight) {
            super(context, 0, list);
            this.transitType = transitType;
            this.onFirstElementHeight = onFirstElementHeight;
            onLayoutChangeListener = new OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    onFirstElementHeight.accept(bottom - top);
                }
            };
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.next_to_arrive_unit_multistop, parent, false);
            }

            NextArrivalModelResponse.NextArrivalRecord item = getItem(position);
            DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);

            android.widget.TextView origLineNameText = (android.widget.TextView) convertView.findViewById(R.id.orig_line_name_text);
            origLineNameText.setText(item.getOrigRouteName());

            ((ImageView) convertView.findViewById(R.id.orig_line_marker_left)).setColorFilter(ContextCompat.getColor(getContext(), transitType.getLineColor(item.getOrigRouteId(), getContext())));
            ((ImageView) convertView.findViewById(R.id.orig_line_marker_right)).setColorFilter(ContextCompat.getColor(getContext(), transitType.getLineColor(item.getOrigRouteId(), getContext())));


            android.widget.TextView origArrivalTimeText = (android.widget.TextView) convertView.findViewById(R.id.orig_arrival_time_text);
            origArrivalTimeText.setText(dateFormat.format(item.getOrigDepartureTime()) + " - " + dateFormat.format(item.getOrigArrivalTime()));

            android.widget.TextView origTripNumberText = (android.widget.TextView) convertView.findViewById(R.id.orig_trip_number_text);
            origTripNumberText.setText(item.getOrigLineTripId() + " to " + item.getOrigLastStopName());

            android.widget.TextView origDepartureTime = (android.widget.TextView) convertView.findViewById(R.id.orig_depature_time);
            int origDepartsInMinutes = ((int) (item.getOrigDepartureTime().getTime() + (item.getOrigDelayMinutes() * 60000) - System.currentTimeMillis()) / 60000);
            origDepartureTime.setText(String.valueOf(origDepartsInMinutes + " Minutes"));

            android.widget.TextView origTardyText = (android.widget.TextView) convertView.findViewById(R.id.orig_tardy_text);
            if (item.getOrigDelayMinutes() > 0) {
                origTardyText.setText(item.getOrigDelayMinutes() + " min late.");
                origTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.delay_minutes));
                View origDepartingBorder = convertView.findViewById(R.id.orig_departing_border);
                origDepartingBorder.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.late_boarder));
                origDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.late_departing));
            } else {
                origTardyText.setText("On time");
                origTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.no_delay_minutes));
                origDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.late_departing));
            }

            android.widget.TextView connectionStationText = (android.widget.TextView) convertView.findViewById(R.id.connection_station_name);
            connectionStationText.setText(item.getConnectionStationName());

            android.widget.TextView termLineNameText = (android.widget.TextView) convertView.findViewById(R.id.term_line_name_text);
            termLineNameText.setText(item.getTermRouteName());

            ((ImageView) convertView.findViewById(R.id.term_line_marker_left)).setColorFilter(ContextCompat.getColor(getContext(), transitType.getLineColor(item.getTermRouteId(), getContext())));
            ((ImageView) convertView.findViewById(R.id.term_line_marker_right)).setColorFilter(ContextCompat.getColor(getContext(), transitType.getLineColor(item.getTermRouteId(), getContext())));

            android.widget.TextView termArrivalTimeText = (android.widget.TextView) convertView.findViewById(R.id.term_arrival_time_text);
            termArrivalTimeText.setText(dateFormat.format(item.getTermDepartureTime()) + " - " + dateFormat.format(item.getTermArrivalTime()));

            android.widget.TextView termTripNumberText = (android.widget.TextView) convertView.findViewById(R.id.term_trip_number_text);
            termTripNumberText.setText(item.getTermLineTripId() + " to " + item.getTermLastStopName());

            android.widget.TextView termDepartureTime = (android.widget.TextView) convertView.findViewById(R.id.term_depature_time);
            int termDepartsInMinutes = ((int) (item.getTermDepartureTime().getTime() + (item.getTermDelayMinutes() * 60000) - System.currentTimeMillis()) / 60000);
            termDepartureTime.setText(String.valueOf(termDepartsInMinutes + " Minutes"));

            android.widget.TextView termTardyText = (android.widget.TextView) convertView.findViewById(R.id.term_tardy_text);
            if (item.getTermDelayMinutes() > 0) {
                termTardyText.setText(item.getTermDelayMinutes() + " min late.");
                termTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.delay_minutes));
                View termDepartingBorder = convertView.findViewById(R.id.orig_departing_border);
                termDepartingBorder.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.late_boarder));
                termDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.late_departing));
            } else {
                termTardyText.setText("On time");
                termTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.no_delay_minutes));
                termDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.on_time_departing));
            }

            if (onFirstElementHeight != null) {
                if (position == 0) {
                    convertView.addOnLayoutChangeListener(onLayoutChangeListener);
                } else {
                    convertView.removeOnLayoutChangeListener(onLayoutChangeListener);
                }
            }

            return convertView;

        }
    }

    private static class SingleLinesListAdapter extends ArrayAdapter<NextToArriveLine> {

        private final TransitType transitType;
        private final String startStopId;
        private final String destStopId;
        private final String routeId;
        Consumer<Integer> onFirstElementHeight;
        OnLayoutChangeListener onLayoutChangeListener;
        private int firstHeaderHeight = 0;

        public SingleLinesListAdapter(@NonNull Context context, List<NextToArriveLine> list, TransitType transitType, String startStopId, String destStopId, String routeId, final Consumer<Integer> onFirstElementHeight) {
            super(context, 0, list);
            this.transitType = transitType;
            this.startStopId = startStopId;
            this.destStopId = destStopId;
            this.routeId = routeId;
            this.onFirstElementHeight = onFirstElementHeight;

            onLayoutChangeListener = new OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    onFirstElementHeight.accept(bottom - top + firstHeaderHeight);
                }
            };
        }

        @Override
        public View getView(int position, View convertView, final ViewGroup parent) {
            List<NextArrivalModelResponse.NextArrivalRecord> tripList = getItem(position).getList();
            Collections.sort(
                    tripList, new Comparator<NextArrivalRecord>() {
                        @Override
                        public int compare(NextArrivalModelResponse.NextArrivalRecord x, NextArrivalModelResponse.NextArrivalRecord y) {
                            return x.getOrigDepartureTime().compareTo(y.getOrigDepartureTime());
                        }
                    });

            tripList = tripList.subList(0, (3 < tripList.size()) ? 3 : tripList.size());


            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.next_to_arrive_line, parent, false);
            }

            android.widget.TextView lineNameText = (android.widget.TextView) convertView.findViewById(R.id.orig_line_name_text);
            lineNameText.setText(getItem(position).lineName);
            ((ImageView) convertView.findViewById(R.id.orig_line_marker_left)).setColorFilter(ContextCompat.getColor(getContext(), transitType.getLineColor(getItem(position).getList().get(0).getOrigRouteId(), getContext())));
            ((ImageView) convertView.findViewById(R.id.orig_line_marker_right)).setColorFilter(ContextCompat.getColor(getContext(), transitType.getLineColor(getItem(position).getList().get(0).getOrigRouteId(), getContext())));

            LinearLayout arrivalList = (LinearLayout) convertView.findViewById(R.id.arrival_list);
            arrivalList.removeAllViews();

            DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);

            View firstLine = null;

            for (final NextArrivalModelResponse.NextArrivalRecord unit : tripList) {
                View line = LayoutInflater.from(getContext()).inflate(R.layout.next_to_arrive_unit, null, false);
                if (firstLine == null)
                    firstLine = line;

                line.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Intent intent = new Intent(getContext(), NextToArriveTripDetailActivity.class);
//
//                        intent.putExtra(Constants.DESTINATAION_STATION, destStopId);
//                        intent.putExtra(Constants.STARTING_STATION, startStopId);
//                        intent.putExtra(Constants.TRANSIT_TYPE, transitType);
//                        intent.putExtra(Constants.LINE_ID, routeId);
//                        intent.putExtra(Constants.ARRIVAL_RECORD, unit);
//                        intent.putExtra(Constants.TRIP_ID, unit.getOrigLineTripId());
//
//                        getContext().startActivity(intent);
                    }
                });


                android.widget.TextView origArrivalTimeText = (android.widget.TextView) line.findViewById(R.id.orig_arrival_time_text);
                origArrivalTimeText.setText(dateFormat.format(unit.getOrigDepartureTime()) + " - " + dateFormat.format(unit.getOrigArrivalTime()));
                arrivalList.addView(line);

                android.widget.TextView origTripNumberText = (android.widget.TextView) line.findViewById(R.id.orig_trip_number_text);
                origTripNumberText.setText(unit.getOrigLineTripId() + " to " + unit.getOrigLastStopName());

                android.widget.TextView origDepartureTime = (android.widget.TextView) line.findViewById(R.id.orig_depature_time);
                int origDepartsInMinutes = ((int) (unit.getOrigDepartureTime().getTime() + (unit.getOrigDelayMinutes() * 60000) - System.currentTimeMillis()) / 60000);
                origDepartureTime.setText(String.valueOf(origDepartsInMinutes + " Minutes"));

                android.widget.TextView origTardyText = (android.widget.TextView) line.findViewById(R.id.orig_tardy_text);
                if (unit.getOrigDelayMinutes() > 0) {
                    origTardyText.setText(unit.getOrigDelayMinutes() + " min late.");
                    origTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.delay_minutes));
                    View origDepartingBorder = line.findViewById(R.id.orig_departing_border);
                    origDepartingBorder.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.late_boarder));
                    origDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.late_departing));
                } else {
                    origTardyText.setText("On time");
                    origTardyText.setTextColor(ContextCompat.getColor(getContext(), R.color.no_delay_minutes));
                    origDepartureTime.setTextColor(ContextCompat.getColor(getContext(), R.color.on_time_departing));
                }
            }

            if (onFirstElementHeight != null && firstLine!=null) {
                if (position == 0) {
                    firstHeaderHeight = convertView.findViewById(R.id.linearLayout1).getHeight();
                    firstLine.addOnLayoutChangeListener(onLayoutChangeListener);
                } else {
                    firstLine.removeOnLayoutChangeListener(onLayoutChangeListener);
                }
            }


            return convertView;
        }
    }
}

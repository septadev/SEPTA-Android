package org.septa.android.app.locationpicker;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.support.BaseTabActivityHandler;
import org.septa.android.app.support.Criteria;
import org.septa.android.app.support.CursorAdapterSupplier;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by jkampf on 7/29/17.
 */

public class LineAwareLocationPickerTabActivityHandler extends BaseTabActivityHandler {
    CursorAdapterSupplier<RouteDirectionModel> routeCursorAdapterSupplier;
    CursorAdapterSupplier<StopModel> stopCursorAdapterSupplier;
    CursorAdapterSupplier<StopModel> busStopAfterCursorAdapterSupplier;
    String headerStringName;
    TransitType transitType;
    Class targetClass;
    String buttonText;
    Bundle prepopulate;

    private static final int LINE_PICKER_ID = 1;
    private static final int START_MODEL_ID = 2;
    private static final int DEST_MODEL_ID = 3;

    public LineAwareLocationPickerTabActivityHandler(String title, String headerStringName, String buttonText, TransitType transitType, CursorAdapterSupplier<RouteDirectionModel> routeCursorAdapterSupplier, CursorAdapterSupplier<StopModel> busStopCursorAdapterSupplier, CursorAdapterSupplier<StopModel> busStopAfterCursorAdapterSupplier, Class targetClass) {
        super(title, transitType.getTabInactiveImageResource(), transitType.getTabActiveImageResource());
        this.routeCursorAdapterSupplier = routeCursorAdapterSupplier;
        this.stopCursorAdapterSupplier = busStopCursorAdapterSupplier;
        this.busStopAfterCursorAdapterSupplier = busStopAfterCursorAdapterSupplier;
        this.transitType = transitType;
        this.targetClass = targetClass;
        this.headerStringName = headerStringName;
        this.buttonText = buttonText;
    }

    @Override
    public Fragment getFragment() {
        LineAwareLocationPickerTabActivityHandler.PlaceholderFragment fragment = LineAwareLocationPickerTabActivityHandler.PlaceholderFragment.newInstance(headerStringName, buttonText, transitType, routeCursorAdapterSupplier, stopCursorAdapterSupplier, busStopAfterCursorAdapterSupplier, targetClass, prepopulate);
        return fragment;
    }

    public void setPrepopulate(Bundle prepopulate) {
        this.prepopulate = prepopulate;
    }

    public static class PlaceholderFragment extends Fragment implements LinePickerCallBack {
        CursorAdapterSupplier<RouteDirectionModel> routeCursorAdapterSupplier;
        CursorAdapterSupplier<StopModel> stopCursorAdapterSupplier;
        CursorAdapterSupplier<StopModel> stopAfterCursorAdapterSupplier;
        TransitType transitType;
        Class targetClass;
        String headerStringName;
        String buttonText;

        View progressView;
        private StopModel startingStation;
        private StopModel destinationStation;

        RouteDirectionModel selectedRoute;
        private TextView startingStopEditText;
        private TextView destinationStopEditText;
        private TextView lineText;
        private Button queryButton;

        boolean prePopulated = false;

        public static PlaceholderFragment newInstance(String headerStringName, String buttonText, TransitType transitType, CursorAdapterSupplier<RouteDirectionModel> routeCursorAdapterSupplier, CursorAdapterSupplier<StopModel> busStopCursorAdapterSupplier, CursorAdapterSupplier<StopModel> busStopAfterCursorAdapterSupplier, Class targetClass, Bundle prepopulate) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args;
            if (prepopulate == null) {
                args = new Bundle();
                args.putSerializable("prepopulated", Boolean.FALSE);
            } else {
                args = prepopulate;
                args.putSerializable("prepopulated", Boolean.TRUE);
            }

            args.putSerializable("transitType", transitType);
            args.putSerializable("targetClass", targetClass);
            args.putString("headerStringName", headerStringName);
            args.putString("buttonText", buttonText);

            args.putSerializable("routeCursorAdapterSupplier", routeCursorAdapterSupplier);
            args.putSerializable("stopCursorAdapterSupplier", busStopCursorAdapterSupplier);
            args.putSerializable("stopAfterCursorAdapterSupplier", busStopAfterCursorAdapterSupplier);
            args.putSerializable("routeCursorAdapterSupplier", routeCursorAdapterSupplier);

            fragment.setArguments(args);

            return fragment;
        }

        private void restoreArguments() {
            transitType = (TransitType) getArguments().getSerializable("transitType");
            targetClass = (Class) getArguments().getSerializable("targetClass");
            headerStringName = getArguments().getString("headerStringName");
            buttonText = getArguments().getString("buttonText");

            routeCursorAdapterSupplier = (CursorAdapterSupplier<RouteDirectionModel>) getArguments().getSerializable("routeCursorAdapterSupplier");
            stopCursorAdapterSupplier = (CursorAdapterSupplier<StopModel>) getArguments().getSerializable("stopCursorAdapterSupplier");
            stopAfterCursorAdapterSupplier = (CursorAdapterSupplier<StopModel>) getArguments().getSerializable("stopAfterCursorAdapterSupplier");
            routeCursorAdapterSupplier = (CursorAdapterSupplier<RouteDirectionModel>) getArguments().getSerializable("routeCursorAdapterSupplier");

            if (getArguments().getSerializable("prepopulated") != null) {
                prePopulated = (boolean) getArguments().getSerializable("prepopulated");
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            restoreArguments();

            View rootView = inflater.inflate(R.layout.line_aware_next_to_arrive_search, container, false);

            if (getContext() == null) {
                return rootView;
            }

            TextView pickerHeaderText = (TextView) rootView.findViewById(R.id.picker_header_text);
            pickerHeaderText.setText(transitType.getString(headerStringName, getContext()));

            ((TextView) rootView.findViewById(R.id.line_label)).setText(transitType.getString("line_label", getContext()));
            ((TextView) rootView.findViewById(R.id.line_text)).setText(transitType.getString("line_text", getContext()));


            lineText = (TextView) rootView.findViewById(R.id.line_text);
            lineText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentTransaction ft = getFragmentManager().beginTransaction();

                    LinePickerFragment newFragment = LinePickerFragment.newInstance(routeCursorAdapterSupplier, transitType);
                    newFragment.setTargetFragment(PlaceholderFragment.this, LINE_PICKER_ID);
                    newFragment.show(ft, "line_picker");
                }
            });
            progressView = rootView.findViewById(R.id.progress_view);

            startingStopEditText = (TextView) rootView.findViewById(R.id.starting_stop);
            startingStopEditText.setText(transitType.getString("start_stop_text", getContext()));

            destinationStopEditText = (TextView) rootView.findViewById(R.id.destination_stop);
            destinationStopEditText.setText(transitType.getString("dest_stop_text", getContext()));


            startingStopEditText.setOnTouchListener(new StopPickerOnTouchListener(this, START_MODEL_ID, stopCursorAdapterSupplier, false));
            destinationStopEditText.setOnTouchListener(new StopPickerOnTouchListener(this, DEST_MODEL_ID, stopAfterCursorAdapterSupplier, true));

            queryButton = (Button) rootView.findViewById(R.id.view_buses_button);
            queryButton.setText(buttonText);
            queryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (startingStation == null || destinationStation == null) {
                        return;
                    }

                    if (getActivity() == null)
                        return;

                    Intent intent = new Intent(getActivity(), targetClass);
                    intent.putExtra(Constants.STARTING_STATION, startingStation);
                    intent.putExtra(Constants.DESTINATION_STATION, destinationStation);
                    intent.putExtra(Constants.TRANSIT_TYPE, transitType);
                    intent.putExtra(Constants.ROUTE_DIRECTION_MODEL, selectedRoute);

                    getActivity().startActivityForResult(intent, Constants.NTA_REQUEST);
                }
            });
            queryButton.setClickable(false);

            View resetView = rootView.findViewById(R.id.reset_button);
            resetView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedRoute = null;
                    lineText.setText(transitType.getString("line_text", getContext()));
                    lineText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    startingStation = null;
                    disableView(startingStopEditText);
                    startingStopEditText.setText(transitType.getString("start_stop_text", getContext()));
                    destinationStopEditText.setText(transitType.getString("dest_stop_text", getContext()));
                    destinationStation = null;
                    disableView(destinationStopEditText);
                    disableView(queryButton);
                }
            });

            if (savedInstanceState != null) {
                restoreSavedState(savedInstanceState);
            } else if (prePopulated) {
                new PrePopulateAsyncTask(this).execute(getArguments());
            }

            return rootView;
        }

        void setDestinationStop(StopModel var1) {
            destinationStation = var1;
            destinationStopEditText.setText(destinationStation.getStopName());
            activateView(queryButton);
        }

        void setStartStop(StopModel var1) {
            setStartingStation(var1);
            destinationStopEditText.setText(transitType.getString("dest_stop_text", getContext()));
            disableView(queryButton);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            // route was picked
            if (requestCode == LINE_PICKER_ID && resultCode == LinePickerFragment.SUCCESS) {
                RouteDirectionModel var1 = (RouteDirectionModel) data.getSerializableExtra(LinePickerFragment.ROUTE_DIRECTION_MODEL);
                if (var1 != null) {
                    setRoute(var1);
                }
                return;
            }

            // start stop was picked
            if (requestCode == START_MODEL_ID && resultCode == LocationPickerFragment.SUCCESS) {
                StopModel var1 = (StopModel) data.getSerializableExtra(LocationPickerFragment.STOP_MODEL);
                if (var1 != null) {
                    setStartStop(var1);
                }
                return;
            }

            // destination stop was picked
            if (requestCode == DEST_MODEL_ID && resultCode == LocationPickerFragment.SUCCESS) {
                StopModel var1 = (StopModel) data.getSerializableExtra(LocationPickerFragment.STOP_MODEL);
                if (var1 != null) {
                    setDestinationStop(var1);
                }
                return;
            }
        }

        @Override
        public void setRoute(RouteDirectionModel var1) {
            selectedRoute = var1;
            if (getContext() == null) {
                return;
            }
            //lineText.setText(selectedRoute.getRouteLongName());
            int color;
            try {
                color = ContextCompat.getColor(getContext(), transitType.getLineColor(var1.getRouteId(), getContext()));
            } catch (Exception e) {
                color = ContextCompat.getColor(getContext(), R.color.default_line_color);
            }

            Drawable[] drawables = lineText.getCompoundDrawables();
            Drawable bullet = ContextCompat.getDrawable(getContext(), R.drawable.shape_line_marker);
            bullet.setColorFilter(color, PorterDuff.Mode.SRC);

            lineText.setCompoundDrawablesWithIntrinsicBounds(bullet, drawables[1], drawables[2], drawables[3]);

            if (transitType == TransitType.RAIL) {
                lineText.setText(selectedRoute.getRouteId() + " " + selectedRoute.getDirectionDescription());
            } else {
                lineText.setText(selectedRoute.getRouteShortName() + ": to " + selectedRoute.getDirectionDescription());
            }
            startingStation = null;
            activateView(startingStopEditText);
            startingStopEditText.setText(transitType.getString("start_stop_text", getContext()));
            destinationStopEditText.setText(transitType.getString("dest_stop_text", getContext()));
            destinationStation = null;
            disableView(destinationStopEditText);
            disableView(queryButton);
        }

        private void disableView(View view) {
            view.setAlpha((float) .3);
            view.setClickable(false);
        }

        private void activateView(View view) {
            view.setAlpha(1);
            view.setClickable(true);
        }

        void setStartingStation(StopModel start) {
            startingStation = start;
            startingStopEditText.setText(startingStation.getStopName());
            destinationStopEditText.setText(transitType.getString("dest_stop_text", getContext()));
            destinationStation = null;
            activateView(destinationStopEditText);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putSerializable("selectedRoute", selectedRoute);
            outState.putSerializable("startingStation", startingStation);
            outState.putSerializable("destinationStation", destinationStation);
        }

        private void restoreSavedState(Bundle outState) {
            if (outState == null)
                return;

            selectedRoute = (RouteDirectionModel) outState.getSerializable("selectedRoute");
            if (selectedRoute != null)
                setRoute(selectedRoute);
            else return;

            startingStation = (StopModel) outState.getSerializable("startingStation");
            if (startingStation != null)
                setStartingStation(startingStation);
            else return;

            destinationStation = (StopModel) outState.getSerializable("destinationStation");
            if (destinationStation != null)
                setDestinationStop(destinationStation);
        }
    }


    public static class StopPickerOnTouchListener implements View.OnTouchListener {
        private PlaceholderFragment parent;
        private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;
        private boolean userAfter;
        private int requestCode;

        StopPickerOnTouchListener(PlaceholderFragment parent, int requestCode, CursorAdapterSupplier<StopModel> cursorAdapterSupplier, boolean userAfter) {
            this.parent = parent;
            this.requestCode = requestCode;
            this.cursorAdapterSupplier = cursorAdapterSupplier;
            this.userAfter = userAfter;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int action = motionEvent.getActionMasked();
            if (action == MotionEvent.ACTION_UP) {
                if (userAfter && parent.startingStation == null) {
                    return true;
                }

                FragmentTransaction ft = parent.getFragmentManager().beginTransaction();
//                Fragment prev = parent.getFragmentManager().findFragmentByTag("dialog");
//                if (prev != null) {
//                    ft.remove(prev);
//                }
//                ft.addToBackStack(null);

                String stopId = null;
                if (parent.startingStation != null) {
                    stopId = parent.startingStation.getStopId();
                }
                CursorAdapterSupplier<StopModel> routeSpecificCursorAdapterSupplier =
                        new RouteSpecificCursorAdapterSupplier(cursorAdapterSupplier, parent.selectedRoute.getRouteId(),
                                parent.selectedRoute.getDirectionCode(), stopId, userAfter);

                // Create and show the dialog.
                LocationPickerFragment newFragment = LocationPickerFragment.newInstance(routeSpecificCursorAdapterSupplier);
                newFragment.setTargetFragment(parent, requestCode);
                newFragment.show(ft, "dialog");

                return true;
            }
            return false;
        }
    }

    static class RouteSpecificCursorAdapterSupplier implements CursorAdapterSupplier<StopModel> {
        CursorAdapterSupplier<StopModel> cursorAdapterSupplier;
        private boolean userAfter;
        String routeId;
        String routeDesc;
        String stopId;


        public RouteSpecificCursorAdapterSupplier(CursorAdapterSupplier<StopModel> cursorAdapterSupplier, String routeId, String routeDesc, String stopId, boolean userAfter) {
            this.cursorAdapterSupplier = cursorAdapterSupplier;
            this.userAfter = userAfter;
            this.routeId = routeId;
            this.routeDesc = routeDesc;
            this.stopId = stopId;
        }

        @Override
        public Cursor getCursor(Context context, List<Criteria> whereClause) {
            StringBuilder whereClauseBuilder = new StringBuilder();
            if (whereClause == null) {
                whereClause = new ArrayList<Criteria>();
            }
            whereClause.add(new Criteria("route_id", Criteria.Operation.EQ, routeId));
            whereClause.add(new Criteria("direction_id", Criteria.Operation.EQ, routeDesc));
            if (userAfter) {
                whereClause.add(new Criteria("after_stop_id", Criteria.Operation.EQ, stopId));
            }

            return cursorAdapterSupplier.getCursor(context, whereClause);
        }

        @Override
        public StopModel getCurrentItemFromCursor(Cursor cursor) {
            return cursorAdapterSupplier.getCurrentItemFromCursor(cursor);
        }

        @Override
        public StopModel getItemFromId(Context context, Object id) {
            return cursorAdapterSupplier.getItemFromId(context, id);
        }
    }

    static class PrePopulateAsyncTask extends AsyncTask<Bundle, Void, Bundle> {
        PlaceholderFragment fragment;

        PrePopulateAsyncTask(PlaceholderFragment fragment) {
            this.fragment = fragment;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            fragment.progressView.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bundle doInBackground(Bundle... params) {
            Context context = fragment.getContext();
            Bundle returnBundle = new Bundle();

            StopModel inDest = (StopModel) params[0].get(Constants.DESTINATION_STATION);
            StopModel inStart = (StopModel) params[0].get(Constants.STARTING_STATION);
            RouteDirectionModel inputRoute = (RouteDirectionModel) params[0].get(Constants.ROUTE_DIRECTION_MODEL);
            if (inputRoute == null)
                return returnBundle;

            RouteDirectionModel foundRoute = null;
            StopModel foundStart = null;
            StopModel foundDest = null;
            Cursor routeCursor = fragment.routeCursorAdapterSupplier.getCursor(context, null);
            if (routeCursor.moveToFirst()) {
                do {
                    RouteDirectionModel model = fragment.routeCursorAdapterSupplier.getCurrentItemFromCursor(routeCursor);
                    if (inputRoute.getRouteId().equals(model.getRouteId()) && inputRoute.getDirectionCode().equals(model.getDirectionCode())) {
                        foundRoute = model;
                        break;
                    }

                } while (routeCursor.moveToNext());

                if (foundRoute == null)
                    return new Bundle();
            }

            returnBundle.putSerializable(Constants.ROUTE_DIRECTION_MODEL, foundRoute);

            RouteSpecificCursorAdapterSupplier cursorAdapterStopSupplier = new RouteSpecificCursorAdapterSupplier(fragment.stopCursorAdapterSupplier, foundRoute.getRouteId(),
                    foundRoute.getDirectionCode(), null, false);
            Cursor startCursor = cursorAdapterStopSupplier.getCursor(context, null);
            if (startCursor.moveToFirst()) {
                do {
                    StopModel model = cursorAdapterStopSupplier.getCurrentItemFromCursor(startCursor);
                    if (inStart.getStopId().equals(model.getStopId())) {
                        foundStart = model;
                        break;
                    }
                } while (startCursor.moveToNext());
            }

            if (foundStart == null)
                return returnBundle;

            returnBundle.putSerializable(Constants.STARTING_STATION, foundStart);
            RouteSpecificCursorAdapterSupplier cursorAdapterStopAfterSupplier = new RouteSpecificCursorAdapterSupplier(fragment.stopAfterCursorAdapterSupplier, foundRoute.getRouteId(),
                    foundRoute.getDirectionCode(), foundStart.getStopId(), true);
            Cursor destCursor = cursorAdapterStopAfterSupplier.getCursor(context, null);
            if (destCursor.moveToFirst()) {
                do {
                    StopModel model = cursorAdapterStopAfterSupplier.getCurrentItemFromCursor(destCursor);
                    if (inDest.getStopId().equals(model.getStopId())) {
                        foundDest = model;
                        break;
                    }
                } while (destCursor.moveToNext());
            }

            if (foundDest != null) {
                returnBundle.putSerializable(Constants.DESTINATION_STATION, foundDest);
            }

            return returnBundle;
        }

        @Override
        protected void onPostExecute(Bundle bundle) {

            try {
                if (bundle.containsKey(Constants.ROUTE_DIRECTION_MODEL)) {
                    fragment.setRoute((RouteDirectionModel) bundle.get(Constants.ROUTE_DIRECTION_MODEL));
                } else {
                    return;
                }

                if (bundle.containsKey(Constants.STARTING_STATION)) {
                    fragment.setStartStop((StopModel) bundle.get(Constants.STARTING_STATION));
                } else {
                    return;
                }

                if (bundle.containsKey(Constants.DESTINATION_STATION)) {
                    fragment.setDestinationStop((StopModel) bundle.get(Constants.DESTINATION_STATION));
                }
            } finally {
                fragment.progressView.setVisibility(View.GONE);
            }
        }
    }

}

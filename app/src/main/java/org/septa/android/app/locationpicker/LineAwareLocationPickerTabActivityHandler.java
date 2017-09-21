package org.septa.android.app.locationpicker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.support.BaseTabActivityHandler;
import org.septa.android.app.support.Consumer;
import org.septa.android.app.support.Criteria;
import org.septa.android.app.support.CursorAdapterSupplier;
import org.septa.android.app.support.RouteModelComparator;

import java.util.ArrayList;
import java.util.Collections;
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
        LineAwareLocationPickerTabActivityHandler.PlaceholderFragment fragment = LineAwareLocationPickerTabActivityHandler.PlaceholderFragment.newInstance();
        fragment.setRouteCursorAdapterSupplier(routeCursorAdapterSupplier);
        fragment.setStopCursorAdapterSupplier(stopCursorAdapterSupplier);
        fragment.setStopAfterCursorAdapterSupplier(busStopAfterCursorAdapterSupplier);
        fragment.setTransitType(transitType);
        fragment.setTabName(this.getTabTitle());
        fragment.setTargetClass(targetClass);
        fragment.setHeaderStringName(headerStringName);
        fragment.setButtonText(buttonText);
        fragment.setPrepopulate(prepopulate);
        return fragment;
    }

    public void setPrepopulate(Bundle prepopulate) {
        this.prepopulate = prepopulate;
    }

    public static class PlaceholderFragment extends Fragment {
        CursorAdapterSupplier<RouteDirectionModel> routeCursorAdapterSupplier;
        CursorAdapterSupplier<StopModel> stopCursorAdapterSupplier;
        CursorAdapterSupplier<StopModel> stopAfterCursorAdapterSupplier;
        TransitType transitType;
        Class targetClass;
        String headerStringName;
        String buttonText;

        View progressView;
        private StopModel startingStation;
        private StopModel endingStation;

        private String tabName;

        RouteDirectionModel selectedRoute;
        private TextView startingStopEditText;
        private TextView closestStopText;
        private TextView destinationStopEditText;
        private TextView lineText;
        private Button queryButton;

        private Bundle prepopulate;

        public static PlaceholderFragment newInstance() {
            PlaceholderFragment fragment = new PlaceholderFragment();
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.line_aware_next_to_arrive_search, container, false);

            if (getContext() == null) {
                return rootView;
            }

            TextView pickerHeaderText = (TextView) rootView.findViewById(R.id.picker_header_text);
            pickerHeaderText.setText(transitType.getString(headerStringName, getContext()));

            lineText = (TextView) rootView.findViewById(R.id.line_text);
            lineText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentTransaction ft = getChildFragmentManager().beginTransaction();

                    LinePickerFragment newFragment = LinePickerFragment.newInstance(routeCursorAdapterSupplier, transitType, new Consumer<RouteDirectionModel>() {
                        @Override
                        public void accept(RouteDirectionModel var1) {
                            selectRoute(var1);

                        }
                    });

                    newFragment.show(ft, "line_picker");
                }
            });
            progressView = rootView.findViewById(R.id.progress_view);

            startingStopEditText = (TextView) rootView.findViewById(R.id.starting_stop);
            destinationStopEditText = (TextView) rootView.findViewById(R.id.destination_stop);
            closestStopText = (TextView) rootView.findViewById(R.id.closest_stop);

            startingStopEditText.setOnTouchListener(new StopPickerOnTouchListener(this, new Consumer<StopModel>() {
                        @Override
                        public void accept(StopModel var1) {
                            setStartStop(var1);
                        }
                    }, stopCursorAdapterSupplier, false)
            );

            destinationStopEditText.setOnTouchListener(new StopPickerOnTouchListener(this, new Consumer<StopModel>() {
                        @Override
                        public void accept(StopModel var1) {
                            setDestinationStop(var1);
                        }
                    }, stopAfterCursorAdapterSupplier, true)
            );

            queryButton = (Button) rootView.findViewById(R.id.view_buses_button);
            queryButton.setText(buttonText);
            queryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (startingStation == null || endingStation == null) {
                        Toast.makeText(getActivity(), "Need to choose a start and end station.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(getActivity(), targetClass);
                    intent.putExtra(Constants.STARTING_STATION, startingStation);
                    intent.putExtra(Constants.DESTINATAION_STATION, endingStation);
                    intent.putExtra(Constants.TRANSIT_TYPE, transitType);
                    intent.putExtra(Constants.LINE_ID, selectedRoute);

                    startActivity(intent);
                }
            });

            View resetView = rootView.findViewById(R.id.reset_button);
            resetView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedRoute = null;
                    lineText.setText("");
                    startingStation = null;
                    disableView(startingStopEditText);
                    startingStopEditText.setText("");
                    endingStation = null;
                    disableView(destinationStopEditText);
                    destinationStopEditText.setText("");
                    disableView(queryButton);
                }
            });

            if (prepopulate != null) {
                new PrePopulateAsyncTask(this).execute(prepopulate);
            }

            return rootView;
        }

        void setDestinationStop(StopModel var1) {
            endingStation = var1;
            destinationStopEditText.setText(endingStation.getStopName());
            activateView(queryButton);
        }

        void setStartStop(StopModel var1) {
            setStartingStation(var1, View.INVISIBLE);
            activateView(destinationStopEditText);
            destinationStopEditText.setText("");
            disableView(queryButton);
        }

        void selectRoute(RouteDirectionModel var1) {
            selectedRoute = var1;
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

            lineText.setCompoundDrawablesWithIntrinsicBounds(bullet, drawables[1],
                    drawables[2], drawables[3]);

            lineText.setText(selectedRoute.getRouteShortName() + ": to " + selectedRoute.getDirectionDescription());
            startingStation = null;
            activateView(startingStopEditText);
            startingStopEditText.setText("");
            endingStation = null;
            disableView(destinationStopEditText);
            destinationStopEditText.setText("");
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

        public void setRouteCursorAdapterSupplier(CursorAdapterSupplier<RouteDirectionModel> cursorAdapterSupplier) {
            this.routeCursorAdapterSupplier = cursorAdapterSupplier;
        }

        public void setStopCursorAdapterSupplier(CursorAdapterSupplier<StopModel> stopCursorAdapterSupplier) {
            this.stopCursorAdapterSupplier = stopCursorAdapterSupplier;
        }

        public void setTabName(String tabName) {
            this.tabName = tabName;
        }

        void setStartingStation(StopModel start, int invisible) {
            startingStation = start;
            startingStopEditText.setText(startingStation.getStopName());
            closestStopText.setVisibility(invisible);
            destinationStopEditText.setText(null);
            endingStation = null;
        }

        public void setStopAfterCursorAdapterSupplier(CursorAdapterSupplier<StopModel> stopAfterCursorAdapterSupplier) {
            this.stopAfterCursorAdapterSupplier = stopAfterCursorAdapterSupplier;
        }

        public void setTransitType(TransitType transitType) {
            this.transitType = transitType;
        }

        public void setTargetClass(Class targetClass) {
            this.targetClass = targetClass;
        }

        public void setHeaderStringName(String headerStringName) {
            this.headerStringName = headerStringName;
        }

        public void setButtonText(String buttonText) {
            this.buttonText = buttonText;
        }

        public void setPrepopulate(Bundle prepopulate) {
            this.prepopulate = prepopulate;
        }
    }


    public static class StopPickerOnTouchListener implements View.OnTouchListener {
        private PlaceholderFragment parent;
        private Consumer<StopModel> consumer;
        private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;
        private boolean userAfter;

        StopPickerOnTouchListener(PlaceholderFragment parent, Consumer<StopModel> consumer, CursorAdapterSupplier<StopModel> cursorAdapterSupplier, boolean userAfter) {
            this.parent = parent;
            this.consumer = consumer;
            this.cursorAdapterSupplier = cursorAdapterSupplier;
            this.userAfter = userAfter;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int action = motionEvent.getActionMasked();
            if (action == MotionEvent.ACTION_UP) {
                if (userAfter && parent.startingStation == null)
                    return true;

                FragmentTransaction ft = parent.getFragmentManager().beginTransaction();
                Fragment prev = parent.getFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                CursorAdapterSupplier<StopModel> routeSpecificCursorAdapterSupplier = new RouteSpecificCursorAdapterSupplier(cursorAdapterSupplier, parent, userAfter);

                // Create and show the dialog.
                LocationPickerFragment newFragment = LocationPickerFragment.newInstance(consumer, routeSpecificCursorAdapterSupplier);
                newFragment.show(ft, "dialog");

                return true;
            }
            return false;
        }
    }

    static class RouteSpecificCursorAdapterSupplier implements CursorAdapterSupplier<StopModel> {
        CursorAdapterSupplier<StopModel> cursorAdapterSupplier;
        PlaceholderFragment parent;
        private boolean userAfter;


        public RouteSpecificCursorAdapterSupplier(CursorAdapterSupplier<StopModel> cursorAdapterSupplier, PlaceholderFragment parent, boolean userAfter) {
            this.cursorAdapterSupplier = cursorAdapterSupplier;
            this.parent = parent;
            this.userAfter = userAfter;
        }

        @Override
        public Cursor getCursor(Context context, List<Criteria> whereClause) {
            StringBuilder whereClauseBuilder = new StringBuilder();
            if (whereClause == null) {
                whereClause = new ArrayList<Criteria>();
            }
            whereClause.add(new Criteria("route_id", Criteria.Operation.EQ, parent.selectedRoute.getRouteId()));
            whereClause.add(new Criteria("direction_id", Criteria.Operation.EQ, parent.selectedRoute.getDirectionCode()));
            if (userAfter) {
                whereClause.add(new Criteria("after_stop_id", Criteria.Operation.EQ, parent.startingStation.getStopId()));
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
        protected Bundle doInBackground(Bundle... params) {

            Context context = fragment.getContext();

            StopModel inDest = (StopModel) params[0].get(Constants.DESTINATAION_STATION);
            StopModel inStart = (StopModel) params[0].get(Constants.STARTING_STATION);
            RouteDirectionModel inputRoute = (RouteDirectionModel) params[0].get(Constants.LINE_ID);

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

            Bundle returnBundle = new Bundle();
            returnBundle.putSerializable(Constants.LINE_ID, foundRoute);

            fragment.selectedRoute = foundRoute;
            RouteSpecificCursorAdapterSupplier cursorAdapterStopSupplier = new RouteSpecificCursorAdapterSupplier(fragment.stopCursorAdapterSupplier, fragment, false);
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
            fragment.startingStation = foundStart;
            RouteSpecificCursorAdapterSupplier cursorAdapterStopAfterSupplier = new RouteSpecificCursorAdapterSupplier(fragment.stopAfterCursorAdapterSupplier, fragment, true);
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
                returnBundle.putSerializable(Constants.DESTINATAION_STATION, foundDest);
            }

            return returnBundle;
        }

        @Override
        protected void onPostExecute(Bundle bundle) {
            if (bundle.containsKey(Constants.LINE_ID)) {
                fragment.selectRoute((RouteDirectionModel) bundle.get(Constants.LINE_ID));
            } else {
                return;
            }

            if (bundle.containsKey(Constants.STARTING_STATION)) {
                fragment.setStartStop((StopModel) bundle.get(Constants.STARTING_STATION));
            } else {
                return;
            }

            if (bundle.containsKey(Constants.DESTINATAION_STATION)) {
                fragment.setDestinationStop((StopModel) bundle.get(Constants.DESTINATAION_STATION));
            }
        }
    }

}

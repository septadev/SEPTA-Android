package org.septa.android.app.transitview;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.database.DatabaseManager;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.support.CursorAdapterSupplier;
import org.septa.android.app.support.RouteModelComparator;
import org.septa.android.app.view.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.septa.android.app.transitview.TransitViewUtils.isTrolley;

public class TransitViewFragment extends Fragment implements TransitViewLinePickerFragment.TransitViewLinePickerListener {

    private static final String TAG = TransitViewFragment.class.getSimpleName();

    private RouteDirectionModel firstRoute, secondRoute, thirdRoute;
    private CursorAdapterSupplier<RouteDirectionModel> busRouteCursorAdapterSupplier, trolleyRouteCursorAdapterSupplier;
    public static final String TRANSITVIEW_ROUTE_FIRST = "TRANSITVIEW_ROUTE_FIRST",
            TRANSITVIEW_ROUTE_SECOND = "TRANSITVIEW_ROUTE_SECOND",
            TRANSITVIEW_ROUTE_THIRD = "TRANSITVIEW_ROUTE_THIRD";

    // layout variables
    private TextView firstRoutePicker, secondRoutePicker, thirdRoutePicker;
    private View queryButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if (getActivity() == null) {
            return null;
        }

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        DatabaseManager dbManager = DatabaseManager.getInstance(getActivity());
        busRouteCursorAdapterSupplier = dbManager.getBusNoDirectionRouteCursorAdapterSupplier();
        trolleyRouteCursorAdapterSupplier = dbManager.getTrolleyNoDirectionRouteCursorAdapterSupplier();

        View rootView = inflater.inflate(R.layout.fragment_transitview, null);
        View resetView = rootView.findViewById(R.id.reset_button);
        firstRoutePicker = rootView.findViewById(R.id.transitview_line_picker_first);
        secondRoutePicker = rootView.findViewById(R.id.transitview_line_picker_second);
        thirdRoutePicker = rootView.findViewById(R.id.transitview_line_picker_third);
        queryButton = rootView.findViewById(R.id.view_map);

        // first route picker
        firstRoutePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get which routes have already been selected
                String[] selectedRoutes = new String[2];
                if (secondRoute != null) {
                    selectedRoutes[0] = secondRoute.getRouteId();
                }
                if (thirdRoute != null) {
                    selectedRoutes[1] = thirdRoute.getRouteId();
                }

                // pop-up transitview route picker
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                TransitViewLinePickerFragment newFragment = TransitViewLinePickerFragment.newInstance(busRouteCursorAdapterSupplier, trolleyRouteCursorAdapterSupplier, selectedRoutes);
                newFragment.setTargetFragment(TransitViewFragment.this, 1);
                newFragment.show(ft, "line_picker");
            }
        });

        // second route picker
        secondRoutePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get which routes have already been selected
                String[] selectedRoutes = new String[2];
                if (firstRoute != null) {
                    selectedRoutes[0] = firstRoute.getRouteId();
                }
                if (thirdRoute != null) {
                    selectedRoutes[1] = thirdRoute.getRouteId();
                }

                // pop-up transitview route picker
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                TransitViewLinePickerFragment newFragment = TransitViewLinePickerFragment.newInstance(busRouteCursorAdapterSupplier, trolleyRouteCursorAdapterSupplier, selectedRoutes);
                newFragment.setTargetFragment(TransitViewFragment.this, 2);
                newFragment.show(ft, "line_picker");
            }
        });

        // third route picker
        thirdRoutePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get which routes have already been selected
                String[] selectedRoutes = new String[2];
                if (firstRoute != null) {
                    selectedRoutes[0] = firstRoute.getRouteId();
                }
                if (secondRoute != null) {
                    selectedRoutes[1] = secondRoute.getRouteId();
                }

                // pop-up transitview route picker
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                TransitViewLinePickerFragment newFragment = TransitViewLinePickerFragment.newInstance(busRouteCursorAdapterSupplier, trolleyRouteCursorAdapterSupplier, selectedRoutes);
                newFragment.setTargetFragment(TransitViewFragment.this, 3);
                newFragment.show(ft, "line_picker");
            }
        });

        // reset selection
        resetView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firstRoute = null;
                secondRoute = null;
                thirdRoute = null;

                firstRoutePicker.setText(R.string.transitview_line_picker_first_text);
                secondRoutePicker.setText(R.string.transitview_line_picker_second_text);
                thirdRoutePicker.setText(R.string.transitview_line_picker_third_text);

                firstRoutePicker.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(getContext(), R.drawable.ic_line_picker), null);
                secondRoutePicker.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(getContext(), R.drawable.ic_line_picker), null);
                thirdRoutePicker.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(getContext(), R.drawable.ic_line_picker), null);

                disableView(secondRoutePicker);
                disableView(thirdRoutePicker);
                disableView(queryButton);
            }
        });

        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToTransitViewResults();
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == TransitViewLinePickerFragment.SUCCESS) {
            switch (requestCode) {
                case 1:
                    selectFirstRoute((RouteDirectionModel) data.getSerializableExtra(TransitViewLinePickerFragment.ROUTE_DIRECTION_MODEL));
                    break;
                case 2:
                    selectSecondRoute((RouteDirectionModel) data.getSerializableExtra(TransitViewLinePickerFragment.ROUTE_DIRECTION_MODEL));
                    break;
                case 3:
                    selectThirdRoute((RouteDirectionModel) data.getSerializableExtra(TransitViewLinePickerFragment.ROUTE_DIRECTION_MODEL));
                    break;
                default:
                    Log.e(TAG, "Invalid request code returned from TransitViewLinePicker");
                    break;
            }

        } else {
            Log.e(TAG, "Unsuccessful request code from TransitViewLinePicker #" + requestCode);
        }
    }

    @Override
    public void selectFirstRoute(RouteDirectionModel route) {
        this.firstRoute = route;
        firstRoutePicker.setText(firstRoute.getRouteLongName());

        TransitType transitType = TransitType.BUS;
        if (isTrolley(firstRoute.getRouteId())) {
            transitType = TransitType.TROLLEY;
        }

        // add color bullet icon beside route name
        int color = ContextCompat.getColor(getContext(), transitType.getLineColor(firstRoute.getRouteId(), getContext()));
        Drawable bullet = ContextCompat.getDrawable(getContext(), R.drawable.shape_line_marker);
        bullet.setColorFilter(color, PorterDuff.Mode.SRC);
        firstRoutePicker.setCompoundDrawablesWithIntrinsicBounds(bullet, null, ContextCompat.getDrawable(getContext(), R.drawable.ic_line_picker), null);

        activateView(secondRoutePicker);
        activateView(queryButton);
    }

    @Override
    public void selectSecondRoute(RouteDirectionModel route) {
        this.secondRoute = route;
        secondRoutePicker.setText(secondRoute.getRouteLongName());

        TransitType transitType = TransitType.BUS;
        if (isTrolley(secondRoute.getRouteId())) {
            transitType = TransitType.TROLLEY;
        }

        // add color bullet icon beside route name
        int color = ContextCompat.getColor(getContext(), transitType.getLineColor(secondRoute.getRouteId(), getContext()));
        Drawable bullet = ContextCompat.getDrawable(getContext(), R.drawable.shape_line_marker);
        bullet.setColorFilter(color, PorterDuff.Mode.SRC);
        secondRoutePicker.setCompoundDrawablesWithIntrinsicBounds(bullet, null, ContextCompat.getDrawable(getContext(), R.drawable.ic_line_picker), null);

        activateView(thirdRoutePicker);
    }

    @Override
    public void selectThirdRoute(RouteDirectionModel route) {
        this.thirdRoute = route;
        thirdRoutePicker.setText(thirdRoute.getRouteLongName());

        TransitType transitType = TransitType.BUS;
        if (isTrolley(thirdRoute.getRouteId())) {
            transitType = TransitType.TROLLEY;
        }

        // add color bullet icon beside route name
        int color = ContextCompat.getColor(getContext(), transitType.getLineColor(thirdRoute.getRouteId(), getContext()));
        Drawable bullet = ContextCompat.getDrawable(getContext(), R.drawable.shape_line_marker);
        bullet.setColorFilter(color, PorterDuff.Mode.SRC);
        thirdRoutePicker.setCompoundDrawablesWithIntrinsicBounds(bullet, null, ContextCompat.getDrawable(getContext(), R.drawable.ic_line_picker), null);
    }

    private void disableView(View view) {
        view.setAlpha((float) .3);
        view.setClickable(false);
    }

    private void activateView(View view) {
        view.setAlpha(1);
        view.setClickable(true);
    }

    private void goToTransitViewResults() {
        // TODO: sort the routes in TransitViewActivity
        Intent intent = new Intent(getActivity(), TransitViewResultsActivity.class);

        // sort the routes and append null routes to the end
        List<RouteDirectionModel> selectedRoutes = new ArrayList<>();
        selectedRoutes.add(firstRoute);
        if (secondRoute != null) {
            selectedRoutes.add(secondRoute);
        }
        if (thirdRoute != null) {
            selectedRoutes.add(thirdRoute);
        }
        Collections.sort(selectedRoutes, new RouteModelComparator());
        while (selectedRoutes.size() < 3) {
            selectedRoutes.add(null);
        }

        intent.putExtra(TRANSITVIEW_ROUTE_FIRST, selectedRoutes.get(0));
        intent.putExtra(TRANSITVIEW_ROUTE_SECOND, selectedRoutes.get(1));
        intent.putExtra(TRANSITVIEW_ROUTE_THIRD, selectedRoutes.get(2));
        startActivity(intent);
    }

}
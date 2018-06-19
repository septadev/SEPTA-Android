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
import org.septa.android.app.view.TextView;

import java.util.Arrays;

public class TransitViewFragment extends Fragment implements TransitViewLinePickerFragment.TransitViewLinePickerListener {

    private static final String TAG = TransitViewFragment.class.getSimpleName();

    RouteDirectionModel firstRoute, secondRoute, thirdRoute;
    CursorAdapterSupplier<RouteDirectionModel> busRouteCursorAdapterSupplier, trolleyRouteCursorAdapterSupplier;

    // layout variables
    TextView firstRoutePicker, secondRoutePicker, thirdRoutePicker;
    View queryButton;

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
        firstRoutePicker = (TextView) rootView.findViewById(R.id.transitview_line_picker_first);
        secondRoutePicker = (TextView) rootView.findViewById(R.id.transitview_line_picker_second);
        thirdRoutePicker = (TextView) rootView.findViewById(R.id.transitview_line_picker_third);
        queryButton = rootView.findViewById(R.id.view_map);

        // first route picker
        firstRoutePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // pop-up transitview route picker
                FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                TransitViewLinePickerFragment newFragment = TransitViewLinePickerFragment.newInstance(busRouteCursorAdapterSupplier, trolleyRouteCursorAdapterSupplier);
                newFragment.setTargetFragment(TransitViewFragment.this, 1);
                newFragment.show(ft, "line_picker");
            }
        });

        // second route picker
        secondRoutePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // pop-up transitview route picker
                FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                TransitViewLinePickerFragment newFragment = TransitViewLinePickerFragment.newInstance(busRouteCursorAdapterSupplier, trolleyRouteCursorAdapterSupplier);
                newFragment.setTargetFragment(TransitViewFragment.this, 2);
                newFragment.show(ft, "line_picker");
            }
        });

        // third route picker
        thirdRoutePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // pop-up transitview route picker
                FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                TransitViewLinePickerFragment newFragment = TransitViewLinePickerFragment.newInstance(busRouteCursorAdapterSupplier, trolleyRouteCursorAdapterSupplier);
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
                // TODO: go to TransitViewResultsActivity
                Log.e(TAG, "TransitView query button clicked!");
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

    private boolean isTrolley(String routeId) {
        String[] trolleyRouteIds = new String[]{"10", "11", "13", "15", "34", "36", "101", "102"};
        return Arrays.asList(trolleyRouteIds).contains(routeId);
    }

}

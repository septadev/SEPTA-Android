package org.septa.android.app.transitview;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
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
import org.septa.android.app.support.AnalyticsManager;
import org.septa.android.app.support.CursorAdapterSupplier;
import org.septa.android.app.view.TextView;

import static org.septa.android.app.transitview.TransitViewUtils.isTrolley;

public class TransitViewFragment extends Fragment implements TransitViewLinePickerFragment.TransitViewLinePickerListener {

    private static final String TAG = TransitViewFragment.class.getSimpleName();

    private Activity activity;
    private TransitViewFragmentListener mListener;

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

        activity = getActivity();
        if (activity == null) {
            return null;
        }

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        DatabaseManager dbManager = DatabaseManager.getInstance(activity);
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
                AnalyticsManager.logContentViewEvent(TAG, AnalyticsManager.CONTENT_VIEW_EVENT_TRANSITVIEW_FROM_PICKER, AnalyticsManager.CONTENT_ID_TRANSITVIEW, null);
                mListener.goToTransitViewResults(firstRoute, secondRoute, thirdRoute);
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof TransitViewFragmentListener)) {
            throw new RuntimeException("Context must implement FavoritesFragmentListener");
        } else {
            mListener = (TransitViewFragmentListener) context;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == TransitViewLinePickerFragment.SUCCESS) {
            switch (requestCode) {
                case 1:
                    addFirstRoute((RouteDirectionModel) data.getSerializableExtra(TransitViewLinePickerFragment.ROUTE_DIRECTION_MODEL));
                    break;
                case 2:
                    addSecondRoute((RouteDirectionModel) data.getSerializableExtra(TransitViewLinePickerFragment.ROUTE_DIRECTION_MODEL));
                    break;
                case 3:
                    addThirdRoute((RouteDirectionModel) data.getSerializableExtra(TransitViewLinePickerFragment.ROUTE_DIRECTION_MODEL));
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
    public void addFirstRoute(RouteDirectionModel route) {
        this.firstRoute = route;
        firstRoutePicker.setText(getString(R.string.line_picker_selection, firstRoute.getRouteId(), firstRoute.getRouteLongName()));
        addColorBulletBesideRoute(firstRoute, firstRoutePicker);

        activateView(secondRoutePicker);
        activateView(queryButton);
    }

    @Override
    public void addSecondRoute(RouteDirectionModel route) {
        this.secondRoute = route;
        secondRoutePicker.setText(getString(R.string.line_picker_selection, secondRoute.getRouteId(), secondRoute.getRouteLongName()));
        addColorBulletBesideRoute(secondRoute, secondRoutePicker);

        activateView(thirdRoutePicker);
    }

    @Override
    public void addThirdRoute(RouteDirectionModel route) {
        this.thirdRoute = route;
        thirdRoutePicker.setText(getString(R.string.line_picker_selection, thirdRoute.getRouteId(), thirdRoute.getRouteLongName()));
        addColorBulletBesideRoute(thirdRoute, thirdRoutePicker);
    }

    private void addColorBulletBesideRoute(RouteDirectionModel route, TextView routePicker) {
        Context context = getContext();
        if (context != null) {
            TransitType transitType = TransitType.BUS;
            if (isTrolley(context, route.getRouteId())) {
                transitType = TransitType.TROLLEY;
            }

            // add color bullet icon beside route name
            int color = ContextCompat.getColor(context, transitType.getLineColor(route.getRouteId(), context));
            Drawable bullet = ContextCompat.getDrawable(context, R.drawable.shape_line_marker);
            bullet.setColorFilter(color, PorterDuff.Mode.SRC);
            routePicker.setCompoundDrawablesWithIntrinsicBounds(bullet, null, ContextCompat.getDrawable(context, R.drawable.ic_line_picker), null);
        }
    }

    private void disableView(View view) {
        view.setAlpha((float) .3);
        view.setClickable(false);
    }

    private void activateView(View view) {
        view.setAlpha(1);
        view.setClickable(true);
    }

    public interface TransitViewFragmentListener {
        void goToTransitViewResults(RouteDirectionModel firstRoute, RouteDirectionModel secondRoute, RouteDirectionModel thirdRoute);
    }

}
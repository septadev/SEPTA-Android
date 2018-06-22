package org.septa.android.app.transitview;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import org.septa.android.app.R;
import org.septa.android.app.database.DatabaseManager;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.TransitViewModelResponse;
import org.septa.android.app.support.CursorAdapterSupplier;
import org.septa.android.app.support.RouteModelComparator;
import org.septa.android.app.view.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransitViewResultsActivity extends AppCompatActivity implements Runnable, TransitViewLinePickerFragment.TransitViewLinePickerListener {

    private static final String TAG = TransitViewResultsActivity.class.getSimpleName();

    private RouteDirectionModel firstRoute, secondRoute, thirdRoute;
    private String routeIds;

    private Handler refreshHandler;

    // layout variables
    TextView addLabel, firstRouteLabel, secondRouteLabel, thirdRouteLabel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeActivity(savedInstanceState);

        setTitle(R.string.transit_view);
        setContentView(R.layout.activity_transitview_results);

        // initialize view
        initializeView();

        // initialize route labels
        updateRouteLabels(firstRoute, secondRoute, thirdRoute);

        // set up automatic refresh
        if (firstRoute != null) {
            refreshHandler = new Handler();
            refreshHandler.postDelayed(this, 30 * 1000);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO: inflate options menu

        return true;
    }

    @Override
    public void run() {
        refreshData();
    }

    @Override
    public void selectFirstRoute(RouteDirectionModel route) {
        Log.e(TAG, "Invalid attempt to select the first route from the TransitViewResultsActivity -- going back to TransitView route picker");

        Toast.makeText(this, R.string.transitview_add_route, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void selectSecondRoute(RouteDirectionModel route) {
        // sort the routes and append a null route to the end
        List<RouteDirectionModel> selectedRoutes = new ArrayList<>();
        selectedRoutes.add(firstRoute);
        selectedRoutes.add(route);
        Collections.sort(selectedRoutes, new RouteModelComparator());
        selectedRoutes.add(null);

        updateRouteLabels(selectedRoutes.get(0), selectedRoutes.get(1), selectedRoutes.get(2));
    }

    @Override
    public void selectThirdRoute(RouteDirectionModel route) {
        // sort the routes
        List<RouteDirectionModel> selectedRoutes = new ArrayList<>();
        selectedRoutes.add(firstRoute);
        selectedRoutes.add(secondRoute);
        selectedRoutes.add(route);
        Collections.sort(selectedRoutes, new RouteModelComparator());

        updateRouteLabels(selectedRoutes.get(0), selectedRoutes.get(1), selectedRoutes.get(2));
    }

    private void initializeActivity(@Nullable Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Bundle bundle = getIntent().getExtras();

        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        } else {
            restoreState(bundle);
        }
    }

    private void restoreState(Bundle bundle) {
        firstRoute = (RouteDirectionModel) bundle.get(TransitViewFragment.TRANSITVIEW_ROUTE_FIRST);
        secondRoute = (RouteDirectionModel) bundle.get(TransitViewFragment.TRANSITVIEW_ROUTE_SECOND);
        thirdRoute = (RouteDirectionModel) bundle.get(TransitViewFragment.TRANSITVIEW_ROUTE_THIRD);
    }

    private void initializeView() {
        firstRouteLabel = (TextView) findViewById(R.id.first_route_delete);
        secondRouteLabel = (TextView) findViewById(R.id.second_route_delete);
        thirdRouteLabel = (TextView) findViewById(R.id.third_route_delete);
        addLabel = (TextView) findViewById(R.id.header_add_label);

        firstRouteLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: prompt to delete

                if (secondRoute != null || thirdRoute != null) {
                    // delete first route
                    updateRouteLabels(secondRoute, thirdRoute, null);
                } else {
                    // take user back to picker screen
                    finish();
                }
            }
        });

        secondRouteLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: prompt to delete

                // delete second route
                updateRouteLabels(firstRoute, thirdRoute, null);
            }
        });

        thirdRouteLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: prompt to delete

                // delete third route
                updateRouteLabels(firstRoute, secondRoute, null);
            }
        });

        addLabel.setOnClickListener(new View.OnClickListener() {
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

                // pop-up transitview route picker dialog
                DatabaseManager dbManager = DatabaseManager.getInstance(TransitViewResultsActivity.this);
                CursorAdapterSupplier<RouteDirectionModel> busRouteCursorAdapterSupplier = dbManager.getBusNoDirectionRouteCursorAdapterSupplier(),
                        trolleyRouteCursorAdapterSupplier = dbManager.getTrolleyNoDirectionRouteCursorAdapterSupplier();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                TransitViewLinePickerFragment newFragment = TransitViewLinePickerFragment.newInstance(busRouteCursorAdapterSupplier, trolleyRouteCursorAdapterSupplier, selectedRoutes);
                newFragment.show(ft, "line_picker");
            }
        });
    }

    private void updateRouteLabels(@NonNull RouteDirectionModel first, RouteDirectionModel second, RouteDirectionModel third) {
        this.firstRoute = first;
        this.secondRoute = second;
        this.thirdRoute = third;

        StringBuilder routeIdBuilder = new StringBuilder(firstRoute.getRouteId());

        firstRouteLabel.setText(firstRoute.getRouteId());

        if (secondRoute != null) {
            routeIdBuilder.append(",").append(secondRoute.getRouteId());
            secondRouteLabel.setText(secondRoute.getRouteId());
            secondRouteLabel.setVisibility(View.VISIBLE);
        } else {
            secondRouteLabel.setText(null);
            secondRouteLabel.setVisibility(View.GONE);
        }

        if (thirdRoute != null) {
            // update third route label
            routeIdBuilder.append(",").append(thirdRoute.getRouteId());
            thirdRouteLabel.setText(thirdRoute.getRouteId());
            thirdRouteLabel.setVisibility(View.VISIBLE);

            // disable add button
            disableView(addLabel);

        } else {
            thirdRouteLabel.setText(null);
            thirdRouteLabel.setVisibility(View.GONE);

            // make add button clickable
            activateView(addLabel);
        }
        routeIds = routeIdBuilder.toString();

        refreshData();
    }

    private void disableView(View view) {
        view.setAlpha((float) .3);
        view.setClickable(false);
    }

    private void activateView(View view) {
        view.setAlpha(1);
        view.setClickable(true);
    }

    private void refreshData() {
        Log.d(TAG, "Refreshing TransitView data for " + routeIds);

        SeptaServiceFactory.getTransitViewService().getTransitViewResults(routeIds).enqueue(new Callback<TransitViewModelResponse>() {
            @Override
            public void onResponse(Call<TransitViewModelResponse> call, Response<TransitViewModelResponse> response) {
                Map<String, List<TransitViewModelResponse.TransitViewRecord>> routesMap = response.body().getResults().get(0);

                List<TransitViewModelResponse.TransitViewRecord> firstRoutesResults = routesMap.get(firstRoute.getRouteId()),
                        secondRoutesResults, thirdRoutesResults;
                Log.e(TAG, firstRoutesResults.toString()); // TODO: remove

                if (secondRoute != null) {
                    secondRoutesResults = routesMap.get(secondRoute.getRouteId());
                    Log.e(TAG, secondRoutesResults.toString()); // TODO: remove
                }

                if (thirdRoute != null) {
                    thirdRoutesResults = routesMap.get(thirdRoute.getRouteId());
                    Log.e(TAG, thirdRoutesResults.toString()); // TODO: remove
                }

            }

            @Override
            public void onFailure(Call<TransitViewModelResponse> call, Throwable t) {
                Log.e(TAG, "No TransitView results found for the routes: " + routeIds, t);
                showNoResultsFoundErrorMessage(); // TODO: how should this be handled
            }
        });
    }

    private void showNoResultsFoundErrorMessage() {
        // show error message and hide
        Log.e(TAG, "No TransitView results found");
    }

}

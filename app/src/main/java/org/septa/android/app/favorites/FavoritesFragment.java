package org.septa.android.app.favorites;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.nextarrive.NextArrivalModelResponseParser;
import org.septa.android.app.nextarrive.NextToArriveResultsActivity;
import org.septa.android.app.nextarrive.NextToArriveTripView;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Favorite;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalModelResponse;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by jkampf on 9/5/17.
 */
public class FavoritesFragment extends Fragment implements Runnable {

    // this version is the most recent to require a force delete of user favorites
    private static final int FAVORITES_LAST_UPDATED_VERSION = 268;

    private static final String KEY_TITLE = "KEY_TITLE";
    private static final int REFRESH_DELAY_SECONDS = 30;
    private Map<String, Favorite> favoritesMap;
    private Map<String, TextView> favoriteTitlesMap = new HashMap<String, TextView>();
    private Map<String, NextToArriveTripView> nextToArriveTripViewMap = new HashMap<String, NextToArriveTripView>();
    private Map<String, View> progressViewMap = new HashMap<String, View>();
    int initialCount;
    private FavoritesFragmentCallBacks favoritesFragmentCallBacks;
    Handler refreshHandler = null;
    View fragmentView;
    String alertMessage;
    boolean firstPass = true;

    public static FavoritesFragment newInstance() {
        FavoritesFragment instance = new FavoritesFragment();
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        favoritesMap = SeptaServiceFactory.getFavoritesService().getFavorites(getContext());
        alertMessage = evaluateAndRemoveFavorites(favoritesMap);

        initialCount = favoritesMap.size();
        if (favoritesMap.size() == 0)
            return onCreateViewNoFavorites(inflater, container, savedInstanceState);
        else return onCreateViewFavorites(inflater, container, savedInstanceState);
    }

    private String evaluateAndRemoveFavorites(Map<String, Favorite> favoritesMap) {
        // In Version 268 we fixed some issues with NHSL and Subway.  However users could have created
        // faulty favorites with the version before 268.  We added a created with version number to
        // the favorite record defaulting to 0.  We compare that value to the value 268.  If the
        // favorite is NHSL or Subway and created in version is older than 268 we delete the
        // favorite and display a message.

        // If need to force remove favorites in new version then change FAVORITES_LAST_UPDATED_VERSION

        Map<String, Favorite> loopMap = new HashMap<String, Favorite>();
        loopMap.putAll(favoritesMap);

        List<Favorite> toDelete = new LinkedList<Favorite>();

        String msg = null;
        for (Map.Entry<String, Favorite> entry : loopMap.entrySet()) {
            if ((entry.getValue().getCreatedWithVersion() < FAVORITES_LAST_UPDATED_VERSION) &&
                    ((entry.getValue().getTransitType() == TransitType.NHSL) ||
                            (entry.getValue()).getTransitType() == TransitType.SUBWAY)) {
                favoritesMap.remove(entry.getKey());
                toDelete.add(entry.getValue());
                msg = getString(R.string.force_delete_nhsl_subway_favorite);
            }
        }

        Activity activity = getActivity();
        if (activity == null)
            return null;

        for (Favorite favorite : toDelete) {
            SeptaServiceFactory.getFavoritesService().deleteFavorite(activity, favorite.getKey());
        }


        return msg;
    }

    private View onCreateViewFavorites(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        fragmentView = inflater.inflate(R.layout.favorites_view, container, false);
        LinearLayout favoritesListView = (LinearLayout) fragmentView.findViewById(R.id.favorites_list);

        for (Map.Entry<String, Favorite> entry : favoritesMap.entrySet()) {
            final Favorite favorite = entry.getValue();
            View convertView = inflater.inflate(R.layout.favorite_item, favoritesListView, false);
            TextView favName = (TextView) convertView.findViewById(R.id.favorite_title_text);
            favoriteTitlesMap.put(entry.getKey(), favName);
            favName.setText(favorite.getName());
            Drawable drawables[] = favName.getCompoundDrawables();
            favName.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(),
                    favorite.getTransitType().getTabActiveImageResource()),
                    drawables[1], drawables[2], drawables[3]);

            ViewGroup containerView = (ViewGroup) convertView.findViewById(R.id.next_to_arrive_trip_details);
            final View progressView = containerView.findViewById(R.id.progress_view);
            progressViewMap.put(entry.getKey(), progressView);
            progressView.setVisibility(View.VISIBLE);

            final NextToArriveTripView tripView = new NextToArriveTripView(getContext());
            tripView.setMaxResults(3);
            tripView.setTransitType(favorite.getTransitType());
            tripView.setStart(favorite.getStart());
            tripView.setDestination(favorite.getDestination());
            tripView.setRouteDirectionModel(favorite.getRouteDirectionModel());
            nextToArriveTripViewMap.put(entry.getKey(), tripView);

            containerView.addView(tripView);
            favoritesListView.addView(convertView);

            refreshFavorite(favorite, tripView, progressView);

            View moreButton = convertView.findViewById(R.id.more_button);
            moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (getActivity() == null)
                        return;
                    Intent intent = new Intent(getActivity(), NextToArriveResultsActivity.class);
                    intent.putExtra(Constants.STARTING_STATION, favorite.getStart());
                    intent.putExtra(Constants.DESTINATION_STATION, favorite.getDestination());
                    intent.putExtra(Constants.TRANSIT_TYPE, favorite.getTransitType());
                    intent.putExtra(Constants.ROUTE_DIRECTION_MODEL, favorite.getRouteDirectionModel());
                    intent.putExtra(Constants.EDIT_FAVORITES_FLAG, Boolean.TRUE);

                    getActivity().startActivityForResult(intent, Constants.NTA_REQUEST);
                }
            });
            moreButton.setContentDescription("Tap for more Next to Arrive details for your favorite, " + favName.getText());
        }


        return fragmentView;
    }

    @Override
    public void run() {
        for (Map.Entry<String, Favorite> entry : favoritesMap.entrySet()) {
            refreshFavorite(entry.getValue(), nextToArriveTripViewMap.get(entry.getKey()), progressViewMap.get(entry.getKey()));
        }

        refreshHandler.postDelayed(this, REFRESH_DELAY_SECONDS * 1000);
    }

    private void refreshFavorite(final Favorite favorite, final NextToArriveTripView tripView, final View progressView) {
        progressView.setVisibility(View.VISIBLE);

        String routeId = null;
        if (favorite.getRouteDirectionModel() != null)
            routeId = favorite.getRouteDirectionModel().getRouteId();

        Call<NextArrivalModelResponse> results = SeptaServiceFactory.getNextArrivalService().getNextArrival(Integer.parseInt(favorite.getStart().getStopId()),
                Integer.parseInt(favorite.getDestination().getStopId()),
                favorite.getTransitType().name(), routeId);

        results.enqueue(new Callback<NextArrivalModelResponse>() {
            @Override
            public void onResponse(@NonNull Call<NextArrivalModelResponse> call, @NonNull Response<NextArrivalModelResponse> response) {
                if (response != null && response.body() != null)
                    tripView.setNextToArriveData(new NextArrivalModelResponseParser(response.body()));
                progressView.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(@NonNull Call<NextArrivalModelResponse> call, @NonNull Throwable t) {
                tripView.setNextToArriveData(new NextArrivalModelResponseParser());
                Snackbar snackbar = Snackbar.make(fragmentView, R.string.realtime_failure_message, Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("Schedules »", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        favoritesFragmentCallBacks.gotoSchedules();
                    }
                });
                View snackbarView = snackbar.getView();
                android.widget.TextView tv = (android.widget.TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                tv.setMaxLines(10);
                snackbar.show();
                progressView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshHandler = new Handler();
        if (!firstPass) {
            favoritesMap = SeptaServiceFactory.getFavoritesService().getFavorites(getContext());

            if (initialCount != favoritesMap.size()) {
                favoritesFragmentCallBacks.refresh();
                return;
            }

            for (Map.Entry<String, TextView> entry : favoriteTitlesMap.entrySet()) {
                Favorite fav = favoritesMap.get(entry.getKey());
                if (fav == null) {
                    favoritesFragmentCallBacks.refresh();
                    return;
                }
                entry.getValue().setText(fav.getName());
            }

            run();
        } else {
            firstPass = false;
        }

        if (alertMessage != null) {

            Activity activity = getActivity();
            if (activity != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage(alertMessage);
                builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }

            alertMessage = null;
        }


    }

    @Override
    public void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(this);
    }

    private View onCreateViewNoFavorites(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(false);
        View fragmentView = inflater.inflate(R.layout.no_favorites, container, false);

        View button = fragmentView.findViewById(R.id.add_favorite_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favoritesFragmentCallBacks.addNewFavorite();
            }
        });

        return fragmentView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_favorite) {
            favoritesFragmentCallBacks.addNewFavorite();
        }

        return true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof FavoritesFragmentCallBacks)) {
            throw new RuntimeException("Context must implement FavoritesFragmentCallBacks");
        } else {
            favoritesFragmentCallBacks = (FavoritesFragmentCallBacks) context;
        }

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.my_favorites_menu, menu);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_TITLE, getActivity().getTitle().toString());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            String title = savedInstanceState.getString(KEY_TITLE);
            if (title != null && getActivity() != null)
                getActivity().setTitle(title);
        }
    }
}



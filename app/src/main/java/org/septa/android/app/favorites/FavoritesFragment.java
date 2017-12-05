package org.septa.android.app.favorites;

import android.content.Context;
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
import org.septa.android.app.nextarrive.NextArrivalModelResponseParser;
import org.septa.android.app.nextarrive.NextToArriveResultsActivity;
import org.septa.android.app.nextarrive.NextToArriveTripView;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Favorite;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalModelResponse;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by jkampf on 9/5/17.
 */
public class FavoritesFragment extends Fragment implements Runnable {

    private static final int REFRESH_DELAY_SECONDS = 30;
    private Map<String, Favorite> favoritesMap;
    private Map<String, TextView> favoriteTitlesMap = new HashMap<String, TextView>();
    private Map<String, NextToArriveTripView> nextToArriveTripViewMap = new HashMap<String, NextToArriveTripView>();
    private Map<String, View> progressViewMap = new HashMap<String, View>();
    int initialCount;
    private FavoritesFragmentCallBacks favoritesFragmentCallBacks;
    Handler refreshHandler = null;
    View fragmentView;

    public static FavoritesFragment newInstance() {
        FavoritesFragment instance = new FavoritesFragment();
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        favoritesMap = SeptaServiceFactory.getFavoritesService().getFavorites(getContext());
        initialCount = favoritesMap.size();
        if (favoritesMap.size() == 0)
            return onCreateViewNoFavorites(inflater, container, savedInstanceState);
        else return onCreateViewFavorites(inflater, container, savedInstanceState);
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
                    intent.putExtra(Constants.DESTINATAION_STATION, favorite.getDestination());
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
                snackbar.setAction("Scehedules", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        favoritesFragmentCallBacks.gotoSchedules();
                    }
                });
                snackbar.show();
                progressView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
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

        refreshHandler = new Handler();
        run();

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
        outState.putString("title", getActivity().getTitle().toString());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            String title = savedInstanceState.getString("title");
            if (title != null && getActivity() != null)
                getActivity().setTitle(title);
        }
    }
}



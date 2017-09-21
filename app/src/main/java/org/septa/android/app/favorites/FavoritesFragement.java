package org.septa.android.app.favorites;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.nextarrive.NextArrivalModelResponseParser;
import org.septa.android.app.nextarrive.NextToArriveResultsActivity;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Favorite;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalModelResponse;
import org.septa.android.app.nextarrive.NextToArriveTripView;
import org.septa.android.app.support.Consumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by jkampf on 9/5/17.
 */
public class FavoritesFragement extends Fragment {

    private Runnable buttonExecution;
    private Runnable refreshRunnable;
    private Map<String, Favorite> favoritesMap;
    private Map<String, TextView> favoriteTitlesMap = new HashMap<String, TextView>();
    private Map<String, NextToArriveTripView> nextToArriveTripViewMap = new HashMap<String, NextToArriveTripView>();
    Consumer<Integer> menuIdConsumer;
    int initialCount;

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
        View fragmentView = inflater.inflate(R.layout.favorites_view, container, false);
        LinearLayout favoritesListView = (LinearLayout) fragmentView.findViewById(R.id.favorites_list);

        menuIdConsumer.accept(R.menu.my_favorites_menu);

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

            final NextToArriveTripView tripView = new NextToArriveTripView(getContext());
            tripView.setMaxResults(3);
            tripView.setTransitType(favorite.getTransitType());
            tripView.setStart(favorite.getStart());
            tripView.setDestination(favorite.getDestination());
            tripView.setRouteDirectionModel(favorite.getRouteDirectionModel());
            nextToArriveTripViewMap.put(entry.getKey(), tripView);

            containerView.addView(tripView);
            favoritesListView.addView(convertView);

            String routeId = null;
            if (favorite.getRouteDirectionModel() != null)
                routeId = favorite.getRouteDirectionModel().getRouteId();

            Call<NextArrivalModelResponse> results = SeptaServiceFactory.getNextArrivalService()
                    .getNextArriaval(Integer.parseInt(favorite.getStart().getStopId()),
                            Integer.parseInt(favorite.getDestination().getStopId()),
                            favorite.getTransitType().name(), routeId);

            results.enqueue(new Callback<NextArrivalModelResponse>() {
                @Override
                public void onResponse(@NonNull Call<NextArrivalModelResponse> call, @NonNull Response<NextArrivalModelResponse> response) {
                    tripView.setNextToArriveData(new NextArrivalModelResponseParser(response.body()));
                }

                @Override
                public void onFailure(@NonNull Call<NextArrivalModelResponse> call, @NonNull Throwable t) {

                }
            });

            View moreButton = convertView.findViewById(R.id.more_button);
            moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), NextToArriveResultsActivity.class);
                    intent.putExtra(Constants.STARTING_STATION, favorite.getStart());
                    intent.putExtra(Constants.DESTINATAION_STATION, favorite.getDestination());
                    intent.putExtra(Constants.TRANSIT_TYPE, favorite.getTransitType());
                    intent.putExtra(Constants.LINE_ID, favorite.getRouteDirectionModel());
                    intent.putExtra(Constants.EDIT_FAVORITES_FLAG, Boolean.TRUE);

                    getActivity().startActivityForResult(intent, Constants.NTA_REQUEST);
                }
            });
        }


        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        favoritesMap = SeptaServiceFactory.getFavoritesService().getFavorites(getContext());

        if (initialCount != favoritesMap.size()) {
            refreshRunnable.run();
            return;
        }

        for (Map.Entry<String, TextView> entry : favoriteTitlesMap.entrySet()) {
            entry.getValue().setText(favoritesMap.get(entry.getKey()).getName());
        }

    }

    private View onCreateViewNoFavorites(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        menuIdConsumer.accept(0);
        View fragmentView = inflater.inflate(R.layout.no_favorites, container, false);

        View button = fragmentView.findViewById(R.id.add_favorite_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonExecution.run();
            }
        });

        return fragmentView;
    }

    public static FavoritesFragement newInstance(Runnable buttonExecution, Consumer<Integer> menuIdConsumer, Runnable refreshRunnable) {
        FavoritesFragement instance = new FavoritesFragement();
        instance.buttonExecution = buttonExecution;
        instance.menuIdConsumer = menuIdConsumer;
        instance.refreshRunnable = refreshRunnable;
        return instance;
    }

}

package org.septa.android.app.favorites;

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

import org.septa.android.app.R;
import org.septa.android.app.nextarrive.NextArrivalModelResponseParser;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Favorite;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalModelResponse;
import org.septa.android.app.view.NextToArriveTripView;

import java.util.ArrayList;
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
    private Map<String, Favorite> favorites;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        favorites = SeptaServiceFactory.getFavoritesService().getFavorites(getContext());

        if (favorites.size() == 0)
            return onCreateViewNoFavorites(inflater, container, savedInstanceState);
        else return onCreateViewFavorites(inflater, container, savedInstanceState);
    }

    private View onCreateViewFavorites(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.favorites_view, container, false);
        LinearLayout favoritesListView = (LinearLayout) fragmentView.findViewById(R.id.favorites_list);

        List<Favorite> favoritesList = new ArrayList<>(favorites.size());
        favoritesList.addAll(favorites.values());

        for (int i = 0; i < favoritesList.size(); i++) {
            Favorite favorite = favoritesList.get(i);
            View convertView = inflater.inflate(R.layout.favorite_item, favoritesListView, false);
            TextView favName = (TextView) convertView.findViewById(R.id.favorite_title_text);
            favName.setText(favorite.getName());
            Drawable drawables[] = favName.getCompoundDrawables();
            favName.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(),
                    favorite.getTransitType().getTabActiveImageResource()),
                    drawables[1], drawables[2], drawables[3]);

            ViewGroup containerView = (ViewGroup) convertView.findViewById(R.id.next_to_arrive_trip_details);

            final NextToArriveTripView tripView = new NextToArriveTripView(getContext());
            tripView.setTransitType(favorite.getTransitType());
            tripView.setStartStopId(favorite.getStartId());
            tripView.setDestStopId(favorite.getDestinationId());
            tripView.setRouteId(favorite.getLineId());

            containerView.addView(tripView);
            favoritesListView.addView(convertView);

            Call<NextArrivalModelResponse> results = SeptaServiceFactory.getNextArrivalService()
                    .getNextArriaval(Integer.parseInt(favorite.getStartId()),
                            Integer.parseInt(favorite.getDestinationId()),
                            favorite.getTransitType().name(), favorite.getLineId());

            results.enqueue(new Callback<NextArrivalModelResponse>() {
                @Override
                public void onResponse(@NonNull Call<NextArrivalModelResponse> call, @NonNull Response<NextArrivalModelResponse> response) {
                    tripView.setNextToArriveData(new NextArrivalModelResponseParser(response.body()));
                }

                @Override
                public void onFailure(@NonNull Call<NextArrivalModelResponse> call, @NonNull Throwable t) {

                }
            });
        }


        return fragmentView;
    }

    private View onCreateViewNoFavorites(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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

    public static Fragment newInstance(Runnable buttonExecution) {
        FavoritesFragement instance = new FavoritesFragement();
        instance.buttonExecution = buttonExecution;
        return instance;
    }
}

package org.septa.android.app.favorites;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.nextarrive.NextArrivalModelResponseParser;
import org.septa.android.app.nextarrive.NextToArriveDetailsFragment;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Favorite;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalModelResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by jkampf on 9/5/17.
 */

public class FavoritesFragement extends Fragment {


    private Runnable buttonExecution;
    Map<String, Favorite> favorites;
    LinearLayout favoritesListView;
    Map<Favorite, Integer> viewMap = new HashMap<Favorite, Integer>();


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
        View fragmentView = inflater.inflate(R.layout.favorites_view, null);
        favoritesListView = (LinearLayout) fragmentView.findViewById(R.id.favorites_list);

        List<Favorite> favoritesList = new ArrayList<Favorite>(favorites.size());
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

            View containerView = convertView.findViewById(R.id.next_to_arrive_trip_details);
            int id = (i + 1);

            containerView.setId(id);
            favoritesListView.addView(convertView);
        }


        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        List<Favorite> favoritesList = new ArrayList<Favorite>(favorites.size());
        favoritesList.addAll(favorites.values());

        for (int i = 0; i < favoritesList.size(); i++) {
            Favorite favorite = favoritesList.get(i);

            final NextToArriveDetailsFragment fragment = new NextToArriveDetailsFragment();
            fragment.setTransitType(favorite.getTransitType());
            fragment.setStartStopId(favorite.getStartId());
            fragment.setDestStopId(favorite.getDestinationId());
            fragment.setRouteId(favorite.getLineId());

            int id = (i + 1);

            getFragmentManager().beginTransaction().replace(id, fragment).commit();
            //getChildFragmentManager().beginTransaction().replace(id, fragment).commit();

            Call<NextArrivalModelResponse> results = SeptaServiceFactory.getNextArrivalService()
                    .getNextArriaval(Integer.parseInt(favorite.getStartId()),
                            Integer.parseInt(favorite.getDestinationId()),
                            favorite.getTransitType().name(), favorite.getLineId());

            results.enqueue(new Callback<NextArrivalModelResponse>() {
                @Override
                public void onResponse(Call<NextArrivalModelResponse> call, Response<NextArrivalModelResponse> response) {
                    //fragment.setNextToArriveData(new NextArrivalModelResponseParser(response.body()));
                }

                @Override
                public void onFailure(Call<NextArrivalModelResponse> call, Throwable t) {

                }
            });

        }
    }

    private View onCreateViewNoFavorites(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.no_favorites, null);

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

    private class FavoriteArrayAdapter extends ArrayAdapter<Favorite> {

        public FavoriteArrayAdapter(@NonNull Context context, @NonNull List<Favorite> objects) {
            super(context, 0, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.favorite_item, parent, false);
            }

            Favorite favorite = getItem(position);
            TextView favName = (TextView) convertView.findViewById(R.id.favorite_title_text);
            favName.setText(favorite.getName());
            Drawable drawables[] = favName.getCompoundDrawables();
            favName.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(),
                    favorite.getTransitType().getTabActiveImageResource()),
                    drawables[1], drawables[2], drawables[3]);

            Call<NextArrivalModelResponse> results = SeptaServiceFactory.getNextArrivalService()
                    .getNextArriaval(Integer.parseInt(favorite.getStartId()),
                            Integer.parseInt(favorite.getDestinationId()),
                            favorite.getTransitType().name(), favorite.getLineId());

            final NextToArriveDetailsFragment fragment = new NextToArriveDetailsFragment();
            fragment.setTransitType(favorite.getTransitType());
            fragment.setStartStopId(favorite.getStartId());
            fragment.setDestStopId(favorite.getDestinationId());
            fragment.setRouteId(favorite.getLineId());

            View containerView = convertView.findViewById(R.id.next_to_arrive_trip_details);


            getChildFragmentManager().beginTransaction().add(R.id.next_to_arrive_trip_details, fragment).commit();

            results.enqueue(new Callback<NextArrivalModelResponse>() {
                @Override
                public void onResponse(Call<NextArrivalModelResponse> call, Response<NextArrivalModelResponse> response) {
                    fragment.setNextToArriveData(new NextArrivalModelResponseParser(response.body()));
                }

                @Override
                public void onFailure(Call<NextArrivalModelResponse> call, Throwable t) {

                }
            });

            return convertView;
        }
    }
}

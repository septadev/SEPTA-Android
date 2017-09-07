package org.septa.android.app.favorites;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.septa.android.app.R;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Favorite;

import java.util.Map;

/**
 * Created by jkampf on 9/5/17.
 */

public class FavoritesFragement extends Fragment {


    private Runnable buttonExecution;
    Map<String, Favorite> favorites;

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

        return fragmentView;
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
}

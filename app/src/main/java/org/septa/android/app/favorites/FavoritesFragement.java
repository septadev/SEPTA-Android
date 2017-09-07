package org.septa.android.app.favorites;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.septa.android.app.R;

/**
 * Created by jkampf on 9/5/17.
 */

public class FavoritesFragement extends Fragment {


    private Runnable buttonExecution;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return noFavorites(inflater, container, savedInstanceState);
    }


    private View noFavorites(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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

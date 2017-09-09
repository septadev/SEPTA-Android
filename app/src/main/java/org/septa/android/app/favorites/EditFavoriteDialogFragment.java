package org.septa.android.app.favorites;

import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Favorite;
import org.septa.android.app.support.Consumer;
import org.w3c.dom.Text;

/**
 * Created by jkampf on 9/9/17.
 */

public class EditFavoriteDialogFragment extends DialogFragment {

    private Favorite favorite;
    private Consumer<Favorite> favoriteConsumer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.edit_favorite_dialog, container);

        View exit = rootView.findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        TextView nameText = (TextView) rootView.findViewById(R.id.favorite_name);
        nameText.setText(favorite.getName());

        final EditText nameEditText = (EditText) rootView.findViewById(R.id.name_edit_text);
        nameEditText.setText(favorite.getName());

        View saveButton = rootView.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favorite.setName(nameEditText.getText().toString());
                SeptaServiceFactory.getFavoritesService().addFavorites(getContext(), favorite);
                favoriteConsumer.accept(favorite);
                getDialog().dismiss();
            }
        });

        View deleteButton = rootView.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeptaServiceFactory.getFavoritesService().deleteFavorite(getContext(), favorite.getKey());
                getDialog().dismiss();
                getActivity().onBackPressed();
            }
        });

        return rootView;
    }

    public static EditFavoriteDialogFragment getInstance(Favorite favorite, Consumer<Favorite> consumer) {
        EditFavoriteDialogFragment fragment = new EditFavoriteDialogFragment();
        fragment.setFavorite(favorite);
        fragment.setFavoriteConsumer(consumer);
        return fragment;
    }

    public void setFavorite(Favorite favorite) {
        this.favorite = favorite;
    }

    public void setFavoriteConsumer(Consumer<Favorite> favoriteConsumer) {
        this.favoriteConsumer = favoriteConsumer;
    }
}

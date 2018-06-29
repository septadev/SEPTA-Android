package org.septa.android.app.favorites.edit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.favorites.DeleteFavoritesAsyncTask;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalFavorite;

public class RenameFavoriteDialogFragment extends DialogFragment {

    private NextArrivalFavorite nextArrivalFavorite;
    private RenameFavoriteCallBack renameFavoriteCallBack;

    private static final String KEY_FAVORITE = "KEY_FAVORITE";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        restoreArgs();

        View rootView = inflater.inflate(R.layout.edit_favorite_dialog, container);

        View exit = rootView.findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        TextView nameText = rootView.findViewById(R.id.favorite_name);
        nameText.setText(nextArrivalFavorite.getName());

        final EditText nameEditText = rootView.findViewById(R.id.name_edit_text);
        nameEditText.setText(nextArrivalFavorite.getName());

        View saveButton = rootView.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextArrivalFavorite.setName(nameEditText.getText().toString());
                SeptaServiceFactory.getFavoritesService().renameFavorite(getContext(), nextArrivalFavorite);
                if (renameFavoriteCallBack != null) {
                    renameFavoriteCallBack.updateFavorite(nextArrivalFavorite);
                }
                getDialog().dismiss();
            }
        });

        View deleteButton = rootView.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() != null) {
                    new AlertDialog.Builder(getContext()).setCancelable(true).setTitle(R.string.delete_fav_modal_title)
                            .setMessage(R.string.delete_fav_modal_text)
                            .setPositiveButton(R.string.delete_fav_pos_button, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DeleteFavoritesAsyncTask task = new DeleteFavoritesAsyncTask(getContext());
                                    task.execute(nextArrivalFavorite.getKey());
                                    getDialog().dismiss();
                                    if (getActivity() == null) {
                                        return;
                                    }
                                    getActivity().onBackPressed();
                                }
                            })
                            .setNegativeButton(R.string.delete_fav_neg_button, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .create().show();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if ((context instanceof RenameFavoriteCallBack)) {
            renameFavoriteCallBack = (RenameFavoriteCallBack) context;
        }

    }

    public static RenameFavoriteDialogFragment getInstance(NextArrivalFavorite nextArrivalFavorite) {
        RenameFavoriteDialogFragment fragment = new RenameFavoriteDialogFragment();

        Bundle args = new Bundle();
        args.putSerializable(KEY_FAVORITE, nextArrivalFavorite);
        fragment.setArguments(args);

        return fragment;
    }

    private void restoreArgs() {
        nextArrivalFavorite = (NextArrivalFavorite) getArguments().getSerializable(KEY_FAVORITE);
    }


}

package org.septa.android.app.favorites.edit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.favorites.DeleteFavoritesAsyncTask;
import org.septa.android.app.favorites.SaveFavoritesAsyncTask;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Favorite;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalFavorite;
import org.septa.android.app.services.apiinterfaces.model.TransitViewFavorite;
import org.septa.android.app.support.AnalyticsManager;
import org.septa.android.app.support.CrashlyticsManager;

public class RenameFavoriteDialogFragment extends DialogFragment {

    private static final String TAG = RenameFavoriteDialogFragment.class.getSimpleName();

    private Favorite favorite;
    private RenameFavoriteListener mListener;
    private boolean isTransitViewFavorite, isAnOldFavorite;

    public static final String EDIT_FAVORITE_DIALOG_KEY = "EDIT_FAVORITE_DIALOG_KEY";
    private static final String KEY_FAVORITE = "KEY_FAVORITE",
            KEY_FAVORITE_TYPE = "KEY_FAVORITE_TYPE",
            KEY_FAVORITE_EXISTS = "KEY_FAVORITE_EXISTS";

    private TextView favoriteType;

    public static RenameFavoriteDialogFragment newInstance(boolean isTransitViewFavorite, boolean isAnOldFavorite, Favorite favorite) {
        RenameFavoriteDialogFragment fragment = new RenameFavoriteDialogFragment();

        Bundle args = new Bundle();
        args.putSerializable(KEY_FAVORITE, favorite);
        args.putBoolean(KEY_FAVORITE_EXISTS, isAnOldFavorite);
        args.putBoolean(KEY_FAVORITE_TYPE, isTransitViewFavorite);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        restoreArgs();

        View rootView = inflater.inflate(R.layout.edit_favorite_dialog, container);

        TextView nameText = rootView.findViewById(R.id.favorite_name);
        View exit = rootView.findViewById(R.id.exit);
        final EditText nameEditText = rootView.findViewById(R.id.name_edit_text);
        favoriteType = rootView.findViewById(R.id.favorite_type);
        View saveButton = rootView.findViewById(R.id.save_button);
        View deleteButton = rootView.findViewById(R.id.delete_button);

        nameText.setText(favorite.getName());
        nameEditText.setText(favorite.getName());

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

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

                                    AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_DELETE_FAVORITE, AnalyticsManager.CUSTOM_EVENT_ID_FAVORITES_MANAGEMENT, null);

                                    task.execute(favorite.getKey());
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

        // disable delete button on favorite creation
        if (isAnOldFavorite) {
            activateView(deleteButton);
        } else {
            disableView(deleteButton);
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get new favorite name
                favorite.setName(nameEditText.getText().toString());

                if (isTransitViewFavorite) {
                    updateTransitViewFavorite();
                } else {
                    updateNTAFavorite();
                }

                getDialog().dismiss();
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if ((context instanceof RenameFavoriteListener)) {
            mListener = (RenameFavoriteListener) context;
        } else {
            IllegalArgumentException iae = new IllegalArgumentException("Context Must Implement RenameFavoriteListener");
            CrashlyticsManager.log(Log.ERROR, TAG, iae.toString());
            throw iae;
        }
    }

    private void restoreArgs() {
        isAnOldFavorite = getArguments().getBoolean(KEY_FAVORITE_EXISTS);

        if (getArguments().getBoolean(KEY_FAVORITE_TYPE)) {
            isTransitViewFavorite = true;
            favorite = (TransitViewFavorite) getArguments().getSerializable(KEY_FAVORITE);
        } else {
            isTransitViewFavorite = false;
            favorite = (NextArrivalFavorite) getArguments().getSerializable(KEY_FAVORITE);
        }
    }

    private void disableView(View view) {
        view.setAlpha((float) .3);
        view.setClickable(false);
    }

    private void activateView(View view) {
        view.setAlpha(1);
        view.setClickable(true);
    }

    private void updateNTAFavorite() {
        favoriteType.setText(R.string.rename_favorite_nta);
        final NextArrivalFavorite ntaFavorite = (NextArrivalFavorite) favorite;

        if (isAnOldFavorite) {
            AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_RENAME_FAVORITE_NTA, AnalyticsManager.CUSTOM_EVENT_ID_FAVORITES_MANAGEMENT, null);

            // rename existing favorite
            SeptaServiceFactory.getFavoritesService().renameFavorite(getContext(), ntaFavorite);

            // update menu
            mListener.renameFavorite(ntaFavorite);

        } else {
            // create new nta favorite
            SaveFavoritesAsyncTask task = new SaveFavoritesAsyncTask(getContext(), new Runnable() {
                @Override
                public void run() {
                    CrashlyticsManager.log(Log.ERROR, TAG, "Failed to create new NTA favorite for " + ntaFavorite.getKey());
                    mListener.favoriteCreationFailed();
                }
            }, new Runnable() {
                @Override
                public void run() {
                    AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_CREATE_FAVORITE_NTA, AnalyticsManager.CUSTOM_EVENT_ID_FAVORITES_MANAGEMENT, null);

                    // update menu
                    mListener.updateFavorite(ntaFavorite);
                }
            });
            task.execute(ntaFavorite);
        }
    }

    private void updateTransitViewFavorite() {
        favoriteType.setText(R.string.rename_favorite_transitview);
        final TransitViewFavorite transitViewFavorite = (TransitViewFavorite) favorite;

        if (isAnOldFavorite) {
            AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_RENAME_FAVORITE_TRANSITVIEW, AnalyticsManager.CUSTOM_EVENT_ID_FAVORITES_MANAGEMENT, null);

            // rename existing favorite
            SeptaServiceFactory.getFavoritesService().renameFavorite(getContext(), transitViewFavorite);

            // update menu
            mListener.renameFavorite(transitViewFavorite);

        } else {
            // create new transitview favorite
            SaveFavoritesAsyncTask task = new SaveFavoritesAsyncTask(getContext(), new Runnable() {
                @Override
                public void run() {
                    CrashlyticsManager.log(Log.ERROR, TAG, "Failed to create new TransitView favorite for " + transitViewFavorite.getKey());
                    mListener.favoriteCreationFailed();
                }
            }, new Runnable() {
                @Override
                public void run() {
                    AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_CREATE_FAVORITE_TRANSITVIEW, AnalyticsManager.CUSTOM_EVENT_ID_FAVORITES_MANAGEMENT, null);

                    // update menu
                    mListener.updateFavorite(transitViewFavorite);
                }
            });
            task.execute(transitViewFavorite);
        }
    }
}
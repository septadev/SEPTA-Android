package org.septa.android.app.favorites;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.nextarrive.NextToArriveResultsActivity;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Favorite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by jkampf on 9/5/17.
 */
public class FavoritesFragment extends Fragment implements Runnable, FavoriteItemAdapter.FavoriteItemListener {
    private static final String TAG = FavoritesFragment.class.getSimpleName();

    // this version is the most recent to require a force delete of user favorites
    private static final int FAVORITES_LAST_UPDATED_VERSION = 268;

    private static final String KEY_TITLE = "KEY_TITLE";
    private static final int REFRESH_DELAY_SECONDS = 30;

    // favorites management
    private RecyclerView favoritesListView;
    private List<FavoriteState> favoriteStateList;
    private Map<String, Favorite> favoritesMap;
    private FavoritesFragmentListener mListener;
    private FavoriteItemAdapter favoriteItemAdapter;

    int initialCount;
    Handler refreshHandler = null;
    View fragmentView;
    String alertMessage;
    boolean firstPass = true;

    ViewGroup mContainer;

    public static FavoritesFragment newInstance() {
        FavoritesFragment instance = new FavoritesFragment();
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        favoriteStateList = SeptaServiceFactory.getFavoritesService().getFavoriteStates(getContext());
        favoritesMap = SeptaServiceFactory.getFavoritesService().getFavorites(getContext());
        alertMessage = evaluateAndRemoveFavorites(favoritesMap);

        mContainer = container;

        initialCount = favoritesMap.size();
        if (favoritesMap.size() == 0) {
            return onCreateViewNoFavorites(inflater, container);
        } else {
            return onCreateViewFavorites(inflater, container);
        }
    }

    private String evaluateAndRemoveFavorites(Map<String, Favorite> favoritesMap) {
        // In Version 268 we fixed some issues with NHSL and Subway.  However users could have created
        // faulty favorites with the version before 268.  We added a created with version number to
        // the favorite record defaulting to 0.  We compare that value to the value 268.  If the
        // favorite is NHSL or Subway and created in version is older than 268 we delete the
        // favorite and display a message.

        // If need to force remove favorites in new version then change FAVORITES_LAST_UPDATED_VERSION

        Map<String, Favorite> loopMap = new HashMap<>();
        loopMap.putAll(favoritesMap);

        List<Favorite> toDelete = new LinkedList<>();

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

    private View onCreateViewFavorites(LayoutInflater inflater, final ViewGroup container) {
        setHasOptionsMenu(true);
        fragmentView = inflater.inflate(R.layout.fragment_favorites, container, false);
        favoritesListView = (RecyclerView) fragmentView.findViewById(R.id.favorites_list);

        // make new favorite text clickable
        LinearLayout addNewFavorite = (LinearLayout) fragmentView.findViewById(R.id.favorites_add_new);
        addNewFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.addNewFavorite();
            }
        });

        // initialize favoriteStateList with favorites collapsed by default
        if (favoriteStateList.size() != favoritesMap.size()) {
            SeptaServiceFactory.getFavoritesService().deleteAllFavoriteStates(getContext());
            Log.d(TAG, "Reinitializing favorite states now...");

            for (Map.Entry<String, Favorite> entry : favoritesMap.entrySet()) {
                FavoriteState favoriteState = new FavoriteState(entry.getKey());
                favoriteStateList.add(favoriteState);
            }

            SeptaServiceFactory.getFavoritesService().setFavoriteStates(getContext(), favoriteStateList);
        }

        favoritesListView.setVerticalScrollBarEnabled(true);
        setupListRecyclerView();

        // TODO: remove this
        Log.e(TAG, favoriteStateList.toString());

        return fragmentView;
    }

    @Override
    public void run() {
        favoriteItemAdapter.refreshFavorites(SeptaServiceFactory.getFavoritesService().getFavoriteStates(getContext()));

        refreshHandler.postDelayed(this, REFRESH_DELAY_SECONDS * 1000);
    }

    @Override
    public void showSnackbarNoConnection() {
        // this snackbar will persist until a connection is reestablished and favorites are refreshed
        Snackbar snackbar = Snackbar.make(fragmentView, R.string.realtime_failure_message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.snackbar_no_connection_link_text, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.gotoSchedules();
            }
        });

        View snackbarView = snackbar.getView();
        android.widget.TextView tv = (android.widget.TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        tv.setMaxLines(10);
        snackbar.show();
    }

    @Override
    public void autoDismissSnackbar() {
        final Snackbar snackbar = Snackbar.make(fragmentView, R.string.empty_string, Snackbar.LENGTH_SHORT);
        snackbar.show();
        snackbar.dismiss();
    }

    @Override
    public void goToSchedulesForTarget(Favorite favorite) {
        mListener.goToSchedulesForTarget(favorite.getStart(), favorite.getDestination(), favorite.getTransitType(), favorite.getRouteDirectionModel());
    }

    @Override
    public void goToNextToArrive(Favorite favorite) {
        if (getActivity() == null) {
            return;
        }
        Intent intent = new Intent(getActivity(), NextToArriveResultsActivity.class);
        intent.putExtra(Constants.STARTING_STATION, favorite.getStart());
        intent.putExtra(Constants.DESTINATION_STATION, favorite.getDestination());
        intent.putExtra(Constants.TRANSIT_TYPE, favorite.getTransitType());
        intent.putExtra(Constants.ROUTE_DIRECTION_MODEL, favorite.getRouteDirectionModel());
        intent.putExtra(Constants.EDIT_FAVORITES_FLAG, Boolean.TRUE);

        getActivity().startActivityForResult(intent, Constants.NTA_REQUEST);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshHandler = new Handler();
        if (!firstPass) {
            favoritesMap = SeptaServiceFactory.getFavoritesService().getFavorites(getContext());

            if (initialCount != favoritesMap.size()) {
                mListener.refreshFavoritesInstance();
                return;
            }

//            favoriteItemAdapter.refreshFavoriteTitles();

            run();
        } else {
            firstPass = false;
        }

        if (alertMessage != null) {

            Activity activity = getActivity();
            if (activity != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage(alertMessage);
                builder.setNeutralButton(R.string.button_ok, new DialogInterface.OnClickListener() {
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

    private View onCreateViewNoFavorites(LayoutInflater inflater, @Nullable ViewGroup container) {
        setHasOptionsMenu(false);
        View fragmentView = inflater.inflate(R.layout.no_favorites, container, false);

        View button = fragmentView.findViewById(R.id.add_favorite_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.addNewFavorite();
            }
        });

        return fragmentView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.my_favorites_menu, menu);

        menu.findItem(R.id.edit_favorites).setTitle(R.string.favorites_menu_item_edit);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.edit_favorites) {

            // do not open edit mode if user has no favorites
            if (favoritesMap == null || favoritesMap.isEmpty()) {
                Snackbar snackbar = Snackbar.make(fragmentView, R.string.no_favorites_to_edit, Snackbar.LENGTH_SHORT);
                snackbar.show();

            } else {
                mListener.toggleEditFavoritesMode(false);
            }
        }

        return true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof FavoritesFragmentListener)) {
            throw new RuntimeException("Context must implement FavoritesFragmentListener");
        } else {
            mListener = (FavoritesFragmentListener) context;
        }

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

    public List<Favorite> openEditMode() {
        // TODO: enable swipe to delete

        return getFavoritesInOrder();
    }

    public List<Favorite> getFavoritesInOrder() {
        List<Favorite> favoriteList = new ArrayList<>();
        for (FavoriteState favoriteState : favoriteStateList) {
            Favorite currentFavorite = SeptaServiceFactory.getFavoritesService().getFavoriteByKey(getContext(), favoriteState.getFavoriteKey());
            favoriteList.add(currentFavorite);
        }
        return favoriteList;
    }

    private void setupListRecyclerView() {
        favoritesListView.setLayoutManager(new LinearLayoutManager(getContext()));
        favoriteItemAdapter = new FavoriteItemAdapter(getContext(), favoriteStateList, R.layout.item_favorite, this);
        favoritesListView.setAdapter(favoriteItemAdapter);

        favoriteItemAdapter.updateList(favoriteStateList);
    }

    public interface FavoritesFragmentListener {
        void refreshFavoritesInstance();

        void addNewFavorite();

        void gotoSchedules();

        void goToSchedulesForTarget(StopModel start, StopModel destination, TransitType transitType, RouteDirectionModel routeDirectionModel);

        void toggleEditFavoritesMode(boolean isInEditMode);
    }

}



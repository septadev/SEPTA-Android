package org.septa.android.app.favorites;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.nextarrive.NextToArriveResultsActivity;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalFavorite;
import org.septa.android.app.services.apiinterfaces.model.TransitViewFavorite;
import org.septa.android.app.support.AnalyticsManager;
import org.septa.android.app.support.SwipeController;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FavoritesFragment extends Fragment implements Runnable, FavoriteItemAdapter.FavoriteItemListener, SwipeController.SwipeControllerListener {
    private static final String TAG = FavoritesFragment.class.getSimpleName();

    // this version is the most recent to require a force delete of user favorites
    private static final int FAVORITES_LAST_UPDATED_VERSION = 268;

    private static final String KEY_TITLE = "KEY_TITLE";
    private static final int REFRESH_DELAY_SECONDS = 30;

    // favorites management
    private RecyclerView favoritesListView;
    private List<FavoriteState> favoriteStateList;
    private Map<String, NextArrivalFavorite> ntaFavoritesMap;
    private Map<String, TransitViewFavorite> transitViewFavoritesMap;
    private FavoritesFragmentListener mListener;
    private FavoriteItemAdapter favoriteItemAdapter;
    private FavoritesSwipeRefreshLayout mRefreshLayout;
    private ItemTouchHelper itemTouchHelper;

    private int initialCount;
    private Handler refreshHandler = null;
    private View fragmentView;
    private String alertMessage;
    private boolean firstPass = true;

    // snackbar no connection message
    private View noFavoritesMessage;
    private boolean isSnackbarShowing = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        SeptaServiceFactory.getFavoritesService().resyncFavoritesMap(getContext());

        favoriteStateList = SeptaServiceFactory.getFavoritesService().getFavoriteStates(getContext());
        ntaFavoritesMap = SeptaServiceFactory.getFavoritesService().getNTAFavorites(getContext());
        transitViewFavoritesMap = SeptaServiceFactory.getFavoritesService().getTransitViewFavorites(getContext());
        alertMessage = evaluateAndRemoveFavorites(ntaFavoritesMap);

        initialCount = favoriteStateList.size();

        fragmentView = inflater.inflate(R.layout.fragment_favorites, container, false);
        noFavoritesMessage = fragmentView.findViewById(R.id.favorites_none_message);
        mRefreshLayout = fragmentView.findViewById(R.id.favorites_swipe_refresh_layout);
        favoritesListView = fragmentView.findViewById(R.id.favorites_list);

        if (favoriteStateList.isEmpty()) {
            // user has no favorites
            showNoFavoritesMessage();
        } else {
            // user has at least one favorite
            showFavorites();
        }
        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshHandler = new Handler();
        if (!firstPass) {
            ntaFavoritesMap = SeptaServiceFactory.getFavoritesService().getNTAFavorites(getContext());
            transitViewFavoritesMap = SeptaServiceFactory.getFavoritesService().getTransitViewFavorites(getContext());
            if (initialCount != favoriteStateList.size()) {
                mListener.refreshFavoritesInstance();
                return;
            }

            if (!favoriteStateList.isEmpty()) {
                run();
            }

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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_TITLE, getActivity().getTitle().toString());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            String title = savedInstanceState.getString(KEY_TITLE);
            if (title != null && getActivity() != null) {
                getActivity().setTitle(title);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.my_favorites_menu, menu);

        MenuItem menuItemAdd = menu.findItem(R.id.add_favorite);
        MenuItem menuItemEdit = menu.findItem(R.id.edit_favorites);

        // show 'add' action in toolbar
        if (menuItemAdd != null) {
            menuItemAdd.setVisible(true);
        }
        // change title to edit
        if (menuItemEdit != null) {
            menuItemEdit.setTitle(R.string.favorites_menu_item_edit);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_favorite) {
            mListener.addNewFavorite();

        } else if (item.getItemId() == R.id.edit_favorites) {
            // do not open edit mode if user has no favorites
            if (favoriteStateList.isEmpty()) {
                Snackbar snackbar = Snackbar.make(fragmentView, R.string.no_favorites_to_edit, Snackbar.LENGTH_SHORT);
                snackbar.show();

            } else {
                AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_EDIT_FAVORITES_BUTTON, AnalyticsManager.CUSTOM_EVENT_ID_FAVORITES_MANAGEMENT, null);
                mListener.toggleEditFavoritesMode(false);
            }
        }

        return true;
    }

    @Override
    public void run() {
        refreshFavorites();
    }

    @Override
    public void showSnackbarNoConnection() {
        // this snackbar will persist until a connection is reestablished and favorites are refreshed
        Snackbar snackbar = Snackbar.make(fragmentView, R.string.realtime_failure_message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.snackbar_no_connection_link_text, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnalyticsManager.logContentViewEvent(TAG, AnalyticsManager.CONTENT_VIEW_EVENT_SCHEDULE_FROM_FAVORITES, AnalyticsManager.CONTENT_ID_SCHEDULE, null);
                mListener.gotoSchedules();
            }
        });
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onShown(Snackbar sb) {
                super.onShown(sb);
                isSnackbarShowing = true;
            }

            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                isSnackbarShowing = false;
            }
        });

        View snackbarView = snackbar.getView();
        android.widget.TextView tv = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        tv.setMaxLines(10);
        snackbar.show();
    }

    @Override
    public void autoDismissSnackbar() {
        if (isSnackbarShowing) {
            final Snackbar snackbar = Snackbar.make(fragmentView, R.string.empty_string, Snackbar.LENGTH_SHORT);
            snackbar.show();
            snackbar.dismiss();
        }
    }

    @Override
    public void goToSchedulesForTarget(NextArrivalFavorite nextArrivalFavorite) {
        mListener.goToSchedulesForTarget(nextArrivalFavorite.getStart(), nextArrivalFavorite.getDestination(), nextArrivalFavorite.getTransitType(), nextArrivalFavorite.getRouteDirectionModel());
        AnalyticsManager.logContentViewEvent(TAG, AnalyticsManager.CONTENT_VIEW_EVENT_SCHEDULE_FROM_FAVORITES, AnalyticsManager.CONTENT_ID_SCHEDULE, null);
    }

    @Override
    public void goToNextToArrive(NextArrivalFavorite nextArrivalFavorite) {
        if (getActivity() == null) {
            return;
        }
        Intent intent = new Intent(getActivity(), NextToArriveResultsActivity.class);
        intent.putExtra(Constants.STARTING_STATION, nextArrivalFavorite.getStart());
        intent.putExtra(Constants.DESTINATION_STATION, nextArrivalFavorite.getDestination());
        intent.putExtra(Constants.TRANSIT_TYPE, nextArrivalFavorite.getTransitType());
        intent.putExtra(Constants.ROUTE_DIRECTION_MODEL, nextArrivalFavorite.getRouteDirectionModel());
        intent.putExtra(Constants.EDIT_FAVORITES_FLAG, Boolean.TRUE);

        AnalyticsManager.logContentViewEvent(TAG, AnalyticsManager.CONTENT_VIEW_EVENT_NTA_FROM_FAVORITES, AnalyticsManager.CONTENT_ID_NEXT_TO_ARRIVE, null);

        getActivity().startActivityForResult(intent, Constants.NTA_REQUEST);
    }

    @Override
    public void goToTransitView(TransitViewFavorite transitViewFavorite) {
        mListener.goToTransitViewResults(transitViewFavorite.getFirstRoute(), transitViewFavorite.getSecondRoute(), transitViewFavorite.getThirdRoute());
    }

    @Override
    public void promptToDeleteFavorite(final int favoriteIndex) {
        if (favoriteIndex >= 0 && favoriteIndex < favoriteStateList.size()) {
            final String favoriteKey = favoriteStateList.get(favoriteIndex).getFavoriteKey();

            new AlertDialog.Builder(getContext()).setCancelable(true).setTitle(R.string.delete_fav_modal_title)
                    .setMessage(R.string.delete_fav_modal_text)

                    // confirm to delete
                    .setPositiveButton(R.string.delete_fav_pos_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                            DeleteFavoritesAsyncTask task = new DeleteFavoritesAsyncTask(getContext(), new Runnable() {
                                @Override
                                public void run() {
                                    // on unsuccessful deletion
                                    dialog.dismiss();
                                    revertSwipe(favoriteIndex);
                                    Log.e(TAG, "Favorite with key " + favoriteKey + " could not be deleted at this time");
                                }
                            }, new Runnable() {
                                @Override
                                public void run() {
                                    deleteFavorite(favoriteIndex);
                                }
                            });

                            AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_DELETE_FAVORITE, AnalyticsManager.CUSTOM_EVENT_ID_FAVORITES_MANAGEMENT, null);

                            task.execute(favoriteKey);
                        }
                    })

                    // cancel deletion
                    .setNegativeButton(R.string.delete_fav_neg_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            revertSwipe(favoriteIndex);
                        }
                    })
                    .create().show();
        }
    }

    @Override
    public void revertSwipe(int index) {
        itemTouchHelper.attachToRecyclerView(null);
        itemTouchHelper.attachToRecyclerView(favoritesListView);

        // revert UI for swiped row
        favoriteItemAdapter.notifyItemChanged(index);
    }

    private void showFavorites() {
        setHasOptionsMenu(true);

        // hide no favorite message and show list of favorites
        noFavoritesMessage.setVisibility(View.GONE);
        mRefreshLayout.setVisibility(View.VISIBLE);

        // swipe down to refresh favorites
        mRefreshLayout.setScrollingView(favoritesListView);
        mRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.setRefreshing(false);
                        refreshFavorites();
                    }
                }, 2000);
            }
        });

        // enabled swipe to delete
        final SwipeController swipeController = new SwipeController(getContext(), FavoritesFragment.this);
        itemTouchHelper = new ItemTouchHelper(swipeController);
        itemTouchHelper.attachToRecyclerView(favoritesListView);
        favoritesListView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });

        setupListRecyclerView();
    }

    private void showNoFavoritesMessage() {
        setHasOptionsMenu(false);

        // show no favorite message and hide list of favorites
        mRefreshLayout.setVisibility(View.GONE);
        noFavoritesMessage.setVisibility(View.VISIBLE);

        // add button clickable
        View addButton = fragmentView.findViewById(R.id.add_favorite_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.addNewFavorite();
            }
        });
    }

    private String evaluateAndRemoveFavorites(Map<String, NextArrivalFavorite> favoritesMap) {
        // In Version 268 we fixed some issues with NHSL and Subway.  However users could have created
        // faulty favorites with the version before 268.  We added a created with version number to
        // the favorite record defaulting to 0.  We compare that value to the value 268.  If the
        // favorite is NHSL or Subway and created in version is older than 268 we delete the
        // favorite and display a message.

        // If need to force remove favorites in new version then change FAVORITES_LAST_UPDATED_VERSION

        Map<String, NextArrivalFavorite> loopMap = new HashMap<>();
        loopMap.putAll(favoritesMap);

        List<NextArrivalFavorite> toDelete = new LinkedList<>();

        String msg = null;
        for (Map.Entry<String, NextArrivalFavorite> entry : loopMap.entrySet()) {

            NextArrivalFavorite nextArrivalFavorite = entry.getValue();
            if (nextArrivalFavorite.getCreatedWithVersion() < FAVORITES_LAST_UPDATED_VERSION &&
                    ((nextArrivalFavorite.getTransitType() == TransitType.NHSL) ||
                            nextArrivalFavorite.getTransitType() == TransitType.SUBWAY)) {
                ntaFavoritesMap.remove(entry.getKey());
                toDelete.add(nextArrivalFavorite);
                msg = getString(R.string.force_delete_nhsl_subway_favorite);
            }
        }

        Activity activity = getActivity();
        if (activity == null) {
            return null;
        }

        for (NextArrivalFavorite nextArrivalFavorite : toDelete) {
            SeptaServiceFactory.getFavoritesService().deleteFavorite(activity, nextArrivalFavorite.getKey());
        }

        return msg;
    }

    private void setupListRecyclerView() {
        favoritesListView.setLayoutManager(new LinearLayoutManager(getContext()));
        favoriteItemAdapter = new FavoriteItemAdapter(getContext(), favoriteStateList, this);
        favoritesListView.setAdapter(favoriteItemAdapter);
        favoriteItemAdapter.updateList(favoriteStateList);
    }

    private void refreshFavorites() {
        if (getContext() != null) {
            favoriteItemAdapter.refreshFavorites(SeptaServiceFactory.getFavoritesService().getFavoriteStates(getContext()));

            // postpone next refresh to after 30 secs
            refreshHandler.postDelayed(this, REFRESH_DELAY_SECONDS * 1000);
        } else {
            Log.d(TAG, "Could not refresh favorites because context was null");
        }
    }

    private void deleteFavorite(int favoriteIndex) {
        // on successful deletion
        favoriteStateList.remove(favoriteIndex);

        favoriteItemAdapter.notifyItemRemoved(favoriteIndex);
        favoriteItemAdapter.notifyDataSetChanged();

        // reattach recyclerview so that deleting last row hides red background
        itemTouchHelper.attachToRecyclerView(null);
        itemTouchHelper.attachToRecyclerView(favoritesListView);

        // show no favorites message if that was the last favorite
        if (favoriteStateList.isEmpty()) {
            showNoFavoritesMessage();
        }
    }

    public interface FavoritesFragmentListener {
        void refreshFavoritesInstance();

        void addNewFavorite();

        void gotoSchedules();

        void goToSchedulesForTarget(StopModel start, StopModel destination, TransitType transitType, RouteDirectionModel routeDirectionModel);

        void goToTransitViewResults(RouteDirectionModel firstRoute, RouteDirectionModel secondRoute, RouteDirectionModel thirdRoute);

        void toggleEditFavoritesMode(boolean isInEditMode);
    }

}



package org.septa.android.app.favorites.edit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.ImageView;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.draggable.DragItem;
import org.septa.android.app.draggable.DragListView;
import org.septa.android.app.favorites.DeleteFavoritesAsyncTask;
import org.septa.android.app.favorites.FavoriteState;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.support.AnalyticsManager;
import org.septa.android.app.support.SwipeController;

import java.util.List;

public class ManageFavoritesFragment extends Fragment implements DraggableFavoriteItemAdapter.DraggableFavoriteItemListener, SwipeController.SwipeControllerListener {

    private static final String TAG = ManageFavoritesFragment.class.getSimpleName();

    // favorites management
    private DragListView favoritesListView;
    private List<FavoriteState> favoriteStateList;
    private ManageFavoritesFragmentListener mListener;
    private DraggableFavoriteItemAdapter favoriteItemAdapter;
    private ItemTouchHelper itemTouchHelper;

    int initialCount;
    View fragmentView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        favoriteStateList = SeptaServiceFactory.getFavoritesService().getFavoriteStates(getContext());

        initialCount = favoriteStateList.size();

        setHasOptionsMenu(true);
        fragmentView = inflater.inflate(R.layout.fragment_favorites_manage, container, false);
        favoritesListView = fragmentView.findViewById(R.id.favorites_list_draggable);

        // enabled swipe to delete
        final SwipeController swipeController = new SwipeController(getContext(),ManageFavoritesFragment.this);
        itemTouchHelper = new ItemTouchHelper(swipeController);
        itemTouchHelper.attachToRecyclerView(favoritesListView.getRecyclerView());
        favoritesListView.getRecyclerView().addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });

        // enable drag to reorder
        favoritesListView.setDragListListener(new DragListView.DragListListenerAdapter() {
            @Override
            public void onItemDragStarted(int position) {
                // remove delete buttons when starting drag
                swipeController.removeButtons();

                super.onItemDragStarted(position);
            }

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                // reorder favorite when drag item dropped
                if (fromPosition != toPosition) {
                    reorderFavorite(fromPosition, toPosition);
                }
            }
        });

        setupListRecyclerView();

        return fragmentView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof ManageFavoritesFragmentListener)) {
            throw new RuntimeException("Context must implement ManageFavoritesFragmentListener");
        } else {
            mListener = (ManageFavoritesFragmentListener) context;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.my_favorites_menu, menu);

        MenuItem menuItemAdd = menu.findItem(R.id.add_favorite);
        MenuItem menuItemEdit = menu.findItem(R.id.edit_favorites);

        // hide 'add' action in toolbar
        if (menuItemAdd != null) {
            menuItemAdd.setVisible(false);
        }
        // change title to done
        if (menuItemEdit != null) {
            menuItemEdit.setTitle(R.string.favorites_menu_item_done);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_favorite) {
            Log.e(TAG, "User is able to access 'Add' favorite button from ManageFavoritesFragment");
            mListener.addNewFavorite();
        } else if (item.getItemId() == R.id.edit_favorites) {
            // close edit mode and save new order
            mListener.toggleEditFavoritesMode(true);
        }

        return true;
    }

    @Override
    public void promptToDeleteFavorite(final int favoriteIndex) {
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
                                Log.e(TAG, "NextArrivalFavorite with key " + favoriteKey + " could not be deleted at this time");
                            }
                        }, new Runnable() {
                            @Override
                            public void run() {
                                // on successful deletion
                                favoriteStateList.remove(favoriteIndex);
                                favoriteItemAdapter.notifyItemRemoved(favoriteIndex);
                                favoriteItemAdapter.notifyDataSetChanged();

                                // reattach recyclerview so that deleting last row hides red background
                                itemTouchHelper.attachToRecyclerView(null);
                                itemTouchHelper.attachToRecyclerView(favoritesListView.getRecyclerView());

                                // close edit mode if no favorites left
                                if (favoriteStateList.isEmpty()) {
                                    mListener.toggleEditFavoritesMode(true);
                                }
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
                }).create().show();
    }

    @Override
    public void revertSwipe(int index) {
        itemTouchHelper.attachToRecyclerView(null);
        itemTouchHelper.attachToRecyclerView(favoritesListView.getRecyclerView());

        // revert UI for swiped row
        favoriteItemAdapter.notifyItemChanged(index);
    }

    private void setupListRecyclerView() {
        favoritesListView.setLayoutManager(new LinearLayoutManager(getContext()));

        favoriteItemAdapter = new DraggableFavoriteItemAdapter(getContext(), favoriteStateList, R.layout.item_favorite_draggable, R.id.favorite_item_drag_handle, false, this);
        favoritesListView.setAdapter(favoriteItemAdapter, true);
        favoritesListView.setCanDragHorizontally(false);
        favoritesListView.setCustomDragItem(new FavoriteDragItem(getContext(), R.layout.item_favorite_draggable));

        favoriteItemAdapter.updateList(favoriteStateList);
    }

    private void reorderFavorite(int fromPosition, int toPosition) {
        SeptaServiceFactory.getFavoritesService().moveFavoriteStateToIndex(getContext(), fromPosition, toPosition);
        favoriteItemAdapter.notifyItemMoved(fromPosition, toPosition);

    }

    private static class FavoriteDragItem extends DragItem {
        FavoriteDragItem(Context context, int layoutId) {
            super(context, layoutId);
        }

        @Override
        public void onBindDragView(View clickedView, View dragView) {
            // set favorite name on dragging view
            CharSequence text = ((TextView) clickedView.findViewById(R.id.favorite_title_text)).getText();
            ((TextView) dragView.findViewById(R.id.favorite_title_text)).setText(text);

            // set transit type icon
            Drawable transitTypeIcon = ((ImageView) clickedView.findViewById(R.id.favorite_title_transit_type_icon)).getDrawable();
            ((ImageView) dragView.findViewById(R.id.favorite_title_transit_type_icon)).setImageDrawable(transitTypeIcon);

            // set dragging view background
            dragView.findViewById(R.id.item_favorite_row_draggable).setBackgroundColor(Color.WHITE);
        }
    }

    public interface ManageFavoritesFragmentListener {
        void toggleEditFavoritesMode(boolean isInEditMode);
        void addNewFavorite();
    }

}

package org.septa.android.app.favorites;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
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

import org.septa.android.app.R;
import org.septa.android.app.draggable.DragItem;
import org.septa.android.app.draggable.DragListView;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Favorite;

import java.util.List;

public class ManageFavoritesFragment extends Fragment implements DraggableFavoriteItemAdapter.DraggableFavoriteItemListener, SwipeController.SwipeControllerListener {

    private static final String TAG = ManageFavoritesFragment.class.getSimpleName();
    private static final String KEY_FAVORITE = "KEY_FAVORITE";

    // favorites management
    private DragListView favoritesListView;
    private List<Favorite> favoriteList;
    private ManageFavoritesFragmentListener mListener;
    private DraggableFavoriteItemAdapter favoriteItemAdapter;
    private ItemTouchHelper itemTouchHelper;

    int initialCount;
    View fragmentView;

    public static ManageFavoritesFragment newInstance(List<Favorite> favoriteList) {
        ManageFavoritesFragment instance = new ManageFavoritesFragment();
        instance.favoriteList = favoriteList;
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        initialCount = favoriteList.size();

        setHasOptionsMenu(true);
        fragmentView = inflater.inflate(R.layout.fragment_manage_favorites, container, false);
        favoritesListView = (DragListView) fragmentView.findViewById(R.id.favorites_list_draggable);

//        favoritesListView.getRecyclerView().setVerticalScrollBarEnabled(true);

        // enable drag to reorder
        favoritesListView.setDragListListener(new DragListView.DragListListenerAdapter() {
            @Override
            public void onItemDragStarted(int position) {
                // Toast.makeText(mDragListView.getContext(), "Start - position: " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                if (fromPosition != toPosition) {
                    // Toast.makeText(mDragListView.getContext(), "End - position: " + toPosition, Toast.LENGTH_SHORT).show();
                }
            }
        });


//        ItemTouchHelper.SimpleCallback swipeToDeleteCallback = new ItemTouchHelper.SimpleCallback(0, LEFT) {
//            @Override
//            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
//                return false;
//            }
//
//            @Override
//            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
//                if (direction == LEFT) {
//                    deleteFavorite(viewHolder.getAdapterPosition());
//                }
//            }
//        };
//        new ItemTouchHelper(swipeToDeleteCallback).attachToRecyclerView(favoritesListView.getRecyclerView());

        final SwipeController swipeController = new SwipeController(getContext(),ManageFavoritesFragment.this);
        itemTouchHelper = new ItemTouchHelper(swipeController);
        itemTouchHelper.attachToRecyclerView(favoritesListView.getRecyclerView());
        favoritesListView.getRecyclerView().addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });

        // enable swipe to delete
//        favoritesListView.setSwipeListener(new ListSwipeHelper.OnSwipeListenerAdapter() {
//            @Override
//            public void onItemSwipeStarted(ListSwipeItem item) {
//
//                Favorite adapterItem = (Favorite) item.getTag();
//                int pos = favoriteItemAdapter.getPositionForItem(adapterItem);
//
//                // TODO: remove
//                Log.e(TAG, "Favorite at position " + pos + " swiped: " + adapterItem.toString());
//            }
//
//            @Override
//            public void onItemSwipeEnded(ListSwipeItem item, ListSwipeItem.SwipeDirection swipedDirection) {
//                // swipe left to delete
//                if (swipedDirection == ListSwipeItem.SwipeDirection.LEFT) {
//                    Favorite adapterItem = (Favorite) item.getTag();
//                    int pos = favoriteItemAdapter.getPositionForItem(adapterItem);
//                    deleteFavorite(pos);
//                }
//            }
//        });

        setupListRecyclerView();

        // TODO: remove this
        Log.e(TAG, favoriteList.toString());

        return fragmentView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.my_favorites_menu, menu);

        menu.findItem(R.id.edit_favorites).setTitle(R.string.favorites_menu_item_done);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // close edit mode and save new order
        if (item.getItemId() == R.id.edit_favorites) {
            mListener.toggleEditFavoritesMode(true);
        }

        return true;
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
    public void deleteFavorite(final int favoriteIndex) {
        new AlertDialog.Builder(getContext()).setCancelable(true).setTitle(R.string.delete_fav_modal_title)
                .setMessage(R.string.delete_fav_modal_text)

                // confirm to delete
                .setPositiveButton(R.string.delete_fav_pos_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SeptaServiceFactory.getFavoritesService().deleteFavorite(getContext(), favoriteList.get(favoriteIndex).getKey());
                        favoriteList.remove(favoriteIndex);
                        favoriteItemAdapter.deleteFavorite(favoriteIndex);
                    }
                })

                // cancel deletion
                .setNegativeButton(R.string.delete_fav_neg_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
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

    // TODO: when item dropped, reorder in list but DO NOT change expanded state
    private void setupListRecyclerView() {
        favoritesListView.setLayoutManager(new LinearLayoutManager(getContext()));

        favoriteItemAdapter = new DraggableFavoriteItemAdapter(getContext(), favoriteList, R.layout.item_favorite_draggable, R.id.favorite_item_drag_handle, true, this);
        favoritesListView.setAdapter(favoriteItemAdapter, true);
        favoritesListView.setCanDragHorizontally(false);
        favoritesListView.setCustomDragItem(new FavoriteDragItem(getContext(), R.layout.item_favorite_draggable));

        favoriteItemAdapter.updateList(favoriteList);
    }

    public void closeEditMode() {
        // TODO: save new order of favorites -- this may happen on drop
        // TODO: disable swipe to delete
    }

    private static class FavoriteDragItem extends DragItem {
        FavoriteDragItem(Context context, int layoutId) {
            super(context, layoutId);
        }

        @Override
        public void onBindDragView(View clickedView, View dragView) {
            // TODO: custom dragging view
//            CharSequence text = ((TextView) clickedView.findViewById(R.id.text)).getText();
//            ((TextView) dragView.findViewById(R.id.favorite_item_drag_handle)).setText(text);
//            dragView.findViewById(R.id.item_favorite_row_draggable).setBackground(dragView.getResources().getDrawable(R.drawable.full_page_gradient_background));
        }
    }

    public interface ManageFavoritesFragmentListener {
        void toggleEditFavoritesMode(boolean isInEditMode);
    }

}

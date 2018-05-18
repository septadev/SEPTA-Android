package org.septa.android.app.favorites;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.draggable.DragItem;
import org.septa.android.app.draggable.DragListView;
import org.septa.android.app.draggable.swipe.ListSwipeHelper;
import org.septa.android.app.draggable.swipe.ListSwipeItem;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Favorite;

import java.util.List;

public class ManageFavoritesFragment extends Fragment implements DraggableFavoriteItemAdapter.DraggableFavoriteItemListener {

    private static final String TAG = ManageFavoritesFragment.class.getSimpleName();
    private static final String KEY_FAVORITE = "KEY_FAVORITE";

    // favorites management
    private DragListView favoritesListView;
    private List<Favorite> favoriteList;
    private ManageFavoritesFragmentListener mListener;
    private DraggableFavoriteItemAdapter favoriteItemAdapter;

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

        // make favorites draggable
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


        // enable swipe to delete
        favoritesListView.setSwipeListener(new ListSwipeHelper.OnSwipeListenerAdapter() {
            @Override
            public void onItemSwipeStarted(ListSwipeItem item) {

            }

            @Override
            public void onItemSwipeEnded(ListSwipeItem item, ListSwipeItem.SwipeDirection swipedDirection) {
                // TODO: confirm to delete dialog pop up?

                // Swipe to delete on left
                if (swipedDirection == ListSwipeItem.SwipeDirection.LEFT) {
                    Favorite adapterItem = (Favorite) item.getTag();
                    int pos = favoritesListView.getAdapter().getPositionForItem(adapterItem);
                    favoritesListView.getAdapter().removeItem(pos);
                }
            }
        });

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
    public void deleteFavorite(int favoriteIndex) {
        SeptaServiceFactory.getFavoritesService().deleteFavorite(getContext(), favoriteList.get(favoriteIndex).getKey());
        favoriteList.remove(favoriteIndex);
        favoriteItemAdapter.deleteFavorite(favoriteIndex);
    }

    // TODO: when item dropped, reorder in list but DO NOT change expanded state
    private void setupListRecyclerView() {
        favoritesListView.setLayoutManager(new LinearLayoutManager(getContext()));

        favoriteItemAdapter = new DraggableFavoriteItemAdapter(getContext(), favoriteList, R.layout.item_favorite_draggable, R.id.favorite_item_drag_handle, true, this);
        favoritesListView.setAdapter(favoriteItemAdapter, true);
        favoritesListView.setCanDragHorizontally(false);
        favoritesListView.setCustomDragItem(new FavoriteDragItem(getContext(), R.layout.item_favorite));

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
            // TODO: what should happen in here???
            CharSequence text = ((TextView) clickedView.findViewById(R.id.text)).getText();
            ((TextView) dragView.findViewById(R.id.favorite_item_drag_handle)).setText(text);
            dragView.findViewById(R.id.item_favorite_row_draggable).setBackground(dragView.getResources().getDrawable(R.drawable.full_page_gradient_background));
        }
    }

    public interface ManageFavoritesFragmentListener {
        void toggleEditFavoritesMode(boolean isInEditMode);
    }

}

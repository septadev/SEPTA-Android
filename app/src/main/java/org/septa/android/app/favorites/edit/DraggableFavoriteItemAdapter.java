package org.septa.android.app.favorites.edit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.draggable.DragItemAdapter;
import org.septa.android.app.draggable.swipe.ListSwipeItem;
import org.septa.android.app.favorites.FavoriteState;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Favorite;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalFavorite;
import org.septa.android.app.services.apiinterfaces.model.TransitViewFavorite;

import java.util.List;

public class DraggableFavoriteItemAdapter extends DragItemAdapter<FavoriteState, DraggableFavoriteItemAdapter.DraggableFavoriteViewHolder> {

    private static final String TAG = DraggableFavoriteItemAdapter.class.getSimpleName();

    private Context context;
    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;
    private DraggableFavoriteItemListener mListener;

    DraggableFavoriteItemAdapter(Context context, List<FavoriteState> list, int layoutId, int grabHandleId, boolean dragOnLongPress, DraggableFavoriteItemListener draggableFavoriteItemListener) {
        this.context = context;
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        mListener = draggableFavoriteItemListener;
        setItemList(list);
    }

    @NonNull
    @Override
    public DraggableFavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new DraggableFavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final DraggableFavoriteViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);

        String favoriteKey = mItemList.get(position).getFavoriteKey();
        Favorite favorite = SeptaServiceFactory.getFavoritesService().getFavoriteByKey(context, favoriteKey);

        if (favorite == null) {
            Log.e(TAG, "Favorite not found");
            // TODO: hide row

        } else {
            // favorite name
            holder.favoriteName.setText(favorite.getName());

            // delete button clickable
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.promptToDeleteFavorite(holder.getAdapterPosition());
                }
            });

            // tag for draggability
            holder.itemView.setTag(mItemList.get(position));

            if (favorite instanceof NextArrivalFavorite) {
                // transit type icon on left
                holder.transitTypeIcon.setImageDrawable(ContextCompat.getDrawable(context, ((NextArrivalFavorite) favorite).getTransitType().getTabActiveImageResource()));

            } else if (favorite instanceof TransitViewFavorite) {
                // transitview icon on left
                holder.transitTypeIcon.setImageResource(R.drawable.ic_transitview_circle);

            } else {
                Log.e(TAG, "Invalid Favorite class Type in onBindViewHolder");
            }
        }
    }

    @Override
    public long getUniqueItemId(int position) {
        return (long) mItemList.get(position).hashCode();
    }

    public void updateList(List<FavoriteState> favoriteList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new FavoriteDiffCallback(this.mItemList, favoriteList));
        this.mItemList = favoriteList;

        // make changes to view
        diffResult.dispatchUpdatesTo(this);
    }

    class DraggableFavoriteViewHolder extends DragItemAdapter.ViewHolder {
        ListSwipeItem favoriteRow;
        ImageView transitTypeIcon;
        TextView favoriteName;
        ImageButton dragHandle, deleteButton;

        DraggableFavoriteViewHolder(final View itemView) {
            super(itemView, mGrabHandleId, mDragOnLongPress);
            favoriteRow = itemView.findViewById(R.id.item_favorite_row_draggable);
            transitTypeIcon = itemView.findViewById(R.id.favorite_title_transit_type_icon);
            favoriteName = itemView.findViewById(R.id.favorite_title_text);
            dragHandle = itemView.findViewById(R.id.favorite_item_drag_handle);
            deleteButton = itemView.findViewById(R.id.favorite_item_delete_button);
        }
    }

    public interface DraggableFavoriteItemListener {
        void promptToDeleteFavorite(int favoriteIndex);
    }
}
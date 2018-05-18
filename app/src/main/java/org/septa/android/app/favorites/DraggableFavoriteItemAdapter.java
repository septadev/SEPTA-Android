package org.septa.android.app.favorites;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.septa.android.app.R;
import org.septa.android.app.draggable.DragItemAdapter;
import org.septa.android.app.services.apiinterfaces.model.Favorite;

import java.util.List;

public class DraggableFavoriteItemAdapter extends DragItemAdapter<Favorite, DraggableFavoriteItemAdapter.DraggableFavoriteViewHolder> {

    private static final String TAG = DraggableFavoriteItemAdapter.class.getSimpleName();

    private Context context;
    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;
    private DraggableFavoriteItemListener mListener;

    DraggableFavoriteItemAdapter(Context context, List<Favorite> list, int layoutId, int grabHandleId, boolean dragOnLongPress, DraggableFavoriteItemListener draggableFavoriteItemListener) {
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

        final Favorite favorite = mItemList.get(position);

        // favorite name
        holder.favoriteName.setText(favorite.getName());

        // transit type icon on left
        Drawable drawables[] = holder.favoriteName.getCompoundDrawables();
        holder.favoriteName.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context,
                favorite.getTransitType().getTabActiveImageResource()),
                drawables[1], drawables[2], drawables[3]);

        // delete button fires confirmation message
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context).setCancelable(true).setTitle(R.string.delete_fav_modal_title)
                        .setMessage(R.string.delete_fav_modal_text)
                        .setPositiveButton(R.string.delete_fav_pos_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mListener.deleteFavorite(holder.getAdapterPosition());
                            }
                        })
                        .setNegativeButton(R.string.delete_fav_neg_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });

        // tag for draggability
        holder.itemView.setTag(mItemList.get(position));
        // TODO: drag handle stuff??
    }

    @Override
    public long getUniqueItemId(int position) {
        return (long) mItemList.get(position).hashCode();
    }

    public void updateList(List<Favorite> favoriteList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new FavoriteDiffCallback(this.mItemList, favoriteList));
        this.mItemList = favoriteList;

        // make changes to view
        diffResult.dispatchUpdatesTo(this);
    }

    public void deleteFavorite(int favoriteIndex) {
        notifyItemRemoved(favoriteIndex);
    }

    public class DraggableFavoriteViewHolder extends DragItemAdapter.ViewHolder {
        LinearLayout favoriteRow;
        TextView favoriteName;
        ImageButton dragHandle, deleteButton;

        DraggableFavoriteViewHolder(final View itemView) {
            super(itemView, mGrabHandleId, mDragOnLongPress);
            favoriteRow = (LinearLayout) itemView.findViewById(R.id.item_favorite_row_draggable);
            favoriteName = (TextView) itemView.findViewById(R.id.favorite_title_text);
            dragHandle = (ImageButton) itemView.findViewById(R.id.favorite_item_drag_handle);
            deleteButton = (ImageButton) itemView.findViewById(R.id.favorite_item_delete_button);

        }

        @Override
        public void onItemClicked(View view) {
            Toast.makeText(view.getContext(), "Item clicked", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onItemLongClicked(View view) {
            Toast.makeText(view.getContext(), "Item long clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    public interface DraggableFavoriteItemListener {
        void deleteFavorite(int favoriteIndex);
    }
}
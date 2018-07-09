package org.septa.android.app.favorites.edit;

import android.support.v7.util.DiffUtil;

import org.septa.android.app.favorites.FavoriteState;

import java.util.List;

class FavoriteDiffCallback extends DiffUtil.Callback {

    private List<FavoriteState> oldFavorites, newFavorites;

    FavoriteDiffCallback(List<FavoriteState> mItemList, List<FavoriteState> favoriteStateList) {
        this.newFavorites = favoriteStateList;
        this.oldFavorites = mItemList;
    }

    @Override
    public int getOldListSize() {
        return oldFavorites.size();
    }

    @Override
    public int getNewListSize() {
        return newFavorites.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldFavorites.get(oldItemPosition).getFavoriteKey() == newFavorites.get(newItemPosition).getFavoriteKey();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldFavorites.get(oldItemPosition).equals(newFavorites.get(newItemPosition));
    }

}

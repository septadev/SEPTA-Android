package org.septa.android.app.favorites.edit;

import android.support.v7.util.DiffUtil;

import org.septa.android.app.services.apiinterfaces.model.Favorite;

import java.util.List;

class FavoriteDiffCallback extends DiffUtil.Callback {

    private List<Favorite> oldFavorites, newFavorites;

    FavoriteDiffCallback(List<Favorite> mItemList, List<Favorite> favoriteStateList) {
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
        return oldFavorites.get(oldItemPosition).getKey().equals(newFavorites.get(newItemPosition).getKey());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldFavorites.get(oldItemPosition).equals(newFavorites.get(newItemPosition));
    }

}

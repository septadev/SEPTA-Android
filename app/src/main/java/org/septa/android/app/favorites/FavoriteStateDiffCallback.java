package org.septa.android.app.favorites;

import android.support.v7.util.DiffUtil;

import java.util.List;

class FavoriteStateDiffCallback extends DiffUtil.Callback {

    private List<FavoriteState> oldFavoriteStates, newFavoriteStates;

    FavoriteStateDiffCallback(List<FavoriteState> mItemList, List<FavoriteState> favoriteStateList) {
        this.newFavoriteStates = favoriteStateList;
        this.oldFavoriteStates = mItemList;
    }

    @Override
    public int getOldListSize() {
        return oldFavoriteStates.size();
    }

    @Override
    public int getNewListSize() {
        return newFavoriteStates.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldFavoriteStates.get(oldItemPosition).getFavoriteKey().equalsIgnoreCase(newFavoriteStates.get(newItemPosition).getFavoriteKey());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldFavoriteStates.get(oldItemPosition).equals(newFavoriteStates.get(newItemPosition));
    }

}
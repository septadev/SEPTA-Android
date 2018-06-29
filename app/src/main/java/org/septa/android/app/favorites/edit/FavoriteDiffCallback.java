package org.septa.android.app.favorites.edit;

import android.support.v7.util.DiffUtil;

import org.septa.android.app.services.apiinterfaces.model.NextArrivalFavorite;

import java.util.List;

class FavoriteDiffCallback extends DiffUtil.Callback {

    private List<NextArrivalFavorite> oldNextArrivalFavorites, newNextArrivalFavorites;

    FavoriteDiffCallback(List<NextArrivalFavorite> mItemList, List<NextArrivalFavorite> nextArrivalFavoriteStateList) {
        this.newNextArrivalFavorites = nextArrivalFavoriteStateList;
        this.oldNextArrivalFavorites = mItemList;
    }

    @Override
    public int getOldListSize() {
        return oldNextArrivalFavorites.size();
    }

    @Override
    public int getNewListSize() {
        return newNextArrivalFavorites.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldNextArrivalFavorites.get(oldItemPosition).getKey().equals(newNextArrivalFavorites.get(newItemPosition).getKey());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldNextArrivalFavorites.get(oldItemPosition).equals(newNextArrivalFavorites.get(newItemPosition));
    }

}

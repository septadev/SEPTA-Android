package org.septa.android.app.services.apiinterfaces;

import android.content.Context;

import org.septa.android.app.favorites.FavoriteState;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalFavorite;

import java.util.List;
import java.util.Map;

public interface FavoritesSharedPrefsUtils {

    Map<String, NextArrivalFavorite> getFavorites(Context context);

    List<FavoriteState> getFavoriteStates(Context context);

    void addFavorites(Context context, NextArrivalFavorite nextArrivalFavorite);

    void addFavoriteState(Context context, FavoriteState favoriteState);

    void setFavorites(Context context, List<NextArrivalFavorite> nextArrivalFavoriteList);

    void setFavoriteStates(Context context, List<FavoriteState> favoriteStateList);

    void modifyFavoriteState(Context context, int index, boolean expanded);

    void deleteFavorite(Context context, String id);

    void deleteAllFavorites(Context context);

    void deleteAllFavoriteStates(Context context);

    NextArrivalFavorite getFavoriteByKey(Context context, String key);

    FavoriteState getFavoriteStateByKey(Context context, String key);

    FavoriteState getFavoriteStateByIndex(Context context, int index);

    void moveFavoriteStateToIndex(Context context, int fromPosition, int toPosition);

    void resyncFavoritesMap(Context context);

    void renameFavorite(Context context, NextArrivalFavorite nextArrivalFavorite);
}

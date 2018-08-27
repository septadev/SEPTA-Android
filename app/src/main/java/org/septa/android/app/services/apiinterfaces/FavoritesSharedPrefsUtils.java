package org.septa.android.app.services.apiinterfaces;

import android.content.Context;

import org.septa.android.app.favorites.FavoriteState;
import org.septa.android.app.services.apiinterfaces.model.Favorite;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalFavorite;
import org.septa.android.app.services.apiinterfaces.model.TransitViewFavorite;

import java.util.List;
import java.util.Map;

public interface FavoritesSharedPrefsUtils {

    Map<String, NextArrivalFavorite> getNTAFavorites(Context context);

    Map<String, TransitViewFavorite> getTransitViewFavorites(Context context);

    List<FavoriteState> getFavoriteStates(Context context);

    void addFavorites(Context context, Favorite favorite);

    void addFavoriteState(Context context, FavoriteState favoriteState);

    void setFavoriteStates(Context context, List<FavoriteState> favoriteStateList);

    void modifyFavoriteState(Context context, int index, boolean expanded);

    void deleteFavorite(Context context, String favoriteKey);

    void deleteAllFavorites(Context context);

    void deleteAllFavoriteStates(Context context);

    Favorite getFavoriteByKey(Context context, String key);

    void moveFavoriteStateToIndex(Context context, int fromPosition, int toPosition);

    void resyncFavoritesMap(Context context);

    void renameFavorite(Context context, Favorite favorite);
}

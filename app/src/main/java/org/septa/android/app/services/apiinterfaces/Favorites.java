package org.septa.android.app.services.apiinterfaces;

import android.content.Context;

import org.septa.android.app.favorites.FavoriteState;
import org.septa.android.app.services.apiinterfaces.model.Favorite;

import java.util.List;
import java.util.Map;

/**
 * Created by jkampf on 9/6/17.
 */

public interface Favorites {

    Map<String, Favorite> getFavorites(Context context);

    List<FavoriteState> getFavoriteStates(Context context);

    void addFavorites(Context context, Favorite favorite);

    void addFavoriteState(Context context, FavoriteState favoriteState);

    void setFavorites(Context context, List<Favorite> favoriteList);

    void setFavoriteStates(Context context, List<FavoriteState> favoriteStateList);

    void modifyFavoriteState(Context context, int index, boolean expanded);

    void deleteFavorite(Context context, String id);

    void deleteAllFavorites(Context context);

    void deleteAllFavoriteStates(Context context);

    Favorite getFavoriteByKey(Context context, String key);

    FavoriteState getFavoriteStateByKey(Context context, String key);

    FavoriteState getFavoriteStateByIndex(Context context, int index);

    void moveFavoriteStateToIndex(Context context, int fromPosition, int toPosition);

    void resyncFavoritesMap(Context context);
}

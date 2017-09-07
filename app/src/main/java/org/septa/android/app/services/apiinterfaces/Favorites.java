package org.septa.android.app.services.apiinterfaces;

import android.content.Context;

import org.septa.android.app.services.apiinterfaces.model.Favorite;

import java.util.Map;

/**
 * Created by jkampf on 9/6/17.
 */

public interface Favorites {

    public Map<String, Favorite> getFavorites(Context context);

    public void addFavorites(Context context, Favorite favorite);

    public void deleteFavorite(Context context, String id);

    public Favorite getFavoriteByKey(Context context, String key);

}

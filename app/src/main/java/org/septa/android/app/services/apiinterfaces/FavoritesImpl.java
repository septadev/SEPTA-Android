package org.septa.android.app.services.apiinterfaces;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.septa.android.app.services.apiinterfaces.model.Favorite;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by jkampf on 9/6/17.
 */

public class FavoritesImpl implements Favorites {
    public static final String PREFERENCE_NAME = "SEPTAFavorites";
    public static final String KEY = "favorite_json";

    @Override
    public Map<String, Favorite> getFavorites(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return getFavorites(sharedPreferences);
    }

    @Override
    public void addFavorites(Context context, Favorite favorite) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        Map<String, Favorite> favorites = getFavorites(sharedPreferences);

        favorites.put(favorite.getKey(), favorite);
        storeFavorites(sharedPreferences, favorites);
    }

    @Override
    public void deleteFavorite(Context context, String id) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        Map<String, Favorite> favorites = getFavorites(sharedPreferences);
        favorites.remove(id);
        storeFavorites(sharedPreferences, favorites);
    }

    @Override
    public void deleteAllFavorites(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        sharedPreferences.edit().remove(KEY).apply();
    }

    @Override
    public Favorite getFavoriteByKey(Context context, String key) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        Map<String, Favorite> favorites = getFavorites(sharedPreferences);
        return favorites.get(key);
    }

    private SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE);
    }

    private Map<String, Favorite> getFavorites(SharedPreferences sharedPreferences) {
        String prefrencesJson = sharedPreferences.getString(KEY, null);

        if (prefrencesJson == null)
            return new HashMap<String, Favorite>();

        Gson gson = new Gson();
        try {
            return gson.fromJson(prefrencesJson, new TypeToken<Map<String, Favorite>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            sharedPreferences.edit().remove(KEY);
            return new HashMap<String, Favorite>();
        }

    }

    private void storeFavorites(SharedPreferences sharedPreferences, Map<String, Favorite> favorites) {
        Gson gson = new Gson();
        String favortiesJson = gson.toJson(favorites);
        sharedPreferences.edit().putString(KEY, favortiesJson).apply();
    }

}

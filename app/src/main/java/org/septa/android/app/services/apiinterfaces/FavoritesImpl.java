package org.septa.android.app.services.apiinterfaces;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.septa.android.app.favorites.FavoriteState;
import org.septa.android.app.services.apiinterfaces.model.Favorite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jkampf on 9/6/17.
 */

public class FavoritesImpl implements Favorites {

    public static final String TAG = FavoritesImpl.class.getSimpleName();

    public static final String KEY_FAVORITES = "favorite_json";
    public static final String KEY_FAVORITES_STATE = "favorite_state_json";

    /**
     * fixing some corrupt favorites
     * @param context
     * @return list of valid favorites
     */
    @Override
    public Map<String, Favorite> getFavorites(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        Map<String, Favorite> favorites = getFavorites(sharedPreferences);
        for (Map.Entry<String, Favorite> entry : favorites.entrySet()){

            if (entry.getValue().getStart() == null){
                deleteAllFavorites(context);
                return new HashMap<>();
            }
        }
        return favorites;
    }

    @Override
    public List<FavoriteState> getFavoriteStates(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        String preferencesJson = sharedPreferences.getString(KEY_FAVORITES_STATE, null);

        if (preferencesJson == null)
            return new ArrayList<>();

        Gson gson = new Gson();
        try {
            return gson.fromJson(preferencesJson, new TypeToken<List<FavoriteState>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            sharedPreferences.edit().remove(KEY_FAVORITES_STATE);
            return new ArrayList<>();
        }
    }

    @Override
    public void addFavorites(Context context, Favorite favorite) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        Map<String, Favorite> favorites = getFavorites(sharedPreferences);

        favorites.put(favorite.getKey(), favorite);
        addFavoriteState(context, new FavoriteState(favorite.getKey()));
        storeFavorites(sharedPreferences, favorites);
    }

    @Override
    public void addFavoriteState(Context context, FavoriteState favoriteState) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        List<FavoriteState> favoritesState = getFavoriteStates(context);

        favoritesState.add(favoriteState);
        storeFavoritesState(sharedPreferences, favoritesState);
    }

    @Override
    public void setFavorites(Context context, List<Favorite> favoriteList) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);

        Map<String, Favorite> favoritesMap = new HashMap<>();
        for (Favorite favorite : favoriteList) {
            favoritesMap.put(favorite.getKey(), favorite);
        }

        storeFavorites(sharedPreferences, favoritesMap);
    }

    @Override
    public void setFavoriteStates(Context context, List<FavoriteState> favoriteStateList) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        storeFavoritesState(sharedPreferences, favoriteStateList);
    }

    @Override
    public void modifyFavoriteState(Context context, int index, boolean expanded) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        List<FavoriteState> favoriteStateList = getFavoriteStates(context);
        favoriteStateList.get(index).setExpanded(expanded);
        storeFavoritesState(sharedPreferences, favoriteStateList);
    }

    @Override
    public void deleteFavorite(Context context, String id) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        Map<String, Favorite> favorites = getFavorites(sharedPreferences);
        favorites.remove(id);
        deleteFavoriteState(context, id);
        storeFavorites(sharedPreferences, favorites);
    }

    private void deleteFavoriteState(Context context, String favoriteKey) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        List<FavoriteState> favoriteStates = getFavoriteStates(context);
        int indexToRemove = -1;
        for (int i = 0; i < favoriteStates.size(); i++) {
            if (favoriteKey.equals(favoriteStates.get(i).getFavoriteKey())) {
                indexToRemove = i;
                break;
            }
        }
        if (indexToRemove != -1) {
            favoriteStates.remove(indexToRemove);
        } else {
            Log.e(TAG, "Could not delete");
        }
        storeFavoritesState(sharedPreferences, favoriteStates);
    }

    @Override
    public void deleteAllFavorites(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        sharedPreferences.edit().remove(KEY_FAVORITES).apply();
        deleteAllFavoriteStates(context);
    }

    @Override
    public void deleteAllFavoriteStates(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        sharedPreferences.edit().remove(KEY_FAVORITES_STATE).apply();
    }

    @Override
    public Favorite getFavoriteByKey(Context context, String key) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        Map<String, Favorite> favorites = getFavorites(sharedPreferences);
        return favorites.get(key);
    }

    @Override
    public FavoriteState getFavoriteStateByKey(Context context, String key) {
        List<FavoriteState> favoriteStates = getFavoriteStates(context);
        for (FavoriteState favoriteState : favoriteStates) {
            if (key.equals(favoriteState.getFavoriteKey())) {
                return favoriteState;
            }
        }
        Log.e(TAG, "FavoriteState with key " + key + " does not exist");
        return null;
    }

    @Override
    public FavoriteState getFavoriteStateByIndex(Context context, int index) {
        List<FavoriteState> favoriteStates = getFavoriteStates(context);
        if (favoriteStates.size() > index) {
            return favoriteStates.get(index);
        }
        Log.e(TAG, "FavoriteState at index " + index + " does not exist");
        return null;
    }

    @Override
    public void moveFavoriteStateToIndex(Context context, int index, FavoriteState favoriteState) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);

        // remove favorite state
        deleteFavoriteState(context, favoriteState.getFavoriteKey());

        // set it at index which shifts everything else back one
        List<FavoriteState> favoritesState = getFavoriteStates(context);
        favoritesState.set(index, favoriteState);
        storeFavoritesState(sharedPreferences, favoritesState);
    }

    @Override
    public void resyncFavoritesMap(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        List<FavoriteState> favoriteStateList = getFavoriteStates(context);
        Map<String, Favorite> favoriteMap = getFavorites(context);

        if (favoriteStateList != null && !favoriteStateList.isEmpty() && favoriteMap != null && !favoriteMap.isEmpty()) {
            deleteAllFavorites(context);

            Map<String, Favorite> newFavorites = new HashMap<>();

            for (FavoriteState favoriteState: favoriteStateList) {
                String favoriteKey = favoriteState.getFavoriteKey();
                newFavorites.put(favoriteKey, favoriteMap.get(favoriteKey));
            }

            storeFavorites(sharedPreferences, newFavorites);
        } else {
            Log.e(TAG, "Resync of favorites map could not occur");
        }
    }

    private SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE);
    }

    private Map<String, Favorite> getFavorites(SharedPreferences sharedPreferences) {
        String preferencesJson = sharedPreferences.getString(KEY_FAVORITES, null);

        if (preferencesJson == null)
            return new HashMap<>();

        Gson gson = new Gson();
        try {
            return gson.fromJson(preferencesJson, new TypeToken<Map<String, Favorite>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            sharedPreferences.edit().remove(KEY_FAVORITES);
            return new HashMap<>();
        }
    }

    private void storeFavorites(SharedPreferences sharedPreferences, Map<String, Favorite> favorites) {
        Gson gson = new Gson();
        String favoritesJson = gson.toJson(favorites);
        sharedPreferences.edit().putString(KEY_FAVORITES, favoritesJson).apply();
    }

    private void storeFavoritesState(SharedPreferences sharedPreferences, List<FavoriteState> favoriteStateList) {
        Gson gson = new Gson();
        String favoritesStatesJson = gson.toJson(favoriteStateList);
        sharedPreferences.edit().putString(KEY_FAVORITES_STATE, favoritesStatesJson).apply();
    }

}

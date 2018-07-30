package org.septa.android.app.services.apiinterfaces;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.septa.android.app.favorites.FavoriteState;
import org.septa.android.app.services.apiinterfaces.model.Favorite;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalFavorite;
import org.septa.android.app.services.apiinterfaces.model.TransitViewFavorite;
import org.septa.android.app.transitview.TransitViewUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoritesSharedPrefsUtilsImpl implements FavoritesSharedPrefsUtils {

    public static final String TAG = FavoritesSharedPrefsUtilsImpl.class.getSimpleName();

    private static final String KEY_FAVORITES_NTA = "favorite_json";
    private static final String KEY_FAVORITES_TRANSITVIEW = "favorite_transitview_json";
    private static final String KEY_FAVORITES_STATE = "favorite_state_json";

    // using commit() instead of apply() so that the values are immediately written to memory

    /**
     * fixing some corrupt favorites
     *
     * @param context
     * @return list of valid favorites
     */
    @Override
    public Map<String, NextArrivalFavorite> getNTAFavorites(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        Map<String, NextArrivalFavorite> favorites = getNTAFavorites(sharedPreferences);
        for (Map.Entry<String, NextArrivalFavorite> entry : favorites.entrySet()) {

            // delete any invalid NTA favorites
            if (entry.getValue().getStart() == null) {
                deleteAllFavorites(context);
                return new HashMap<>();
            }
        }
        return favorites;
    }

    /**
     * fixing some corrupt favorites
     *
     * @param context
     * @return list of valid favorites
     */
    @Override
    public Map<String, TransitViewFavorite> getTransitViewFavorites(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        Map<String, TransitViewFavorite> favorites = getTransitViewFavorites(sharedPreferences);
        for (Map.Entry<String, TransitViewFavorite> entry : favorites.entrySet()) {

//             TODO: delete / fix any invalid TransitView favorites
            if (entry.getValue().getSecondRoute() == null && entry.getValue().getThirdRoute() != null) {
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

        if (preferencesJson == null) {
            return new ArrayList<>();
        }

        Gson gson = new Gson();
        try {
            return gson.fromJson(preferencesJson, new TypeToken<List<FavoriteState>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            Log.e(TAG, e.toString());
            sharedPreferences.edit().remove(KEY_FAVORITES_STATE).commit();
            return new ArrayList<>();
        }
    }

    @Override
    public void addFavorites(Context context, Favorite favorite) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        if (favorite instanceof NextArrivalFavorite) {
            Map<String, NextArrivalFavorite> favorites = getNTAFavorites(sharedPreferences);

            favorites.put(favorite.getKey(), (NextArrivalFavorite) favorite);
            addFavoriteState(context, new FavoriteState(favorite.getKey()));

            storeNTAFavorites(sharedPreferences, favorites);
        } else if (favorite instanceof TransitViewFavorite) {
            Map<String, TransitViewFavorite> favorites = getTransitViewFavorites(sharedPreferences);

            favorites.put(favorite.getKey(), (TransitViewFavorite) favorite);
            addFavoriteState(context, new FavoriteState(favorite.getKey()));

            storeTransitViewFavorites(sharedPreferences, favorites);
        } else {
            Log.e(TAG, "Invalid class type -- could not create a new Favorite for " + favorite.getKey());
        }
    }

    @Override
    public void addFavoriteState(Context context, FavoriteState favoriteState) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        List<FavoriteState> favoritesState = getFavoriteStates(context);

        if (!favoritesState.contains(favoriteState)) {
            favoritesState.add(favoriteState);
            storeFavoritesState(sharedPreferences, favoritesState);
        } else {
            Log.d(TAG, "Already have a favorite state for " + favoriteState.getFavoriteKey());
        }
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
    public void renameFavorite(Context context, Favorite favorite) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);

        if (favorite instanceof NextArrivalFavorite) {
            Map<String, NextArrivalFavorite> favorites = getNTAFavorites(sharedPreferences);

            if (favorites.containsKey(favorite.getKey())) {
                favorites.put(favorite.getKey(), (NextArrivalFavorite) favorite);
                storeNTAFavorites(sharedPreferences, favorites);
            } else {
                Log.d(TAG, "NTA Favorite could not be renamed because it did not exist!");
                addFavorites(context, favorite);
            }

        } else if (favorite instanceof TransitViewFavorite) {
            Map<String, TransitViewFavorite> favorites = getTransitViewFavorites(sharedPreferences);

            if (favorites.containsKey(favorite.getKey())) {
                favorites.put(favorite.getKey(), (TransitViewFavorite) favorite);
                storeTransitViewFavorites(sharedPreferences, favorites);
            } else {
                Log.d(TAG, "TransitView Favorite could not be renamed because it did not exist!");
                addFavorites(context, favorite);
            }

        } else {
            Log.e(TAG, "Invalid class type -- could not rename Favorite " + favorite.getKey());
        }

    }

    @Override
    public void deleteFavorite(Context context, String favoriteKey) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);

        // attempt to delete NTA favorite
        Map<String, NextArrivalFavorite> ntaFavorites = getNTAFavorites(sharedPreferences);
        if (ntaFavorites.remove(favoriteKey) != null) {
            storeNTAFavorites(sharedPreferences, ntaFavorites);

        } else {
            // attempt to delete TransitView favorite
            Map<String, TransitViewFavorite> transitViewFavorites = getTransitViewFavorites(sharedPreferences);
            if (transitViewFavorites.remove(favoriteKey) != null) {
                storeTransitViewFavorites(sharedPreferences, transitViewFavorites);

            } else {
                Log.e(TAG, "Could not delete Favorite with key " + favoriteKey);
            }
        }
        deleteFavoriteState(context, favoriteKey);
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
            Log.e(TAG, "Could not delete favorite state with key " + favoriteKey);
        }
        storeFavoritesState(sharedPreferences, favoriteStates);
    }

    @Override
    public void deleteAllFavorites(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        sharedPreferences.edit().remove(KEY_FAVORITES_NTA).commit();
        sharedPreferences.edit().remove(KEY_FAVORITES_TRANSITVIEW).commit();
        deleteAllFavoriteStates(context);
    }

    @Override
    public void deleteAllFavoriteStates(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        sharedPreferences.edit().remove(KEY_FAVORITES_STATE).commit();
    }

    @Override
    public Favorite getFavoriteByKey(Context context, String key) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);

        Favorite favorite = getNTAFavorites(sharedPreferences).get(key);

        if (favorite == null) {
            favorite = getTransitViewFavorites(sharedPreferences).get(key);
        }

        return favorite;
    }

    @Override
    public void moveFavoriteStateToIndex(Context context, int fromPosition, int toPosition) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        List<FavoriteState> favoriteStateList = getFavoriteStates(context);
        FavoriteState favoriteStateToMove = favoriteStateList.get(fromPosition);

        // remove favorite state
        favoriteStateList.remove(fromPosition);

        // re-add at index which shifts everything else back one
        favoriteStateList.add(toPosition, favoriteStateToMove);

        storeFavoritesState(sharedPreferences, favoriteStateList);
    }

    @Override
    public void resyncFavoritesMap(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        List<FavoriteState> favoriteStateList = getFavoriteStates(context);

        Map<String, NextArrivalFavorite> ntaFavorites = getNTAFavorites(context);
        Map<String, TransitViewFavorite> transitViewFavorites = getTransitViewFavorites(context);

        if (favoriteStateList.isEmpty() && (!ntaFavorites.isEmpty() || !transitViewFavorites.isEmpty())) {
            // initialize favorite state list
            Log.d(TAG, "Initializing favorite states now...");

            for (NextArrivalFavorite entry : ntaFavorites.values()) {
                FavoriteState favoriteState = new FavoriteState(entry.getKey());
                favoriteStateList.add(favoriteState);
            }

            for (TransitViewFavorite entry : transitViewFavorites.values()) {
                FavoriteState favoriteState = new FavoriteState(entry.getKey());
                favoriteStateList.add(favoriteState);
            }

            setFavoriteStates(context, favoriteStateList);
        } else if (favoriteStateList.size() != (ntaFavorites.size() + transitViewFavorites.size())) {
            // resync because state list does not map 1-to-1
            Log.d(TAG, "Resyncing favorite states now...");

            deleteAllFavorites(context);

            Map<String, NextArrivalFavorite> newNTAFavorites = new HashMap<>();
            Map<String, TransitViewFavorite> newTransitViewFavorites = new HashMap<>();

            for (FavoriteState favoriteState : favoriteStateList) {
                String favoriteKey = favoriteState.getFavoriteKey();

                // copy over favorite
                if (TransitViewUtils.isATransitViewFavorite(favoriteKey)) {
                    newTransitViewFavorites.put(favoriteKey, transitViewFavorites.get(favoriteKey));
                } else {
                    newNTAFavorites.put(favoriteKey, ntaFavorites.get(favoriteKey));
                }

                // create new favorite state for it
                if (getFavoriteByKey(context, favoriteKey) == null) {
                    addFavoriteState(context, new FavoriteState(favoriteKey));
                } else {
                    Log.d(TAG, "Favorite state already exists for favorite with key: " + favoriteKey);
                }
            }

            storeNTAFavorites(sharedPreferences, newNTAFavorites);
            storeTransitViewFavorites(sharedPreferences, newTransitViewFavorites);
        } else {
            Log.d(TAG, "Resync of favorites map did not occur. State list size: " + favoriteStateList.size() +
                    " NTA Map size: " + ntaFavorites.size() + " TransitView map size: " + transitViewFavorites.size());
        }
    }

    private SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE);
    }

    private Map<String, NextArrivalFavorite> getNTAFavorites(SharedPreferences sharedPreferences) {
        String preferencesJson = sharedPreferences.getString(KEY_FAVORITES_NTA, null);

        if (preferencesJson == null) {
            return new HashMap<>();
        }

        Gson gson = new Gson();
        try {
            return gson.fromJson(preferencesJson, new TypeToken<Map<String, NextArrivalFavorite>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            Log.e(TAG, e.toString());
            sharedPreferences.edit().remove(KEY_FAVORITES_NTA).commit();
            return new HashMap<>();
        }
    }

    private Map<String, TransitViewFavorite> getTransitViewFavorites(SharedPreferences sharedPreferences) {
        String preferencesJson = sharedPreferences.getString(KEY_FAVORITES_TRANSITVIEW, null);

        if (preferencesJson == null) {
            return new HashMap<>();
        }

        Gson gson = new Gson();
        try {
            return gson.fromJson(preferencesJson, new TypeToken<Map<String, TransitViewFavorite>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            Log.e(TAG, e.toString());
            sharedPreferences.edit().remove(KEY_FAVORITES_TRANSITVIEW).commit();
            return new HashMap<>();
        }
    }

    private void storeNTAFavorites(SharedPreferences sharedPreferences, Map<String, NextArrivalFavorite> favorites) {
        Gson gson = new Gson();
        String favoritesJson = gson.toJson(favorites);
        sharedPreferences.edit().putString(KEY_FAVORITES_NTA, favoritesJson).commit();
    }

    private void storeTransitViewFavorites(SharedPreferences sharedPreferences, Map<String, TransitViewFavorite> favorites) {
        Gson gson = new Gson();
        String favoritesJson = gson.toJson(favorites);
        sharedPreferences.edit().putString(KEY_FAVORITES_TRANSITVIEW, favoritesJson).commit();
    }

    private void storeFavoritesState(SharedPreferences sharedPreferences, List<FavoriteState> favoriteStateList) {
        Gson gson = new Gson();
        String favoritesStatesJson = gson.toJson(favoriteStateList);
        sharedPreferences.edit().putString(KEY_FAVORITES_STATE, favoritesStatesJson).commit();
    }

}

/*
 * NextToArriveFavoritesAndRecentlyViewedStore.java
 * Last modified on 06-06-2014 16:57-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.managers;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.septa.android.app.models.NextToArriveFavoriteModel;
import org.septa.android.app.models.NextToArriveRecentlyViewedModel;
import org.septa.android.app.models.NextToArriveStoredTripModel;
import org.septa.android.app.models.ObjectFactory;

import java.util.ArrayList;
import java.util.List;

public class NextToArriveFavoritesAndRecentlyViewedStore {

    private static final String TAG = "NextToArriveFavoritesAndRecentlyViewedStore";

    private ArrayList<NextToArriveFavoriteModel> favoritesList;
    private ArrayList<NextToArriveRecentlyViewedModel> recentlyViewedList;
    private Context context;

    public NextToArriveFavoritesAndRecentlyViewedStore(Context context) {

        this.context = context;
        recentlyViewedList = getRecentlyViewedList();
        favoritesList = getFavoriteList();
    }

    public int trueRecentlyViewedListSize() {
        // if the list has a size of 3, make sure the entries are true and not null
        if (recentlyViewedList.size() == 3) {
            if (recentlyViewedList.get(2) != null) {
                return 3;
            } else {
                if (recentlyViewedList.get(1) != null) {
                    return 2;
                } else {
                    if (recentlyViewedList.get(0) != null) {
                        return 1;
                    }
                }
            }
            return 0;
        }

        // the size of the list is not 3, thus they must be real being added in individually
        return recentlyViewedList.size();
    }

    public boolean isFavorite(NextToArriveStoredTripModel nextToArriveModel) {
        for (NextToArriveStoredTripModel storedTrip : favoritesList) {
            if (storedTrip.compareTo(nextToArriveModel) == 0) {
                return true;
            }
        }

        return false;
    }

    public boolean isRecentlyViewed(NextToArriveStoredTripModel nextToArriveModel) {
        for (NextToArriveStoredTripModel storedTrip : recentlyViewedList) {
            if (storedTrip.compareTo(nextToArriveModel) == 0) {
                return true;
            }
        }

        return false;
    }

    public void addFavorite(NextToArriveFavoriteModel nextToArriveFavoriteModel) {
        // search the list for a duplicate, if yes just return
        // the code should be safe guarding against this event even occuring
        for (NextToArriveStoredTripModel storedTrip : favoritesList) {
            if (storedTrip.compareTo(nextToArriveFavoriteModel) == 0) {
                return;
            }
        }

        favoritesList.add(0, nextToArriveFavoriteModel);

        // in case this favorite is also a recently viewed
        removeRecentlyViewed(nextToArriveFavoriteModel);

        sendToSharedPreferencesFavorites();
    }

    public void removeFavorite(NextToArriveStoredTripModel nextToArriveFavoriteModel) {
        for (int i=0; i< favoritesList.size(); i++) {
            if (favoritesList.get(i).compareTo(nextToArriveFavoriteModel) == 0) {
                favoritesList.remove(i);

                sendToSharedPreferencesFavorites();
            }
        }
    }

    public void removeRecentlyViewed(NextToArriveStoredTripModel nextToArriveModel) {
        for (int i=0; i < recentlyViewedList.size(); i++) {
            if (recentlyViewedList.get(i) != null) {
                if (recentlyViewedList.get(i).compareTo(nextToArriveModel) == 0) {
                    recentlyViewedList.remove(i);

                    sendToSharedPreferencesRecentlyViewed();
                }
            }
        }
    }

    public void addRecentlyViewed(NextToArriveRecentlyViewedModel nextToArriveModel) {

        // duplicate avoidance
        // check if we already have this recently viewed
        // if we find a duplicate, remove it form the list, the list will shuffle properly.
        for (int i=0; i < recentlyViewedList.size(); i++) {
            if (recentlyViewedList.get(i) != null) {
                if (recentlyViewedList.get(i).compareTo(nextToArriveModel) == 0) {
                    recentlyViewedList.remove(i);
                }
            }
        }

        // put the recently viewed item at the top of the list
        recentlyViewedList.add(0, nextToArriveModel);

        // check if we have a (overly) full list, and remove the 4th item if exists
        if (recentlyViewedList.size() == 4) {
            recentlyViewedList.remove(3);
        }

        sendToSharedPreferencesRecentlyViewed();
    }

    public ArrayList<NextToArriveRecentlyViewedModel> getRecentlyViewedList() {
        Gson gson = new Gson();

        String json = SharedPreferencesManager.getInstance().getNextToArriveRecentlyViewedList();
        try {
            recentlyViewedList = gson.fromJson(json, new TypeToken<List<NextToArriveRecentlyViewedModel>>(){}.getType());
        } catch (JsonSyntaxException e) {
            SharedPreferencesManager.getInstance().clearNextToArriveRecentlyViewedList();
            Log.i(TAG, "Clearing recentlyViewedList");
            recentlyViewedList = null;
        }

        if (recentlyViewedList == null) {
            recentlyViewedList = new ArrayList<NextToArriveRecentlyViewedModel>(3);
        }

        return recentlyViewedList;
    }

    public ArrayList<NextToArriveFavoriteModel> getFavoriteList() {
        Gson gson = new Gson();

        String json = SharedPreferencesManager.getInstance().getNextToArriveFavoritesList();
        try {
            favoritesList = gson.fromJson(json, new TypeToken<List<NextToArriveFavoriteModel>>(){}.getType());
        } catch (JsonSyntaxException e) {
            SharedPreferencesManager.getInstance().clearNexttoArriveFavoritesList();
            Log.i(TAG, "Clearing favoritesList");
            favoritesList = null;
        }

        if (favoritesList == null) {
            favoritesList = new ArrayList<NextToArriveFavoriteModel>();
        }

        return favoritesList;
    }

    private void sendToSharedPreferencesRecentlyViewed() {
        Gson gson = new Gson();
        String recentlyViewedListAsJSON = gson.toJson(recentlyViewedList);

        SharedPreferencesManager.getInstance().setNextToArriveRecentlyViewedList(recentlyViewedListAsJSON);
    }

    private void sendToSharedPreferencesFavorites() {
        Gson gson = new Gson();
        String favoritesListAsJSON = gson.toJson(favoritesList);

        SharedPreferencesManager.getInstance().setNexttoArriveFavoritesList(favoritesListAsJSON);
    }
}

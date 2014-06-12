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
import com.google.gson.reflect.TypeToken;

import org.septa.android.app.models.NextToArriveFavoriteModel;
import org.septa.android.app.models.NextToArriveRecentlyViewedModel;
import org.septa.android.app.models.ObjectFactory;

import java.util.ArrayList;
import java.util.List;

public class NextToArriveFavoritesAndRecentlyViewedStore {

    private ArrayList<NextToArriveFavoriteModel>favoritesJSONList;
    private ArrayList<NextToArriveRecentlyViewedModel>recentlyViewedJSONList;
    private Context context;

    public NextToArriveFavoritesAndRecentlyViewedStore(Context context) {

        this.context = context;
        recentlyViewedJSONList = getRecentlyViewedList();
        favoritesJSONList = getFavoriteList();
    }

    public int trueRecentlyViewedListSize() {
        // if the list has a size of 3, make sure the entries are true and not null
        if (recentlyViewedJSONList.size() == 3) {
            if (recentlyViewedJSONList.get(2) != null) {
                return 3;
            } else {
                if (recentlyViewedJSONList.get(1) != null) {
                    return 2;
                } else {
                    if (recentlyViewedJSONList.get(0) != null) {
                        return 1;
                    }
                }
            }
            return 0;
        }

        // the size of the list is not 3, thus they must be real being added in individually
        return recentlyViewedJSONList.size();
    }

    public void addRecentlyViewed(NextToArriveRecentlyViewedModel nextToArriveModel) {

        // duplicate avoidance
        // check if we already have this recently viewed
        // if we find a duplicate, remove it form the list, the list will shuffle properly.
        for (int i=0; i < recentlyViewedJSONList.size(); i++) {
            if (recentlyViewedJSONList.get(i) != null) {
                if (recentlyViewedJSONList.get(i).compareTo(nextToArriveModel) == 0) {
                    recentlyViewedJSONList.remove(i);
                }
            }
        }

        // put the recently viewed item at the top of the list
        recentlyViewedJSONList.add(0, nextToArriveModel);

        // check if we have a (overly) full list, and remove the 4th item if exists
        if (recentlyViewedJSONList.size() == 4) {
            recentlyViewedJSONList.remove(3);
        }

        sendToSharedPreferencesRecentlyViewed();
    }

    public ArrayList<NextToArriveRecentlyViewedModel> getRecentlyViewedList() {
        Gson gson = new Gson();

        String json = ObjectFactory.getInstance().getSharedPreferencesManager(context).getNextToArriveRecentlyViewedList();
        recentlyViewedJSONList = gson.fromJson(json, new TypeToken<List<NextToArriveRecentlyViewedModel>>(){}.getType());

        if (recentlyViewedJSONList == null) {
            recentlyViewedJSONList = new ArrayList<NextToArriveRecentlyViewedModel>(3);
        }

        return recentlyViewedJSONList;
    }

    public ArrayList<NextToArriveFavoriteModel> getFavoriteList() {
        Gson gson = new Gson();

        String json = ObjectFactory.getInstance().getSharedPreferencesManager(context).getNextToArriveFavoritesList();
        favoritesJSONList = gson.fromJson(json, new TypeToken<List<NextToArriveFavoriteModel>>(){}.getType());

        if (favoritesJSONList == null) {
            favoritesJSONList = new ArrayList<NextToArriveFavoriteModel>(3);
        }

        return favoritesJSONList;
    }

    private void sendToSharedPreferencesRecentlyViewed() {
        Gson gson = new Gson();
        String recentlyViewedListAsJSON = gson.toJson(recentlyViewedJSONList);

        ObjectFactory.getInstance().getSharedPreferencesManager(context).setNextToArriveRecentlyViewedList(recentlyViewedListAsJSON);
    }

    private void sendToSharedPreferencesFavorites() {
        Gson gson = new Gson();
        String favoritesListAsJSON = gson.toJson(favoritesJSONList);

        ObjectFactory.getInstance().getSharedPreferencesManager(context).setNexttoArriveFavoritesList(favoritesListAsJSON);
    }
}

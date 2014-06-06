/*
 * NextToArriveFavoritesAndRecentlyViewedStore.java
 * Last modified on 06-06-2014 16:57-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.managers;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.septa.android.app.models.NextToArriveFavoriteModel;
import org.septa.android.app.models.NextToArriveRecentlyViewedModel;
import org.septa.android.app.models.NextToArriveStoredTripModel;
import org.septa.android.app.models.ObjectFactory;
import org.septa.android.app.models.servicemodels.NextToArriveModel;

import java.util.ArrayList;
import java.util.List;

public class NextToArriveFavoritesAndRecentlyViewedStore {

    private ArrayList<NextToArriveFavoriteModel>favoritesJSONList = new ArrayList<NextToArriveFavoriteModel>(3);
    private ArrayList<NextToArriveRecentlyViewedModel>recentlyViewedJSONList = new ArrayList<NextToArriveRecentlyViewedModel>(3);
    private Context context;

    public NextToArriveFavoritesAndRecentlyViewedStore(Context context) {

        this.context = context;
    }

    public void addRecentlyViewed(NextToArriveRecentlyViewedModel nextToArriveModel) {
        recentlyViewedJSONList.set(2, recentlyViewedJSONList.get(1));
        recentlyViewedJSONList.set(1, recentlyViewedJSONList.get(0));
        recentlyViewedJSONList.set(0, nextToArriveModel);

        sendToSharedPreferencesRecentlyViewed();
    }

    public ArrayList<NextToArriveRecentlyViewedModel> getRecentlyViewedList() {
        Gson gson = new Gson();

        String json = ObjectFactory.getInstance().getSharedPreferencesManager(context).getNextToArriveRecentlyViewedList();
        recentlyViewedJSONList = gson.fromJson(json, new TypeToken<List<NextToArriveRecentlyViewedModel>>(){}.getType());

        if (recentlyViewedJSONList == null) {
            recentlyViewedJSONList = new ArrayList<NextToArriveRecentlyViewedModel>();
        }

        return recentlyViewedJSONList;
    }

    private void sendToSharedPreferencesRecentlyViewed() {
        Gson gson = new Gson();
        String recentlyViewedListAsJSON = gson.toJson(recentlyViewedJSONList);

        ObjectFactory.getInstance().getSharedPreferencesManager(context).setNexttoArriveRecentlyViewedList(recentlyViewedListAsJSON);
    }
}

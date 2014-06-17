package org.septa.android.app.managers;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.septa.android.app.models.SchedulesFavoriteModel;
import org.septa.android.app.models.SchedulesRecentlyViewedModel;
import org.septa.android.app.models.ObjectFactory;
import org.septa.android.app.models.SchedulesRouteModel;

import java.util.ArrayList;
import java.util.List;

public class SchedulesFavoritesAndRecentlyViewedStore {

    private ArrayList<SchedulesFavoriteModel> favoritesList;
    private ArrayList<SchedulesRecentlyViewedModel> recentlyViewedList;
    private Context context;

    public SchedulesFavoritesAndRecentlyViewedStore(Context context) {

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

    public boolean isFavorite(SchedulesRouteModel schedulesModel) {
        for (SchedulesRouteModel storedTrip : favoritesList) {
            if (storedTrip.compareTo(schedulesModel) == 0) {
                return true;
            }
        }

        return false;
    }

    public void addFavorite(SchedulesFavoriteModel schedulesFavoriteModel) {
        // search the list for a duplicate, if yes just return
        // the code should be safe guarding against this event even occurring
        for (SchedulesRouteModel storedTrip : favoritesList) {
            if (storedTrip.compareTo(schedulesFavoriteModel) == 0) {
                return;
            }
        }

        favoritesList.add(0, schedulesFavoriteModel);

        // in case this favorite is also a recently viewed
        removeRecentlyViewed(schedulesFavoriteModel);

        sendToSharedPreferencesFavorites();
    }

    public void removeFavorite(SchedulesFavoriteModel schedulesFavoriteModel) {
        Log.d("tt", "remove favorite being run with list size of " + favoritesList.size());
        for (int i=0; i< favoritesList.size(); i++) {
            if (favoritesList.get(i).compareTo(schedulesFavoriteModel) == 0) {
                Log.d("qqq", "found a match... removing");
                favoritesList.remove(i);
                Log.d("qqq", "new list size is "+favoritesList.size());

                sendToSharedPreferencesFavorites();
            }
        }
    }

    public void removeRecentlyViewed(SchedulesRouteModel schedulesModel) {
        for (int i=0; i < recentlyViewedList.size(); i++) {
            if (recentlyViewedList.get(i) != null) {
                if (recentlyViewedList.get(i).compareTo(schedulesModel) == 0) {
                    recentlyViewedList.remove(i);

                    sendToSharedPreferencesRecentlyViewed();
                }
            }
        }
    }

    public void addRecentlyViewed(SchedulesRecentlyViewedModel schedulesModel) {

        // duplicate avoidance
        // check if we already have this recently viewed
        // if we find a duplicate, remove it form the list, the list will shuffle properly.
        for (int i=0; i < recentlyViewedList.size(); i++) {
            Log.d("f", "processing the recently viewed list with size "+recentlyViewedList.size());
            if (recentlyViewedList.get(i) != null) {
                Log.d("F", "recently viewed for position "+i);
                if (recentlyViewedList.get(i).compareTo(schedulesModel) == 0) {
                    Log.d("d", "recently viewed at that position is a dup");
                    recentlyViewedList.remove(i);
                }
            }
        }

        // put the recently viewed item at the top of the list
        recentlyViewedList.add(0, schedulesModel);

        // check if we have a (overly) full list, and remove the 4th item if exists
        if (recentlyViewedList.size() == 4) {
            recentlyViewedList.remove(3);
        }

        sendToSharedPreferencesRecentlyViewed();
    }

    public ArrayList<SchedulesRecentlyViewedModel> getRecentlyViewedList() {
        Gson gson = new Gson();

        String json = ObjectFactory.getInstance().getSharedPreferencesManager(context).getSchedulesRecentlyViewedList();
        recentlyViewedList = gson.fromJson(json, new TypeToken<List<SchedulesRecentlyViewedModel>>(){}.getType());

        if (recentlyViewedList == null) {
            recentlyViewedList = new ArrayList<SchedulesRecentlyViewedModel>(3);
        }

        return recentlyViewedList;
    }

    public ArrayList<SchedulesFavoriteModel> getFavoriteList() {
        Gson gson = new Gson();

        String json = ObjectFactory.getInstance().getSharedPreferencesManager(context).getSchedulesFavoritesList();
        favoritesList = gson.fromJson(json, new TypeToken<List<SchedulesFavoriteModel>>(){}.getType());

        if (favoritesList == null) {
            favoritesList = new ArrayList<SchedulesFavoriteModel>();
        }

        return favoritesList;
    }

    private void sendToSharedPreferencesRecentlyViewed() {
        Gson gson = new Gson();
        String recentlyViewedListAsJSON = gson.toJson(recentlyViewedList);
        Log.d("y", "about to store this json "+recentlyViewedListAsJSON);

        ObjectFactory.getInstance().getSharedPreferencesManager(context).setSchedulesRecentlyViewedList(recentlyViewedListAsJSON);
    }

    private void sendToSharedPreferencesFavorites() {
        Gson gson = new Gson();
        String favoritesListAsJSON = gson.toJson(favoritesList);

        ObjectFactory.getInstance().getSharedPreferencesManager(context).setNexttoArriveFavoritesList(favoritesListAsJSON);
    }
}

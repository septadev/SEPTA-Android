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
import java.util.HashMap;
import java.util.List;

public class SchedulesFavoritesAndRecentlyViewedStore {

    private HashMap<String, ArrayList<SchedulesFavoriteModel>> favoritesListMap;
    private HashMap<String, ArrayList<SchedulesRecentlyViewedModel>> recentlyViewedListMap;

    private Context context;

    public SchedulesFavoritesAndRecentlyViewedStore(Context context) {

        this.context = context;

        favoritesListMap = new HashMap<String, ArrayList<SchedulesFavoriteModel>>();
        recentlyViewedListMap = new HashMap<String, ArrayList<SchedulesRecentlyViewedModel>>();
    }

    public boolean isFavorite(String routeType, SchedulesRouteModel schedulesModel) {
        ArrayList<SchedulesFavoriteModel>favoritesList = favoritesListMap.get(routeType);

        if (favoritesList != null) {
            for (SchedulesRouteModel storedTrip : favoritesList) {
                if (storedTrip.compareTo(schedulesModel) == 0) {
                    return true;
                }
            }
        }

        return false;
    }

    public void addFavorite(String routeType, SchedulesFavoriteModel schedulesFavoriteModel) {
        // search the list for a duplicate, if yes just return
        // the code should be safe guarding against this event even occurring
        ArrayList<SchedulesFavoriteModel>favoritesList = favoritesListMap.get(routeType);

        if (favoritesList != null) {

            for (SchedulesRouteModel storedTrip : favoritesList) {
                if (storedTrip.compareTo(schedulesFavoriteModel) == 0) {
                    return;
                }
            }
        } else {
            favoritesList = new ArrayList<SchedulesFavoriteModel>();
        }

        favoritesList.add(0, schedulesFavoriteModel);

        // in case this favorite is also a recently viewed
        removeRecentlyViewed(routeType, schedulesFavoriteModel);

        favoritesListMap.put(routeType, favoritesList);

        sendToSharedPreferencesFavorites();
    }

    public void removeFavorite(String routeType, SchedulesFavoriteModel schedulesFavoriteModel) {
        ArrayList<SchedulesFavoriteModel>favoritesList = favoritesListMap.get(routeType);

        if (favoritesList != null) {
            Log.d("tt", "remove favorite being run with list size of " + favoritesList.size());
            for (int i = 0; i < favoritesList.size(); i++) {
                if (favoritesList.get(i).compareTo(schedulesFavoriteModel) == 0) {
                    Log.d("qqq", "found a match... removing");
                    favoritesList.remove(i);
                    Log.d("qqq", "new list size is " + favoritesList.size());

                    sendToSharedPreferencesFavorites();
                }
            }
        }
    }

    public void removeRecentlyViewed(String routeType, SchedulesRouteModel schedulesModel) {
        ArrayList<SchedulesRecentlyViewedModel> recentlyViewedList = recentlyViewedListMap.get(routeType);
        if (recentlyViewedList != null) {
            for (int i = 0; i < recentlyViewedList.size(); i++) {
                if (recentlyViewedList.get(i) != null) {
                    if (recentlyViewedList.get(i).compareTo(schedulesModel) == 0) {
                        recentlyViewedList.remove(i);

                        sendToSharedPreferencesRecentlyViewed();
                    }
                }
            }
        }
    }

    public void addRecentlyViewed(String routeType, SchedulesRecentlyViewedModel schedulesModel) {
        // duplicate avoidance
        // check if we already have this recently viewed
        // if we find a duplicate, remove it form the list, the list will shuffle properly.
        ArrayList<SchedulesRecentlyViewedModel> recentlyViewedList = recentlyViewedListMap.get(routeType);
        if (recentlyViewedList != null) {
            Log.d("yy", "we have already a recently viewed");
            for (int i = 0; i < recentlyViewedList.size(); i++) {
                Log.d("yy", "processing the recently viewed list with size " + recentlyViewedList.size());
                if (recentlyViewedList.get(i) != null) {
                    Log.d("yy", "recently viewed for position " + i);
                    if (recentlyViewedList.get(i).compareTo(schedulesModel) == 0) {
                        Log.d("yy", "recently viewed at that position is a dup");
                        recentlyViewedList.remove(i);
                    }
                }
            }
        } else {
            Log.d("yy", "recently viewed list is null");
            recentlyViewedList = new ArrayList<SchedulesRecentlyViewedModel>();
        }

        // put the recently viewed item at the top of the list
        recentlyViewedList.add(0, schedulesModel);

        // check if we have a (overly) full list, and remove the 4th item if exists
        if (recentlyViewedList.size() == 4) {
            recentlyViewedList.remove(3);
        }

        recentlyViewedListMap.put(routeType, recentlyViewedList);

        sendToSharedPreferencesRecentlyViewed();
    }

    public ArrayList<SchedulesRecentlyViewedModel> getRecentlyViewedList(String routeType) {
        Gson gson = new Gson();

        ArrayList<SchedulesRecentlyViewedModel> recentlyViewedList = recentlyViewedListMap.get(routeType);

        String json = ObjectFactory.getInstance().getSharedPreferencesManager(context).getSchedulesRecentlyViewedList();
        Log.d("hh", "fetched from the shared preferences manager as json "+json);
        recentlyViewedListMap = gson.fromJson(json, new TypeToken<HashMap<String, List<SchedulesRecentlyViewedModel>>>() {
        }.getType());

        if (recentlyViewedListMap != null) {
            Log.d("yy", "recently viewed list map is not null here");
            recentlyViewedList = recentlyViewedListMap.get(routeType);
        } else {
            Log.d("yy", "recently viewed list map is null here... make a new one");
            recentlyViewedListMap = new HashMap<String, ArrayList<SchedulesRecentlyViewedModel>>();
        }

        if (recentlyViewedList == null) {
            Log.d("hh", "getting the recently viewed list hashmap and returning the array list, but null");
            recentlyViewedList = new ArrayList<SchedulesRecentlyViewedModel>(3);
        }

        return recentlyViewedList;
    }

    public ArrayList<SchedulesFavoriteModel> getFavoriteList(String routeType) {
        Gson gson = new Gson();

        ArrayList<SchedulesFavoriteModel>favoritesList = favoritesListMap.get(routeType);

        String json = ObjectFactory.getInstance().getSharedPreferencesManager(context).getSchedulesFavoritesList();
        favoritesListMap = gson.fromJson(json, new TypeToken<HashMap<String,List<SchedulesFavoriteModel>>>() {
        }.getType());

        if (favoritesListMap != null) {
            favoritesList = favoritesListMap.get(routeType);
        } else {
            favoritesListMap = new HashMap<String, ArrayList<SchedulesFavoriteModel>>();
        }

        if (favoritesList == null) {
            favoritesList = new ArrayList<SchedulesFavoriteModel>();
        }

        return favoritesList;
    }

    private void sendToSharedPreferencesRecentlyViewed() {
        Gson gson = new Gson();
        String recentlyViewedListAsJSON = gson.toJson(recentlyViewedListMap);
        Log.d("y", "about to store this json "+recentlyViewedListAsJSON);

        ObjectFactory.getInstance().getSharedPreferencesManager(context).setSchedulesRecentlyViewedList(recentlyViewedListAsJSON);
    }

    private void sendToSharedPreferencesFavorites() {
        Gson gson = new Gson();
        String favoritesListAsJSON = gson.toJson(favoritesListMap);

        ObjectFactory.getInstance().getSharedPreferencesManager(context).setNexttoArriveFavoritesList(favoritesListAsJSON);
    }
}

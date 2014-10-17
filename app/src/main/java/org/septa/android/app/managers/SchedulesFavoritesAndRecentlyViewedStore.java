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
import java.util.Iterator;
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

    public boolean isRecentlyViewed(String routeType, SchedulesRouteModel schedulesModel) {
        ArrayList<SchedulesRecentlyViewedModel>recentlyViewedList = recentlyViewedListMap.get(routeType);

        if (recentlyViewedList != null) {
            for (SchedulesRouteModel storedTrip : recentlyViewedList) {
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

        favoritesListMap.put(routeType, favoritesList);

        sendToSharedPreferencesFavorites();
    }

    public void removeFavorite(String routeType, SchedulesFavoriteModel schedulesFavoriteModel) {
        ArrayList<SchedulesFavoriteModel>favoritesList = favoritesListMap.get(routeType);

        if (favoritesList != null) {
            for (int i = 0; i < favoritesList.size(); i++) {
                if (favoritesList.get(i).compareTo(schedulesFavoriteModel) == 0) {
                    favoritesList.remove(i);

                    sendToSharedPreferencesFavorites();
                    return;
                }
            }
        }
    }

    public void removeRecentlyViewed(String routeType, SchedulesRouteModel schedulesModel) {
        Log.d("d", "removeRecentlyViewed... with routeType as "+routeType);
        Log.d("yy", "recentlyViewedListMap here is size "+recentlyViewedListMap.size());
        ArrayList<SchedulesRecentlyViewedModel> recentlyViewedList = recentlyViewedListMap.get(routeType);
        if (recentlyViewedList != null) {
            Log.d("rr", "recentlyViewed is not null");
            for (int i = 0; i < recentlyViewedList.size(); i++) {
                if (recentlyViewedList.get(i) != null) {
                    if (recentlyViewedList.get(i).compareTo(schedulesModel) == 0) {
                        Log.d("DD", "compared recently viewed is equal");
                        recentlyViewedList.remove(i);

                        sendToSharedPreferencesRecentlyViewed();
                        return;
                    }
                }
            }
        } else {
            Log.d("dd", "recentlyViewedlistmap for this route type says null but how can that be????" );
        }
    }

    public void addRecentlyViewed(String routeType, SchedulesRecentlyViewedModel schedulesModel) {
        // duplicate avoidance
        // check if we already have this recently viewed
        // if we find a duplicate, remove it form the list, the list will shuffle properly.
        ArrayList<SchedulesRecentlyViewedModel> recentlyViewedList = recentlyViewedListMap.get(routeType);
        if (recentlyViewedList != null) {
            for (int i = 0; i < recentlyViewedList.size(); i++) {
                if (recentlyViewedList.get(i) != null) {
                    if (recentlyViewedList.get(i).compareTo(schedulesModel) == 0) {

                        recentlyViewedList.remove(i);
                    }
                }
            }
        } else {

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

        String json = SharedPreferencesManager.getInstance().getSchedulesRecentlyViewedList();
        recentlyViewedListMap = gson.fromJson(json, new TypeToken<HashMap<String, List<SchedulesRecentlyViewedModel>>>() {
        }.getType());

        if (recentlyViewedListMap != null) {

            recentlyViewedList = recentlyViewedListMap.get(routeType);
            //Remove invalid recently viewed
            Iterator<SchedulesRecentlyViewedModel> iterator = recentlyViewedList.iterator();
            int count = 0;
            while(iterator.hasNext()) {
                SchedulesRecentlyViewedModel schedulesRecentlyViewedModel = iterator.next();
                if(schedulesRecentlyViewedModel.getRouteShortName() == null) {
                    iterator.remove();
                    count++;
                }
            }
            if(count > 0) {
                sendToSharedPreferencesRecentlyViewed();
            }
        } else {

            recentlyViewedListMap = new HashMap<String, ArrayList<SchedulesRecentlyViewedModel>>();
        }

        if (recentlyViewedList == null) {

            recentlyViewedList = new ArrayList<SchedulesRecentlyViewedModel>(3);
        }

        return recentlyViewedList;
    }

    public ArrayList<SchedulesFavoriteModel> getFavoriteList(String routeType) {
        Gson gson = new Gson();

        ArrayList<SchedulesFavoriteModel>favoritesList = favoritesListMap.get(routeType);

        String json = SharedPreferencesManager.getInstance().getSchedulesFavoritesList();
        favoritesListMap = gson.fromJson(json, new TypeToken<HashMap<String,List<SchedulesFavoriteModel>>>() {
        }.getType());

        if (favoritesListMap != null) {

            favoritesList = favoritesListMap.get(routeType);
            //Remove invalid favorites
            Iterator<SchedulesFavoriteModel> iterator = favoritesList.iterator();
            int count = 0;
            while(iterator.hasNext()) {
                SchedulesFavoriteModel favoriteModel = iterator.next();
                if(favoriteModel.getRouteShortName() == null) {
                    iterator.remove();
                    count++;
                }
            }
            if(count > 0) {
                sendToSharedPreferencesFavorites();
            }
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

        SharedPreferencesManager.getInstance().setSchedulesRecentlyViewedList(recentlyViewedListAsJSON);
    }

    private void sendToSharedPreferencesFavorites() {
        Gson gson = new Gson();
        String favoritesListAsJSON = gson.toJson(favoritesListMap);

        SharedPreferencesManager.getInstance().setSchedulesFavoritesList(favoritesListAsJSON);
    }
}

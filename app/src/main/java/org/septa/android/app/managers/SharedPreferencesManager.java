/*
 * SharedPreferencesManager.java
 * Last modified on 05-01-2014 15:39-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.septa.android.app.fragments.FindNearestLocationsListFragment;

import java.util.Date;

public class SharedPreferencesManager {
    public static final String TAG = FindNearestLocationsListFragment.class.getName();

    private static SharedPreferencesManager mInstance;

    private SharedPreferences sharedPreferences = null;
    private int mainTabbarSelectedSection = 0;    // the default starting value is 0
    private float nearestLocationMapSearchRadius = 2.5f;    // the default starting value is 2.5 miles

    private int systemStatusSelectedTab = 0;    // the default selected tab value is 0

    private boolean systemStatusFilterEnabled = false;  // the default filter enabled value is false

    private String nextToArriveRecentlyViewedList;
    private String nextToArriveFavoritesList;

    private String schedulesRecentlyViewedList;
    private String schedulesFavoritesList;
    private Date lastAlertUpdate;  //date that new alerts were last loaded successfully


    private static final String KEY_LAST_ALERT_UPDATE= "last_alert_update";
    private static final String KEY_SCHEDULE_FAVORITES_LIST = "schedule_favorites_list";
    private static final String KEY_SCHEDULE_RECENTLY_VIEWED_LIST = "schedule_recentlyviewed_list";
    private static final String KEY_NEXTTOARRIVE_FAVORITES_LIST = "nexttoarrive_favorites_list";
    private static final String KEY_NEXTTOARRIVE_RECENTLY_VIEWED_LIST = "nexttoarrive_recentlyviewed_list";
    private static final String KEY_SYSTEM_STATUS_FILTERS_ENABLED= "systemstatus_filter_enabled";
    private static final String KEY_SYSTEM_STATUS_SELECTED_TAB= "systemstatus_selected_tab";
    private static final String KEY_FIND_NEAREST_LOCATION_MAPSEARCH_RADIUS= "findnearestlocation_mapsearch_radius";
    private static final String KEY_MAINTAB_SELECTED_SECTION= "maintabbar_selected_section";

    public static SharedPreferencesManager getInstance(){
        if(mInstance == null){
            mInstance = new SharedPreferencesManager();
        }
        return mInstance;
    }

    public void init(Context context){
        sharedPreferences = context.getSharedPreferences("SEPTAPreferences",Context.MODE_PRIVATE);
        loadPreferenceValues();
    }

    private SharedPreferencesManager() {

    }

    private void loadPreferenceValues(){

        lastAlertUpdate = new Date(sharedPreferences.getLong(KEY_LAST_ALERT_UPDATE, 0L));
        mainTabbarSelectedSection = sharedPreferences.getInt(KEY_MAINTAB_SELECTED_SECTION, mainTabbarSelectedSection);
        nearestLocationMapSearchRadius = sharedPreferences.getFloat(KEY_FIND_NEAREST_LOCATION_MAPSEARCH_RADIUS, nearestLocationMapSearchRadius);
        systemStatusSelectedTab = sharedPreferences.getInt(KEY_SYSTEM_STATUS_SELECTED_TAB, systemStatusSelectedTab);
        systemStatusFilterEnabled = sharedPreferences.getBoolean(KEY_SYSTEM_STATUS_FILTERS_ENABLED, systemStatusFilterEnabled);
        nextToArriveRecentlyViewedList = sharedPreferences.getString(KEY_NEXTTOARRIVE_RECENTLY_VIEWED_LIST, nextToArriveRecentlyViewedList);
        nextToArriveFavoritesList = sharedPreferences.getString(KEY_NEXTTOARRIVE_FAVORITES_LIST , nextToArriveFavoritesList);
        nextToArriveRecentlyViewedList = sharedPreferences.getString(KEY_SCHEDULE_RECENTLY_VIEWED_LIST, nextToArriveRecentlyViewedList);
        schedulesFavoritesList = sharedPreferences.getString(KEY_SCHEDULE_FAVORITES_LIST , schedulesFavoritesList);
    }

    public int getMainTabbarSelectedSection() {
        return mainTabbarSelectedSection;
    }

    public void setMainTabbarSelectedSection(int mainTabbarSelectedSection) {
        this.mainTabbarSelectedSection = mainTabbarSelectedSection;
        saveInt(KEY_MAINTAB_SELECTED_SECTION, this.mainTabbarSelectedSection);
    }

    public float getNearestLocationMapSearchRadius() {
        return nearestLocationMapSearchRadius;
    }

    public void setNearestLocationMapSearchRadius(float nearestLocationMapSearchRadius) {
        this.nearestLocationMapSearchRadius = nearestLocationMapSearchRadius;
        saveFloat(KEY_FIND_NEAREST_LOCATION_MAPSEARCH_RADIUS, this.nearestLocationMapSearchRadius);
    }

    public int getSystemStatusSelectedTab() {
        return systemStatusSelectedTab;
    }

    public void setSystemStatusSelectedTab(int selectedTab) {
        this.systemStatusSelectedTab = selectedTab;
        saveInt(KEY_SYSTEM_STATUS_SELECTED_TAB, systemStatusSelectedTab);
    }

    public boolean getSystemStatusFilterEnabled() {
        return systemStatusFilterEnabled;
    }

    public void setSystemStatusFilterEnabled(boolean filterEnabled) {
        this.systemStatusFilterEnabled = filterEnabled;
        saveBoolean(KEY_SYSTEM_STATUS_FILTERS_ENABLED, filterEnabled);
    }

    public String getNextToArriveRecentlyViewedList() {
        return nextToArriveRecentlyViewedList;
    }

    public void setNextToArriveRecentlyViewedList(String recentlyViewedList) {
        this.nextToArriveRecentlyViewedList = recentlyViewedList;
        saveString(KEY_NEXTTOARRIVE_RECENTLY_VIEWED_LIST, recentlyViewedList);
    }

    public String getNextToArriveFavoritesList() {
        return nextToArriveFavoritesList;
    }

    public void setNexttoArriveFavoritesList(String favoritesList) {
        this.nextToArriveFavoritesList = favoritesList;
        saveString(KEY_NEXTTOARRIVE_FAVORITES_LIST, nextToArriveFavoritesList);
    }

    public String getSchedulesRecentlyViewedList() {
        return schedulesRecentlyViewedList;
    }

    public void setSchedulesRecentlyViewedList(String recentlyViewedList) {
        this.schedulesRecentlyViewedList = recentlyViewedList;
        saveString(KEY_SCHEDULE_RECENTLY_VIEWED_LIST, schedulesRecentlyViewedList);
    }

    public String getSchedulesFavoritesList() {
        return schedulesFavoritesList;
    }

    public void setSchedulesFavoritesList(String favoritesList) {
        this.schedulesFavoritesList = favoritesList;
        saveString(KEY_SCHEDULE_FAVORITES_LIST, schedulesFavoritesList);
    }

    public Date getLastAlertUpdate() {
        return lastAlertUpdate;
    }

    public void setLastAlertUpdate(Date date) {
        this.lastAlertUpdate = date;
        saveDate(KEY_LAST_ALERT_UPDATE, date);
    }

    private void saveString(String key, String value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private void saveBoolean(String key, Boolean value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void saveInt(String key, int value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    private void saveFloat(String key, float value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    private void saveDate(String key, Date value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value.getTime());
        editor.apply();
    }

}

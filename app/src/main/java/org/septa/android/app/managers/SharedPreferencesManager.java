/*
 * SharedPreferencesManager.java
 * Last modified on 05-01-2014 15:39-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.septa.android.app.fragments.FindNearestLocationsListFragment;

public class SharedPreferencesManager {
    public static final String TAG = FindNearestLocationsListFragment.class.getName();

    private SharedPreferences sharedPreferences = null;
    private int mainTabbarSelectedSection = 0;    // the default starting value is 0
    private float nearestLocationMapSearchRadius = 2.5f;    // the default starting value is 2.5 miles

    private int systemStatusSelectedTab = 0;    // the default selected tab value is 0

    private boolean systemStatusFilterEnabled = false;  // the default filter enabled value is false

    private String nextToArriveRecentlyViewedList;
    private String nextToArriveFavoritesList;

    private String schedulesRecentlyViewedList;
    private String schedulesFavoritesList;

    public SharedPreferencesManager(Context context) {

        sharedPreferences = context.getSharedPreferences("SEPTAPreferences",Context.MODE_PRIVATE);

        readFromPreferencesMainTabbarSelectedSection();
        readFromPreferencesNearestLocationMapSearchRadius();
        readFromPreferencesSystemStatusSelectedTab();
        readFromPreferencesSystemStatusFilterEnabled();
        readFromPreferencesNextToArriveFavoritesList();
        readFromPreferencesNextToArriveRecentlyViewedList();
        readFromPreferencesSchedulesRecentlyViewedList();
        readFromPreferencesSchedulesFavoritesList();
    }

    public int getMainTabbarSelectedSection() {

        return mainTabbarSelectedSection;
    }

    public void setMainTabbarSelectedSection(int mainTabbarSelectedSection) {
        this.mainTabbarSelectedSection = mainTabbarSelectedSection;
        writePreferenceForMaintabbarSelectedSection();
    }

    private void readFromPreferencesMainTabbarSelectedSection() {

        mainTabbarSelectedSection = sharedPreferences.getInt("maintabbar_selected_section", mainTabbarSelectedSection);
    }

    private void writePreferenceForMaintabbarSelectedSection() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("maintabbar_selected_section", mainTabbarSelectedSection);
        editor.apply();
    }

    public float getNearestLocationMapSearchRadius() {
        Log.d(TAG, "returning "+ nearestLocationMapSearchRadius +"for the nearest location map search radius");
        return nearestLocationMapSearchRadius;
    }

    public void setNearestLocationMapSearchRadius(float nearestLocationMapSearchRadius) {
        this.nearestLocationMapSearchRadius = nearestLocationMapSearchRadius;
        writePreferenceForNearestLocationMapSearchRedius();
    }

    private void readFromPreferencesNearestLocationMapSearchRadius() {

        nearestLocationMapSearchRadius = sharedPreferences.getFloat("findnearestlocation_mapsearch_radius", nearestLocationMapSearchRadius);
    }

    private void writePreferenceForNearestLocationMapSearchRedius() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("findnearestlocation_mapsearch_radius", nearestLocationMapSearchRadius);
        editor.apply();
    }

    public int getSystemStatusSelectedTab() {
        return systemStatusSelectedTab;
    }

    public void setSystemStatusSelectedTab(int selectedTab) {
        this.systemStatusSelectedTab = selectedTab;
        writePreferenceForSystemStatusSelectedTab();
    }

    private void readFromPreferencesSystemStatusSelectedTab() {

        systemStatusSelectedTab = sharedPreferences.getInt("systemstatus_selected_tab", systemStatusSelectedTab);
    }

    private void writePreferenceForSystemStatusSelectedTab() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("systemstatus_selected_tab", systemStatusSelectedTab);
        editor.apply();
    }

    /*
        Realtime -> System Status : Filter Enabled
     */

    public boolean getSystemStatusFilterEnabled() {
        return systemStatusFilterEnabled;
    }

    public void setSystemStatusFilterEnabled(boolean filterEnabled) {
        this.systemStatusFilterEnabled = filterEnabled;
        writePreferenceForSystemStatusFilterEnabled();
    }

    private void readFromPreferencesSystemStatusFilterEnabled() {

        systemStatusFilterEnabled = sharedPreferences.getBoolean("systemstatus_filter_enabled", systemStatusFilterEnabled);
    }

    private void writePreferenceForSystemStatusFilterEnabled() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("systemstatus_filter_enabled", systemStatusFilterEnabled);
        editor.apply();
    }

    public String getNextToArriveRecentlyViewedList() {
        return nextToArriveRecentlyViewedList;
    }

    public void setNextToArriveRecentlyViewedList(String recentlyViewedList) {
        this.nextToArriveRecentlyViewedList = recentlyViewedList;
        writePreferenceForNextToArriveRecentlyViewedList();
    }

    private void readFromPreferencesNextToArriveRecentlyViewedList() {

        nextToArriveRecentlyViewedList = sharedPreferences.getString("nexttoarrive_recentlyviewed_list", nextToArriveRecentlyViewedList);
    }

    private void writePreferenceForNextToArriveRecentlyViewedList() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("nexttoarrive_recentlyviewed_list", nextToArriveRecentlyViewedList);
        editor.apply();
    }

    public String getNextToArriveFavoritesList() {
        return nextToArriveFavoritesList;
    }

    public void setNexttoArriveFavoritesList(String favoritesList) {
        this.nextToArriveFavoritesList = favoritesList;
        writePreferenceForNextToArriveFavoritesList();
    }

    private void readFromPreferencesNextToArriveFavoritesList() {

        nextToArriveFavoritesList = sharedPreferences.getString("nexttoarrive_favorites_list", nextToArriveFavoritesList);
    }

    private void writePreferenceForNextToArriveFavoritesList() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("nexttoarrive_favorites_list", nextToArriveFavoritesList);
        editor.apply();
    }

    public String getSchedulesRecentlyViewedList() {
        return schedulesRecentlyViewedList;
    }

    public void setSchedulesRecentlyViewedList(String recentlyViewedList) {
        this.schedulesRecentlyViewedList = recentlyViewedList;
        writePreferenceForSchedulesRecentlyViewedList();
    }

    private void readFromPreferencesSchedulesRecentlyViewedList() {

        schedulesRecentlyViewedList = sharedPreferences.getString("schedule_recentlyviewed_list", schedulesRecentlyViewedList);
    }

    private void writePreferenceForSchedulesRecentlyViewedList() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("schedule_recentlyviewed_list", schedulesRecentlyViewedList);
        editor.apply();
    }

    public String getSchedulesFavoritesList() {
        return schedulesFavoritesList;
    }

    public void setSchedulesFavoritesList(String favoritesList) {
        this.schedulesFavoritesList = favoritesList;
        writePreferenceForSchedulesFavoritesList();
    }

    private void readFromPreferencesSchedulesFavoritesList() {

        schedulesFavoritesList = sharedPreferences.getString("schedule_favorites_list", schedulesFavoritesList);
    }

    private void writePreferenceForSchedulesFavoritesList() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("schedule_favorites_list", schedulesFavoritesList);
        editor.apply();
    }
}

/*
 * SharedPreferencesManager.java
 * Last modified on 05-01-2014 15:39-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.managers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {
    private SharedPreferences sharedPreferences = null;
    private int mainTabbarSelectedSection = 0;    // the default starting value is 0
    private float nearestLocationMapSearchRadius = 2.5f;    // the default starting value is 2.5 miles

    public SharedPreferencesManager(Context context) {

        sharedPreferences = context.getSharedPreferences("SEPTAPreferences",Context.MODE_PRIVATE);

        readFromPreferencesMainTabbarSelectedSection();
        readFromPreferencesNearestLocationMapSearchRadius();
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

        return nearestLocationMapSearchRadius;
    }

    public void setNearestLocationMapSearchRadius(float nearestLocationMapSearchRadius) {
        this.nearestLocationMapSearchRadius = nearestLocationMapSearchRadius;
    }

    private void readFromPreferencesNearestLocationMapSearchRadius() {

        nearestLocationMapSearchRadius = sharedPreferences.getFloat("findnearestlocation_mapsearch_radius", nearestLocationMapSearchRadius);
    }

    private void writePreferenceForNearestLocationMapSearchRedius() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("findnearestlocation_mapsearch_radius", nearestLocationMapSearchRadius);
        editor.apply();
    }
}

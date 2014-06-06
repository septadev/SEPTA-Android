/*
 * StopsModel.java
 * Last modified on 06-05-2014 19:14-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.TimingLogger;
import org.septa.android.app.databases.SEPTADatabase;

import java.util.ArrayList;

public class StopsModel {
private static final String TAG = StopsModel.class.getName();
    private ArrayList<StopModel>stopModelArrayList;
    private boolean loaded = false;

    public StopsModel() {

        this.stopModelArrayList = new ArrayList<StopModel>(0);
    }

    public void loadStops(Context context) {
        if (loaded) return;

        TimingLogger timings = new TimingLogger("StopsModel", "loadStops");

        SEPTADatabase septaDatabase = new SEPTADatabase(context);
        SQLiteDatabase database = septaDatabase.getReadableDatabase();

        timings.addSplit("  opened the database...");

        String queryString = "SELECT stop_id, stop_name, wheelchair_boarding FROM stops_rail ORDER BY stop_name";

        Cursor cursor = database.rawQuery(queryString, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    StopModel stopModel = new StopModel(cursor.getString(0), cursor.getString(1), (cursor.getInt(2) == 1) ? true : false);
                    getStopModelArrayList().add(stopModel);
                } while (cursor.moveToNext());
            }

            cursor.close();
        } else {
            Log.d(TAG, "cursor is null");
        }

        database.close();

        timings.dumpToLog();
    }

    public void reset() {

        this.loaded = false;
    }

    public ArrayList<StopModel> getStopModelArrayList() {
        return stopModelArrayList;
    }
}
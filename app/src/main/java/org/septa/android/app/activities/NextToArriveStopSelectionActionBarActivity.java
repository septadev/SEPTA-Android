/*
 * NextToArriveStopSelectionActionBarActivity.java
 * Last modified on 06-04-2014 16:14-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.septa.android.app.R;
import org.septa.android.app.adapters.NextToArrive_StopSelection_ListViewItem_ArrayAdapter;
import org.septa.android.app.databases.SEPTADatabase;
import org.septa.android.app.fragments.StopSelectionListFragment;
import org.septa.android.app.models.StopModel;

import java.util.ArrayList;

public class NextToArriveStopSelectionActionBarActivity extends BaseAnalyticsActionBarActivity {
    public static final String TAG = NextToArriveStopSelectionActionBarActivity.class.getName();
    NextToArrive_StopSelection_ListViewItem_ArrayAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String resourceName = getString(R.string.actionbar_iconimage_imagename_base).concat("nexttoarrive");

        int id = getResources().getIdentifier(resourceName, "drawable", getPackageName());

        // get the start or destination string from the extra intent string if it exists
        String nexttoarriveStopSelectionStartOrDestinationString = getIntent().getStringExtra(getString(R.string.nexttoarrive_stopselection_startordestination));
        if (nexttoarriveStopSelectionStartOrDestinationString != null) {
            getSupportActionBar().setTitle("Select "+nexttoarriveStopSelectionStartOrDestinationString);
        } else {
            getSupportActionBar().setTitle("Select Start");
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(id);

        setContentView(R.layout.nexttoarrive_stopselection);

        adapter = new NextToArrive_StopSelection_ListViewItem_ArrayAdapter(this, new ArrayList<StopModel>(0));
        StopSelectionListFragment stopsListFragment = (StopSelectionListFragment)getSupportFragmentManager().findFragmentById(R.id.stopselection_list_fragment);
        stopsListFragment.setListAdapter(adapter);
        stopsListFragment.setStartOrDestinationSelectionMode(nexttoarriveStopSelectionStartOrDestinationString);

        StopsLoader stopsLoader = new StopsLoader();
        stopsLoader.execute();
    }

    private class StopsLoader extends AsyncTask<Void, Integer, Boolean> {
        ArrayList<StopModel> stopsList = new ArrayList<StopModel>();

        private void loadStops() {
            SEPTADatabase septaDatabase = new SEPTADatabase(NextToArriveStopSelectionActionBarActivity.this);
            SQLiteDatabase database = septaDatabase.getReadableDatabase();

            String queryString = null;
                    Log.d("f", "type is rail, loading the trips");
                    queryString = "SELECT stop_id, stop_name, wheelchair_boarding FROM stops_rail ORDER BY stop_name";

            Cursor cursor = database.rawQuery(queryString, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        StopModel stopModel = new StopModel();

                        stopModel.setStopId(cursor.getString(0));       // s.stop_id
                        stopModel.setStopName(cursor.getString(1));     // s.stop_name
                        if (cursor.getInt(2) == 1) {                    // s.wheelchair_boarding
                            stopModel.setWheelchairBoarding(true);
                        } else {
                            stopModel.setWheelchairBoarding(false);
                        }

                        stopsList.add(stopModel);
                    } while (cursor.moveToNext());
                }

                cursor.close();
            } else {
                Log.d("f", "cursor is null");
            }

            database.close();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d("f", "about to call the loadTrips...");
            loadStops();
            Log.d("f", "called the loadTrips.");

            return false;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);

            Log.d("f", "calling onPostExecute...");

            StopSelectionListFragment stopsListFragment = (StopSelectionListFragment)getSupportFragmentManager().findFragmentById(R.id.stopselection_list_fragment);
            stopsListFragment.setStopList(stopsList);

            Log.d("f", "done with the onPostExecute call.");
        }
    }
}
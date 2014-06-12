/*
 * LoadKMLFileTask.java
 * Last modified on 04-29-2014 14:35-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.TimingLogger;

import org.septa.android.app.models.ObjectFactory;

public class LoadKMLFileTask extends AsyncTask<Context, Void, Void> {
    public static final String TAG = LoadKMLFileTask.class.getName();

    Context mContext;
    String kmlFileName;
    TimingLogger timingLogger;

    public LoadKMLFileTask(Context context, String kmlFileName) {
        super();

        this.mContext = context;
        this.kmlFileName = kmlFileName;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        Log.d(TAG, "starting the load the kml file with the filename "+kmlFileName);
        timingLogger = new TimingLogger("LoadKMLFileTask", kmlFileName);

        // create a show the dialog announcing the loading of the database file.
    }

    @Override
    protected Void doInBackground(Context... contexts) {
        // call the getReadableDatabase() method, which will either obtain the readable database or
        //  kick off a copy of the compressed database file into the private data location for the app
        ObjectFactory.getInstance().getKMLModel(contexts[0], kmlFileName);

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        timingLogger.dumpToLog();
        timingLogger = null;
   }
}
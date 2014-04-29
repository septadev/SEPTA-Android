/*
 * LoadDatabaseTask.java
 * Last modified on 04-21-2014 13:17-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.septa.android.app.R;
import org.septa.android.app.databases.SEPTADatabase;

public class LoadDatabaseTask extends AsyncTask<Context, Void, Void> {
    public static final String TAG = LoadDatabaseTask.class.getName();

    Context mContext;
    ProgressDialog mDialog;

    // Provide a constructor so we can get a Context to use to create
    // the ProgressDialog.
    public LoadDatabaseTask(Context context, ProgressDialog progressDialog) {
        super();

        mContext = context;
        mDialog = progressDialog;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // create a show the dialog announcing the loading of the database file.
        mDialog.setMessage(mContext.getString(R.string.database_loading_message));
        mDialog.show();
    }

    @Override
    protected Void doInBackground(Context... contexts) {
        // call the getReadableDatabase() method, which will either obtain the readable database or
        //  kick off a copy of the compressed database file into the private data location for the app
        new SEPTADatabase(contexts[0]).getReadableDatabase();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }
}
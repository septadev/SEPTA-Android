/*
 * SplashScreenActivity.java
 * Last modified on 01-29-2014 12:56-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.view.Gravity;
import android.widget.ImageView;

import org.septa.android.app.R;
import org.septa.android.app.databases.SEPTADatabase;
import org.septa.android.app.tasks.LoadKMLFileTask;

public class SplashScreenActivity extends BaseAnalyticsActivity {
    public static final String TAG = SplashScreenActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.splashscreen);

        String[] splashscreen_icons = getResources().getStringArray(R.array.splashscreen_icons_inorder);

        GridLayout gridLayout = (GridLayout) findViewById(R.id.splashscreen_icons_gridlayout);

        int position = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                ImageView imageView = new ImageView(this);

                GridLayout.LayoutParams param =new GridLayout.LayoutParams();
                param.height = GridLayout.LayoutParams.WRAP_CONTENT;
                param.width = GridLayout.LayoutParams.WRAP_CONTENT;
                param.rightMargin = 4;
                param.topMargin = 4;
                param.setGravity(Gravity.CENTER);
                param.columnSpec = GridLayout.spec(col);
                param.rowSpec = GridLayout.spec(row);
                imageView.setLayoutParams (param);

                String resourceName = "splashscreen_".concat(splashscreen_icons[position++].toLowerCase());

                Context context = imageView.getContext();

                assert context != null;
                int id = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
                imageView.setImageResource(id);

                gridLayout.addView(imageView);
            }
        }

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.database_loading_message));
        progressDialog.show();

        // Wait until database finishes loading before leaving Splash Screen
        // Also causes a database update if needed, otherwise just opens and closes existing DB
        new AsyncTask<Context, Void, Void>() {

            @Override
            protected Void doInBackground(Context... contexts) {
                Context context = contexts[0];
                new SEPTADatabase(context).getReadableDatabase().close();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                progressDialog.dismiss();
                startActivity(new Intent(SplashScreenActivity.this, MainTabbarActivity.class));
                finish();
            }
        }.execute(this);


        // since the regional rail KML file is rather large, pre-load it via an AsyncTask using
        //  the factory and held in a singleton until it is needed.
        LoadKMLFileTask loadKMLFileTask = new LoadKMLFileTask(this, "kml/train/regionalrail.kml");
        loadKMLFileTask.execute(this);
    }
}

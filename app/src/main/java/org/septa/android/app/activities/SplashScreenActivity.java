/*
 * SplashScreenActivity.java
 * Last modified on 01-29-2014 12:56-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.util.TimingLogger;
import android.view.Gravity;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import org.septa.android.app.R;
import org.septa.android.app.models.ObjectFactory;
import org.septa.android.app.tasks.LoadDatabaseTask;
import org.septa.android.app.tasks.LoadKMLFileTask;

import java.io.File;

public class SplashScreenActivity extends BaseAnalyticsActivity {
    public static final String TAG = SplashScreenActivity.class.getName();

    private static final String DATABASE_NAME = "SEPTA.sqlite";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);

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

        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                startActivity(new Intent(SplashScreenActivity.this,
                        MainTabbarActivity.class));
//                startActivity(new Intent(SplashScreenActivity.this,
//                        MainNavigationDrawerActivity.class));
                finish();
            }
        }, getResources().getInteger(R.integer.splashscreen_delaytime_seconds) * 1000);

        File databaseFileName = new File(this.getApplicationInfo().dataDir+"/databases/"+DATABASE_NAME);
        if (!databaseFileName.exists()) {
            new LoadDatabaseTask(this, progressDialog).execute(this, null, null);
        }

        // since the regional rail KML file is rather large, pre-load it via an AsyncTask using
        //  the factory and held in a singleton until it is needed.
        LoadKMLFileTask loadKMLFileTask = new LoadKMLFileTask(this, "kml/train/regionalrail.kml");
        loadKMLFileTask.execute(this);
    }
}

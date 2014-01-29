/*
 * SplashScreenActivity.java
 * Last modified on 01-29-2014 12:56-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayout;
import android.view.Gravity;
import android.widget.ImageView;

import org.septa.android.app.R;

public class SplashScreenActivity extends BaseAnalyticsActivity {
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
                param.rightMargin = 12;
                param.topMargin = 12;
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

        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivity(new Intent(SplashScreenActivity.this,
                        MainTabbarActivity.class));
//                startActivity(new Intent(SplashScreenActivity.this,
//                        MainNavigationDrawerActivity.class));
                finish();
            }
        }, getResources().getInteger(R.integer.splashscreen_delaytime_seconds) * 1000);
    }
}

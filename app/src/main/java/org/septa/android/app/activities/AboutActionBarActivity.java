/*
 * AboutActionBarActivity.java
 * Last modified on 02-09-2014 16:39-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.septa.android.app.BuildConfig;
import org.septa.android.app.R;
import org.septa.android.app.adapters.About_ListViewItem_ArrayAdapter;
import org.septa.android.app.adapters.FareInformation_ListViewItem_ArrayAdapter;
import org.septa.android.app.models.adapterhelpers.IconTextPendingIntentModel;
import org.septa.android.app.models.adapterhelpers.TextImageModel;

public class AboutActionBarActivity extends BaseAnalyticsActionBarActivity {
    public static final String TAG = AboutActionBarActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String titleText = getIntent().getStringExtra(getString(R.string.actionbar_titletext_key));

        setContentView(R.layout.about);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setIcon(R.drawable.ic_actionbar_tips);
        getSupportActionBar().setTitle(titleText);

        ListView aboutListView = (ListView)findViewById(R.id.aboutListView);

        int aboutListViewItemCount = getResources().getStringArray(R.array.about_listview_items_text).length;
        IconTextPendingIntentModel[] values = new IconTextPendingIntentModel[aboutListViewItemCount];
        for (int i = 0; i < aboutListViewItemCount; i++) {
            String text = getResources().getStringArray(R.array.about_listview_items_text)[i];

            if (text.equals("Version")) {
                text = text.concat(":  " + BuildConfig.VERSIONNAME);
            }

            String icon_ImageBase = getResources().getString(R.string.about_icon_imageBase);
            String icon_ImageSuffix = getResources().getStringArray(R.array.about_listview_items_iconSuffixs)[i];

            IconTextPendingIntentModel iconTextPendingIntentModel = new IconTextPendingIntentModel(text,
                    icon_ImageBase, icon_ImageSuffix, null);

            values[i] = iconTextPendingIntentModel;
        }

        ArrayAdapter<IconTextPendingIntentModel> adapter = new About_ListViewItem_ArrayAdapter(this, values);

        aboutListView.setAdapter(adapter);

        // set the divider to null in order to allow the gradient to work
        aboutListView.setDivider(null);
        aboutListView.setPadding(0, 5, 0, 0);
        aboutListView.setDividerHeight(5);
    }
}

/*
 * AboutAttributionsActionBarActiity.java
 * Last modified on 02-10-2014 10:09-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.septa.android.app.BuildConfig;
import org.septa.android.app.R;
import org.septa.android.app.adapters.About_ListViewItem_ArrayAdapter;
import org.septa.android.app.models.adapterhelpers.IconTextPendingIntentModel;

public class AboutAttributionsActionBarActiity  extends BaseAnalyticsActionBarActivity {
    public static final String TAG = AboutAttributionsActionBarActiity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String titleText = getIntent().getStringExtra(getString(R.string.actionbar_titletext_key));

        setContentView(R.layout.about_attribution);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setIcon(R.drawable.ic_actionbar_tips);
        getSupportActionBar().setTitle(titleText);

        ListView aboutListView = (ListView)findViewById(R.id.about_attribution_ListView);

        int aboutListViewItemCount = getResources().getStringArray(R.array.about_listview_items_texts).length;
        IconTextPendingIntentModel[] values = new IconTextPendingIntentModel[aboutListViewItemCount];
        for (int i = 0; i < aboutListViewItemCount; i++) {
            String text = getResources().getStringArray(R.array.about_listview_items_texts)[i];

            String icon_ImageBase = getResources().getString(R.string.about_icon_imageBase);
            String icon_ImageSuffix = getResources().getStringArray(R.array.about_listview_items_iconSuffixes)[i];
            String uri_toLoad = getResources().getStringArray(R.array.about_attributions_listview_items_urls)[i];

            IconTextPendingIntentModel iconTextPendingIntentModel = new IconTextPendingIntentModel(text,
                    icon_ImageBase, icon_ImageSuffix, uri_toLoad, true);

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

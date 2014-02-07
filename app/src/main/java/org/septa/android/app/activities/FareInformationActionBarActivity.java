/*
 * FareInformationActionBarActivity.java
 * Last modified on 02-07-2014 15:01-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.activities.BaseAnalyticsActionBarActivity;
import org.septa.android.app.adapters.ConnectListFragmentItemsArrayAdapter;
import org.septa.android.app.adapters.FareInformation_ListViewItem_ArrayAdapter;
import org.septa.android.app.models.adapterhelpers.TextImageModel;

public class FareInformationActionBarActivity extends BaseAnalyticsActionBarActivity {
    public static final String TAG = FareInformationActionBarActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String actionBarTitleText = getIntent().getStringExtra(getString(R.string.actionbar_titletext_key));
        String iconImageNameSuffix = getIntent().getStringExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key));

        String resourceName = getString(R.string.actionbar_iconimage_imagename_base).concat(iconImageNameSuffix);

        int id = getResources().getIdentifier(resourceName, "drawable", getPackageName());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(actionBarTitleText);
        getSupportActionBar().setIcon(id);

        setContentView(R.layout.fareinformation);


        int fareInformationCount = getResources().getStringArray(R.array.connect_fares_listview_item_texts).length;
        TextImageModel[] values = new TextImageModel[(fareInformationCount+1)];
        for (int i = 0; i < fareInformationCount; i++) {
            String fareInformation_item_text = (String)getResources().getStringArray(R.array.connect_fares_listview_item_texts)[i];
            String fareInformation_item_imageBase = (String)getResources().getString(R.string.fareinformation_listViewItems_imagebase);
            String fareInformation_item_imageSuffix = (String)getResources().getStringArray(R.array.connect_fares_listview_item_image_suffixes)[i];

            Log.d(TAG, "the imagebase is "+fareInformation_item_imageBase);
            Log.d(TAG, "the imagesuffix is "+fareInformation_item_imageSuffix);

            TextImageModel textImageModel = new TextImageModel(fareInformation_item_text,
                    fareInformation_item_imageBase,
                    fareInformation_item_imageSuffix);

            values[i] = textImageModel;
        }

        values[(values.length-1)] = new TextImageModel("Get More Details", null, null);

        ArrayAdapter<TextImageModel> adapter = new FareInformation_ListViewItem_ArrayAdapter(this, values);

        ListView listView = (ListView)findViewById(R.id.fareInformation_listView);
        listView.setAdapter(adapter);
    }
}

/*
 * TrainViewActionBarActivity.java
 * Last modified on 04-03-2014 16:41-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.os.Bundle;
import android.util.Log;

import org.septa.android.app.R;
import org.septa.android.app.models.KMLModel;
import org.septa.android.app.utilities.KMLSAXXMLProcessor;

import java.util.List;

public class TrainViewActionBarActivity extends BaseAnalyticsActionBarActivity {
    public static final String TAG = TrainViewActionBarActivity.class.getName();

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

        setContentView(R.layout.trainview);

        KMLSAXXMLProcessor processor = new KMLSAXXMLProcessor(getAssets());
        processor.readKMLFile("kml/train/regionalrail.kml");

        KMLModel kmlModel = processor.getKMLModel();

        List<KMLModel.Document.MultiGeometry.LineString.Coordinate> coordinateList = kmlModel.getDocument().getCoordinates();
    }
}

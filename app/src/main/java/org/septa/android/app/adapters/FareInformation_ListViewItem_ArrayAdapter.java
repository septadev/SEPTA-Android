/*
 * FareInformation_ListViewItem_ArrayAdapter.java
 * Last modified on 02-07-2014 15:36-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.activities.FareInformationGetMoreDetailsActionBarActivity;
import org.septa.android.app.models.adapterhelpers.TextImageModel;

public class FareInformation_ListViewItem_ArrayAdapter extends ArrayAdapter<TextImageModel> {
    private final Context context;
    private final TextImageModel[] values;

    public FareInformation_ListViewItem_ArrayAdapter(Context context, TextImageModel[] values) {
        super(context, R.layout.fareinformation_listview_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = null;

        if (values[position].getImageNameBase() == null && values[position].getImageNameSuffix() == null) {
            // create the bottom button view
            rowView = inflater.inflate(R.layout.fareinformation_listview_item_bottombutton, parent, false);

            Button bottomButton = (Button)rowView.findViewById(R.id.fareInformation_getMoreDetails_Button);
            bottomButton.setText("Get More Details");

            final ViewGroup finalParent = parent;
            bottomButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View v) {
                    Context context = finalParent.getContext();

                    Intent intent = new Intent(context, FareInformationGetMoreDetailsActionBarActivity.class);
                    intent.putExtra(context.getString(R.string.actionbar_titletext_key), "| Fares Details");
                    context.startActivity(intent);
                }});
            } else {
            rowView = inflater.inflate(R.layout.fareinformation_listview_item, parent, false);

            ImageView frontImageView = (ImageView) rowView.findViewById(R.id.fareInformation_front_imageView);
            ImageView backgroundImageView = (ImageView) rowView.findViewById(R.id.fareInformation_background_imageView);
            TextView mainTextView = (TextView) rowView.findViewById(R.id.fareInformation_main_textView);

            mainTextView.setText(values[position].getMainText());

            Context context = frontImageView.getContext();
            String resourceName = values[position].getImageNameBase()
                    .concat(values[position].getImageNameSuffix()
                            .concat(context.getResources().getString(R.string.fareinformation_listViewItems_frontImage_ending)));
            int id = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
            frontImageView.setImageResource(id);

            resourceName = values[position].getImageNameBase()
                    .concat(values[position].getImageNameSuffix()
                            .concat(context.getResources().getString(R.string.fareinformation_listViewItems_background_ending)));
            context = backgroundImageView.getContext();
            id = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
            backgroundImageView.setImageResource(id);
        }

        return rowView;
    }

    @Override
    public boolean areAllItemsEnabled() {

        return false;
    }

    @Override
    public boolean isEnabled(int position) {

        /** if the position is the last in the list, it must be the button */
        return values.length == (position + 1);
    }
}
/*
 * About_ListViewItem_ArrayAdapter.java
 * Last modified on 02-09-2014 20:36-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.activities.FareInformationGetMoreDetailsActionBarActivity;
import org.septa.android.app.models.adapterhelpers.IconTextPendingIntentModel;

public class About_ListViewItem_ArrayAdapter extends ArrayAdapter<IconTextPendingIntentModel> {
    private final Context context;
    private final IconTextPendingIntentModel[] values;

    public About_ListViewItem_ArrayAdapter(Context context, IconTextPendingIntentModel[] values) {
        super(context, R.layout.about_listview_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.about_listview_item, parent, false);

        ImageView icon_imageView = (ImageView) rowView.findViewById(R.id.about_icon_ImageView);
        TextView text_TextView = (TextView) rowView.findViewById(R.id.about_text_TextView);

        text_TextView.setText(values[position].getText());

        Context context = icon_imageView.getContext();
        String resourceName = values[position].getIconImageNameBase()
                .concat(values[position].getIconImageNameSuffix());
        int id = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
        icon_imageView.setImageResource(id);

        // TODO: set up the click listen on the row if the pendingIntent is not null

        return rowView;
    }
}
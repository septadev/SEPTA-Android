/*
 * NavigationDrawerListViewItemArrayAdapter.java
 * Last modified on 01-29-2014 13:26-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.septa.android.app.R;

public class NavigationDrawerListViewItemArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;

    public NavigationDrawerListViewItemArrayAdapter(Context context, String[] values) {
        super(context, R.layout.fragment_navigation_drawer_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.fragment_navigation_drawer_item, parent, false);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.navDrawerListViewItemIcon);
        TextView textView = (TextView) rowView.findViewById(R.id.navDrawerListViewItemText);

        String tabName = values[position];
        textView.setText(tabName);

        String resourceName = "ic_tab_".concat(tabName.toLowerCase());

        Context context = imageView.getContext();
        int id = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
        imageView.setImageResource(id);

        return rowView;
    }
}


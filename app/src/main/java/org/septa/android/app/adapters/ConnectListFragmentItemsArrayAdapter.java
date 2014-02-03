/*
 * ConnectListFragmentItemsArrayAdapter.java
 * Last modified on 02-02-2014 18:52-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.septa.android.app.R;

public class ConnectListFragmentItemsArrayAdapter extends ArrayAdapter<String> {
    private static final String TAG = ConnectListFragmentItemsArrayAdapter.class.getName();

    private final Context context;
    private final String[] values;

    public ConnectListFragmentItemsArrayAdapter(Context context, String[] values) {
        super(context, R.layout.connect_listfragment_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.connect_listfragment_item, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.connect_listfragment_item_textView);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.connect_listfragment_item_imageView);

        textView.setText(values[position]);

        String resourceName = "ic_connect_".concat(values[position].toLowerCase().replaceAll("\\s+", ""));
        Log.d(TAG, "resource name for connect listfragment item is "+resourceName);

        Context context = imageView.getContext();
        int id = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
        imageView.setImageResource(id);

        return rowView;
    }
}


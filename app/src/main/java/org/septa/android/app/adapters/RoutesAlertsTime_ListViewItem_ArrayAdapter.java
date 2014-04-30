/*
 * RoutesAlertsTime_ListViewItem_ArrayAdapter.java
 * Last modified on 04-30-2014 09:15-0400 by brianhmayo
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

import org.septa.android.app.R;

public class RoutesAlertsTime_ListViewItem_ArrayAdapter  extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;

    public RoutesAlertsTime_ListViewItem_ArrayAdapter(Context context, String[] values) {
        super(context, R.layout.fareinformation_listview_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = null;

        rowView = inflater.inflate(R.layout.fareinformation_listview_item_bottombutton, parent, false);

        return rowView;
    }

    @Override
    public boolean areAllItemsEnabled() {

        return false;
    }

    @Override
    public boolean isEnabled(int position) {

        return false;
    }
}
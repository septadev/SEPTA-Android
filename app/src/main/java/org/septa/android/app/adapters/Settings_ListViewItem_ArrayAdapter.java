/*
 * Settings_ListViewItem_ArrayAdapter.java
 * Last modified on 02-10-2014 17:39-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.adapters;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.models.adapterhelpers.IconTextPendingIntentModel;

public class Settings_ListViewItem_ArrayAdapter extends ArrayAdapter<IconTextPendingIntentModel> {
    public static final String TAG = Settings_ListViewItem_ArrayAdapter.class.getName();

    private final Context context;
    private final IconTextPendingIntentModel[] values;

    public Settings_ListViewItem_ArrayAdapter(Context context, IconTextPendingIntentModel[] values) {
        super(context, R.layout.about_listview_item, values);
        this.context = context;
        this.values = values;
    }

    @SuppressLint("NewApi")   // suppress the lint warnings about the Switch.setChecked calls
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.settings_listview_item, parent, false);

        ImageView icon_imageView = (ImageView) rowView.findViewById(R.id.settings_icon_ImageView);
        TextView text_TextView = (TextView) rowView.findViewById(R.id.settings_text_TextView);

        text_TextView.setText(values[position].getText());

        Context context = icon_imageView.getContext();
        String resourceName = values[position].getIconImageNameBase()
                .concat(values[position].getIconImageNameSuffix());
        int id = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
        icon_imageView.setImageResource(id);

        rowView.setTag(values[position]);

        if (values[position].getText().equals("Update")) {

            rowView.findViewById(R.id.settings_24hour_control).setVisibility(View.GONE);
        } else {
            rowView.findViewById(R.id.settings_24hour_control).setVisibility(View.VISIBLE);

            CompoundButton twentyFourHoursControl = (CompoundButton) rowView.findViewById(R.id.settings_24hour_control);
            if (DateFormat.is24HourFormat(context)) {

                twentyFourHoursControl.setChecked(true);
            } else {

                twentyFourHoursControl.setChecked(false);

            }

            twentyFourHoursControl.setClickable(false);
            twentyFourHoursControl.setFocusable(false);
            twentyFourHoursControl.setFocusableInTouchMode(false);
        }

        return rowView;
    }
//
//    @Override
//    public boolean areAllItemsEnabled() {
//
//        return true;
//    }
//
//    @Override
//    public boolean isEnabled(int position) {
//    Log.d(TAG, "checking position " + position + " to see if click is enabled");
//        Log.d(TAG, "is it??? " + values[position].isEnabled());
//        return values[position].isEnabled();
//    }
}

/*
 * SplashScreen_ImageAdapter.java
 * Last modified on 01-29-2014 13:25-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.adapters;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import org.septa.android.app.R;

public class SplashScreen_ImageAdapter extends BaseAdapter {
    private static final String TAG = SplashScreen_ImageAdapter.class.getName();
    private Context mContext;
    private String[] splashscreen_icons;

    public SplashScreen_ImageAdapter(Context c) {
        mContext = c;
        splashscreen_icons = mContext.getResources().getStringArray(R.array.splashscreen_icons_inorder);
    }

    public int getCount() {
        return splashscreen_icons.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);

            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);

        } else {
            imageView = (ImageView) convertView;
        }

        String resourceName = "splashscreen_".concat(splashscreen_icons[position-1].toLowerCase());
        Log.d(TAG, "the resource name string is " + resourceName);

        Context context = imageView.getContext();
        int id = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
        imageView.setImageResource(id);

        return imageView;
    }
}
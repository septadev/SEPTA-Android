/*
 * RealtimeMenuFragment.java
 * Last modified on 02-04-2014 07:53-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import org.septa.android.app.R;
import org.septa.android.app.adapters.RealTimeMenuAdapter;
import org.septa.android.app.managers.AlertManager;
import org.septa.android.app.managers.SharedPreferencesManager;
import org.septa.android.app.models.RealTimeMenuItem;
import org.septa.android.app.models.servicemodels.AlertModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class RealtimeMenuFragment extends Fragment implements AlertManager.IAlertListener, OnItemClickListener {
    public static final String TAG = RealtimeMenuFragment.class.getName();

    public static Boolean fetchedResults = false;

    private static RealTimeMenuAdapter mRealTimeMenuAdapter;

    private static final long MAX_ALERT_AGE = 1000 * 60 * 60;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.realtime_menu_fragment, container, false);
        GridView gridview = (GridView) view.findViewById(R.id.gridview);

        final String[] realtime_menu_icon_selectors = getResources().getStringArray(R.array.realtime_menu_icon_selectors_inorder);
        final String[] realtime_menu_icons = getResources().getStringArray(R.array.realtime_menu_icons_inorder);
        final String[] realtime_menu_strings = getResources().getStringArray(R.array.realtime_menu_strings_inorder);
        final String[] realtime_menu_classnames = getResources().getStringArray(R.array.realtime_menu_classnames_inorder);

        List<RealTimeMenuItem> menuItemList = new ArrayList<RealTimeMenuItem>();
        for (int i = 0; i < realtime_menu_icons.length; i++) {

            Class classname = null;
            try {
                classname = Class.forName(realtime_menu_classnames[i]);
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            menuItemList.add(new RealTimeMenuItem(classname, realtime_menu_icons[i], realtime_menu_icon_selectors[i], realtime_menu_strings[i]));
        }

        mRealTimeMenuAdapter = new RealTimeMenuAdapter(menuItemList);
        gridview.setAdapter(mRealTimeMenuAdapter);
        gridview.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        AlertManager.getInstance().addListener(this);
        AlertManager.getInstance().fetchGlobalAlert();
    }

    @Override
    public void onPause() {
        AlertManager.getInstance().removeListener(this);
        super.onPause();
    }

    @Override
    public void alertsDidUpdate() {
        Date lastUpdate = SharedPreferencesManager.getInstance().getLastAlertUpdate();
        AlertModel alert = AlertManager.getInstance().getGlobalAlert();
        if (alert != null && !alert.getCurrentMessage().isEmpty() && alert.getLastUpdate() != null && (lastUpdate == null || alert.getLastUpdate().compareTo(lastUpdate) != 0)) {
            Crouton.makeText(getActivity(), alert.getCurrentMessage(), Style.ALERT).show();

            //save the date of the last retrieved alert for comparison on future requests
            SharedPreferencesManager.getInstance().setLastAlertUpdate(alert.getLastUpdate());
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        RealTimeMenuItem realTimeMenuItem = (RealTimeMenuItem) mRealTimeMenuAdapter.getItem(position);
        String iconText = realTimeMenuItem.getIcon();
        StringBuilder titleText = new StringBuilder();
        titleText.append("| ").append(realTimeMenuItem.getTitle());
        Class intentClass = realTimeMenuItem.getClassname();

        if (intentClass != null) {
            Intent intent = new Intent(getActivity(), intentClass);
            intent.putExtra(getString(R.string.actionbar_titletext_key), titleText.toString());
            intent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), iconText);
            startActivity(intent);
        }
    }
}

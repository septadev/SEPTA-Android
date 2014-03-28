/*
 * FindNearestLocationsListFragment.java
 * Last modified on 03-27-2014 18:24-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.septa.android.app.R;

public class FindNearestLocationsListFragment extends ListFragment {
    ListView listview;
    View view;
    String[] array=new String[]{};

    public View onCreateView(LayoutInflater i,ViewGroup container,Bundle savedInstanceState)
    {

        ArrayAdapter<String> adapter=new ArrayAdapter<String>(getActivity(), R.layout.findnearestlocations_listview_item,R.id.findnearestlocations_listView_items_locationName_textView,array);

        setListAdapter(adapter);
        return super.onCreateView(i,container,savedInstanceState);
    }
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        listview=getListView();
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Toast.makeText(getActivity(), (CharSequence) listview.getItemAtPosition(i), Toast.LENGTH_LONG).show();
            }
        });
    }
}

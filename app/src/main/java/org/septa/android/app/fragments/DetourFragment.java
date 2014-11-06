/**
 * Created by acampbell on 11/4/14.
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.models.servicemodels.ServiceAdvisoryModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class DetourFragment extends Fragment {

    private static final String KEY_ALERTS = "alerts";

    public static DetourFragment newInstance(ArrayList<ServiceAdvisoryModel> alerts) {
        DetourFragment fragment = new DetourFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(KEY_ALERTS, alerts);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        ArrayList<ServiceAdvisoryModel> alerts = args.getParcelableArrayList(KEY_ALERTS);

        View view = inflater.inflate(R.layout.detour_fragment, container, false);

        ListView listView = (ListView) view.findViewById(R.id.detour_listview);
        listView.setAdapter(new DetourArrayAdapter(getActivity(), R.layout.detour_list_item , alerts));

        return view;
    }

    private class DetourArrayAdapter extends ArrayAdapter<ServiceAdvisoryModel> {

        private int resourceId;
        private SimpleDateFormat dateFormat;

        public DetourArrayAdapter(Context context, int resource, List<ServiceAdvisoryModel> objects) {
            super(context, resource, objects);
            this.resourceId = resource;
            dateFormat = new SimpleDateFormat("MM/dd/yyyy h:mm a");
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(resourceId, parent, false);
            }

            ServiceAdvisoryModel alert = getItem(position);

            TextView tv = (TextView) convertView.findViewById(R.id.detour_start_location_textview);
            tv.setText(Html.fromHtml(alert.getDetourStartLocation()));

            tv = (TextView) convertView.findViewById(R.id.detour_start_date_textview);
            tv.setText(dateFormat.format(alert.getDetourStartDateTime()));

            tv = (TextView) convertView.findViewById(R.id.detour_end_date_textview);
            tv.setText(dateFormat.format(alert.getDetourEndDateTime()));

            tv = (TextView) convertView.findViewById(R.id.detour_reason_textview);
            tv.setText(Html.fromHtml(alert.getDetourReason()));

            tv = (TextView) convertView.findViewById(R.id.detour_details_textview);
            tv.setText(Html.fromHtml(alert.getDetourMessage()));

            return convertView;
        }
    }
}

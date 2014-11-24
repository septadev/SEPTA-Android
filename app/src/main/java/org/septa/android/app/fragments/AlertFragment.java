/**
 * Created by acampbell on 11/4/14.
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.septa.android.app.R;

public class AlertFragment extends Fragment {

    private static final String KEY_MESSAGE = "message";

    public static AlertFragment newInstance(String message) {

        AlertFragment fragment = new AlertFragment();
        Bundle args = new Bundle();
        args.putString(KEY_MESSAGE, message);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        String message = args.getString(KEY_MESSAGE);

        View view = inflater.inflate(R.layout.alert_fragment, container, false);
        TextView tv = (TextView) view.findViewById(R.id.alert_message_textview);
        tv.setText(Html.fromHtml(message));

        return view;
    }
}
